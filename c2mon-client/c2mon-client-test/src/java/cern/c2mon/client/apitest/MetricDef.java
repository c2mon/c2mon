package cern.c2mon.client.apitest;

public class MetricDef {
   long equipmentRuleTag;
	String metricName;

	public long getEquipmentRuleTag() {
		return equipmentRuleTag;
	}

	public String getMetricName() {
		return metricName;
	}
	
	public MetricDef(final long equipmentRuleTag, final String equipmentName) {
		this.equipmentRuleTag = equipmentRuleTag;
		this.metricName = equipmentName;
		
	}
}
