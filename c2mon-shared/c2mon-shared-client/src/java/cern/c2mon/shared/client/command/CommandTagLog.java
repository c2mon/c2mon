package cern.c2mon.shared.client.command;

import java.sql.Timestamp;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * Object used by the C2MON persistence manager for logging objects to
 * the STL DB with fallback to file logging.
 * 
 * @author Mark Brightwell 
 *
 */
public class CommandTagLog implements IFallback {
  
  /**
   * Command id.
   */
  private Long tagId;
  
  /**
   * Command name.
   */
  private String name;
  
  /**
   * Operational mode.
   */
  private Short mode;
  
  /**
   * Time this command was executed.
   */
  private Timestamp executionTime;
  
  /**
   * Value of the command when executed.
   */
  private String value;
  
  /**
   * Datatype of the command.
   */
  private String dataType;
  
  /**
   * User executing it.
   */
  private String user;
  
  /**
   * Host this command was executed on.
   */
  private String host;
  
  /**
   * Time in the report.
   */
  private Timestamp reportTime;
  
  /**
   * Report status.
   */
  private CommandExecutionStatus reportStatus;
  
  /**
   * Report description.
   */
  private String reportDescription;
  
  private transient static Gson gson = GsonFactory.createGson();
   
  /**
   * @return the command id
   */
  public String getId() {
    return tagId.toString();
  }  
  
  /**
   * @return the value of the command when executed
   */
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return gson.toJson(this);
  }
  
  @Override
  public IFallback getObject(final String line) throws DataFallbackException {
    return gson.fromJson(line, this.getClass());
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * @return the mode
   */
  public Short getMode() {
    return mode;
  }

  /**
   * @param mode the mode to set
   */
  public void setMode(final Short mode) {
    this.mode = mode;
  }

  /**
   * @return the executiontime
   */
  public Timestamp getExecutionTime() {
    return executionTime;
  }

  /**
   * @param executiontime the executiontime to set
   */
  public void setExecutionTime(final Timestamp executiontime) {
    this.executionTime = executiontime;
  }

  /**
   * @return the dataType
   */
  public String getDataType() {
    return dataType;
  }

  /**
   * @param dataType the dataType to set
   */
  public void setDataType(final String dataType) {
    this.dataType = dataType;
  }

  /**
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(final String user) {
    this.user = user;
  }

  /**
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * @param host the host to set
   */
  public void setHost(final String host) {
    this.host = host;
  }

  /**
   * @return the reportTime
   */
  public Timestamp getReportTime() {
    return reportTime;
  }

  /**
   * @param reportTime the reportTime to set
   */
  public void setReportTime(final Timestamp reportTime) {
    this.reportTime = reportTime;
  }

  /**
   * @return the reportStatus
   */
  public CommandExecutionStatus getReportStatus() {
    return reportStatus;
  }

  /**
   * @param reportStatus the reportStatus to set
   */
  public void setReportStatus(final CommandExecutionStatus reportStatus) {
    this.reportStatus = reportStatus;
  }

  /**
   * @return the reportDesc
   */
  public String getReportDescription() {
    return reportDescription;
  }

  /**
   * @param reportDesc the reportDesc to set
   */
  public void setReportDescription(final String reportDesc) {
    this.reportDescription = reportDesc;
  }

  /**
   * @param id the id to set
   */
  public void setId(final Long id) {
    this.tagId = id;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final String value) {
    this.value = value;
  }

  /**
   * @return the tagId
   */
  public Long getTagId() {
    return tagId;
  }

  /**
   * @param tagId the tagId to set
   */
  public void setTagId(final Long tagId) {
    this.tagId = tagId;
  }

  /**
   * New hashcode implementation.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
    result = prime * result + ((executionTime == null) ? 0 : executionTime.hashCode());
    result = prime * result + ((host == null) ? 0 : host.hashCode());
    result = prime * result + ((mode == null) ? 0 : mode.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((reportDescription == null) ? 0 : reportDescription.hashCode());
    result = prime * result + ((reportStatus == null) ? 0 : reportStatus.hashCode());
    result = prime * result + ((reportTime == null) ? 0 : reportTime.hashCode());
    result = prime * result + ((tagId == null) ? 0 : tagId.hashCode());
    result = prime * result + ((user == null) ? 0 : user.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }


  /**
   * True iff the log objects correspond to the same log event.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CommandTagLog other = (CommandTagLog) obj;
    if (dataType == null) {
      if (other.dataType != null)
        return false;
    } else if (!dataType.equals(other.dataType))
      return false;
    if (executionTime == null) {
      if (other.executionTime != null)
        return false;
    } else if (!executionTime.equals(other.executionTime))
      return false;
    if (host == null) {
      if (other.host != null)
        return false;
    } else if (!host.equals(other.host))
      return false;
    if (mode == null) {
      if (other.mode != null)
        return false;
    } else if (!mode.equals(other.mode))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (reportDescription == null) {
      if (other.reportDescription != null)
        return false;
    } else if (!reportDescription.equals(other.reportDescription))
      return false;
    if (reportStatus == null) {
      if (other.reportStatus != null)
        return false;
    } else if (!reportStatus.equals(other.reportStatus))
      return false;
    if (reportTime == null) {
      if (other.reportTime != null)
        return false;
    } else if (!reportTime.equals(other.reportTime))
      return false;
    if (tagId == null) {
      if (other.tagId != null)
        return false;
    } else if (!tagId.equals(other.tagId))
      return false;
    if (user == null) {
      if (other.user != null)
        return false;
    } else if (!user.equals(other.user))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

}
