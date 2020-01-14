package cern.c2mon.shared.common.supervision;

/**
 * The possible status changes of the supervised
 * entities. The status should only pertain to whether
 * the component is running correctly or not (not to
 * other status information such as reconfiguration
 * status).
 *
 * UNCERTAIN: indicates the server is not sure of the status of the DAQ/Equipment,
 *            for instance after a server downtime
 *
 * STOPPED: Deprecated in 2019/10/17 as there didn't seem to be any consumers
 *
 * @author Mark Brightwell
 *
 */
public enum SupervisionStatus { RUNNING, DOWN, STARTUP, @Deprecated STOPPED, UNCERTAIN, RUNNING_LOCAL }
