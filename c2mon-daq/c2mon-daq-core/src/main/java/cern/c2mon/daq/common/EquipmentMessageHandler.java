/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.common;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationHandler;
import cern.c2mon.daq.common.impl.EquipmentMessageSender;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;

/**
 * The Abstract EquipmentMessageHandler is a general superclass for all
 * specialized handlers for different devices (e.g TDS, DIP, Siemens). The class
 * has different methods to access parts of the core like sending, configuration
 * logging or command handling.
 */
public abstract class EquipmentMessageHandler {
	/**
	 * The equipment-specific logger. It is created for each equipment message
	 * handler instance.
	 */
  @Setter @Getter
	private EquipmentLoggerFactory equipmentLoggerFactory;
	/**
	 * The equipment message sender to send and filter data tags which should be
	 * send to the server.
	 */
	@Setter
	private EquipmentMessageSender equipmentMessageSender;
	/**
	 * The Equipment configuration object.
	 */
	@Getter
	private IEquipmentConfigurationHandler equipmentConfigurationHandler;
	/**
	 * The command handler to register the command runner.
	 */
	@Setter @Getter
	private IEquipmentCommandHandler equipmentCommandHandler;
	
	/**
	 * The process logger
	 */
	private static final Logger COMMON_LOGGER = LoggerFactory.getLogger(EquipmentMessageHandler.class);

	/**
	 * This static method is used to create an instance of appropriate
	 * EquipmentMessageHandler subclass
	 *
	 * @param handlerClassName The name of the handler
	 * @param equipmentCommandHandler The command handler to register the command runner.
	 * @param equipmentConfigurationHandler The configuration handler to access the
	 * configuration and listen to changes.
	 * @param equipmentMessageSender The equipment message sender to control all
	 * sending operations.
	 * @return EquipmentMessageHandler The new EquipmentMessageHandler.
	 * @throws ClassNotFoundException May throw a ClassNotFoundException.
	 * @throws IllegalAccessException May throw a IllegalAccessException.
	 * @throws InstantiationException May throw a InstantiationException.
	 */
	public static final EquipmentMessageHandler createEquipmentMessageHandler(
			final String handlerClassName,
			final IEquipmentCommandHandler equipmentCommandHandler,
			final IEquipmentConfigurationHandler equipmentConfigurationHandler,
			final EquipmentMessageSender equipmentMessageSender)
					throws InstantiationException, IllegalAccessException,
					ClassNotFoundException {
		COMMON_LOGGER.debug("entering createFromConfiguration()..");
		COMMON_LOGGER.debug("creating EquipmentMessageHandler...");
		COMMON_LOGGER.debug("\t Class name = " + handlerClassName);
		EquipmentMessageHandler eqMh = (EquipmentMessageHandler) Class.forName(handlerClassName).newInstance();
		eqMh.setEquipmentConfigurationHandler(equipmentConfigurationHandler);
		eqMh.setEquipmentCommandHandler(equipmentCommandHandler);
		eqMh.setEquipmentMessageSender(equipmentMessageSender);
		COMMON_LOGGER.debug("leaving createFromConfiguration()");
		return eqMh;
	}

	/**
	 * This method returns a reference to the Equipment configuration object
	 * (containing configuration parameters of the considered equipment unit).
	 * It does the same as calling
	 * getEquipmentConfigurationHandler().getEquipmentConfiguration().
	 *
	 * @return EquipmentConfiguration
	 */
	public IEquipmentConfiguration getEquipmentConfiguration() {
		return getEquipmentConfigurationHandler().getEquipmentConfiguration();
	}


	/**
	 * This method sets the Equipment unit's reference to the
	 * EquipmentConfigurationHandler object
	 *
	 * @param equipmentConfigurationHandler
	 *            The EquipmentConfigurationHandler object
	 */
	public final void setEquipmentConfigurationHandler(
			final IEquipmentConfigurationHandler equipmentConfigurationHandler) {
		this.equipmentConfigurationHandler = equipmentConfigurationHandler;
	}

	/**
	 * Returns the equipment logger of this handler.
	 * @return The equipment logger.
	 */
	public EquipmentLogger getEquipmentLogger() {
		return equipmentLoggerFactory.getEquipmentLogger();
	}

	/**
	 * Returns the equipment logger of this handler.
	 * @param loggerName name for a more sophisticated logging.
	 * @return The equipment logger.
	 */
	public EquipmentLogger getEquipmentLogger(final String loggerName) {
		return equipmentLoggerFactory.getEquipmentLogger(loggerName);
	}

	/**
	 * Returns the equipment logger of this handler.
	 * @param clazz Class for a more sophisticated logging.
	 * @return The equipment logger.
	 */
	public EquipmentLogger getEquipmentLogger(final Class< ? > clazz) {
		return equipmentLoggerFactory.getEquipmentLogger(clazz);
	}

	/**
	 * This abstract method needs to be implemented by the specialized
	 * EquipmentMessageHandler class. When this method is called, the
	 * EquipmentMessageHandler is expected to connect to its data source. If the
	 * connection fails (potentially, after several attempts), an EqIOException
	 * must be thrown.
	 *
	 * @throws EqIOException
	 *             Error while connecting to the data source.
	 */
	public abstract void connectToDataSource() throws EqIOException;

	/**
	 * This abstract method needs to be implemented by the specialized
	 * EquipmentMessageHandler class. When this method is called, the
	 * EquipmentMessageHandler is expected to disconnect from its data source.
	 * If the disconnection fails, an EqIOException must be thrown.
	 *
	 * @throws EqIOException
	 *             Error while disconnecting from the data source.
	 */
	public abstract void disconnectFromDataSource() throws EqIOException;

	/**
	 * This method should refresh all cache values with the values from the
	 * equipment hardware and send them to the server.
	 */
	public abstract void refreshAllDataTags();

	/**
	 * This method should refresh the data tag cache value with the value
	 * from the equipment hardware and send them to the server.
	 *
	 * @param dataTagId The id of the data tag to refresh.
	 */
	public abstract void refreshDataTag(final long dataTagId);

	/**
	 * Returns the equipment message sender.
	 *
	 * @return The current equipment message sender.
	 */
	public IEquipmentMessageSender getEquipmentMessageSender() {
		return equipmentMessageSender;
	}

	/**
	 * This disconnects the DAQ from the datasource and makes sure
	 * all delayed data (time deadband) is send immediately.
	 *
	 * @throws EqIOException Throws an equipment IOException if the
	 * sending fails.
	 */
	public void shutdown() throws EqIOException {
		disconnectFromDataSource();
		equipmentMessageSender.sendDelayedTimeDeadbandValues();
	}
}
