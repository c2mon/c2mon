package cern.c2mon.driver.common.messaging.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cern.c2mon.driver.common.messaging.ProcessMessageReceiver;
import cern.tim.shared.daq.command.SourceCommandTagReport;
import cern.tim.shared.daq.config.ConfigurationChangeEventReport;
import cern.tim.shared.daq.config.ConfigurationXMLConstants;
import cern.tim.shared.daq.datatag.SourceDataTagValueResponse;

/**
 * This is just a small implementation to test the program by 'sending'
 * messages through dropping files in a directory.
 * 
 * @author Andreas Lang
 *
 */
public class DirectoryMessageReceiver extends ProcessMessageReceiver 
        implements ConfigurationXMLConstants {
//    private static final Logger LOGGER = Logger.getLogger(DirectoryMessageReceiver.class);
    
    private static final String USER_HOME_ENV = "user.home";
    
    private static final String READ_DIR_NAME = "messages";
    
    private static final String TMP_DIR_NAME = "tmp-messages";

    private static final int MAX_TMP_FILES = 10;
    
    private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    
    private String userHomeDirectory;
    
    private Timer timer;
    
    private TimerTask directoryReader =  new TimerTask() {
        @Override
        public void run() {
            File readDir = new File(userHomeDirectory + File.separatorChar + READ_DIR_NAME);
            File[] messages = readDir.listFiles();
            if (messages.length > 0) {
                read(messages[0]);
            }
        }
    };

    public DirectoryMessageReceiver() {
        userHomeDirectory = System.getProperty(USER_HOME_ENV);
        File readDir = new File(userHomeDirectory + File.separatorChar + READ_DIR_NAME);
        if (!readDir.exists()) {
            readDir.mkdir();
        }
        File tmpDir = new File(userHomeDirectory + File.separatorChar + TMP_DIR_NAME);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
    }
    
    private void read(final File file) {
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            if (document.getDocumentElement().getTagName()
                    .equals(CONFIGURATION_CHANGE_EVENT_ELEMENT)) {
                onReconfigureProcess(document, null, null);
            }
            else if (document.getDocumentElement().getTagName()
                    .equals("CommandTag")) {
                onExecuteCommand(document, new Topic() {
                    @Override
                    public String getTopicName() throws JMSException {
                        return "fake";
                    }
                }, null);
            }
            copyToTemp(file);
            deleteOldestTmpFile();
            file.delete();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteOldestTmpFile() {
        File tmpDir = new File(userHomeDirectory + File.separatorChar + TMP_DIR_NAME);
        File[] files = tmpDir.listFiles();
        if (files.length > MAX_TMP_FILES) {
            File oldestFile = null;
            for (File file : files) {
                if (oldestFile == null || file.lastModified() < oldestFile.lastModified()) {
                    oldestFile = file;
                }
            }
            oldestFile.delete();
        }
    }

    private void copyToTemp(final File file) throws IOException {
        Reader reader = null;
        Writer writer = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            writer = new BufferedWriter(new FileWriter(userHomeDirectory 
                    + File.separatorChar + TMP_DIR_NAME + File.separatorChar 
                    + System.currentTimeMillis() + file.getName()));
            int curByte;
            while ((curByte = reader.read()) != -1) {
                writer.write(curByte);
            }
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
        }
    }

    @Override
    public void connect() {
        timer = new Timer(true);
        timer.schedule(directoryReader, 10L, 1000L);
    }

    @Override
    public void disconnect() {
        timer.cancel();
    }

    @Override
    public void sendDataTagValueResponse(SourceDataTagValueResponse sourceDataTagValueResponse, Topic replyTopic, Session session) throws JMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendConfigurationReport(ConfigurationChangeEventReport configurationChangeEventReport, Destination destination, Session session) throws TransformerException, ParserConfigurationException,
            IllegalAccessException, InstantiationException, JMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendCommandReport(SourceCommandTagReport commandReport, Destination destination, Session session) throws JMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void shutdown() {
      // TODO Auto-generated method stub
      
    }

}
