package cern.c2mon.daq.japc;

import static java.lang.String.format;
import static java.lang.System.out;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.test.UseHandler;

public abstract class TestConfigGenerator {

    protected static final String processName = "P_JAPC01";

    protected static final String jmsUserName = processName;
    protected static final String jmsPassword = "JAPC01_P";
    protected static final int processId = 1;
    protected static final int eqId = 1;
    protected static final String eqName = "E_JAPC_JAPC01";
    protected static final String daqHostName = "cs-ccr-dev3.cern.ch";
    protected static final int processAlive = 101;
    protected static final int commfaultTag = 103;

    protected static final String outputFileNameTemplate = "gen-conf-xml/%s-gen-%s-dtags.xml";

    static final String confHeaderTemplate =

    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n %s" + "<ProcessConfiguration process-id=\"" + processId
            + "\" type=\"initialise\">\n" + "<jms-user>" + jmsUserName + "</jms-user>\n" + "<jms-password>"
            + jmsPassword + "</jms-password>\n" + "<jms-qcf-jndi-name>jms/process/factories/QCF</jms-qcf-jndi-name>\n"
            + "<jms-queue-jndi-name>jms/process/destinations/queues/processmessage/" + processName
            + "</jms-queue-jndi-name>\n" + "<jms-listener-topic>tim.process." + daqHostName + "." + processName + "."
            + processName + ".1287494947876</jms-listener-topic>\n" + "<alive-tag-id>" + processAlive
            + "</alive-tag-id>\n" + "<alive-interval>60000</alive-interval>"
            + "<max-message-size>100</max-message-size>\n" + "<max-message-delay>1000</max-message-delay>\n"
            + "<EquipmentUnits>\n" + "   <EquipmentUnit id=\"" + eqId + "\" name=\"" + eqName + "\">\n"
            + "       <handler-class-name>%s</handler-class-name>\n" + "       <commfault-tag-id>" + commfaultTag
            + "</commfault-tag-id>\n" + "       <commfault-tag-value>false</commfault-tag-value>\n"
            + "       <address/>\n" + "       <SubEquipmentUnits>\n" + "       </SubEquipmentUnits>\n"
            + "       <DataTags>\n";

    static final String confFooter =

    "      </DataTags>\n" + "      <CommandTags>\n" + "      </CommandTags>\n" + "   </EquipmentUnit>\n"
            + "</EquipmentUnits>\n" + "</ProcessConfiguration>";

    protected final String getHeader() {
        return String.format(confHeaderTemplate, "<!--\n" + getFileDescription() + "-->\n",
                getEquipmentMessageHandlerClass().getName());
    }

    protected final Class<? extends EquipmentMessageHandler> getEquipmentMessageHandlerClass() {
        Class<? extends TestConfigGenerator> clazz = this.getClass();

        if (clazz.isAnnotationPresent(UseHandler.class)) {
            return clazz.getAnnotation(UseHandler.class).value();
        }

        return null;
    }

    protected abstract String getDataTags();

    protected abstract String getOutputFileName();

    protected abstract String getFileDescription();

    public final void generateConfXML() {

        BufferedOutputStream os = null;
        try {
            File f = new File(getOutputFileName());
            os = new BufferedOutputStream(new FileOutputStream(f));

            out.println(format("Generating file: %s", f.getAbsolutePath()));

            if (f.exists()) {
                os.write(getHeader().getBytes());
                os.write(getDataTags().getBytes());
                os.write(confFooter.getBytes());
            }// if f exists

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (os != null)
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    // not much to do here
                }
        }
    }

}
