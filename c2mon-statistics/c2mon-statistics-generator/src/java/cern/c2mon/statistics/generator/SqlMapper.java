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

package cern.c2mon.statistics.generator;


/**
 * Wrapper class for the SqlMapClient (ibatis)
 *
 * @author mbrightw
 *
 */
public final class SqlMapper {

//    /**
//     * Logger.
//     */
//    private static final Logger LOGGER = Logger.getLogger(SqlMapper.class);
//
//    /**
//     * The SqlMapClient that this class is wrapping.
//     */
//    private static final SqlMapClient SQLMAP;
//
//    //configures the SqlMapClient from the configuration file
//    static {
//      //get properties file containing DB access details
//      String timPropertiesLocation;
//      if (System.getProperty("c2mon.web.conf.url") == null) {
//          //if not set as option, then get from user home
//          timPropertiesLocation = System.getProperty("user.home") + "/c2mon-web.properties";
//      }
//      else {
//          //else use the specified properties
//          timPropertiesLocation = System.getProperty("c2mon.web.conf.url");
//      }
//
//      // the input stream for properties file
//      FileInputStream timPropertiesFile = null;
//
//      // Windows path name hack
//      if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
//          timPropertiesLocation = timPropertiesLocation.replace("file:///", "");
//      } else {
//          timPropertiesLocation = timPropertiesLocation.replace("file://", "");
//      }
//
//      try {
//        timPropertiesFile = new FileInputStream(timPropertiesLocation);
//      } catch (java.io.FileNotFoundException ex) {
//        LOGGER.error("FileNotFoundException caught when looking for properties: " + ex.getMessage());
//        ex.printStackTrace();
//        throw new RuntimeException(ex);
//      }
//
//      Properties properties = new Properties();
//
//      //load the properties
//      try {
//          properties.load(timPropertiesFile);
//      } catch (java.io.IOException ex) {
//          LOGGER.error("IOException caught when loading properties file: " + ex.getMessage());
//          ex.printStackTrace();
//          throw new RuntimeException(ex);
//      }
//
//      //set the SQLMAP, injecting the properties
//      try {
//          String resource = "cern/c2mon/statistics/generator/sqlmap/GeneratorSqlMapConfig.xml";
//          Reader reader = Resources.getResourceAsReader(resource);
//          SQLMAP = SqlMapClientBuilder.buildSqlMapClient(reader, properties);
//      } catch (Exception e) {
//          LOGGER.error("Exception caught while initializing SqlConfig class: " + e.getMessage());
//          e.printStackTrace();
//          throw new RuntimeException("Error initializing SqlConfig class. Cause: " + e);
//      }
//    }
//
//    /**
//     * Private constructor to override public one.
//     */
//    private SqlMapper() {
//    }
//
//    /**
//     * Getter method returning the wrapped SqlMapClient.
//     *
//     * @return the SqlMapClient
//     */
//    public static SqlMapClient getSqlMapInstance() {
//        return SQLMAP;
//    }
//
//    /**
//     * Method returning a List of BarChartValue's read from the database.
//     *
//     * @param tableName the table/view from which the values must be read
//     * @return the list of values
//     * @throws SQLException error in database transaction
//     */
//    public static List<BarChartValue> getBarChartData(final String tableName) throws SQLException {
//        //type safety below is ensured in ibatis result-map statement
//        List<BarChartValue> chartValues = SqlMapper.getSqlMapInstance().queryForList("getBarChartData", tableName);
//        return chartValues;
//    }
//
//    public static List<IChartCollectionValue> getBarChartCollectionData(final String tableName) throws SQLException {
//        //type safety below is ensured in ibatis result-map statement
//        List<IChartCollectionValue> chartValues = SqlMapper.getSqlMapInstance().queryForList("getBarChartCollectionData", tableName);
//        return chartValues;
//    }
//
//    /**
//     * The static method used for collecting the stacked bar chart data from the database.
//     *
//     * @param tableName the table/view from which the values are read
//     * @return the list of values
//     * @throws SQLException exception in database transaction
//     */
//    public static List<StackedBarChartValue> getStackedBarChartData(final String tableName) throws SQLException {
//        //type safety below is ensured in ibatis result-map statement
//        List<StackedBarChartValue> chartValues = SqlMapper.getSqlMapInstance().queryForList("getStackedBarChartData", tableName);
//        return chartValues;
//    }
//
//    /**
//     * Static method returning getting the data from the database for collections of stacked bar charts.
//     * @param tableName the table where the values are kept
//     * @return a list of values
//     * @throws SQLException if problem with database transaction
//     */
//    public static List<IChartCollectionValue> getStackedBarChartCollectionData(final String tableName) throws SQLException {
//        //type safety below is ensured in ibatis result-map statement
//        List<IChartCollectionValue> chartValues = SqlMapper.getSqlMapInstance().queryForList("getStackedBarChartCollectionData", tableName);
//        return chartValues;
//    }
//
//
//    /**
//     * Method returning a List of PieChartValue's read from the database.
//     *
//     * @param tableName the table/view from which the values must be read
//     * @return the list of values
//     * @throws SQLException error in database transaction
//     */
//    public static List<PieChartValue> getPieChartData(final String tableName) throws SQLException {
//        //type safety below is ensured in ibatis result-map statement
//        List<PieChartValue> chartValues = SqlMapper.getSqlMapInstance().queryForList("getPieChartData", tableName);
//        return chartValues;
//    }
//
//    public static List<IChartCollectionValue> getPieChartCollectionData(final String tableName) throws SQLException {
//        //type safety below is ensured in ibatis result-map statement
//        List<IChartCollectionValue> chartValues = SqlMapper.getSqlMapInstance().queryForList("getPieChartCollectionData", tableName);
//        return chartValues;
//    }
}
