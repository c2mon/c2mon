package ch.cern.tim.driver.jec;

import cern.tim.driver.common.EquipmentLogger;

/**
 * This class is used to handle the connection status of the handler. Every
 * time a message arrives to the handler, the actual timestamp is updated.
 * This way, this thread tests if the time difference between the host
 * timestamp and the actual time stamp (time when last message was received)
 * isn't bigger than the maximum delay time. If it is, kills the handler and
 * restarts a new one, reconfiguring the PLC from the beginning.
 */
public class PLCConnectionSampler extends Thread {
    /**
     * The message handler to control.
     */
    private IJECRestarter jecRestarter;
    /**
     * Timestamp of the current AliveTag received
     */
    private long currentAliveTagTime;
    /**
     * The time between checks.
     */
    private long samplerPeriod = 0;
    /**
     * Timestamp of the previous AliveTag received
     */
    private long previousAliveTagTime = 0;
    /**
     * The logger to use.
     */
    private EquipmentLogger equipmentLogger;
    /**
     * The PLC connection sampler to use.
     * 
     * @param jecRestarter Object to restart the JEC DAQ.
     * @param equipmentLogger The equipment logger to use.
     * @param samplerPeriod The sampler period.
     */
    public PLCConnectionSampler(final IJECRestarter jecRestarter, 
            final EquipmentLogger equipmentLogger, final long samplerPeriod) {
        // Defines the Thread priority to 1
        this.jecRestarter = jecRestarter;
        setPriority(Thread.MAX_PRIORITY);
        this.samplerPeriod = samplerPeriod;
        this.equipmentLogger = equipmentLogger;
    }

    /**
     * the run method which implements the behavior of this thread.
     */
    public void run() {
        // Forever cycle (while connection is established)
        while (true) {
            try {
                Thread.sleep(samplerPeriod);
            } catch (java.lang.InterruptedException ex) {
                equipmentLogger.error("Problem detected with the Alive Sampler thread.", ex);
            }
            // No new timestamp - CONNECTION LOST
            if (previousAliveTagTime == currentAliveTagTime) {
                equipmentLogger.debug("PLCConnectionSampler : Connection LOST Detected !!, Setting 'connected' to OFF");
                jecRestarter.forceImmediateRestart();
                break;
            }
            // All went fine - STILL CONNECTED
            else {
                equipmentLogger.debug("PLCConnectionSampler : Connection OK!");
                previousAliveTagTime = currentAliveTagTime;
            }
        }
    }
    
    /**
     * Assigns the current system time to a field.
     * With this field the AliveSampler can check if the connection
     * to the equipment is still open.
    */
    public synchronized void updateAliveTimer() {
        currentAliveTagTime = System.currentTimeMillis();
    }

    /**
     * Returns the current alive timer value.
     * 
     * @return The current alive timer value.
     */
    public synchronized long getAliveTimer() {
        return currentAliveTagTime;
    }
}
