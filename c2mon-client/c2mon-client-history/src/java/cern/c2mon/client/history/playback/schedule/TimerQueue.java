package cern.c2mon.client.history.playback.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cern.c2mon.client.history.playback.schedule.event.TimerQueueListener;

/**
 * A facility for threads to schedule tasks for future execution in a background
 * thread. Tasks may be scheduled for one-time execution, or for repeated
 * execution at regular intervals.
 * 
 * <p>
 * Corresponding to each <tt>Timer</tt> object is a single background thread
 * that is used to execute all of the timer's tasks, sequentially. Timer tasks
 * should complete quickly. If a timer task takes excessive time to complete, it
 * "hogs" the timer's task execution thread. This can, in turn, delay the
 * execution of subsequent tasks, which may "bunch up" and execute in rapid
 * succession when (and if) the offending task finally completes.
 * 
 * <p>
 * After the last live reference to a <tt>Timer</tt> object goes away <i>and</i>
 * all outstanding tasks have completed execution, the timer's task execution
 * thread terminates gracefully (and becomes subject to garbage collection).
 * However, this can take arbitrarily long to occur. By default, the task
 * execution thread does not run as a <i>daemon thread</i>, so it is capable of
 * keeping an application from terminating. If a caller wants to terminate a
 * timer's task execution thread rapidly, the caller should invoke the timer's
 * <tt>cancel</tt> method.
 * 
 * <p>
 * If the timer's task execution thread terminates unexpectedly, for example,
 * because its <tt>stop</tt> method is invoked, any further attempt to schedule
 * a task on the timer will result in an <tt>IllegalStateException</tt>, as if
 * the timer's <tt>cancel</tt> method had been invoked.
 * 
 * <p>
 * This class is thread-safe: multiple threads can share a single <tt>Timer</tt>
 * object without the need for external synchronization.
 * 
 * <p>
 * This class does <i>not</i> offer real-time guarantees: it schedules tasks
 * using the <tt>Object.wait(long)</tt> method.
 * 
 * <p>
 * Implementation note: This class scales to large numbers of concurrently
 * scheduled tasks (thousands should present no problem). Internally, it uses a
 * binary heap to represent its task queue, so the cost to schedule a task is
 * O(log n), where n is the number of concurrently scheduled tasks.
 * 
 * <p>
 * Implementation note: All constructors start a timer thread.
 * 
 * @author Josh Bloch
 * @version 2.0, 07/25/2011
 * @see TimerTask
 * @see Object#wait(long)
 * @since 1.3
 */

public class TimerQueue {
  /**
   * The timer task queue. This data structure is shared with the timer thread.
   * The timer produces tasks, via its various schedule calls, and the timer
   * thread consumes, executing timer tasks as appropriate, and removing them
   * from the queue when they're obsolete.
   */
  private TaskQueue queue = new TaskQueue();

  /**
   * The timer thread.
   */
  private TimerThread thread;

  /**
   * This object causes the timer's task execution thread to exit gracefully
   * when there are no live references to the Timer object and no tasks in the
   * timer queue. It is used in preference to a finalizer on Timer as such a
   * finalizer would be susceptible to a subclass's finalizer forgetting to call
   * it.
   */
  @SuppressWarnings("unused")
  private Object threadReaper = new Object() {
    protected void finalize() throws Throwable {
      synchronized (queue) {
        thread.newTasksMayBeScheduled = false;
        queue.notify(); // In case queue is empty.
      }
    }
  };

  /**
   * This ID is used to generate thread names. (It could be replaced by an
   * AtomicInteger as soon as they become available.)
   */
  private static int nextSerialNumber = 0;

  private static synchronized int serialNumber() {
    return nextSerialNumber++;
  }

  /**
   * Creates a new timer. The associated thread does <i>not</i> run as a daemon.
   * 
   * @see Thread
   * @see #cancel()
   */
  public TimerQueue(final TimerQueueClock clock) {
    this("Timer-" + serialNumber(), clock);
  }

