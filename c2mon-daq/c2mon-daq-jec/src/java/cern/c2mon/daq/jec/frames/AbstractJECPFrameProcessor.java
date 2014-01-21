/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.jec.frames;

import java.io.IOException;
import java.util.LinkedList;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.jec.PLCObjectFactory;
import cern.c2mon.daq.jec.plc.JECPFrames;
import cern.c2mon.daq.jec.plc.StdConstants;
/**
 * Base class for JECPFrame processors. It handles the basic behavior for queuing
 * and processing of incoming frames.
 * 
 * @author Andreas Lang
 *
 */
public abstract class AbstractJECPFrameProcessor extends Thread {
    /**
     * Word mask to make sure bytes acquired from a frame will converted
     * to an integer like they were unsigned. (In general all java bytes 
     * are signed)
     */
    private static final int WORD_MASK = 0xFF;
    /**
     * The PLC object factory to access the driver and create frames.
     */
    private PLCObjectFactory plcFactory;
    /**
     * This is to store the last messageId to avoid repetition.
     */
    private byte lastSequenceNumber;
    /**
     * This is the id of the JECFrame message type which will be processed.
     * Other messages will be ignored.
     */
    private int supervisedMessagesId;
    /**
     * The queue where incoming JECPFrames will be stored till they are
     * processed.
     */
    private final LinkedList<JECPFrames> dataQueue = new LinkedList<JECPFrames>();
    /**
     * The equipment logger to log messages.
     */
    private EquipmentLogger equipmentLogger;
    /**
     * Flag to pause the thread.
     */
    private volatile boolean pause = false;
    /**
     * Flag which can be used to stop the thread.
     */
    private volatile boolean continueRun = true;
    
    /**
     * Constructor which is called from subclasses.
     * 
     * @param supervisedMessagesId The message id which is supervised of this class.
     * @param plcFactory The object factory to create objects for the PLC communication.
     * @param equipmentLogger The equipment logger to log messages.
     */
    public AbstractJECPFrameProcessor(final int supervisedMessagesId,
            final PLCObjectFactory plcFactory, final EquipmentLogger equipmentLogger) {
        setDaemon(true);
        setName(getClass().getSimpleName());
        this.supervisedMessagesId = supervisedMessagesId;
        this.equipmentLogger = equipmentLogger;
        this.plcFactory = plcFactory;
    }

    /**
     * Processes a JECFrame. This method has to be implemented from subclasses.
     * @param jecpFrames The frame to be processed.
     */
    protected abstract void processJECPFrame(JECPFrames jecpFrames);
    
    /**
     * Run method of the thread. Processes a JECPFrame and waits for more of
     * them.
     */
    @Override
    public void run() {
        while (continueRun) {
            waitForFrames();
            try {
                synchronized (this) {
                    if (!pause && continueRun)
                        processNextJECPFrame();
                }
            } catch (NullPointerException e) {
                getEquipmentLogger().error("NullPointerException in data processor.", e);
            }
        }
    }

    /**
     * Adds a new JECFrame to the queue. If the message id and the
     * sequence number are valid. It also acknowledges the frame.
     * 
     * @param jecpFrame
     *            The JECPFrame to add.
     * @param processImmediately True if the frame should be processed immediately.
     * @return True if the frame was successfully added ( and processed).
     */
    public synchronized boolean pushJECPFrame(final JECPFrames jecpFrame, final boolean processImmediately) {
        boolean success = false;
        try {
            if (acknowledgeReceivedMessage(jecpFrame)) {
                lastSequenceNumber = jecpFrame.GetSequenceNumber();
                success = dataQueue.add(jecpFrame);
                if (success) {
                  if (processImmediately) {
                    return processNextJECPFrame();
                  }
                  else {
                    notify();
                  }
                }
            }
        } catch (IOException e) {
            getEquipmentLogger().error("INFO MESSAGE: Error while acknowledging received message to JEC");
        }
        
        return success;
    }

    /**
     * Checks if the message is in sequence. Subclasses might override this method
     * to apply different behavior. The standard behavior is just to check if the
     * previous sequence number does not equal the sequence number of the provided frame.
     *
     * @param jecpFrame The frame to compare the sequence number with.
     * @return True if the last sequence number does not equal the current sequence number
     * else false.
     */
    public boolean isInSequence(final JECPFrames jecpFrame) {
        return lastSequenceNumber != jecpFrame.GetSequenceNumber();
    }

    /**
     * Checks if the message id is valid. This means that it is equal to the 
     * supervised id. This method might be overridden to alter the behavior.
     * 
     * @param jecpFrame The frame to check the message id.
     * @return True if the id is valid else false.
     */
    public boolean isCorrectMessageId(final JECPFrames jecpFrame) {
        return jecpFrame.getMsgID() == getSupervisedMessagesId();
    }

