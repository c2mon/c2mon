package cern.c2mon.statistics.web.tables;

public class QualityCode {
    
    /**
     * The quality code.
     */
    private int code;
    
    /**
     * Quality code name.
     */
    private String name;
    
    /**
     * The quality description.
     */
    private String description;

    
    /**
     * Constructor.
     * @param code
     * @param description
     * @param name 
     */
    public QualityCode(int code, String name, String description) {
        super();
        this.code = code;
        this.description = description;
        this.name = name;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

   
    
    
}