  /**
   * Creates a new timer whose associated thread has the specified name. The
   * associated thread does <i>not</i> run as a daemon.
   * 
   * @param name
   *          the name of the associated thread
   * @throws NullPointerException
   *           if name is null
   * @see Thread#getName()
   * @see Thread#isDaemon()
   * @since 1.5
   */
  public TimerQueue(final String name, final TimerQueueClock clock) {
    thread = new TimerThread(queue, clock);
    thread.setName(name);
    thread.start();
  }

  /**
   * Schedules the specified task for execution after the specified delay.
   * 
   * @param task
   *          task to be scheduled.
   * @param delay
   *          delay in milliseconds before task is to be executed.
   * @throws IllegalArgumentException
   *           if <tt>delay</tt> is negative, or
   *           <tt>delay + System.currentTimeMillis()</tt> is negative.
   * @throws IllegalStateException
   *           if task was already scheduled or cancelled, or timer was
   *           cancelled.
   */
  public void schedule(TimerTask task, long delay) {
    if (delay < 0)
      throw new IllegalArgumentException("Negative delay.");
    sched(task, System.currentTimeMillis() + delay, 0);
  }

  /**
   * Schedules the specified task for execution at the specified time. If the
   * time is in the past, the task is scheduled for immediate execution.
   * 
   * @param task
   *          task to be scheduled.
   * @param time
   *          time at which task is to be executed.
   * @throws IllegalArgumentException
   *           if <tt>time.getTime()</tt> is negative.
   * @throws IllegalStateException
   *           if task was already scheduled or cancelled, timer was cancelled,
   *           or timer thread terminated.
   */
  public void schedule(TimerTask task, Date time) {
    sched(task, time.getTime(), 0);
  }

  /**
   * Schedules the specified task for repeated <i>fixed-delay execution</i>,
   * beginning after the specified delay. Subsequent executions take place at
   * approximately regular intervals separated by the specified period.
   * 
   * <p>
   * In fixed-delay execution, each execution is scheduled relative to the
   * actual execution time of the previous execution. If an execution is delayed
   * for any reason (such as garbage collection or other background activity),
   * subsequent executions will be delayed as well. In the long run, the
   * frequency of execution will generally be slightly lower than the reciprocal
   * of the specified period (assuming the system clock underlying
   * <tt>Object.wait(long)</tt> is accurate).
   * 
   * <p>
   * Fixed-delay execution is appropriate for recurring activities that require
   * "smoothness." In other words, it is appropriate for activities where it is
   * more important to keep the frequency accurate in the short run than in the
   * long run. This includes most animation tasks, such as blinking a cursor at
   * regular intervals. It also includes tasks wherein regular activity is
   * performed in response to human input, such as automatically repeating a
   * character as long as a key is held down.
   * 
   * @param task
   *          task to be scheduled.
   * @param delay
   *          delay in milliseconds before task is to be executed.
   * @param period
   *          time in milliseconds between successive task executions.
   * @throws IllegalArgumentException
   *           if <tt>delay</tt> is negative, or
   *           <tt>delay + System.currentTimeMillis()</tt> is negative.
   * @throws IllegalStateException
   *           if task was already scheduled or cancelled, timer was cancelled,
   *           or timer thread terminated.
   */
  public void schedule(TimerTask task, long delay, long period) {
    if (delay < 0)
      throw new IllegalArgumentException("Negative delay.");
    if (period <= 0)
      throw new IllegalArgumentException("Non-positive period.");
    sched(task, System.currentTimeMillis() + delay, -period);
  }

