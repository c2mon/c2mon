package cern.c2mon.driver.opcua.connection.dcom;

/**
 * Error constants for OPC DCOM.
 * 
 * @author Andreas Lang
 *
 */
public interface IDCOMErrorConstants {

    /**
     * MessageId: OPC_E_INVALIDHANDLE
     * 
     * MessageText:
     * 
     * The value of the handle is invalid.
     */
    Long OPC_E_INVALIDHANDLE = 0xC0040001L;
    /**
     * MessageId: OPC_E_BADTYPE
     * 
     * MessageText:
     * 
     * The server cannot convert the data between the requested data type and
     * the canonical data type.
     */
    Long OPC_E_BADTYPE = 0xC0040004L;
    /**
     * MessageId: OPC_E_PUBLIC
     * 
     * MessageText:
     * 
     * The requested operation cannot be done on a public group.
     * 
     */
    Long OPC_E_PUBLIC = 0xC0040005L;
    /**
     * MessageId: OPC_E_BADRIGHTS
     * 
     * MessageText:
     * 
     * The Items AccessRights do not allow the operation.
     */
    Long OPC_E_BADRIGHTS = 0xC0040006L;
    /**
     * MessageId: OPC_E_UNKNOWNITEMID
     * 
     * MessageText:
     * 
     * The item is no longer available in the server address space
     * 
     */
    Long OPC_E_UNKNOWNITEMID = 0xC0040007L;
    /**
     * MessageId: OPC_E_INVALIDITEMID
     * 
     * MessageText:
     * 
     * The item definition doesn't conform to the server's syntax.
     */
    Long OPC_E_INVALIDITEMID = 0xC0040008L;
    /**
     * MessageId: OPC_E_INVALIDFILTER
     * 
     * MessageText:
     * 
     * The filter string was not valid
     */
    Long OPC_E_INVALIDFILTER = 0xC0040009L;
    /**
     * MessageId: OPC_E_UNKNOWNPATH
     * 
     * MessageText:
     * 
     * The item's access path is not known to the server.
     * 
     */
    Long OPC_E_UNKNOWNPATH = 0xC004000AL;
    /**
     * MessageId: OPC_E_RANGE
     * 
     * MessageText:
     * 
     * The value was out of range.
     * 
     */
    Long OPC_E_RANGE = 0xC004000BL;
    /**
     * MessageId: OPC_E_DUPLICATENAME
     * 
     * MessageText:
     * 
     * Duplicate name not allowed.
     * 
     */
    Long OPC_E_DUPLICATENAME = 0xC004000CL;
    /**
     * MessageId: OPC_S_UNSUPPORTEDRATE
     * 
     * MessageText:
     * 
     * The server does not support the requested data rate but will use the
     * closest available rate.
     * 
     */
    Long OPC_S_UNSUPPORTEDRATE = 0x0004000DL;
    /**
     * MessageId: OPC_S_CLAMP
     * 
     * MessageText: A value passed to WRITE was accepted but the output was
     * clamped.
     */
    Long OPC_S_CLAMP = 0x0004000EL;
    /**
     * MessageId: OPC_S_INUSE
     * 
     * MessageText:
     * 
     * The operation cannot be completed because the object still has references
     * that exist.
     * 
     */
    Long OPC_S_INUSE = 0x0004000FL;
    /**
     * MessageId: OPC_E_INVALIDCONFIGFILE
     * 
     * MessageText:
     * 
     * The server's configuration file is an invalid format.
     */
    Long OPC_E_INVALIDCONFIGFILE = 0xC0040010L;
    /**
     * MessageId: OPC_E_NOTFOUND
     * 
     * MessageText:
     * 
     * The server could not locate the requested object.
     */
    Long OPC_E_NOTFOUND = 0xC0040011L;
    /**
     * MessageId: OPC_E_INVALID_PID
     * 
     * MessageText:
     * 
     * The server does not recognise the passed property ID.
     */
    Long OPC_E_INVALID_PID = 0xC0040203L;
    /**
     * MessageId: OPC_E_DEADBANDNOTSET
     * 
     * MessageText:
     * 
     * The item deadband has not been set for this item.
     */
    Long OPC_E_DEADBANDNOTSET = 0xC0040400L;
    /**
     * MessageId: OPC_E_DEADBANDNOTSUPPORTED
     * 
     * MessageText:
     * 
     * The item does not support deadband.
     */
    Long OPC_E_DEADBANDNOTSUPPORTED = 0xC0040401L;
    /**
     * MessageId: OPC_E_NOBUFFERING
     * 
     * MessageText:
     * 
     * The server does not support buffering of data items that are collected at
     * a faster rate than the group update rate.
     */
    Long OPC_E_NOBUFFERING = 0xC0040402L;
    /**
     * MessageId: OPC_E_INVALIDCONTINUATIONPOINT
     * 
     * MessageText:
     * 
     * The continuation point is not valid.
     */
    Long OPC_E_INVALIDCONTINUATIONPOINT = 0xC0040403L;
    /**
     * MessageId: OPC_S_DATAQUEUEOVERFLOW
     * 
     * MessageText:
     * 
     * Data Queue Overflow - Some value transitions were lost.
     */
    Long OPC_S_DATAQUEUEOVERFLOW = 0x00040404L;
    /**
     * MessageId: OPC_E_RATENOTSET
     * 
     * MessageText:
     * 
     * Server does not support requested rate.
     */
    Long OPC_E_RATENOTSET = 0xC0040405L;
    /**
     * MessageId: OPC_E_NOTSUPPORTED
     * 
     * MessageText:
     * 
     * The server does not support writing of quality and/or timestamp
     */
    Long OPC_E_NOTSUPPORTED = 0xC0040406L;

}
