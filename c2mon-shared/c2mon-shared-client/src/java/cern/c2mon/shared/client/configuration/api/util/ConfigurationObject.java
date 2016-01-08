package cern.c2mon.shared.client.configuration.api.util;


import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * Interface which defines objects which are used to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}.
 * Although the structure of the ConfigurationObjects are well defined each object needs to provide the
 * methods given in this interface.
 * <p/>
 * A ConfigurationObjects is a POJO which holds information for creating the {@link cern.c2mon.shared.client.configuration.ConfigurationElement}.
 * <p/>
 * If the id of the object is known by the Server the {@link cern.c2mon.server.configuration.parser.tasks.SequenceTaskFactory} tries to create a UPDATE
 * {@link cern.c2mon.shared.client.configuration.ConfigurationElement}.
 * If the id of the object is not known by the Server the {@link SequenceTaskFactory} tries to create a CREATE {@link cern.c2mon.shared.client.configuration.
 * ConfigurationElement}.
 * If field deleted is set to true the  {@link SequenceTaskFactory} tries to create a DELETE {@link cern.c2mon.shared.client.configuration.ConfigurationElement}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface ConfigurationObject {

  /**
   * returns the id value of the Id field of the ConfigurationObject
   *
   * @return id value
   */
  Long getId();

  /**
   * determine if the instance of the ConfigurationObject holds the inormation to create a DELETE {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
   *
   * @return boolean value if this object is a delete object
   */
  boolean isDeleted();

  /**
   * checks if a instance of ConfigurationObject have enough fields set to build a CREATE {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
   *
   * @return boolean value if this object is a delete object
   */
  boolean requiredFieldsGiven();
}