  /**
   * Schedules the specified task for repeated <i>fixed-delay execution</i>,
   * beginning at the specified time. Subsequent executions take place at
   * approximately regular intervals, separated by the specified period.
   * 
   * <p>
   * In fixed-delay execution, each execution is scheduled relative to the
   * actual execution time of the previous execution. If an execution is delayed
   * for any reason (such as garbage collection or other background activity),
   * subsequent executions will be delayed as well. In the long run, the
   * frequency of execution will generally be slightly lower than the reciprocal
   * of the specified period (assuming the system clock underlying
   * <tt>Object.wait(long)</tt> is accurate).
   * 
   * <p>
   * Fixed-delay execution is appropriate for recurring activities that require
   * "smoothness." In other words, it is appropriate for activities where it is
   * more important to keep the frequency accurate in the short run than in the
   * long run. This includes most animation tasks, such as blinking a cursor at
   * regular intervals. It also includes tasks wherein regular activity is
   * performed in response to human input, such as automatically repeating a
   * character as long as a key is held down.
   * 
   * @param task
   *          task to be scheduled.
   * @param firstTime
   *          First time at which task is to be executed.
   * @param period
   *          time in milliseconds between successive task executions.
   * @throws IllegalArgumentException
   *           if <tt>time.getTime()</tt> is negative.
   * @throws IllegalStateException
   *           if task was already scheduled or cancelled, timer was cancelled,
   *           or timer thread terminated.
   */
  public void schedule(TimerTask task, Date firstTime, long period) {
    if (period <= 0)
      throw new IllegalArgumentException("Non-positive period.");
    sched(task, firstTime.getTime(), -period);
  }

