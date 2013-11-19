package cern.c2mon.shared.daq.config;

import javax.xml.bind.annotation.XmlValue;


/**
 * The equipment unit add event.
 * 
 * @author wbuczak
 */
public class EquipmentUnitAdd extends Change {

    
    /**
     * the equipment's unique identifier
     */
    private long equipmentId;
    
  

    /**
     * this string contains a CDATA section with EquipmentUnit XML block
     */
    @XmlValue
    private String equipmentUnitXml;

    /**
     * Creates a new equipment unit add event this is internally used.
     */
    public EquipmentUnitAdd() {
    }

    /**
     * This constructor is a kind copy constructor it may be used from subclasses to create an object of this class for
     * serialization to the DAQ core.
     * 
     * @param dataTagAdd The update object to copy.
     */
    public EquipmentUnitAdd(final EquipmentUnitAdd eqUnitAdd) {
        setChangeId(eqUnitAdd.getChangeId());
        this.equipmentId = eqUnitAdd.equipmentId;
        this.equipmentUnitXml = eqUnitAdd.equipmentUnitXml;
    }

    /**
     * Creates a new equipment unit add change.
     * 
     * @param changeId The change id of the new change.
     * @param equipmentId The equipment identifier
     * @param equipmentUnitXml the XML configuration of the equipment (EquipmentUnit block)
     */
    public EquipmentUnitAdd(final Long changeId, final long equipmentId, final String equipmentUnitXml) {
        setChangeId(changeId);
        this.equipmentId = equipmentId;
        this.equipmentUnitXml = equipmentUnitXml;        
    }
    
    public long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(long equipmentId) {
        this.equipmentId = equipmentId;
    }    
    
    public String getEquipmentUnitXml() {
        return equipmentUnitXml;
    }
    
    public void setEquipmentUnitXml(final String equipmentUnitXml) {
        this.equipmentUnitXml = equipmentUnitXml;
    }
    
}