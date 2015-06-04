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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.notification.impl.TagCache;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.rule.ConditionedRuleExpression;
import cern.c2mon.shared.rule.IRuleCondition;
import cern.c2mon.shared.rule.SimpleRuleExpression;
import cern.dmn2.core.Status;
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
    private Configuration config = null;
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

        config = new Configuration();
        config.setDirectoryForTemplateLoading(new File(directory));
        config.setObjectWrapper(new DefaultObjectWrapper());
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
    public String getReportForTag(Tag update, Set<Tag> interestingChildren, TagCache cache) throws IOException, TemplateException {
        StringBuilder bodyBuffer = new StringBuilder();
        bodyBuffer.append(getTextForRuleUpdate(update, cache));
        
        if (interestingChildren.size() > 0) {
            bodyBuffer.append(getFreeTextMapForChildren(interestingChildren, cache));
        }
        return bodyBuffer.toString();
    }

    /**
     * @param update the Tag to generate the subject for.
     * @param interestingTags the interesting children rules. Needed as the passed tag is not sufficient to indicate the
     *            real problem.
     * @return the subject for the message
     */
    public String getMailSubjectForStateChange(Tag update, Set<Tag> interestingTags) {
        logger.trace("TagID={} Building Mail subject..", update.getId());

        StringBuilder subject = new StringBuilder();
        subject.append("DMNNTFY [").append(update.getLatestStatus().toString().toUpperCase()).append("]: ");
        
        // the title should be more descriptive if we directly point to the metric name. 
        // For this, there must be only one metric in the interestingTags.
        
        Tag metric = null;
        int metricTags = 0;
        for (Tag t : interestingTags) {
            if (!t.isRule()) {
                metric = t;
                metricTags++;
            }
        }
        
        if (metricTags == 1) {
            subject.append(metric.getLatestUpdate().getName());
        } else {
            subject.append(update.getLatestUpdate().getName());
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
            Template temp = config.getTemplate("metricUpdate.html");
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
     * Renders the text which contains information on the rule. There is no information on the childs (tags and rules).
     * 
     * @see #getFreeTextMapForChildren(List)
     * @param update the {@link ClientDataTagValue} from which the information should be retrieved.
     * @return a proper string with information on the update
     * @throws IOException
     * @throws TemplateException
     */
    public String getTextForRuleUpdate(Tag update, TagCache cache) throws IOException, TemplateException {
        logger.debug("Entering getTextForUpdate()");

        if (update == null) {
            throw new IllegalArgumentException("Passed argument for Tag was null! ");
        }

        ClientDataTagValue cdtv = update.getLatestUpdate();

        // Create the root hash
        HashMap<String, Object> root = new HashMap<>();

        root.put("notificationType", update.getLatestStatus().toString());

        List<HashMap<String, Object>> children = new ArrayList<HashMap<String, Object>>();
        root.put("dataTags", children);
        if (!update.hasChildRules()) {
            for (Tag metric: update.getAllChildMetrics()) {
                HashMap<String, Object> child = new HashMap<>();
                if (metric.getLatestUpdate() != null) {
                    child.put("tagValue", metric.getLatestUpdate().getValue());
                    child.put("tagName", metric.getLatestUpdate().getName());
                    child.put("tagValueDetails", metric.getLatestUpdate().getValueDescription());
                } else {
                    child.put("tagValue", "Not available");
                    child.put("tagName", "Not available");
                }
                children.add(child);
            }
            if (update.getLatestStatus().worserThan(Status.OK)) {
                root.put("ruleProblemDescription", getProblemDescription(update, cache));
            }
        }
        
        root.put("ruleId", update.getId());
        root.put("ruleStatus", update.getLatestStatus().toString());
        root.put("ruleName", cdtv.getName());
        root.put("ruleDescription", cdtv.getDescription());
        root.put("ruleExpression", cdtv.getRuleExpression().getExpression());
        root.put("ruleServerTimestamp", cdtv.getServerTimestamp());
 
        StringWriter out = new StringWriter();
        try {
            Template temp = config.getTemplate("simpleUpdate.html");
            temp.process(root, out);
            out.flush();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return out.toString();
    }
    
    /**
     * @param list a list of {@link Tag} RULE objects which have only metrics are children.
     * @return a string representation of the problematic metrics.
     * @throws IOException if there is a problem loading the template
     * @throws TemplateException in case there is a problem while setting the variables in the template.
     */
    public String getFreeTextMapForChildren(Set<Tag> list, TagCache tagCache) throws IOException, TemplateException {

        logger.debug("Entering getFreeTextMapForChildren()");
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
//        for (Tag tag : list) {
//            ClientDataTagValue c = tag.getLatestUpdate();
//            HashMap<String, Object> child = new HashMap<String, Object>();
//            child.put("tagId", Long.toString(c.getId()));
//            child.put("tagValue", c.getValue() == null ? "UNKNOWN" : c.getValue());
//            child.put("tagName", c.getName() == null ? "UNKNOWN" : c.getName());
//            child.put("tagDescription", c.getDescription() == null ? "UNKNOWN" : c.getDescription());
//            child.put("tagServerTimestamp", c.getServerTimestamp() == null ? "UNKNOWN" : c.getServerTimestamp());
//            child.put("tagUnit", c.getUnit() == null ? "UNKNOWN" : c.getUnit());
//            child.put("tagValueDescription", c.getValueDescription() == null ? "UNKNOWN" : c.getValueDescription());
//            child.put("tagQuality", c.getDataTagQuality().getDescription() == null ? null : c.getDataTagQuality()
//                    .getDescription());
//            children.add(child);
//        }
        
        for (Tag tag : list) {
            ClientDataTagValue c = tag.getLatestUpdate();
            HashMap<String, Object> child = new HashMap<String, Object>();
            child.put("ruleId", Long.toString(c.getId()));
            child.put("ruleStatus", tag.getLatestStatus().toString());
            child.put("ruleName", c.getName() == null ? "UNKNOWN" : c.getName());
            child.put("ruleDescription", c.getDescription() == null ? "UNKNOWN" : c.getDescription());
            child.put("ruleServerTimestamp", c.getServerTimestamp() == null ? "UNKNOWN" : c.getServerTimestamp());
            child.put("ruleValueDescription", c.getValueDescription() == null ? "UNKNOWN" : c.getValueDescription());
            child.put("ruleQuality", c.getDataTagQuality());
            
            child.put("ruleProblemDescription", getProblemDescription(tag, tagCache));
            
            List<HashMap<String, Object>> datatags = new ArrayList<HashMap<String, Object>>();
            if (c.getName().contains("PROC.MISSING")) {
                HashMap<String, Object> cdatatag = new HashMap<String, Object>();
                cdatatag.put("tagValue", tag.getAllChildMetrics().iterator().next().getLatestUpdate().getValueDescription());
                datatags.add(cdatatag);
            } else {
                // datatags expected here:
                for (Long datatagId : c.getRuleExpression().getInputTagIds()) {
                    HashMap<String, Object> cdatatag = new HashMap<String, Object>();
                    Tag datatag = tagCache.get(datatagId);
                    if (datatag.getLatestUpdate().getValue() instanceof Boolean) {
                        Boolean bool = (Boolean) datatag.getLatestUpdate().getValue();
                        cdatatag.put("tagValue", bool.toString());
                    } else {
                        cdatatag.put("tagValue", datatag.getLatestUpdate().getValue());
                    }
                    
                    cdatatag.put("tagDescription", datatag.getLatestUpdate().getDescription());
                    cdatatag.put("tagValueDescription", datatag.getLatestUpdate().getValueDescription());
                    datatags.add(cdatatag);
                }
            }
            child.put("dataTags", datatags);
            children.add(child);
        }

        /*
         * and render it here :
         */
        HashMap<String, Object> root = new HashMap<String, Object>();
        root.put("children", children);

        Template temp = config.getTemplate("ruleChildren.html");
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

        Template temp = config.getTemplate("sourceDown.html");
        StringWriter out = new StringWriter();
        temp.process(root, out);
        out.flush();

        return out.toString();
    }
    
    public String getProblemDescription(Tag ruleTag, TagCache cache) {
        
        ClientDataTagValue cdtv = ruleTag.getLatestUpdate();
        if ( cdtv.getRuleExpression() instanceof SimpleRuleExpression) {
            return "";
        }
        ConditionedRuleExpression ruleExpression = (ConditionedRuleExpression) cdtv
                .getRuleExpression();
        
        String ruleExpressionText = "";
        
        for (IRuleCondition ruleCondition : ruleExpression.getConditions()) {
            // is this the currently applied condition?
            if (String.valueOf(ruleCondition.getResultValue()).equals(String.valueOf(ruleTag.getValue()))) {
                
                ruleExpressionText = ruleCondition.getExpression().replaceAll("[\\(\\) ]", "");
                if (ruleExpressionText.equals("true")) {
                    ruleExpressionText = "default tag rule";
                } else {
                    // name and values of data-tags
                    ruleExpressionText += " but is ";
                    for (Long tagid : ruleCondition.getInputTagIds()) {
                        
                        Tag c2MonMetric = cache.get(tagid);
                        ruleExpressionText = ruleExpressionText.replaceAll(
                                "#" + String.valueOf(tagid), "'"
                                        + c2MonMetric.getLatestUpdate().getName() + "'");
                        ruleExpressionText += c2MonMetric.getValue() + ";";
                        ruleExpressionText = RuleExplainer.replace(ruleExpressionText);
                    }
                    ruleExpressionText += "";
                }
            } else if (ruleTag.getValue() == null && !"true".equals(ruleCondition.getExpression())) {
                // handle rules without valid results (data tags are invalid)
                ruleExpressionText = ruleCondition.getExpression()
                        .replaceAll("[\\(\\)]", "");
                ruleExpressionText += " has quality " + cdtv.getDataTagQuality().toString();
                for (Long tagid : ruleCondition.getInputTagIds()) {
                    Tag c2MonMetric = cache.get(tagid);
                    ruleExpressionText = ruleExpressionText.replaceAll("#" + String.valueOf(tagid),
                            "\"" + c2MonMetric.getName() + "\"");
                }
            } else {
                // NOOP
            }
        }
        return ruleExpressionText;
    }
    
    private static class RuleExplainer {
        static Map<String, String> tr = new HashMap<>();
        static {
            tr.put("<", "should be more or equal than ");
            tr.put(">", "should be less or equal than ");
            tr.put("<=", "should be more than ");
            tr.put(">=", "should be less than ");
            tr.put("!>", "should not be less than ");
            tr.put("!<", "should not be more than ");
            tr.put("!=", "should be ");
            tr.put("&&", "and ");
        }
        static String replace(String toReplace) {
            String result = toReplace;
            for (Entry<String, String> s : tr.entrySet()) {
                result = result.replace(s.getKey(),s.getValue());                
            }
            return result;
        }
        
    }
    
    public String getSmsTextForValueChange(Tag update) {
        return "Value changed for " + update.getLatestUpdate().getName() + "to " + update.getLatestUpdate().getValue();
    }
    
    public String getSmsTextForRuleChange(Tag update, List<Tag> interestingChildren) {
        return null;
    }

    public String getTextForReminder(Tag t) throws IOException, TemplateException {
        StringBuilder result = new StringBuilder();

        // TODO
        return result.toString();
    }

   
}
