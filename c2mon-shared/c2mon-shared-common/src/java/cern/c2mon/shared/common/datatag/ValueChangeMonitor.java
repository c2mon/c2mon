/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.shared.common.datatag;

import java.io.Serializable;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

import cern.c2mon.shared.util.parser.SimpleXMLParser;

/**
 * The <code>ValueChangeMonitor</code> class (or VCM) defines the value-change-monitoring factors for selected metrics
 * 
 * @author wbuczak
 */
public class ValueChangeMonitor implements Serializable, Cloneable {

    public static enum OPERATOR {
        EQUALS("","=","!="), MORE("M", ">","<="), LESS("L","<", ">="), UNDEFINED("?", "?","?");

        private String operatorXml;
        private String operatorEpl;
        private String negOperatorEpl;

        private OPERATOR(final String operatorXml, final String operatorEpl, final String negOperatorEpl) {
            this.operatorXml = operatorXml;
            this.operatorEpl = operatorEpl;
            this.negOperatorEpl = negOperatorEpl;
        }

        public String getOperatorXml() {
            return operatorXml;
        }
        
        public String getOperatorEpl() {
            return operatorEpl;
        }
                
        public String getNegOperatorEpl() {
            return negOperatorEpl;
        }
    }

    private static final long serialVersionUID = -1866720946000066235L;

    private volatile Double step;

    private volatile Integer timeWindow;

    private volatile OPERATOR operator = OPERATOR.UNDEFINED;

    /**
     * @param fieldNode
     * @return
     */
    public static ValueChangeMonitor fromConfigXML(Element element) {

        ValueChangeMonitor result = new ValueChangeMonitor();

        try {
            if (element.hasAttribute("timeWindow")) {
                result.setTimeWindow(element.getAttribute("timeWindow"));
            }
            if (element.hasAttribute("step")) {
                result.setStep(element.getAttribute("step"));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Exception caught when instantiating ValueChangeMonitor from XML", ex);
        }
        return result;
    }

    /**
     * @param attribute
     */
    private void setTimeWindow(String attribute) {
        timeWindow = Integer.parseInt(attribute);
    }

    /**
     * @param attribute
     */
    private void setStep(String attribute) {
        if (attribute.substring(0, 1).equals("M")) {
            this.setOperator(OPERATOR.MORE);
            this.step = Double.parseDouble(attribute.substring(1));
        } else if (attribute.substring(0, 1).equals("L")) {
            this.setOperator(OPERATOR.LESS);
            this.step = Double.parseDouble(attribute.substring(1));
        } else {
            this.setOperator(OPERATOR.EQUALS);
            this.step = Double.parseDouble(attribute);
        }
    }

    /**
     * @param operator The operator to set.
     */
    private void setOperator(OPERATOR operator) {
        this.operator = operator;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ValueChangeMonitor other = (ValueChangeMonitor) obj;
        if (step == null) {
            if (other.step != null)
                return false;
        } else if (!step.equals(other.step))
            return false;
        if (timeWindow == null) {
            if (other.timeWindow != null)
                return false;
        } else if (!timeWindow.equals(other.timeWindow))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((step == null) ? 0 : step.hashCode());
        result = prime * result + ((timeWindow == null) ? 0 : timeWindow.hashCode());
        return result;
    }

    /**
     * @return Returns the operator.
     */
    public OPERATOR getOperator() {
        return operator;
    }

    public boolean hasStep() {
        return step == null ? false : true;
    }

    public Double getStep() {
        return step;
    }

    public boolean hasTimeWindow() {
        return timeWindow == null ? false : true;
    }

    public Integer getTimeWindow() {
        return timeWindow;
    }

    @Override
    public String toString() {
        return toConfigXML();
    }
    
    public String toConfigXML() {
     
        StringBuilder bld = new StringBuilder("<value-change-monitor ");
        
        if (step != null ) {
            bld.append("step=\"").append(operator.getOperatorXml()).append(step.floatValue()).append("\" ");
        }

        if (timeWindow != null ) {
            bld.append("timeWindow=\"").append(timeWindow).append("\" ");
        }
        
        bld.append("/>");
        
        return bld.toString();
    }
    
}
