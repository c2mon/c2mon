package cern.c2mon.client.common.tag;

import cern.c2mon.client.common.listener.ClientCommandTagReportListener;
import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.tim.shared.client.command.CommandReport;

public interface ClientCommandTag {

  /**
   * Returns the unique identifier of this tag
   * @return the DataTag Identifier
   */
  public Long getId();

  /**
   * Returns the tagname
   * @return the unique tagname for this tag
   */
  public String getName();

  /**
   * Returns the description of this tag
   * @return the description
   */
  public String getDescription();

  /**
   * Returns the last value set on this command tag.
   * @see #getType
   * @return the tag value
   */
  public Object getValue();

  /**
   * Sets a new value for a CommandTag and sends it to the C2MON server for execution.
   * @param value The new value for this <code>ClientCommandTag</code>
   * @return The report sent by the C2MON server
   */
  public CommandReport setValue(final Object value);

  /**
   * Returns true if the user who requested the command tag from the server
   * is authorised to execute the command. 
   * @return authorization information for the tag
   */
  public boolean isAuthorized();

  /**
   * Returns true if the command tag contains a valid handle from the server.
   * @return validity information for the tag
   */
  public boolean isExistingCommand();

  /**
   * Returns the type of the tagValue attribute
   * @see #getValue
   * @return the class of the tag value
   */
  public Class<?> getType();

  /**
   * Adds a <code>DataTagUpdateListener</code> to the ClientDataTag and 
   * generates an initial update event for that listener.
   * Any change to the ClientDataTag value or quality attributes will trigger
   * an update event to all <code>ClientCommandTagReportListener</code> objects 
   * registered.
   * @see DataTagUpdateListener.html removeUpdateListener
   * @param l the DataTagUpdateListener comments
   */
  public void addCommandReportListener(final ClientCommandTagReportListener l);

  /**
   * Removes a previously registered <code>ClientCommandTagReportListener</code>
   * @see #addUpdateListener
   * @param l the DataTagUpdateListener comments
   */
  public void removeCommandReportListener(final ClientCommandTagReportListener l);

  /**
   * @return <code>true</code>, in case that this command tag has listeners registered
   */
  public boolean hasCmdRepListenersRegistered();

  /**
   * Returns the minimum value for this command.
   * If no minimum value has been defined, the method returns null.
   * @return the minimum value for this command
   */
  public Comparable<?> getMinValue();

  /**
   * Returns the maximum value for this command.
   * If no maximum value has been defined, the method returns null.
   * @return the maximum value for this command.
   */
  public Comparable<?> getMaxValue();

}