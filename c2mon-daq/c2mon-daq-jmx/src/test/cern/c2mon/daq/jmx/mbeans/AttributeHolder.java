package cern.c2mon.daq.jmx.mbeans;

public class AttributeHolder implements AttributeHolderMBean {

    private int testAttribute = 5;

    @Override
    public int getTestAttribute() {
        return this.testAttribute;
    }

    @Override
    public void setTestAttribute(int value) {
        this.testAttribute = value;
    }

}
