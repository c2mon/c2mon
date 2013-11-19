package cern.c2mon.shared.daq.config;


/**
 * The equipment unit remove event.
 * 
 * @author wbuczak
 */
public class EquipmentUnitRemove extends Change {

    /**
     * Equipment id
     */
    private long equipmentId;

    /**
     * Creates a new equipment unit remove event this is internally used.
     */
    public EquipmentUnitRemove() {
    }

    /**
     * This constructor is a kind copy constructor it may be used from subclasses to create an object of this class for
     * serialization to the DAQ core.
     * 
     * @param eqUnitRemove The update object to copy.
     */
    public EquipmentUnitRemove(final EquipmentUnitRemove eqUnitRemove) {        
        setChangeId(eqUnitRemove.getChangeId());
        setEquipmentId(eqUnitRemove.equipmentId);
    }

    /**
     * Creates a new equipment unit remove change.
     * 
     * @param changeId The change id of the new change.
     * @param equipmentId The id of the equipment to add the data tag to.
     */
    public EquipmentUnitRemove(final Long changeId, final long equipmentId) {
        setChangeId(changeId);
        setEquipmentId(equipmentId);
    }

    public void setEquipmentId(long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public long getEquipmentId() {
        return equipmentId;
    }

}