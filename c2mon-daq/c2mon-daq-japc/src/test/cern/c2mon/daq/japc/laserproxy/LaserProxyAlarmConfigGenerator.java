/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.japc.laserproxy;

import static java.lang.String.format;
import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cern.c2mon.daq.japc.TestConfigGenerator;
import cern.c2mon.daq.japc.laserproxy.LaserProxyJapcMessageHandler;
import cern.c2mon.daq.test.UseHandler;

@UseHandler(LaserProxyJapcMessageHandler.class)
public class LaserProxyAlarmConfigGenerator extends TestConfigGenerator {

    static final String dataTagTemplate = "         <DataTag id=\"%d\" name=\"%s\" control=\"false\">\n"
            + "            <data-type>%s</data-type>\n"
            + "            <DataTagAddress>\n"
            + "                <HardwareAddress class=\"ch.cern.c2mon.shared.datatag.address.impl.JAPCHardwareAddressImpl\">\n"
            + "                   <protocol>no</protocol>\n" + "                   <service>diamon_demo</service>\n"
            + "                   <device-name>%s</device-name>\n"
            + "                   <property-name>%s</property-name>\n"
            + "                   <data-field-name>%s</data-field-name>\n" + "                 </HardwareAddress>\n"
            + "                 <time-to-live>3600000</time-to-live>\n" + "                 <priority>2</priority>\n"
            + "                 <guaranteed-delivery>false</guaranteed-delivery>\n" + "            </DataTagAddress>\n"
            + "         </DataTag>\n";

    private String createTagXML(long tagId, String tagName, String tagType, String deviceName, String propertyName,
            String fieldName) {
        return String.format(dataTagTemplate, tagId + 1000, tagName, tagType, deviceName, propertyName, fieldName);
    }

    @Override
    protected String getDataTags() {

        StringBuilder str = new StringBuilder();

        List<String> tripplets = null;

        try {
            tripplets = load();
            long tagNo = 1;
            for (String t : tripplets) {
                String tagname = "NO:DIAMON_DEMO:" + t;
                str.append(createTagXML(tagNo++, tagname, "Boolean", "LaserProxy", "Metrics", t));
            }
            out.println(format("number of DataTags generated: %d", tagNo - 1));

        } catch (Exception ex) {

        }

        return str.toString();
    }

    private List<String> load() throws IOException {
        List<String> list = new ArrayList<String>();

        InputStream is = null;

        try {
            is = this.getClass().getResourceAsStream("alarm_def_proxy_per_src.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }

        } finally {
            if (is != null)
                is.close();
        }

        return list;
    }

    @Override
    protected String getFileDescription() {
        StringBuilder descr = new StringBuilder(
                "  this configuration has been created for the JAPC LaserProxy devices\n");
        return descr.toString();
    }

    @Override
    protected String getOutputFileName() {
        return String.format(outputFileNameTemplate, processName, "laserproxy");
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        LaserProxyAlarmConfigGenerator generator = new LaserProxyAlarmConfigGenerator();
        generator.generateConfXML();
        // generator.loadTripplets();

    }

}
