package cern.c2mon.cache.api;

import cern.c2mon.cache.api.parser.XmlParser;
import cern.c2mon.server.common.device.*;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Szymon Halastra
 */

@Slf4j
public class XmlParserTest {

  @Test
  public void parseXmlCommandsForCommandList() throws Exception {
    String xmlString = "<Commands> <Command id = \"70012\" name = \"ping\"> <description>Ping of the DAQ " +
            "process</description> </Command> <Command id = \"70011\" name = \"restart\"> <description>Restart of " +
            "the" + " DAQ process</description> </Command> <Command id = \"70009\" name = \"start\"> " +
            "<description>Start of " + "the DAQ process</description> </Command> <Command id = \"70010\" name = " +
            "\"stop\"> <description>Stop of the DAQ process</description> </Command> </Commands>";

    List<Command> commands = XmlParser.parse(xmlString, CommandList.class);

    assertEquals("List should have 4 elements", 4, commands.size());
    assertTrue("List should have Property elements", commands.get(0) instanceof Command);

    Command command = commands.get(0);
    assertEquals("DeviceProperty should have following id", Long.valueOf(70012L), command.getId());
    assertEquals("DeviceProperty should have following name", "ping", command.getName());
    assertEquals("DeviceProperty should have following description", "Ping of the DAQ process", command.getDescription());
  }

  @Test
  public void parseXmlCommandsForDeviceCommandList() throws Exception {
    String xmlString = "<DeviceCommands> <DeviceCommand id = \"70009\" name = \"start\"> <value>204911</value> " +
            "<category>commandTagId</category> </DeviceCommand> <DeviceCommand id = \"70010\" name = \"stop\"> " +
            "<value>204912</value> <category>commandTagId</category> </DeviceCommand> <DeviceCommand id = \"70011\" "
            + "name = \"restart\"> <value>213882</value> <category>commandTagId</category> </DeviceCommand> " +
            "</DeviceCommands>\n";

    List<DeviceCommand> commands = XmlParser.parse(xmlString, DeviceCommandList.class);

    assertEquals("List should have 4 elements", 3, commands.size());
    assertTrue("List should have Property elements", commands.get(0) instanceof DeviceCommand);

    DeviceCommand deviceCommand = commands.get(0);
    assertEquals("DeviceProperty should have following id", Long.valueOf(70009L), deviceCommand.getId());
    assertEquals("DeviceProperty should have following name", "start", deviceCommand.getName());
    assertEquals("DeviceProperty should have following value", "204911", deviceCommand.getValue());
    assertEquals("DeviceProperty should have following category", "commandTagId", deviceCommand.getCategory());
  }

  @Test
  public void parseXmlPropertiesForPropertyList() throws Exception {
    String xmlString = "<Properties> <Property id = \"70008\" name = \"viewLinks\"> <description>Addional links " +
            "available from the symbol context menu</description> </Property> <Property id = \"70001\" name = " +
            "\"processName\"> <description>DAQ process name</description> </Property> <Property id = \"70007\" name " +
            "=" + " \"directLink\"> <description>Direct link of the symbol</description> </Property> <Property id = "
            + "\"70002\" name = \"equipmentName\"> <description>Equipment host name</description> </Property> " +
            "<Property" + " id = \"70004\" name = \"group\"> <description>Equipment responsible group</description> " +
            "</Property> " + "<Property id = \"70006\" name = \"equipmentStatus\"> <description>Equipment state " +
            "tag</description> " + "</Property> <Property id = \"70003\" name = \"type\"> <description>Equipment " +
            "type</description> " + "</Property> <Property id = \"70005\" name = \"processStatus\"> " +
            "<description>Process state " + "tag</description> </Property> </Properties>";

    List<Property> properties = XmlParser.parse(xmlString, PropertyList.class);

    assertEquals("List should have 4 elements", 8, properties.size());
    assertTrue("List should have Property elements", properties.get(0) instanceof Property);

    Property deviceProperty = properties.get(0);
    assertEquals("DeviceProperty should have following id", Long.valueOf(70008L), deviceProperty.getId());
    assertEquals("DeviceProperty should have following name", "viewLinks", deviceProperty.getName());
    assertEquals("DeviceProperty should have following description", "Addional links available from the symbol context menu", deviceProperty.getDescription());
  }

