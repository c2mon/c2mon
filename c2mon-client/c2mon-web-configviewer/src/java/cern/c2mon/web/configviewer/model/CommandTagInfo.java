package cern.c2mon.web.configviewer.model;

import java.util.List;

public class CommandTagInfo {

    private String name;
    private String id;
    private List<KeyValue> address;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setAddress(List<KeyValue> address) {
        this.address = address;
    }
    public List<KeyValue> getAddress() {
        return address;
    }

}
