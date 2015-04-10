package cern.c2mon.daq.laser.source;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.LASERHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.diamon.alarms.client.AlarmConsumerInterface;
import cern.diamon.alarms.client.AlarmMessageData;
import cern.diamon.alarms.client.AlarmMessageData.AlarmMessageVisitor;
import cern.diamon.alarms.client.ClientAlarmEvent;
import cern.diamon.alarms.shared.data.AlarmDefinition;
import cern.diamon.alarms.shared.data.AlarmHandle.Descriptor;
import cern.diamon.alarms.source.AlarmMessageBuilder.MessageType;

public class AlarmListener implements AlarmConsumerInterface, AlarmMessageVisitor {

    private static final Logger log = LoggerFactory.getLogger(AlarmListener.class);

    private IEquipmentConfiguration equipement;
    private IEquipmentMessageSender equipementMessageSender;
    private LASERHardwareAddress laserAddress;

    public static LaserNativeMessageHandler handler;

    @Override
    public void onMessage(AlarmMessageData alarmMessage) {

        if (alarmMessage.getMt() != MessageType.BACKUP) {
            if (getHandler().getEquipementsName().contains(alarmMessage.getSourceId())) {
                equipement = getHandler().getEquipementByName(alarmMessage.getSourceId());
                equipementMessageSender = getHandler().getEquipementMessageSender(equipement);

                alarmMessage.visit(this);
            }
        }

        else {
            if (getHandler().getEquipementsName().contains(alarmMessage.getSourceId())) {
                try {
                    equipement = getHandler().getEquipementByName(alarmMessage.getSourceId());
                    equipementMessageSender = getHandler().getEquipementMessageSender(equipement);
                    ISourceDataTag heartbeatTag = getHandler().getHeartbeat4Equipement(equipement);

                    equipementMessageSender.sendTagFiltered(heartbeatTag, System.currentTimeMillis(),
                            System.currentTimeMillis());
                    synchronizeWithBackup(alarmMessage);
                } catch (Exception e) {
                    log.error("Error occured while reading the backup message");
                }

            }
        }

    }

    @Override
    public void onAlarm(ClientAlarmEvent alarm) {

        if (isAlarmDeclared(alarm)) {
            if (alarm.getDescriptor().equals(Descriptor.ACTIVE)) {
                ISourceDataTag dataTag = findDataTag(laserAddress);
                if (dataTag != null) {
                    if (dataTag.getCurrentValue() != null && dataTag.getCurrentValue().getValue().equals(true)) {
                        log.debug("This dataTag - " + dataTag.getName() + " - has already the good value :"
                                + dataTag.getCurrentValue().getValue());
                    } else {
                        getHandler().mbean.setDataTag(dataTag.getId());
                        equipementMessageSender.sendTagFiltered(dataTag, Boolean.TRUE, System.currentTimeMillis());
                        getHandler().mbean.setValue((boolean) dataTag.getCurrentValue().getValue());
                    }

                } else {
                    log.warn("Cannot set the value to true, dataTag not found");
                }
            } else if (alarm.getDescriptor().equals(Descriptor.TERMINATE)) {
                ISourceDataTag dataTag = findDataTag(laserAddress);
                if (dataTag != null) {
                    if (dataTag.getCurrentValue() != null && dataTag.getCurrentValue().getValue().equals(false)) {
                        log.debug("This dataTag - " + dataTag.getName() + " - has already the good value :"
                                + dataTag.getCurrentValue().getValue());
                    } else {
                        getHandler().mbean.setDataTag(dataTag.getId());
                        equipementMessageSender.sendTagFiltered(dataTag, Boolean.FALSE, System.currentTimeMillis());
                        getHandler().mbean.setValue((boolean) dataTag.getCurrentValue().getValue());
                    }

                } else {
                    log.warn("Cannot set the value to false, dataTag not found");
                }
            }
        }
    }

    @Override
    public void onException(JMSException e) {
        log.warn("JMS exception due to communication problem: " + e.getMessage());
    }