  /**
   * Schedules the specified task for repeated <i>fixed-rate execution</i>,
   * beginning after the specified delay. Subsequent executions take place at
   * approximately regular intervals, separated by the specified period.
   * 
   * <p>
   * In fixed-rate execution, each execution is scheduled relative to the
   * scheduled execution time of the initial execution. If an execution is
   * delayed for any reason (such as garbage collection or other background
   * activity), two or more executions will occur in rapid succession to
   * "catch up." In the long run, the frequency of execution will be exactly the
   * reciprocal of the specified period (assuming the system clock underlying
   * <tt>Object.wait(long)</tt> is accurate).
   * 
   * <p>
   * Fixed-rate execution is appropriate for recurring activities that are
   * sensitive to <i>absolute</i> time, such as ringing a chime every hour on
   * the hour, or running scheduled maintenance every day at a particular time.
   * It is also appropriate for recurring activities where the total time to
   * perform a fixed number of executions is important, such as a countdown
   * timer that ticks once every second for ten seconds. Finally, fixed-rate
   * execution is appropriate for scheduling multiple repeating timer tasks that
   * must remain synchronized with respect to one another.
   * 
   * @param task
   *          task to be scheduled.
   * @param delay
   *          delay in milliseconds before task is to be executed.
   * @param period
   *          time in milliseconds between successive task executions.
   * @throws IllegalArgumentException
   *           if <tt>delay</tt> is negative, or
   *           <tt>delay + System.currentTimeMillis()</tt> is negative.
   * @throws IllegalStateException
   *           if task was already scheduled or cancelled, timer was cancelled,
   *           or timer thread terminated.
   */
  public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
    if (delay < 0)
      throw new IllegalArgumentException("Negative delay.");
    if (period <= 0)
      throw new IllegalArgumentException("Non-positive period.");
    sched(task, System.currentTimeMillis() + delay, period);
  }

  /**
   * Schedules the specified task for repeated <i>fixed-rate execution</i>,
   * beginning at the specified time. Subsequent executions take place at
   * approximately regular intervals, separated by the specified period.
   * 
   * <p>
   * In fixed-rate execution, each execution is scheduled relative to the
   * scheduled execution time of the initial execution. If an execution is
   * delayed for any reason (such as garbage collection or other background
   * activity), two or more executions will occur in rapid succession to
   * "catch up." In the long run, the frequency of execution will be exactly the
   * reciprocal of the specified period (assuming the system clock underlying
   * <tt>Object.wait(long)</tt> is accurate).
   * 
   * <p>
   * Fixed-rate execution is appropriate for recurring activities that are
   * sensitive to <i>absolute</i> time, such as ringing a chime every hour on
   * the hour, or running scheduled maintenance every day at a particular time.
   * It is also appropriate for recurring activities where the total time to
   * perform a fixed number of executions is important, such as a countdown
   * timer that ticks once every second for ten seconds. Finally, fixed-rate
   * execution is appropriate for scheduling multiple repeating timer tasks that
   * must remain synchronized with respect to one another.
   * 
   * @param task
   *          task to be scheduled.
   * @param firstTime
   *          First time at which task is to be executed.
   * @param period
   *          time in milliseconds between successive task executions.
   * @throws IllegalArgumentException
   *           if <tt>time.getTime()</tt> is negative.
   * @throws IllegalStateException
   *           if task was already scheduled or cancelled, timer was cancelled,
   *           or timer thread terminated.
   */
  public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
    if (period <= 0)
      throw new IllegalArgumentException("Non-positive period.");
    sched(task, firstTime.getTime(), period);
  }

  /**
   * Schedule the specified timer task for execution at the specified time with
   * the specified period, in milliseconds. If period is positive, the task is
   * scheduled for repeated execution; if period is zero, the task is scheduled
   * for one-time execution. Time is specified in Date.getTime() format. This
   * method checks timer state, task state, and initial execution time, but not
   * period.
   * 
   * @throws IllegalArgumentException
   *           if <tt>time()</tt> is negative.
   * @throws IllegalStateException
   *           if task was already scheduled or cancelled, timer was cancelled,
   *           or timer thread terminated.
   */
  private void sched(TimerTask task, long time, long period) {
    if (time < 0)
      throw new IllegalArgumentException("Illegal execution time.");

    synchronized (queue) {
      if (!thread.newTasksMayBeScheduled)
        throw new IllegalStateException("Timer already cancelled.");

      synchronized (task.lock) {
        if (task.state != TimerTask.VIRGIN)
          throw new IllegalStateException("Task already scheduled or cancelled");
        task.nextExecutionTime = time;
        task.period = period;
        task.state = TimerTask.SCHEDULED;
      }

      queue.add(task);
      if (queue.getMin() == task)
        queue.notify();
    }
  }

  public TimerThread getTimerThread() {
    return this.thread;
  }

  /**
   * Terminates this timer, discarding any currently scheduled tasks. Does not
   * interfere with a currently executing task (if it exists). Once a timer has
   * been terminated, its execution thread terminates gracefully, and no more
   * tasks may be scheduled on it.
   * 
   * <p>
   * Note that calling this method from within the run method of a timer task
   * that was invoked by this timer absolutely guarantees that the ongoing task
   * execution is the last task execution that will ever be performed by this
   * timer.
   * 
   * <p>
   * This method may be called repeatedly; the second and subsequent calls have
   * no effect.
   */
  public void cancel() {
    synchronized (queue) {
      thread.newTasksMayBeScheduled = false;
      queue.clear();
      queue.notify(); // In case queue was already empty.
    }
  }

  /**
   * Removes all cancelled tasks from this timer's task queue. <i>Calling this
   * method has no effect on the behavior of the timer</i>, but eliminates the
   * references to the cancelled tasks from the queue. If there are no external
   * references to these tasks, they become eligible for garbage collection.
   * 
   * <p>
   * Most programs will have no need to call this method. It is designed for use
   * by the rare application that cancels a large number of tasks. Calling this
   * method trades time for space: the runtime of the method may be proportional
   * to n + c log n, where n is the number of tasks in the queue and c is the
   * number of cancelled tasks.
   * 
   * <p>
   * Note that it is permissible to call this method from within a a task
   * scheduled on this timer.
   * 
   * @return the number of tasks removed from the queue.
   * @since 1.5
   */
  public int purge() {
    int result = 0;

    synchronized (queue) {
      for (int i = queue.size(); i > 0; i--) {
        if (queue.get(i).state == TimerTask.CANCELLED) {
          queue.quickRemove(i);
          result++;
        }
      }

      if (result != 0)
        queue.heapify();
    }

    return result;
  }

  /**
   * 
   * @param listener
   *          The listener to add
   */
  public void addTimerQueueListener(final TimerQueueListener listener) {
    this.thread.addTimTimerListener(listener);
  }

  /**
   * 
   * @param listener
   *          The listener to remove
   */
  public void removeTimerQueueListener(final TimerQueueListener listener) {
    this.thread.removeTimTimerListener(listener);
  }
}

