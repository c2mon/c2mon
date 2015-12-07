package cern.c2mon.server.eslog.structure.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alban Marguet.
 */
@Slf4j
public class TagNumericMapping extends TagESMapping implements Mapping {
    Properties properties;

    public TagNumericMapping(String type) {
        super();
        setProperties(type);
    }

    @Override
    public String getMapping() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    @Override
    public void setProperties(String tagValueType) {
        if (!(tagValueType.compareTo(intType) == 0) && !(tagValueType.compareTo(doubleType) == 0)) {
            throw new IllegalArgumentException("Type for TagNumeric must be integer or double.");
        } else {
            this.properties = new Properties(tagValueType);
        }
    }
}
