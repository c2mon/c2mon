package cern.c2mon.statistics.consumer;

import java.sql.Timestamp;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.tim.shared.daq.filter.FilteredDataTagValue;

/**
 * Wrapper class for FilterDataTagValue objects, implementing the
 * necessary fallback interface (for fallback manager).
 * @author mbrightw
 *
 */
public class FilterPersistenceObject implements IFallback {

    /**
     * The wrapped FilterDataTagValue.
     */
    private FilteredDataTagValue filterDataTagValue;
    
    /**
     * Constructor of the wrapper persistence object from
     * the FitleredDataTagValue.
     * @param fdtv the wrapped object
     */
    public FilterPersistenceObject(final FilteredDataTagValue fdtv) {   
        filterDataTagValue = fdtv;
        //chop the quality description after 100 characters so that it fits in the DB
        if (filterDataTagValue.getQualityDescription() != null  && filterDataTagValue.getQualityDescription().length() > 100 ) {
          filterDataTagValue.setQualityDescription(
                filterDataTagValue.getQualityDescription().substring(0, 100)
                );
        }
    }
    
    
    /**
     * Converts the object to a String (used by FallBack mechanism).
     * @return the String representing the object
     */
    public final String toString() {
        StringBuffer str = new StringBuffer();
        str.append(filterDataTagValue.getId());
        str.append('\t');
        str.append(filterDataTagValue.getName());
        str.append('\t');
        if (filterDataTagValue.getValue() != null) {
            str.append(filterDataTagValue.getValue());
        } else {
            str.append("null");
        } 
        str.append('\t'); 
        str.append(filterDataTagValue.getQualityCode());
        str.append('\t');
        if (filterDataTagValue.getQualityDescription() != null) {
            if (filterDataTagValue.getQualityDescription().equals("")) {
                str.append("null");
            } else {
                str.append(filterDataTagValue.getQualityDescription());
            }     
        } else {
            str.append("null");
        }                  
        str.append('\t');
        str.append(filterDataTagValue.getTimestamp());
        str.append('\t');
        if (filterDataTagValue.getValueDescription() != null) {
            if (filterDataTagValue.getValueDescription().equals("")) {
                str.append("null");
            } else {
                str.append(filterDataTagValue.getValueDescription());
            }     
        } else {
            str.append("null");
        }    
        str.append('\t');      
        str.append(filterDataTagValue.getDataType());
        str.append('\t');
        str.append(filterDataTagValue.isDynamicFiltered());
        str.append('\t');
        str.append(filterDataTagValue.getFilterApplied());
        return str.toString();
    }
    
    /**
     * Method needed by fallback manager. Converts a String representation of a FilterPersistenceObject
     * back to an object instance.
     * 
     * @param str the String to extract the object from
     * @return the new object generated from the string
     * @throws DataFallbackException if conversion error when creating object from string
     */
    public final IFallback getObject(final String str) throws DataFallbackException {
        String[] value = str.split("\t");
        int j = 0;
        
        FilteredDataTagValue filteredValue;
        try {
            String tmpValue;
            
            Long pId = new Long(value[j++]);                //id
            String pName = value[j++];                      //name
                   
            tmpValue = value[j++];                          //value
            String pValue;
            if (tmpValue == "null") {
                pValue = null;
            } else {
                pValue = tmpValue;
            }
            
            Short pQualityCode = new Short(value[j++]);     //quality code
            
            tmpValue = value[j++];                          //quality desc
            String pQualityDesc;
            if (tmpValue == "null") {
                pQualityDesc = null;
            } else {
                pQualityDesc = tmpValue;
            }
            
            Timestamp pTimestamp = Timestamp.valueOf(value[j++]);  //timestamp
            
            tmpValue = value[j++];                          //value desc
            String pValueDesc;
            if (tmpValue == "null") {
                pValueDesc = null;
            } else {
                pValueDesc = tmpValue;
            }
            
            String pDatatype = value[j++];                  //data type
            Boolean pDynamicFilter = new Boolean(value[j++]).booleanValue(); //dynamic filter
            Short pFilterApplied = new Short(value[j++]).shortValue();       //filter applied
            
            
            
            filteredValue = new FilteredDataTagValue(
                                                    pId,                           //id
                                                    pName,                         //name
                                                    pValue,                        //value
                                                    pQualityCode,                  //quality code
                                                    pQualityDesc,                  //quality desc
                                                    pTimestamp,                    //timestamp
                                                    pValueDesc,                    //value desc
                                                    pDatatype,                     //data type
                                                    pDynamicFilter,                //dynamic filter
                                                    pFilterApplied                 //filter applied
                                                    );
            
        } catch (RuntimeException ex) {
            //to catch conversion errors
            throw new DataFallbackException("Error in fallback mechanism when converting the strings back to objects: " + ex.getStackTrace());
        }
        FilterPersistenceObject filterPersistenceObject = new FilterPersistenceObject(filteredValue);
        return filterPersistenceObject;
    }
    
    /**
     * For fallback mechanism (not used).
     * @return a string representation of the tag id
     */
    public final String getId() {
        return filterDataTagValue.getId().toString();
    }


    /**
     * Getter method returning the wrapped FilterDataTagValue
     * @return the filterDataTagValue
     */
    public final FilteredDataTagValue getFilteredDataTagValue() {
        return filterDataTagValue;
    }
    
}
