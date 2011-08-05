package cern.c2mon.client.apitest;

public class MetricDef {

    Long metricTagId;
    Long ruleTagId;
    String metricName;
    
    
    public long getMetricTagId() {
        return metricTagId == null ? 0 : metricTagId;
    }

    public long getRuleTagId() {
        return ruleTagId == null ? 0 : ruleTagId;
    }

    public String getMetricName() {
        return metricName;
    }

    public MetricDef(final Long metricTagId, final Long equipmentRuleTag, final String metricName) {
        this.metricTagId = metricTagId;
        this.ruleTagId = equipmentRuleTag;
        this.metricName = metricName;

    }
}
