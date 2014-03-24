/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * A class which generates the text for a mail notification
 * 
 * @author felixehm
 */
@ManagedResource(objectName = "cern.dmn2:type=Notifier,name=TemplateLoader")
public class TextCreator {
    /**
     * our Logger.
     */
    private Logger logger = LoggerFactory.getLogger(TextCreator.class);

    /**
     * maximum subject length
     */
    private int SUBJECT_TEXT_MAXLEN = 20;

    /**
     * our FreeMarker Configuration.
     */
    private Configuration c = null;
    /**
     * the directory from where to read the templates.
     */
    private String directory = "templates";

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    /**
     * @throws IOException in case it cannot initialize
     */
    public TextCreator() throws IOException {
        reLoadTemplates();

        logger.info("TextCreator created.");
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * @param directory the name of the directory containing the FreeMarker templates
     * @throws IOException in case the directory cannot be accessed.
     */
    @ManagedAttribute
    public void setTemplateDir(String directory) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting directory for templates to {}", directory);
        }
        this.directory = directory;
        reLoadTemplates();
    }

    /**
     * @return the local path of the directory where to read the templates.
     */
    @ManagedAttribute
    public String getTemplateDir() {
        return directory;
    }

    /**
     * Reloads the FreeMarker templates.
     * 
     * @see TextCreator#setTemplateDir(String)
     * @see TextCreator#getTemplateDir()
     * @throws IOException in case the {@link #directory} cannot be accessed.
     */
    @ManagedOperation
    public void reLoadTemplates() throws IOException {

        if (logger.isDebugEnabled()) {
            logger.debug("Entering reLoadTemplates().");
            logger.debug("Using {} for reading the templates", getTemplateDir());
        }

        c = new Configuration();
        c.setDirectoryForTemplateLoading(new File(directory));
        c.setObjectWrapper(new DefaultObjectWrapper());
    }

    /**
     * Renders a
     * 
     * @param update
     * @param interestingChildren
     * @return the full report
     * @throws IOException
     * @throws TemplateException
     */
    public String getReportForTag(Tag update, List<Tag> interestingChildren) throws IOException, TemplateException {
        StringBuilder bodyBuffer = new StringBuilder();
        bodyBuffer.append(getTextForRuleUpdate(update));
        bodyBuffer.append(getFreeTextMapForChildren(interestingChildren));
        return bodyBuffer.toString();
    }

    /**
     * @param update the Tag to generate the subject for.
     * @param interestingTags the interesting children rules. Needed as the passed tag is not sufficient to indicate the
     *            real problem.
     * @return the subject for the message
     */
    public String getMailSubjectForStateChange(Tag update, List<Tag> interestingTags) {
        logger.trace("TagID={} Building Mail subject..", update.getId());

        StringBuilder subject = new StringBuilder();
        if (interestingTags.size() == 0) {
            subject.append(update.getLatestStatus()).append(" ").append(update.getLatestUpdate().getDescription());
        } else if ((interestingTags.size() == 1 && !interestingTags.get(0).isRule()) || interestingTags.get(0).getLatestUpdate().getDescription().length() > SUBJECT_TEXT_MAXLEN) {
            subject.append("DMNNTFY [").append(update.getLatestStatus().toString().toUpperCase()).append("] : ")
                    .append(interestingTags.get(0).getLatestUpdate().getDescription());
        } else {
            Tag single = interestingTags.get(0);
            subject.append("DMNNTFY [").append(single.getLatestStatus().toString().toUpperCase()).append("] : ")
                    .append(single.getLatestUpdate().getDescription());
        }
        logger.trace("Leaving getMailSubjectForStateChange()");

        return subject.toString();
    }

    /** Generates the Text for an metric value update.
     * 
     * @param metricUpdate the (metric) Tag
     * @param parent The parent rule of the metric tag
     * @return The text for an metric value update.
     *  
     */
    public String getTextForMetricUpdate(Tag metricUpdate, Tag parent) {
        if (metricUpdate.isRule()) {
            throw new IllegalArgumentException("Got rule tag, athough I expect a metric : " + metricUpdate.getId());
        }

        SimpleDateFormat df = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss,SSS");
        HashMap<String, Object> root = new HashMap<String, Object>();

        ClientDataTagValue cdtv = metricUpdate.getLatestUpdate();

        root.put("parentStatus", parent.getLatestStatus());
        root.put("parentTagId", parent.getId());
        root.put("parentName", parent.getLatestUpdate().getName());

        root.put("tagId", metricUpdate.getId());
        root.put("tagValue", cdtv.getValue());
        root.put("tagName", cdtv.getName());
        root.put("time", df.format(cdtv.getServerTimestamp()));
        root.put("tagUnit", cdtv.getUnit());
        root.put("tagValueDescription", cdtv.getValueDescription().length() > 0 ? cdtv.getValueDescription() : "N/A");
        root.put("tagDescription", cdtv.getDescription().length() > 0 ? cdtv.getDescription() : "N/A");

        StringWriter out = new StringWriter();
        try {
            Template temp = c.getTemplate("metricUpdate.html");
            temp.process(root, out);
            out.flush();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return out.toString();
    }

    public static String statusToTransitionText(ClientDataTagValue update) {
        int status = ((Double) update.getValue()).intValue();
        if (status == 1 || status == 2) {
            return "PROBLEM";
        } else if (status == 0) {
            return "RECOVERY";
        } else {
            return "UNKNOWN " + status;
        }
    }

    /**
     * Renders the text which contains information on the rule. There is information no the childs.
     * 
     * @see #getFreeTextMapForChildren(List)
     * @param update the {@link ClientDataTagValue} from which the information should be retrieved.
     * @return a proper string with information on the update
     * @throws IOException
     * @throws TemplateException
     */
    public String getTextForRuleUpdate(Tag update) throws IOException, TemplateException {
        logger.debug("Entering getTextForUpdate()");

        if (update == null) {
            throw new IllegalArgumentException("Passed argument for Tag was null! ");
        }

        ClientDataTagValue cdtv = update.getLatestUpdate();

        // Create the root hash
        HashMap<String, Object> root = new HashMap<String, Object>();

        root.put("notificationType", update.getLatestStatus().toString());

        ArrayList<String> ruleInputTags = new ArrayList<String>();
        for (Long l : cdtv.getRuleExpression().getInputTagIds()) {
            ruleInputTags.add(Long.toString(l));
        }

        root.put("tagId", Long.toString(update.getId()));
        root.put("tagStatus", update.getLatestStatus().toString());
        root.put("tagName", cdtv.getName());
        root.put("tagDescription", cdtv.getDescription());
        root.put("tagRuleExpression", cdtv.getRuleExpression());
        root.put("tagServerTimestamp", cdtv.getServerTimestamp());
        root.put("tagUnit", cdtv.getUnit());
        root.put("tagValueDescription", cdtv.getValueDescription());
        root.put("ruleExpression", cdtv.getRuleExpression());
        root.put("ruleExpressionInput", ruleInputTags);

        StringWriter out = new StringWriter();
        try {
            Template temp = c.getTemplate("simpleUpdate.html");
            temp.process(root, out);
            out.flush();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return out.toString();
    }

    /**
     * @param list a list of {@link Tag} RULE objects.
     * @return a string representation of the problematic metrics.
     * @throws IOException if there is a problem loading the template
     * @throws TemplateException in case there is a problem while setting the variables in the template.
     */
    public String getFreeTextMapForChildren(List<Tag> list) throws IOException, TemplateException {

        logger.trace("Entering getFreeTextMapForChildren()");
        List<HashMap<String, Object>> children = new ArrayList<HashMap<String, Object>>();

        /*
         * Log DEBUG/TRACE
         */
        if (logger.isDebugEnabled()) {
            logger.debug("Rendering text for {} elements.", Integer.valueOf(list.size()));
            if (logger.isTraceEnabled()) {
                StringBuilder b = new StringBuilder();
                for (Tag t : list) {
                    b.append(t.toString()).append("\n");
                }
                logger.trace("Content of passed list :" + b.toString());
            }
        }

        /*
         * put the relevant information into the list.
         */
        for (Tag tag : list) {
            ClientDataTagValue c = tag.getLatestUpdate();
            HashMap<String, Object> child = new HashMap<String, Object>();
            child.put("tagId", Long.toString(c.getId()));
            child.put("tagValue", c.getValue() == null ? "UNKNOWN" : c.getValue());
            child.put("tagName", c.getName() == null ? "UNKNOWN" : c.getName());
            child.put("tagDescription", c.getDescription() == null ? "UNKNOWN" : c.getDescription());
            child.put("tagServerTimestamp", c.getServerTimestamp() == null ? "UNKNOWN" : c.getServerTimestamp());
            child.put("tagUnit", c.getUnit() == null ? "UNKNOWN" : c.getUnit());
            child.put("tagValueDescription", c.getValueDescription() == null ? "UNKNOWN" : c.getValueDescription());
            child.put("tagQuality", c.getDataTagQuality().getDescription() == null ? null : c.getDataTagQuality()
                    .getDescription());
            children.add(child);
        }

        /*
         * and render it here :
         */
        HashMap<String, Object> root = new HashMap<String, Object>();
        root.put("children", children);

        Template temp = c.getTemplate("ruleChildren.html");
        StringWriter out = new StringWriter();
        temp.process(root, out);
        out.flush();

        logger.trace("Leaving getFreeTextMapForChildren()");

        return out.toString();
    }

    /**
     * Generates the notification text for source down message.
     * @param update the {@link Tag} for which the source is marked as down
     * @return The text to send.
     * @throws IOException
     * @throws TemplateException
     */
    public String getTextForSourceDown(Tag update) throws IOException, TemplateException {
        logger.debug("Entering getTextForSourceDown()");
        HashMap<String, Object> root = new HashMap<String, Object>();

        DataTagQuality quality = update.getLatestUpdate().getDataTagQuality();
        ClientDataTagValue cdtv = update.getLatestUpdate();
        if (cdtv != null) {
            root.put("notificationType", quality.getDescription());
            root.put("tagId", Long.toString(update.getId()));
            root.put("tagName", cdtv.getName());
            root.put("tagDescription", cdtv.getDescription());
            root.put("tagServerTimestamp", cdtv.getServerTimestamp());
        }

        Template temp = c.getTemplate("sourceDown.html");
        StringWriter out = new StringWriter();
        temp.process(root, out);
        out.flush();

        return out.toString();
    }

    public String getTextForReminder(Tag t) throws IOException, TemplateException {
        StringBuilder result = new StringBuilder();

        // TODO
        return result.toString();
    }

   
}
