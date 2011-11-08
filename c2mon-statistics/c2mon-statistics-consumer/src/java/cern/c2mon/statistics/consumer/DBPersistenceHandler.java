package cern.c2mon.statistics.consumer;

import java.util.List;

import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;

/**
 * This class is used to access the static methods of SqlMapper used to write
 * to the database from the required fallback methods.
 * @author mbrightw
 *
 */
public class DBPersistenceHandler implements IDBPersistenceHandler {
    
    /**
     * Write the values to the DB
     * @param dataValues values to be written to the database
     * @throws IDBPersistenceException if there is a problem with writing the values to the database
     */
    public final void storeData(final List dataValues) throws IDBPersistenceException {
        SqlMapper.writeToDatabase(dataValues);
    }
    
    /**
     * For fallback mechanism - not used.
     * 
     * @param object the object to be saved
     */
    public void storeData(final IFallback object) {
      throw new UnsupportedOperationException("Method not implemented in this application.");
    }
    
    /**
     * Fallback mechanism method, providing human readable info on DB user.
     * @return the DB info String
     */
    public final String getDBInfo() {
        return "TIMDAQLOG account on tim database";
    }

}
