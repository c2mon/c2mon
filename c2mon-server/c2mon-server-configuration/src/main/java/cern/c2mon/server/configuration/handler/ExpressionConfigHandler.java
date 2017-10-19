package cern.c2mon.server.configuration.handler;

import java.util.Properties;

import cern.c2mon.server.common.expression.ExpressionCacheObject;
import cern.c2mon.server.configuration.handler.impl.TagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * @author Martin Flamm
 */
public interface ExpressionConfigHandler extends TagConfigHandler<ExpressionCacheObject> {

  /**
   * Creates a expression in the C2MON server.
   *
   * @param element contains details of the Tag
   * @throws IllegalAccessException
   */
  void createExpression(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Updates an expression in the C2MON server. Always results in a event being
   * send to the DAQ layer.
   *
   * @param id                the id of the expression to update
   * @param elementProperties details of the fields to modify
   */
  void updateExpression(Long id, Properties elementProperties);

  /**
   * Removes an expression from the C2MON server.
   *
   * @param id        the id of the expression to remove
   * @param tagReport the report for this event;
   *                  is passed as parameter so cascaded action can attach subreports
   */
  void removeExpression(Long id, ConfigurationElementReport tagReport);
}