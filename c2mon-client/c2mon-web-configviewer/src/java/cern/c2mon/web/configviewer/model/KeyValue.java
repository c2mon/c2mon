package cern.c2mon.web.configviewer.model;

public class KeyValue {

    private String key;
    private String value;
    
    public KeyValue() {}
    
    public KeyValue(String key, String value) {
        this();
        setKey(key);
        setValue(value);
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    
    
    
}