/**
 * This "helper class" implements the timer's task execution thread, which waits
 * for tasks on the timer queue, executions them when they fire, reschedules
 * repeating tasks, and removes cancelled tasks and spent non-repeating tasks
 * from the queue.
 */
class TimerThread extends Thread {
  /**
   * How many milliseconds is considered to not be behind schedule
   */
  private static final long NOT_BEHIND_SCHEDULE_THRESHOLD = 20;

  /**
   * The maximum time to wait for the next task. If the speed multiplier on the
   * clock is changed while waiting on the next task, then the next task may
   * suddenly come sooner than expected and will not be executed before finish
   * waiting.<br/>
   * <br/>
   * So in practice, this is the maximum response time from the clock speed is
   * changed to the TimTimer realizes it.
   */
  private static final long MAXIMUM_TIME_TO_WAIT = 300;

  /**
   * This flag is set to false by the reaper to inform us that there are no more
   * live references to our Timer object. Once this flag is true and there are
   * no more tasks in our queue, there is no work left for us to do, so we
   * terminate gracefully. Note that this field is protected by queue's monitor!
   */
  boolean newTasksMayBeScheduled = true;

  /**
   * <code>true</code> if it should invoke the events on the listeners when it
   * is back on schedule
   */
  private boolean invokeWhenOnSchedule = false;

  /**
   * The list of listeners
   */
  private List<TimerQueueListener> listeners = new ArrayList<TimerQueueListener>();

  /**
   * The lock for {@link #listeners}
   */
  private ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();

  /**
   * Our Timer's queue. We store this reference in preference to a reference to
   * the Timer so the reference graph remains acyclic. Otherwise, the Timer
   * would never be garbage-collected and this thread would never go away.
   */
  private TaskQueue queue;

  /** The clock */
  private TimerQueueClock clock;

  TimerThread(final TaskQueue queue, final TimerQueueClock clock) {
    this.clock = clock;
    this.queue = queue;
    setName("TIM-Timer-Thread");
  }

  public void run() {
    try {
      mainLoop();
    }
    finally {
      // Someone killed this Thread, behave as if Timer cancelled
      synchronized (queue) {
        newTasksMayBeScheduled = false;
        queue.clear(); // Eliminate obsolete references
      }
    }
  }