    /**
     * Processes the next frame from the queue.
     * 
     * @return Returns true if a value was processed else false.
     */
    public synchronized boolean processNextJECPFrame() {
        boolean valueProcessed = false;
        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug("Check queue... (Msg Id: '" + getSupervisedMessagesId() + "')");
        if (!dataQueue.isEmpty()) {
            JECPFrames jecpFrames = dataQueue.remove();
            if (getEquipmentLogger().isDebugEnabled())
                getEquipmentLogger().debug("Processing JECFrame... (Msg Id: '" + getSupervisedMessagesId() + "')");
            processJECPFrame(jecpFrames);
            valueProcessed = true;
            if (getEquipmentLogger().isDebugEnabled())
              getEquipmentLogger().debug("Finished processing JECFrame... (Msg Id: '" + getSupervisedMessagesId() + "')");
        }
        return valueProcessed;
    }
    
    /**
     * TODO duplicate code with JECController - if possible remove
     * This method is used to acknowledge every message sent by the PLC. This
     * method was created to guarantee that all messages arrive to the driver.
     * 
     * @param recvMsg JEC received message
     * @throws IOException Throws an exception if the acknowledging fails through an IO error.
     * @return True if the message was acknowledged else false.
     */
    public boolean acknowledgeReceivedMessage(final JECPFrames recvMsg) throws IOException {
        boolean acknowledged = false;
        if (isCorrectMessageId(recvMsg) && isInSequence(recvMsg)) {
            JECPFrames sendFrame = getPlcFactory().getRawSendFrame();
            sendFrame.SetMessageIdentifier(StdConstants.ACK_MSG);
            sendFrame.SetSequenceNumber(recvMsg.GetSequenceNumber());
            sendFrame.SetDataType((byte) (recvMsg.getMsgID()));
    
            if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("RECEIVED FRAME SEQUENCE NUMBER: " + (" 0x" + Integer.toHexString(recvMsg.GetSequenceNumber() & WORD_MASK)));
                getEquipmentLogger().debug("SENT FRAME SEQUENCE NUMBER: " + (" 0x" + Integer.toHexString(sendFrame.GetSequenceNumber() & WORD_MASK)));
                getEquipmentLogger().debug("NUMBER OF RETRIES: " + (" 0x" + Integer.toHexString(recvMsg.GetRetryNumber() & WORD_MASK)));
            }
            if (getPlcFactory().getPLCDriver().Send(sendFrame) == StdConstants.ERROR)
                throw new IOException("A problem occured while trying to send Acknowledge message");
    
            acknowledged = true;
        }
        else {
            if (getEquipmentLogger().isDebugEnabled())
                getEquipmentLogger().warn("Message not in sequence or incorrect message id - discarded.");
        }
        return acknowledged;
    }
    
    /**
     * If the data queue is empty it will wait until a new JECPFrame arrives. It
     * will also remain stopped if the thread is paused.
     */
    private synchronized void waitForFrames() {
        try {
            // while to ensure it will only return if there is something in the
            // queue and the thread is not in pause mode
            while ((dataQueue.isEmpty() || pause) && continueRun)
                wait();
        } catch (InterruptedException e) {
            getEquipmentLogger().error("Data Processor interrupted.", e);
        }
    }

    /**
     * Sets the thread into a pause state. It will not process received frames.
     * 
     * @param pause the pause to set
     */
    public synchronized void setPause(final boolean pause) {
        this.pause = pause;
        if (!pause)
            notify();
    }
    
    /**
     * Returns the pause state of the thread.
     * 
     * @return the pause True if the thread is currently paused else false.
     */
    public synchronized boolean isPause() {
        return pause;
    }
    
    /**
     * Stops this thread. Note that this will be a permanent stop. It cannot be
     * restarted.
     */
    public synchronized void stopThread() {
        continueRun = false;
        notify();
    }

    /**
     * The equipment logger of this processor.
     * @return the equipmentLogger
     */
    public EquipmentLogger getEquipmentLogger() {
        return equipmentLogger;
    }

    /**
     * @return the supervisedMessagesId
     */
    public int getSupervisedMessagesId() {
        return supervisedMessagesId;
    }

    /**
     * Clears all not processed JECFrames from the queue.
     */
    public synchronized void clearDataQueue() {
        dataQueue.clear();
    }

    /**
     * @param plcFactory the plcFactory to set
     */
    public void setPlcFactory(final PLCObjectFactory plcFactory) {
        this.plcFactory = plcFactory;
    }

    /**
     * @return the plcFactory
     */
    public PLCObjectFactory getPlcFactory() {
        return plcFactory;
    }
}
