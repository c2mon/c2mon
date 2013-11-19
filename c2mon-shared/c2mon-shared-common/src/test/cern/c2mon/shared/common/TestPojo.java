package cern.c2mon.shared.common;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;

public class TestPojo {
    private Long id = 1000L;
    private String name = "Pojo";
    private int numberTest = 0;
    private Long testNumber = 238438L;
    @XmlElement(name="Pi")
    private double aDoubleNumber = 3.1415926;
    private transient String transientString = "notInDocument"; // Will be ignored
    private String normalString = "sdjhsd";
    private String nullString; // Will be ignored
    private int notSetInt; // Will be assumed as 0
    private Integer nullInt; // Will be ignored
    private List<String> list = new ArrayList<String>();
    
    @XmlValue
    private String equipmentUnitXml = "<aaa>some internal xml block</aaa>"; // shall be wrapped inside <![CDATA[ ]]
}