  /**
   * The main timer loop. (See class comment.)
   */
  private void mainLoop() {
    while (true) {
      try {
        TimerTask task;
        boolean taskFired;
        long millisecondsBehindSchedule = 0;
        synchronized (queue) {
          // Wait for queue to become non-empty
          while (queue.isEmpty() && newTasksMayBeScheduled)
            queue.wait();
          if (queue.isEmpty())
            break; // Queue is empty and will forever remain; die

          // Queue nonempty; look at first evt and do the right thing
          long currentTime, executionTime;
          task = queue.getMin();
          synchronized (task.lock) {
            if (task.state == TimerTask.CANCELLED) {
              queue.removeMin();
              continue; // No action required, poll queue again
            }

            // This is the only line of code that has been changed
            // compared to the original code of Timer.java. Instead of the
            // system
            // time we fetch the current time of the history player's clock.

            // currentTime = System.currentTimeMillis();
            currentTime = clock.getTime();

            executionTime = task.nextExecutionTime;
            millisecondsBehindSchedule = currentTime - executionTime;
            taskFired = millisecondsBehindSchedule >= 0;
            if (taskFired) {
              if (task.period == 0) { // Non-repeating, remove
                queue.removeMin();
                task.state = TimerTask.EXECUTED;
              }
              else { // Repeating task, reschedule
                queue.rescheduleMin(task.period < 0 ? currentTime - task.period : executionTime + task.period);
              }
            }
          }
          if (!taskFired) { // Task hasn't yet fired; wait
            long waitTime = (long) ((executionTime - currentTime) / clock.getSpeedMultiplier());
            if (waitTime > MAXIMUM_TIME_TO_WAIT) {
              // In case the speed multiplier is changed in the waiting period
              waitTime = MAXIMUM_TIME_TO_WAIT;
            }
            if (waitTime <= 0) {
              waitTime = 1;
            }
            queue.wait(waitTime);
          }
        }
        if (millisecondsBehindSchedule > clock.getBehindScheduleThreshold()) {
          fireIsBehindSchedule(millisecondsBehindSchedule);
          invokeWhenOnSchedule = true;
        }
        else if (invokeWhenOnSchedule && millisecondsBehindSchedule <= NOT_BEHIND_SCHEDULE_THRESHOLD) {
          invokeWhenOnSchedule = false;
          fireIsOnSchedule();
        }
        if (taskFired) // Task fired; run it, holding no locks
          task.run();
      }
      catch (InterruptedException e) {
      }
    }
  }

  /**
   * Is called by the {@link #mainLoop()} when the timer is behind schedule.
   * This can happen if the TimTimerTask(s) takes to long too execute.
   * 
   * @param byTime
   *          The amount of time it is behind schedule
   */
  private void fireIsBehindSchedule(final long byTime) {
    try {
      this.listenersLock.readLock().lock();
      for (TimerQueueListener listener : listeners) {
        listener.timerIsBehindSchedule(byTime);
      }
    }
    finally {
      this.listenersLock.readLock().unlock();
    }
  }

  /**
   * Is called by the {@link #mainLoop()} when the timer is back on schedule.
   */
  private void fireIsOnSchedule() {
    try {
      this.listenersLock.readLock().lock();
      for (TimerQueueListener listener : listeners) {
        listener.timerIsOnSchedule();
      }
    }
    finally {
      this.listenersLock.readLock().unlock();
    }
  }

  /**
   * 
   * @param listener
   *          The listener to add
   */
  public void addTimTimerListener(final TimerQueueListener listener) {
    try {
      this.listenersLock.writeLock().lock();
      listeners.add(listener);
    }
    finally {
      this.listenersLock.writeLock().unlock();
    }
  }

  /**
   * 
   * @param listener
   *          The listener to remove
   */
  public void removeTimTimerListener(final TimerQueueListener listener) {
    try {
      this.listenersLock.writeLock().lock();
      listeners.remove(listener);
    }
    finally {
      this.listenersLock.writeLock().unlock();
    }
  }
}

/**
 * This class represents a timer task queue: a priority queue of TimTimerTasks,
 * ordered on nextExecutionTime. Each Timer object has one of these, which it
 * shares with its TimerThread. Internally this class uses a heap, which offers
 * log(n) performance for the add, removeMin and rescheduleMin operations, and
 * constant time performance for the getMin operation.
 */
class TaskQueue {
  /**
   * Priority queue represented as a balanced binary heap: the two children of
   * queue[n] are queue[2*n] and queue[2*n+1]. The priority queue is ordered on
   * the nextExecutionTime field: The TimTimerTask with the lowest
   * nextExecutionTime is in queue[1] (assuming the queue is nonempty). For each
   * node n in the heap, and each descendant of n, d, n.nextExecutionTime <=
   * d.nextExecutionTime.
   */
  private TimerTask[] queue = new TimerTask[128];

