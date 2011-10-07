package cern.c2mon.web.configviewer.model;

import java.util.List;

public class DataTagInfo {

    private String id;
    private String name;
    private List<KeyValue> value;
    private List<KeyValue> config;
    private List<KeyValue> address;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<KeyValue> getValue() {
        return value;
    }

    public void setValue(List<KeyValue> value) {
        this.value = value;
    }

    public List<KeyValue> getConfig() {
        return config;
    }

    public void setConfig(List<KeyValue> config) {
        this.config = config;
    }

    public List<KeyValue> getAddress() {
        return address;
    }

    public void setAddress(List<KeyValue> address) {
        this.address = address;
    }

    public boolean isConfigEmpty() {
        if (config == null || config.isEmpty())
            return true;
        else
            return false;
    }

    public boolean isValueEmpty() {
        if (value == null || value.isEmpty())
            return true;
        else
            return false;
    }

    public boolean isAddressEmpty() {
        if (address == null || address.isEmpty())
            return true;
        else
            return false;
    }

    public boolean isEmpty() {
        if (isConfigEmpty() && isValueEmpty() && isAddressEmpty())
            return true;
        else
            return false;
    }

}
