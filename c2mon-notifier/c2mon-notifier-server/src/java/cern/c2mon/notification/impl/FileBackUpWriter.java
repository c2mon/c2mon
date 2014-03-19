/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.UserNotFoundException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A RegistryBackupWriter implementation which writes the data to a file using {@link Gson}.
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class FileBackUpWriter implements BackupWriter {

    /**
     * default file to store the data in.
     */
    private String fileName = "registry.backup";
    
    private long lastFullStorageTime = 0;
    
    /**
     * our Logger.
     */
    private Logger logger = LoggerFactory.getLogger(FileBackUpWriter.class);
    
    private long lastStoreTime = 0;
    private long lastLoadTime = 0;
    
    /**
     * @return Returns the lastStoreTime.
     */
    @Override
    @ManagedAttribute
    public long getLastStoreTime() {
        return lastStoreTime;
    }

    /**
     * @return Returns the lastLoadTime.
     */
    @Override
    @ManagedAttribute
    public long getLastLoadTime() {
        return lastLoadTime;
    }
    
    /**
     * Empty constructor.
     * 
     * @param fileName the file where to store the data.
     */
    public FileBackUpWriter(String fileName) {
        super();
        this.fileName = fileName;
    }
    
    /**
     * loads the registry from a file.
     * 
     * @param fileName the name of the file to load the registry from .
     * @return a HashMap with the {@link Subscriber#getUserName()} as key and the {@link Subscriber} as value.
     */
    @Override
    public ConcurrentHashMap<String, Subscriber> load() {
        ConcurrentHashMap<String, Subscriber> newUsers = new ConcurrentHashMap<String, Subscriber>();
        // load newUsers from DB, File, etc....
        
        FileReader fr = null;
        BufferedReader input = null;
        long t1 = System.currentTimeMillis();
        
        try {
            fr = new FileReader(fileName);
            input = new BufferedReader(fr);
            StringBuilder contents = new StringBuilder();
            String line = null; 
            while ((line = input.readLine()) != null) {
              contents.append(line);
              contents.append(System.getProperty("line.separator"));
            }
            Gson gson = new Gson();
            newUsers = gson.fromJson(contents.toString(), 
                        new TypeToken<HashMap<String, Subscriber>>(){
                                                                        //
                                                                    } .getType());
            input.close();
        } catch (Exception ex) {
            logger.error("Cannot store to file " + fileName, ex.getMessage());
        } finally {
            try { if (input != null) input.close(); } catch (IOException e) {
                e.printStackTrace();
            }
            try { if (fr != null) fr.close(); } catch (IOException e) {
                e.printStackTrace();
            }
        }
        lastLoadTime = System.currentTimeMillis() - t1;
        
        long subs = 0;
        for (Subscriber s : newUsers.values()) {
            subs += s.getSubscribedTagIds().size();
        }
        
        logger.info("Loaded {} users and {} subscriptions in {msec" , newUsers.size() , subs , getLastLoadTime());
        return newUsers;
    }
    
    /**
     * Stores the passed data to a file.
     * 
     * @param toStore the data to store.
     */
    @Override
    public void store(final ConcurrentHashMap<String, Subscriber> toStore) {
        FileWriter fr = null;
        BufferedWriter output = null;
        long t1 = System.currentTimeMillis();

        try {
            fr = new FileWriter(fileName);
            output = new BufferedWriter(fr);
            Gson gson = new Gson();
            String toStoreString = gson.toJson(toStore);
            output.write(toStoreString);
            output.close();
        } catch (Exception ex) {
            logger.error("Cannot store to file " + fileName, ex.getMessage());
        } finally {
            try { if (output != null) output.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (fr != null) fr.close(); } catch (IOException e) { e.printStackTrace(); }
        }
        lastStoreTime = System.currentTimeMillis() - t1;
        
        // count the subscriptions
        int subs = 0;
        for (Subscriber s : toStore.values()) {
            subs += s.getSubscribedTagIds().size();
        }
        // info message on storage
        logger.info("Stored {} users and {} subscriptions in {}msec." , toStore.size() , subs , getLastStoreTime());
        lastFullStorageTime = System.currentTimeMillis();
    }
    
    @Override
    @ManagedAttribute
    public boolean isFine() {
        File f = new File(fileName);
        return f.canWrite();
    }

    @Override
    public void addSubscriber(Subscriber s) {
        // NOOP
    }

    @Override
    public void addSubscription(Subscription s) {
        // NOOP
        
    }

    @Override
    public Subscriber getSubscriber(String id) throws UserNotFoundException {
        // NOOP
        return null;
    }

    @Override
    public void removeSubscriber(Subscriber s) {
        // NOOP
        
    }

    @Override
    public void removeSubscription(Subscription s) {
        // NOOP
        
    }

    @Override
    @ManagedAttribute
    public String getUniqueName() {
        return FileBackUpWriter.class.toString();
    }

    @Override
    @ManagedAttribute
    public long getLastFullWrite() {
        return lastFullStorageTime;
    }
}
