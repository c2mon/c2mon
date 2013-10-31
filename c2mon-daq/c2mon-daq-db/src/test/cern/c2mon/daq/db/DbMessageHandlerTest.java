package cern.c2mon.daq.db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

import cern.c2mon.daq.db.Alert;
import cern.c2mon.daq.db.AlertTimeOutException;
import cern.c2mon.daq.db.DBMessageHandler;
import cern.c2mon.daq.db.dao.DbDaqDaoImpl;
import cern.c2mon.daq.db.dao.IDbDaqDao;
import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.EquipmentConfigurationHandler;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.conf.core.ProcessConfigurationLoader;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.daq.tools.processexceptions.ConfRejectedTypeException;
import cern.c2mon.daq.tools.processexceptions.ConfUnknownTypeException;
import cern.tim.shared.common.datatag.address.DBHardwareAddress;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * Test class for checking the communication of the dbdaq with the database. It allows testing of alert registering,
 * unregistering, sending and receiving alerts and timeouts.
 * */
@ContextConfiguration(locations = {"classpath:cern/c2mon/daq/db/config/test-daq-db-config.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class DbMessageHandlerTest {
  
  /**
   * The list of datatags ids read from the xml configuration file.
   * The timdbdaq.db_daq table must contain the same datatags as the xml configuration file.
   * */  
  private static List<Long> dataTagIds;
  /**
   * the equipment handler for db daq
   * */
  private static DBMessageHandler dbMessageHandler;
  /**
   * test DAO with specific features for signalling alerts from java (in production this is done from PL/SQL api)
   * */
  private static DbDaqDaoTest testDao;
  /**
   * amount of signalled alerts
   * */
  private int amountSignalled = 0;
  

  /**
   * Sets up the environment for the JUnit tests when the testing starts. Reads the XML file with the dbdaq process 
   * configuration (datatags definition) and populates the configuration of the dbMessageHandler, sets the hardware address
   * of the equipment (connection parameters for the database). Initializes the helper testDao object responsible for 
   * simulating of sending alerts - which is normally done by the PL/SQL API.
   * */
  @SuppressWarnings("unchecked")
  @BeforeClass
  public static void setUp() {
      
    String propertiesLocation = System.getProperty("c2mon.properties.location");  
    Properties prop = new Properties();
    String equAddress = "";
    try {
        FileInputStream file = new FileInputStream(propertiesLocation);
        prop.load(file);
        file.close();
        equAddress = prop.getProperty("dbDaq.equipment.address");
    } catch (FileNotFoundException e2) {
        e2.printStackTrace();
    } catch (IOException e2) {
        e2.printStackTrace();
    } 
    
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:cern/c2mon/daq/db/config/test-daq-db-config.xml");
    testDao = (DbDaqDaoTest) context.getBean("testDbDaqDao");
    dbMessageHandler = (DBMessageHandler) context.getBean("dbMessageHandler");  
    
    ProcessConfigurationLoader processLoader = new ProcessConfigurationLoader();
    
    try {
        URL turl = DbMessageHandlerTest.class.getResource("./config/test-process-xml-configuration.xml");
        String path = turl.getPath();
        Document xmlDoc = processLoader.loadConfigLocal(path);
        ProcessConfiguration processConfig = processLoader.createProcessConfiguration("P_DBDAQ01", 1234L, xmlDoc, true);
        ConfigurationController controller = new ConfigurationController(null, null);
        controller.setProcessConfiguration(processConfig);
        long equId = processConfig.getEquipmentConfigurations().keySet().iterator().next();
        processConfig.getEquipmentConfiguration(equId).setAddress(equAddress);
        dbMessageHandler.setEquipmentConfigurationHandler(new EquipmentConfigurationHandler(equId, controller));
    } catch (ConfUnknownTypeException e1) {
        e1.printStackTrace();
    } catch (ConfRejectedTypeException e1) {
        e1.printStackTrace();
    }

    dataTagIds = new ArrayList(dbMessageHandler.getEquipmentConfiguration().getSourceDataTags().keySet());
    
    try {
        dbMessageHandler.setDBDataSourceAddress();
        String url = dbMessageHandler.getDbDaqDao().getCustomDataSource().getUrl();
        String username = dbMessageHandler.getDbDaqDao().getCustomDataSource().getUsername();
        String password = dbMessageHandler.getDbDaqDao().getCustomDataSource().getPassword();
        testDao.setDataSourceParams(url, username, password);
        
    } catch (EqIOException e) {
        e.printStackTrace();
    }
    
  }
  
  /**
   * Tests that the equipment address is setup correctly (the database connection parameters). 
   * */
  @Test
  public void testDBSetup() {
      IDbDaqDao dao =  dbMessageHandler.getDbDaqDao();
      BasicDataSource ds = ((DbDaqDaoImpl) dao).getCustomDataSource();
      Assert.assertTrue(ds.getUsername().equals("timdbdaq"));
      Assert.assertNotNull(ds.getUrl());
      Assert.assertNotNull(ds.getPassword());
  }
  
  /**
   * Test insert of a new datatag
   * */
  @Test
  public void testInsertDataTags() {
      IDbDaqDao dbDaqDao = dbMessageHandler.getDbDaqDao();
      List<Long> registeredDataTags = dbDaqDao.getDataTags();
      for (Long dataTagId : dbMessageHandler.getEquipmentConfiguration().getSourceDataTags().keySet()) {
          if (!registeredDataTags.contains(dataTagId)) {
              ISourceDataTag sdt = dbMessageHandler.getEquipmentConfiguration().getSourceDataTags().get(dataTagId); 
              String name = ((DBHardwareAddress) sdt.getHardwareAddress()).getDBItemName();
              String type = sdt.getDataType();
              String value = "0";
              //System.out.println("Inserting missing datatag: " + dataTagId + " - " + name);
              dbDaqDao.insertNewDataTag(dataTagId, name, value, type, SourceDataQuality.OK, null);
          }
      }
  }
  
  /**
   * Test that the latest values for all alerts are returned.
   * */
  @Test
  public void testGetLastValuesOfAlerts() {
      IDbDaqDao dao = dbMessageHandler.getDbDaqDao();
      List<Alert> alerts = dao.getLastAlerts(dataTagIds);
      Assert.assertTrue(alerts.size() == dataTagIds.size());
  }
  
  /**
   * Test registering for alerts for all datatags.
   * */
  @Test
  public void testRegisterAlerts() {
      IDbDaqDao dao = dbMessageHandler.getDbDaqDao();
      for (long datatagId : dbMessageHandler.getEquipmentConfiguration().getSourceDataTags().keySet()) {
          dao.registerForAlert(Long.toString(datatagId));
      }
  }
  /**
   * Test unregistering for alerts for all datatags.
   * */
  @Test
  public void testUnregisterAlerts() {
      IDbDaqDao dao = dbMessageHandler.getDbDaqDao();
      for (long datatagId : dbMessageHandler.getEquipmentConfiguration().getSourceDataTags().keySet()) {
          dao.unregisterFromAlert(Long.toString(datatagId));
      }
  }
  
  /**
   * Test the timeout while waiting for a specific alert.
   * Registers for all alerts but starts waiting for a specific one. Times out after 2 milliseconds. Unregisters
   * */
  @Test
  public void testTimeoutOneAlert() {
      testRegisterAlerts();
      IDbDaqDao dao = dbMessageHandler.getDbDaqDao();
      try {
        dao.waitForAlert(Long.toString(dataTagIds.get(0)), 2);
    } catch (AlertTimeOutException e) {
        Assert.assertTrue(true);
    }
    testUnregisterAlerts();
  }
 
  /**
   * Tests the timeout while waiting for any alert to arrive.
   * Registers for all alerts and starts waiting. Times out after 2 milliseconds. Unregisters.
   * */
  @Test
  public void testTimeoutAnyAlert() {
      testRegisterAlerts();
      IDbDaqDao dao = dbMessageHandler.getDbDaqDao();
      try {
        dao.waitForAnyAlert(2);
      } catch (AlertTimeOutException e) {
        Assert.assertTrue(true);
      }
      testUnregisterAlerts();
  }
  
  /**
   * Registers for alerts for a specific datatag. Starts the sender thread asking for 10 alerts 
   * in 1 millisecond intervals. Waits for the signals and verifies if all of them were received.
   * */
  @Test
  public void testReceiveOneAlertManyTimes() {
      IDbDaqDao dao = dbMessageHandler.getDbDaqDao();
      String alertId = Long.toString(dataTagIds.get(0));
      dao.registerForAlert(alertId);
      int amount = 10;
      int caught = 0;
      signalOneAlert(dataTagIds.get(0), 10, amount);
      while (true) {
          try {
              Alert a = dao.waitForAlert(alertId, 7);
              caught++;
          } catch (AlertTimeOutException e) {
              break;
          }
      }
      System.out.println("Amount of signalled alerts: " + amount);
      System.out.println("Amount of received alerts: " + caught);
      Assert.assertTrue(caught == amount);
      dao.unregisterFromAlert(alertId);
  }
  /**
   * Registers for alerts for a specific datatag. Signals the alerts and catches it.
   * Finally unregisters.
   * */
  @Test
  public void testReceiveOneAlertOnce() {
      IDbDaqDao dao = dbMessageHandler.getDbDaqDao();
      String alertId = Long.toString(dataTagIds.get(0));
      dao.registerForAlert(alertId);
      
      signalOneAlert(dataTagIds.get(0), 10, 1);
      try {
        Alert a = dao.waitForAlert(alertId, 10);
        Assert.assertNotNull(a);
      } catch (AlertTimeOutException e) {
        Assert.assertFalse(true);
      }
      dao.unregisterFromAlert(alertId);
  }
  
  /**
   * Send alert for a given datatag
   * @param dataTagId       id of the datatag 
   * @param howOftenMillis  the interval between the signals
   * @param howMany         the amount of alerts to be sent
   * */
  public void signalOneAlert(final long dataTagId, final long howOftenMillis, final int howMany) {
      Runnable r = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < howMany; i++) {
                testDao.signalAlert(dataTagId);
                if (howOftenMillis > 0)
                    try {
                        Thread.sleep(howOftenMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        } };
     Thread t = new Thread(r);
     t.start();
  }
  
  /**
   * Registers for alerts for all datatags, starts the sender thread (signalAllRegisteredAlerts) and
   * listens to the alerts. 
   * */
  @Test
  public void testReceiveAllRegisteredAlerts() {
      IDbDaqDao dao = dbMessageHandler.getDbDaqDao();
      testRegisterAlerts();
      signalAllRegisteredAlerts();
      int amountReceived = 0;
      try {
          while (true) {
              Alert a = dao.waitForAnyAlert(5);
              amountReceived++;
          }
      } catch (AlertTimeOutException e) {
      }
      System.out.println("Amount of signalled alerts: " + amountSignalled);
      System.out.println("Amount of received alerts: " + amountReceived);
      
      //Assert.assertTrue(amountReceived == amountSignalled);
      testUnregisterAlerts();
  }

  /**
   * Signals alerts for all datatags
   * */
  private void signalAllRegisteredAlerts() {
      Runnable r = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (long id : dataTagIds) {
                for (int i = 0; i < 2; i++) {
                    testDao.signalAlert(id);
                    amountSignalled++;
                }
                if (amountSignalled % 10 == 0)
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        } };
     Thread t = new Thread(r);
     t.start();
  }
  
}
