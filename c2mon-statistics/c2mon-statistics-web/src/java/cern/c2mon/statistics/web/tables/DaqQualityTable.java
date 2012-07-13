package cern.c2mon.statistics.web.tables;

import java.util.ArrayList;
import java.util.List;

public class DaqQualityTable {

    /**
     * The codes to put in the table.
     */
    private List<QualityCode> qualityCodes;
    
    /**
     * Table Constructor.
     */
    public DaqQualityTable() {
        qualityCodes = new ArrayList<QualityCode>();
        qualityCodes.add(new QualityCode(0,"OK","the value is valid (all other codes are invalidity codes)"));
        qualityCodes.add(new QualityCode(1,"OUT_OF_BOUNDS","the value is out of the specified min-max range (set at configuration)"));
        qualityCodes.add(new QualityCode(2,"VALUE_CORRUPTED"," the value received from the equipment was corrupted before it was received by the DAQ"));
        qualityCodes.add(new QualityCode(3,"CONVERSION_ERROR","the value received cannot be converted to the type expected by the DAQ"));
        qualityCodes.add(new QualityCode(4,"DATA_UNAVAILABLE","the tag source is currently unavailable on the equipment side"));
        qualityCodes.add(new QualityCode(5,"UNKNOWN","the value is invalid for an unknown reason or some reason not covered by the other quality codes"));
        qualityCodes.add(new QualityCode(6,"UNSUPPORTED_TYPE","the type of the value received from the source is not supported by the DAQ"));
        qualityCodes.add(new QualityCode(7,"INCORRECT_NATIVE_ADDRESS","the value is cannot be retrieved because the equipment hardware address is incorrect"));
        
    }

    /**
     * @return the qualityCodes
     */
    public List<QualityCode> getQualityCodes() {
        return qualityCodes;
    }

    /**
     * @param qualityCodes the qualityCodes to set
     */
    public void setQualityCodes(List<QualityCode> qualityCodes) {
        this.qualityCodes = qualityCodes;
    }
    
}
