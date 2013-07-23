/*
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.ping;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Target is a host to be checked, with its status data and timestamps etc.
 */
public class Target {
    /**
     * The status a Target might have. Undefined should only be during init stage, and switch to one of the "defined"
     * values. Warning: The app uses also the ordinal() value of the enum ... don't sort the values differently!
     */
    public enum PingStatus {
        Undefined(0, "Target host: %s status undefined"), Reachable(1, "Target host: %s replied within timeout"), Unreachable(
                2, "Destination host: %s unreachable"), UnkownHost(3, "Unknown host: %s");

        private int code;
        private String description;

        private PingStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return this.code;
        }

        public String getDescription() {
            return this.description;
        }

    };

    private final String hostname; // reference information
    private InetAddress address; // derived from hostname

    private volatile PingStatus current = PingStatus.Undefined; // status with initial value
    private volatile PingStatus previous = PingStatus.Undefined;
    private volatile boolean hasChanged = false; // change detection flag

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public Target(String hostname) {
        this.hostname = hostname;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    // -- SETTERS ---------------------------------------------------
    //
    /**
     * Most of the machines in our network have a fixed IP address. This information should therefore not change very
     * often. Nevertheless, it happens that the name is assigned to a new machine, or the machine moved to another
     * network. From time to time, we need to get the latest IP address for the host.
     */
    public void refreshAddress() throws UnknownHostException {
        address = InetAddress.getByName(hostname);
    }

    /**
     * In case of successful contact, some flags and timestamp need to be set.
     */
    public void success() {
        previous = current;
        current = PingStatus.Reachable;
        updateChangedFlag();
    }

    /**
     * To be called when a check failed due do timeout (system simply unreachable), the methods sets the timestamps and
     * flags according to this "standard" problem. If the check fails for another reason, consider @see
     * Target.failure(Exception e)
     */
    public void failure() {
        previous = current;
        current = PingStatus.Unreachable;
        updateChangedFlag();
    }

    /**
     * To be called when the check fails with a Java exception, so that the reason is known to be something else than a
     * simple timeout. The method will first call the parameter-less failure() and than set status and details based on
     * the Exception parameter.
     * 
     * @param e <code>Exception</code>
     */
    public void failure(Exception e) {
        failure();
        if (e instanceof UnknownHostException) {
            previous = current;
            current = PingStatus.UnkownHost;
            // reset the address
            address = null;
        } else {
            failure();
        }
    }

    public void undefined() {
        previous = current;
        current = PingStatus.Undefined;
        updateChangedFlag();
    }

    //
    // -- GETTERS ---------------------------------------------------
    //

    public String getHostname() {
        return hostname;
    }

    public InetAddress getAddress() {
        return address;
    }

    public PingStatus getCurrentStatus() {
        return current;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    private void updateChangedFlag() {
        if (previous != current) {
            hasChanged = true;
        } else {
            hasChanged = false;
        }
    }

}
