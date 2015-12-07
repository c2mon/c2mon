package cern.c2mon.server.eslog.structure.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Alban Marguet.
 */
public class TagBooleanMapping extends TagESMapping implements Mapping {
    Properties properties;

    public TagBooleanMapping(String type) {
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
        if (tagValueType.compareTo(boolType) != 0) {
            throw new IllegalArgumentException("Type for TagBoolean must be boolean.");
        } else {
            properties = new Properties(tagValueType);
        }
    }
}