    @Override
    public void reset() {
        log.info("The connection was reset: you should adapt your internal data accordingly");
    }

    @Override
    public String getName() {
        return "MyTestConsumer";
    }

    /**
     * Checks if the alarm we selected corresponds to one of our dataTags
     * 
     * @param alarm
     * @return
     */
    public boolean isAlarmDeclared(ClientAlarmEvent alarm) {
        AlarmDefinition alarmDef = new AlarmDefinition(alarm.getAlarmId());

        for (LASERHardwareAddress laserHardwareAddress : getHandler().getRegisteredLaserHardwareAddress().get(equipement)) {
            if (laserHardwareAddress.getFaultFamily().equalsIgnoreCase(alarmDef.getDeviceClass())) {
                if (laserHardwareAddress.getFaultMember().equalsIgnoreCase(alarmDef.getDeviceName())) {
                    if (laserHardwareAddress.getFalutCode() == alarmDef.getFaultCode()) {
                        laserAddress = laserHardwareAddress;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ISourceDataTag findDataTag(LASERHardwareAddress laserAddress2) {
        try {
            return getHandler().getTag4LaserAddress(laserAddress2);
        } catch (Exception e) {
            log.info("Error while searching for dataTag");
        }
        return null;
    }

    /**
     * Checks if the datatag we have is present in the backup message. If true, it means that this one should be
     * activated, otherwise it should be terminated.
     * 
     * @param dataTag
     * @param alarm
     * @return
     */
    public boolean isDataTagDeclaredInBackup(ISourceDataTag dataTag, ClientAlarmEvent alarm) {
        LASERHardwareAddress laserHardwareAddress = (LASERHardwareAddress) dataTag.getHardwareAddress();
        AlarmDefinition alarmDef = new AlarmDefinition(alarm.getAlarmId());

        if (laserHardwareAddress.getFaultFamily().equalsIgnoreCase(alarmDef.getDeviceClass())) {
            if (laserHardwareAddress.getFaultMember().equalsIgnoreCase(alarmDef.getDeviceName())) {
                if (laserHardwareAddress.getFalutCode() == alarmDef.getFaultCode()) {
                    laserAddress = laserHardwareAddress;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Synchronize all dataTags values with the alarms values present in the backup
     * 
     * @param messageData
     */
    public void synchronizeWithBackup(AlarmMessageData messageData) {
        boolean isInBackup = false;

        for (ISourceDataTag dataTag : equipement.getSourceDataTags().values()) {

            isInBackup = false;
            if (dataTag.getHardwareAddress() instanceof LASERHardwareAddress) {

                for (ClientAlarmEvent alarm : messageData.getFaults()) {
                    if (isDataTagDeclaredInBackup(dataTag, alarm)) {
                        isInBackup = true;
                        break;
                    }
                }
                if (isInBackup) {
                    if (dataTag.getCurrentValue() != null && dataTag.getCurrentValue().getValue().equals(true)) {
                        log.debug("This dataTag - " + dataTag.getName() + " - has already the good value :"
                                + dataTag.getCurrentValue().getValue());

                    } else {
                        getHandler().mbean.setDataTag(dataTag.getId());
                        equipementMessageSender.sendTagFiltered(dataTag, Boolean.TRUE, System.currentTimeMillis());
                        getHandler().mbean.setValue((boolean) dataTag.getCurrentValue().getValue());
                    }
                } else {
                    if (dataTag.getCurrentValue() != null && dataTag.getCurrentValue().getValue().equals(false)) {
                        log.debug("This dataTag - " + dataTag.getName() + " - has already the good value : "
                                + dataTag.getCurrentValue().getValue());
                    } else {
                        getHandler().mbean.setDataTag(dataTag.getId());
                        equipementMessageSender.sendTagFiltered(dataTag, Boolean.FALSE, System.currentTimeMillis());
                        getHandler().mbean.setValue((boolean) dataTag.getCurrentValue().getValue());
                    }
                }
            }
        }

    }
    
    public synchronized static LaserNativeMessageHandler getHandler() {
        return handler;
    }

}
