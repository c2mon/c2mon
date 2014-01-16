package cern.c2mon.server.common.subequipment;

import cern.c2mon.server.common.equipment.AbstractEquipmentCacheObject;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;

// TODO move out all logic to facade bean (configXML, validation)

/**
 * POJO class representing a SubEquipment entity. This representation will be used to interact with the SubEquipments in
 * the server side. It extends the abstract class MonitoringEquipmentCacheObject. Notice that the handlerClassName
 * attribute will normally contain a dash, since this field does not make sense for a SubEquipment, but it is mandatory
 * for the database.
 * 
 * @author mruizgar
 */
public class SubEquipmentCacheObject extends AbstractEquipmentCacheObject implements SubEquipment, Cacheable, Cloneable {

    private static final long serialVersionUID = -3616744745556547068L;
    /**
     * The id of the subequipment's parent, to the equipment it is attached
     */
    private Long parentId;

    /**
     * Public default constructor (needed by iBatis).
     */
    public SubEquipmentCacheObject() {
        super();
        this.setHandlerClassName("-"); // TODO remove handlerclassname from abstractequipment (unless new design ideas
                                       // of using handler on subequipment level...)
    }

    public SubEquipmentCacheObject(Long id) {
        super(id);
        this.setHandlerClassName("-");
    }

    /**
     * Constructor with minimal set of non-null fields.
     */
    // public SubEquipmentCacheObject(Long id, String name, String handlerClass, Long stateTagId) {
    // super(id, name, handlerClass, stateTagId);
    // }

    /**
     * Clones the SubEquipment object.
     */
    public SubEquipmentCacheObject clone() {
        return (SubEquipmentCacheObject) super.clone();
    }

    /**
     * Creates a new SubEquipmentCacheObject containing the information provided within the parameters
     * 
     * @param pId Identifier of the subequipment
     * @param pName Name of the subequipment
     * @param pDescription Description of the subequipment
     * @param pHandlerClassName Name of the class that handles the subequipment.
     * @param pStateTagId Tag representing the state of the subequipment
     * @param pAliveTagId Alive tag indicating that the subequipment is running
     * @param pAliveInterval Interval for the alivetag in miliseconds
     * @param pCommfaultTagId Tag indicating the status of the communciation with the equipment
     * @param pParentId Indicates the id of the equipment to which this subequipment is attached
     */
    public SubEquipmentCacheObject(final Long pId, final String pName, final String pDescription,
            final String pHandlerClassName, final Long pStateTagId, final Long pAliveTagId, final int pAliveInterval,
            final Long pCommfaultTagId, final Long pParentId) {

        super(pId, pName, pDescription, pHandlerClassName, pStateTagId, pAliveTagId, pAliveInterval, pCommfaultTagId);
        this.parentId = pParentId;
    }

    /**
     * @return the parentId
     */
    @Override
    public final Long getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public final void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public SupervisionEntity getSupervisionEntity() {
        return SupervisionEntity.SUBEQUIPMENT;
    }

}