  @Test
  public void parseXmlPropertiesForDevicePropertyList() throws Exception {
    String xmlString = "<DeviceProperties> <DeviceProperty id = \"70001\" name = \"processName\"> <value>" +
            "<![CDATA[P_ACCESSNORTH]]></value> <category>constantValue</category> </DeviceProperty> <DeviceProperty "
            + "id = \"70002\" name = \"equipmentName\"> <value><![CDATA[ACCESSNORTH]]></value> " +
            "<category>constantValue</category> </DeviceProperty> <DeviceProperty id = \"70003\" name = \"type\"> " +
            "<value> <![CDATA[JEC]]></value> <category>constantValue</category> </DeviceProperty> <DeviceProperty id " +
            "" + "= \"70004\" name = \"group\"> <value> <![CDATA[BE-ICS-CSE]]></value> " +
            "<category>constantValue</category> " + "</DeviceProperty> <DeviceProperty id = \"70005\" name = " +
            "\"processStatus\"> <value>204907</value> " + "<category>tagId</category> </DeviceProperty> " +
            "<DeviceProperty id = \"70006\" name = \"equipmentStatus\"> " + "<value>204909</value> " +
            "<category>tagId</category> </DeviceProperty> <DeviceProperty id = \"70007\" name =" + " \"directLink\"> " +
            "<value> <![CDATA[https://oraweb.cern.ch/pls/timw3/helpalarm" + "" +
            ".AlarmList?p_procname=P_ACCESSNORTH&TAB=true&TITLE=List%20of%20alarms&TYPE=DOCUMENTATION]]></value> " +
            "<category>constantValue</category> </DeviceProperty> <DeviceProperty id = \"70008\" name = " +
            "\"viewLinks\"> <value> <![CDATA[https://oraweb.cern.ch/pls/timw3/helpalarm" + "" +
            ".AlarmList?p_procname=P_ACCESSNORTH&TAB=true&TITLE=List%20of%20alarms%20(HelpAlarm)&TYPE=DOCUMENTATION; " +
            "" + "http://timweb.cern.ch/c2mon-web-configviewer/process/P_ACCESSNORTH?TAB=true&TITLE=DAQ%20Process" +
            "%20Configuration&TYPE=DOCUMENTATION; https://oraweb.cern.ch/pls/timw3/smile" + "" +
            ".queryDisplay?nType=4&Tablename=VSML_ONLINETAGS&vHeader=TAGID&vSelect=TAGID&vClause=PROCNAME%20+LIKE+%20" +
            "" + "'P_ACCESSNORTH'&TAB=true&TITLE=P_ACCESSNORTH%20DataTags&TYPE=TID_VIEW]]></value> " +
            "<category>constantValue</category> </DeviceProperty> </DeviceProperties>\n";

    List<DeviceProperty> deviceProperties = XmlParser.parse(xmlString, DevicePropertyList.class);

    assertEquals("List should have 4 elements", 8, deviceProperties.size());
    assertTrue("List should have Property elements", deviceProperties.get(0) instanceof DeviceProperty);

    DeviceProperty deviceProperty = deviceProperties.get(0);
    assertEquals("DeviceProperty should have following id", Long.valueOf(70001L), deviceProperty.getId());
    assertEquals("DeviceProperty should have following name", "processName", deviceProperty.getName());
    assertEquals("DeviceProperty should have following value", "P_ACCESSNORTH", deviceProperty.getValue());
    assertEquals("DeviceProperty should have following category", "constantValue", deviceProperty.getCategory());
  }
}
