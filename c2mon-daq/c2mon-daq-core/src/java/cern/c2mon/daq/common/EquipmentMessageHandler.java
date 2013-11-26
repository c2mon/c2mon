/******************************************************************************
 * This file is part of the CERN Control and Monitoring (C2MON) platform. See
 * http://cern.ch/c2mon
 * 
 * Copyright (C) 2005-2013 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: C2MON team, c2mon-support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common;

import org.apache.log4j.Logger;

import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationHandler;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.tools.TIMDriverSimpleTypeConverter;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;

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
	private EquipmentLoggerFactory equipmentLoggerFactory;
	/**
	 * The equipment message sender to send and filter data tags which should be
	 * send to the server.
	 */
	private IEquipmentMessageSender equipmentMessageSender;
	/**
	 * The Equipment configuration object.
	 */
	private IEquipmentConfigurationHandler equipmentConfigurationHandler;
	/**
	 * The command handler to register the command runner.
	 */
	private IEquipmentCommandHandler equipmentCommandHandler;
	/**
	 * The TIM driver simple type converter helps to do common conversions
	 * in the EquipmentMessageHandler implementations.
	 */
	private static final TIMDriverSimpleTypeConverter TIM_DRIVER_SIMPLE_TYPE_CONVERTER = new TIMDriverSimpleTypeConverter();
	/**
	 * The process logger
	 */
	private static final Logger COMMON_LOGGER = Logger.getLogger(EquipmentMessageHandler.class);

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
			final IEquipmentMessageSender equipmentMessageSender) 
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
	 * Sets the equipment logger factory.
	 * 
	 * @return  The equipment logger factory.
	 */
	public EquipmentLoggerFactory getEquipmentLoggerFactory() {
		return equipmentLoggerFactory;
	}

	/**
	 * Sets the equipment logger factory.
	 * 
	 * @param equipmentLoggerFactory
	 *            The new equipment logger factory.
	 */
	public void setEquipmentLoggerFactory(final EquipmentLoggerFactory equipmentLoggerFactory) {
		this.equipmentLoggerFactory = equipmentLoggerFactory;
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
	 * Sets the current equipment message sender.
	 * 
	 * @param equipmentMessageSender
	 *            The equipment message sender to set.
	 */
	public void setEquipmentMessageSender(final IEquipmentMessageSender equipmentMessageSender) {
		this.equipmentMessageSender = equipmentMessageSender;
	}

	/**
	 * Returns the equipment message sender.
	 * 
	 * @return The current equipment message sender.
	 */
	public IEquipmentMessageSender getEquipmentMessageSender() {
		return equipmentMessageSender;
	}

	/**
	 * Returns the equipment configuration handler which has all necessary 
	 * configuration for the equipment as well as ways to listen to configuration
	 * changes.
	 * 
	 * @return The equipment configuration handler of this equipment message handler.
	 */
	public IEquipmentConfigurationHandler getEquipmentConfigurationHandler() {
		return equipmentConfigurationHandler;
	}

	/**
	 * Sets the command handler of this equipment message handler.
	 * @param equipmentCommandHandler The command handler to register the equipments 
	 * command runner.
	 */
	public void setEquipmentCommandHandler(final IEquipmentCommandHandler equipmentCommandHandler) {
		this.equipmentCommandHandler = equipmentCommandHandler;
	}

	/**
	 * Returns the command handler which may be used to register the equipments
	 * command runner.
	 * @return The command handler of this equipment.
	 */
	public IEquipmentCommandHandler getEquipmentCommandHandler() {
		return equipmentCommandHandler;
	}

	/**
	 * The TIM driver simple type converter helps to do common conversions
	 * in the EquipmentMessageHandler implementations.
	 * @return The TIMDriverSimpleTypeConverter.
	 */
	public static TIMDriverSimpleTypeConverter getTimDriverSimpleTypeConverter() {
		return TIM_DRIVER_SIMPLE_TYPE_CONVERTER;
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
