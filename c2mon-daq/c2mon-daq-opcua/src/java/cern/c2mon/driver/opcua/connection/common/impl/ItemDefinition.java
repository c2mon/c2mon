package cern.c2mon.driver.opcua.connection.common.impl;

/**
 * The used item defintion.
 * 
 * @author Andreas Lang
 *
 * @param <A> The address type.
 */
public class ItemDefinition<A> {
    
    /**
     * The id of this defintion.
     */
    private long id;
    
    /**
     * The primary address of this defintion.
     */
    private final A address;
    
    /**
     * The redundant address of this defintion.
     */
    private final A redundantAddress;

    /**
     * Flag if the item is subscribed on the server.
     */
    private boolean subscribed;

    /**
     * Creates a new item defintion with the provided address.
     * 
     * @param id The id of the new defintion.
     * @param address The address of the new definition
     */
    public ItemDefinition(final long id, final A address) {
        this(id, address, null);
    }
    
    /**
     * Creates a new item defintion with the provided address and redundant 
     * address. 
     * 
     * @param id The id of the new defintion.
     * @param address The address of the new definition
     * @param redundantAddress The redundant address of this defintion.
     */
    public ItemDefinition(
            final long id, final A address, final A redundantAddress) {
        this.id = id;
        this.address = address;
        this.redundantAddress = redundantAddress;
    }

    /**
     * Returns the primary address.
     * 
     * @return the address
     */
    public A getAddress() {
        return address;
    }
    
    /**
     * Returns the redundant address.
     * 
     * @return the address
     */
    public A getRedundantAddress() {
        return redundantAddress;
    }
    
    /**
     * Returns if the defintion has a redundant address.
     * 
     * @return True if it has a redundant address else false.
     */
    public boolean hasRedundantAddress() {
        return redundantAddress != null;
    }
    
    /**
     * Returns the id of the item defintion.
     * 
     * @return The id of the defintion.
     */
    public long getId() {
        return id;
    }

    /**
     * Overridden equls method. The objects equal if they have the same id.
     * 
     * @param obj The object to compare to.
     * @return true if the objects equal else false.
     */
    @Override
    public boolean equals(final Object obj) {
        boolean equals;
        if (obj != null && obj instanceof ItemDefinition< ? >) {
            equals = ((ItemDefinition< ? >) obj).getId() == id;
        }
        else {
            equals = false;
        }
        return equals;
    }
    
    /**
     * Returns the hash code of this object.
     * 
     * @return The hash code of this object which equals the hash code of its
     * ID.
     */
    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }
    
    /**
     * True if the item is subscribed else false.
     * 
     * @return True if the item is subscribed at the OPC server else false.
     */
    public boolean isSubscribed() {
        return subscribed;
    }
    
    /**
     * Sets the subscription to the subscription state of the provided boolean
     * parameter.
     * 
     * @param subscribed The value the subscribed state should have.
     */
    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }
}
