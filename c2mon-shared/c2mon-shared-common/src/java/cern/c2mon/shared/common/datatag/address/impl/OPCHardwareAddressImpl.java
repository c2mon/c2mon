package cern.c2mon.shared.common.datatag.address.impl;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress;

/**
 * This class represents the hardware address of an OPC server item Each item
 * can provide up to two different addresses, one being for backup reasons
 * 
 * @author mruizgar
 * 
 */
public class OPCHardwareAddressImpl extends HardwareAddressImpl implements OPCHardwareAddress {

    /** UID identifying the class */
    static final long serialVersionUID = 31886551191861823L;

    // ---------------------------------------------------------------------------
    // Private member definitions
    // ---------------------------------------------------------------------------
    protected int namespace;
    /**
     * The name of the OPC tag name to which we subscribe.
     */
    protected String opcItemName;

    /**
     * The name of the OPC tag name to which we subscribe in case the
     * subscription to "opcItemName" fails
     */
    protected String opcRedundantItemName = null;

    /**
     * The OPC item name to which we are currently subscribed or trying to
     * subscribe to
     */
    private transient String currentOPCItemName;

    /**
     * Command pulse length in milliseconds for boolean commands.
     */
    protected int commandPulseLength;

    protected ADDRESS_TYPE addressType = ADDRESS_TYPE.STRING;

    protected COMMAND_TYPE commandType = COMMAND_TYPE.CLASSIC;

    // ---------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------

    /**
     * Create a OPCHardwareAddress object
     * 
     * @param pItemName
     *            name of an opc item
     * @throws ConfigurationException
     *             Exception thrown in case some of the parameters are null
     */
    public OPCHardwareAddressImpl(final String pItemName) throws ConfigurationException {
        this(pItemName, 0);
    }

    /**
     * Create a OPCHardwareAddress object
     * 
     * @param pItemName
     *            name of an opc item
     * @param pCommandPulseLength
     *            Command pulse length in milliseconds for boolean commands
     * @throws ConfigurationException
     *             Exception thrown in case some of the parameters are null
     */
    public OPCHardwareAddressImpl(final String pItemName, final int pCommandPulseLength) throws ConfigurationException {
        setOPCItemName(pItemName);
        setCommandPulseLength(pCommandPulseLength);
        setCurrentOPCItemName(getOPCItemName());
    }

    /**
     * Internal constructor required by the fromConfigXML method of the super
     * class.
     */
    protected OPCHardwareAddressImpl() {
        /* Nothing to do */
    }

    // ---------------------------------------------------------------------------
    // Public accessor methods
    // ---------------------------------------------------------------------------

    /**
     * Get the name of the OPC item The item name can never be null.
     * 
     * @return the name of the OPC item
     */
    public final String getOPCItemName() {
        // For the first time when it has not been initialized yet
        if (currentOPCItemName == null) {
            currentOPCItemName = opcItemName;
        }
        return this.currentOPCItemName;
    }

    /**
     * Gets the name of the OPCItem that is not currently being used, to which
     * the dataTag is not currently attached
     * 
     * @return String The opc redundanta item name
     */
    public final String getOpcRedundantItemName() {
        return opcRedundantItemName;

    }

    /**
     * Returns the commandpulselength attribute
     * 
     * @return int The configured miliseconds for the commandPulseLenght
     */
    public final int getCommandPulseLength() {
        return this.commandPulseLength;
    }

    /**
     * Gets the currentOPCItemName attribute
     * 
     * @return String the currentOPCItemName value
     */
    public final String getCurrentOPCItemName() {
        return this.currentOPCItemName;
    }

    /**
     * Sets the currentOPCItemName value
     * 
     * @param opcItemName
     *            The opcItemName that is being used at the moment
     */
    public final void setCurrentOPCItemName(final String opcItemName) {
        this.currentOPCItemName = opcItemName;
    }

    // ---------------------------------------------------------------------------
    // Private accessor methods
    // ---------------------------------------------------------------------------

    /**
     * Sets the value of the OPCItemName attribute
     * 
     * @param pItemName
     *            The value to be set
     * @throws ConfigurationException
     *             An exception is thrown in the case where the parameter is
     *             NULL
     */
    protected final void setOPCItemName(final String pItemName) throws ConfigurationException {
        if (pItemName == null) {
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"item name\" cannot be null.");
        }
        this.opcItemName = pItemName;
        return;
    }

    /**
     * Set the pulse length in milliseconds for boolean commands.
     * 
     * @param pCmdPulseLength
     *            pulse length im milliseconds
     * @throws ConfigurationException
     *             thrown in case the commandpulselenght is bigger than 2000
     *             miliseconds
     */
    protected final void setCommandPulseLength(final int pCmdPulseLength) throws ConfigurationException {
        if (pCmdPulseLength > 2000) {
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"command pulse length\" must be <= 2000 milliseconds");
        }
        this.commandPulseLength = pCmdPulseLength;
        return;
    }

    /**
     * Sets the opcRedundantItemName value to the one received as argument
     * 
     * @param secondaryOpcItemName
     *            the secondaryOpcItemName to set
     * 
     */
    public final void setOpcRedundantItemName(final String secondaryOpcItemName) {
        this.opcRedundantItemName = secondaryOpcItemName;
    }

    /**
     * Validates the configuration of the hardwareAddress
     * 
     * @throws ConfigurationException
     *             when the opcItemName is set to NULL
     */
    public final void validate() throws ConfigurationException {
        if (opcItemName == null) {
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"opcItemName\" must not be null");
        }
    }

    @Override
    public ADDRESS_TYPE getAddressType() {
        return addressType;
    }

    @Override
    public int getNamespaceId() {
        return namespace;
    }

    @Override
    public COMMAND_TYPE getCommandType() {
        return commandType;
    }

    /**
     * @return the namespace
     */
    public int getNamespace() {
        return namespace;
    }

    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(int namespace) {
        this.namespace = namespace;
    }

    /**
     * @param addressType the addressType to set
     */
    public void setAddressType(ADDRESS_TYPE addressType) {
        this.addressType = addressType;
    }

    /**
     * @param commandType the commandType to set
     */
    public void setCommandType(COMMAND_TYPE commandType) {
        this.commandType = commandType;
    }
}