  /**
   * The number of tasks in the priority queue. (The tasks are stored in
   * queue[1] up to queue[size]).
   */
  private int size = 0;

  /**
   * Returns the number of tasks currently on the queue.
   */
  int size() {
    return size;
  }

  /**
   * Adds a new task to the priority queue.
   */
  void add(TimerTask task) {
    // Grow backing store if necessary
    if (size + 1 == queue.length)
      queue = Arrays.copyOf(queue, 2 * queue.length);

    queue[++size] = task;
    fixUp(size);
  }

  /**
   * Return the "head task" of the priority queue. (The head task is an task
   * with the lowest nextExecutionTime.)
   */
  TimerTask getMin() {
    return queue[1];
  }

  /**
   * Return the ith task in the priority queue, where i ranges from 1 (the head
   * task, which is returned by getMin) to the number of tasks on the queue,
   * inclusive.
   */
  TimerTask get(int i) {
    return queue[i];
  }

  /**
   * Remove the head task from the priority queue.
   */
  void removeMin() {
    queue[1] = queue[size];
    queue[size--] = null; // Drop extra reference to prevent memory leak
    fixDown(1);
  }

  /**
   * Removes the ith element from queue without regard for maintaining the heap
   * invariant. Recall that queue is one-based, so 1 <= i <= size.
   */
  void quickRemove(int i) {
    assert i <= size;

    queue[i] = queue[size];
    queue[size--] = null; // Drop extra ref to prevent memory leak
  }

  /**
   * Sets the nextExecutionTime associated with the head task to the specified
   * value, and adjusts priority queue accordingly.
   */
  void rescheduleMin(long newTime) {
    queue[1].nextExecutionTime = newTime;
    fixDown(1);
  }

  /**
   * Returns true if the priority queue contains no elements.
   */
  boolean isEmpty() {
    return size == 0;
  }

  /**
   * Removes all elements from the priority queue.
   */
  void clear() {
    // Null out task references to prevent memory leak
    for (int i = 1; i <= size; i++)
      queue[i] = null;

    size = 0;
  }

  /**
   * Establishes the heap invariant (described above) assuming the heap
   * satisfies the invariant except possibly for the leaf-node indexed by k
   * (which may have a nextExecutionTime less than its parent's).
   * 
   * This method functions by "promoting" queue[k] up the hierarchy (by swapping
   * it with its parent) repeatedly until queue[k]'s nextExecutionTime is
   * greater than or equal to that of its parent.
   */
  private void fixUp(int k) {
    while (k > 1) {
      int j = k >> 1;
      if (queue[j].nextExecutionTime <= queue[k].nextExecutionTime)
        break;
      TimerTask tmp = queue[j];
      queue[j] = queue[k];
      queue[k] = tmp;
      k = j;
    }
  }

  /**
   * Establishes the heap invariant (described above) in the subtree rooted at
   * k, which is assumed to satisfy the heap invariant except possibly for node
   * k itself (which may have a nextExecutionTime greater than its children's).
   * 
   * This method functions by "demoting" queue[k] down the hierarchy (by
   * swapping it with its smaller child) repeatedly until queue[k]'s
   * nextExecutionTime is less than or equal to those of its children.
   */
  private void fixDown(int k) {
    int j;
    while ((j = k << 1) <= size && j > 0) {
      if (j < size && queue[j].nextExecutionTime > queue[j + 1].nextExecutionTime)
        j++; // j indexes smallest kid
      if (queue[k].nextExecutionTime <= queue[j].nextExecutionTime)
        break;
      TimerTask tmp = queue[j];
      queue[j] = queue[k];
      queue[k] = tmp;
      k = j;
    }
  }

  /**
   * Establishes the heap invariant (described above) in the entire tree,
   * assuming nothing about the order of the elements prior to the call.
   */
  void heapify() {
    for (int i = size / 2; i >= 1; i--)
      fixDown(i);
  }
}
