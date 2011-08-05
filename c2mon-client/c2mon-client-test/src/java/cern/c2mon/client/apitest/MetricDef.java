package cern.c2mon.client.apitest;

public class MetricDef {
   long equipmentRuleTag;
	String equipmentName;

	public long getEquipmentRuleTag() {
		return equipmentRuleTag;
	}

	public String getEquipmentName() {
		return equipmentName;
	}
	
	public MetricDef(final long equipmentRuleTag, final String equipmentName) {
		this.equipmentRuleTag = equipmentRuleTag;
		this.equipmentName = equipmentName;
		
	}
}
