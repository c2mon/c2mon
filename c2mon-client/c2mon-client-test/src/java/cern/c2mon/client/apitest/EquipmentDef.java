package cern.c2mon.client.apitest;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class EquipmentDef {
   
    private Long ruleTagId;
    private String name;
    private String type;
       
    public long getRuleTagId() {
        return ruleTagId == null ? 0 : ruleTagId;
    }

    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    
    public EquipmentDef(final Long ruleTagId, final String name, final String type) {
        this.ruleTagId = ruleTagId;
        this.name = name;
        this.type = type;
    }    
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof EquipmentDef))
            return false;

        EquipmentDef other = (EquipmentDef) obj;
        return new EqualsBuilder().append(this.ruleTagId, other.ruleTagId).append(this.name, other.name).append(
                this.type, other.type).isEquals();
    }
    
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 21).append(this.ruleTagId).append(this.name).append(this.type)
                .hashCode();
    }    
    
}
