/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.notification.shared.Status;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/** A class which generates the text for a mail notification
 * 
 * @author felixehm
 */
@ManagedResource(objectName = "cern.dmn2:type=Notifier,name=TemplateLoader")
public class TextCreator {
    /**
     * our Logger.
     */
    private Logger logger = Logger.getLogger(TextCreator.class);
    
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
    public TextCreator() throws IOException {
        reLoadTemplates();
        
        logger.info("TextCreator created.");
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    
    /**
     * 
     * @param directory the name of the directory containing the FreeMarker templates
     * @throws IOException in case the directory cannot be accessed. 
     */
    @ManagedAttribute
    public void setTemplateDir(String directory) throws IOException {
        if (logger.isDebugEnabled()) { 
                logger.debug("Setting directory for templates to " + directory);
        }
        this.directory = directory;
        reLoadTemplates();
    }
    
    /**
     * 
     * @return the local path of the directory where to read the templates.
     */
    @ManagedAttribute
    public String getTemplateDir() {
        return directory;
    }
    
    /** Reloads the FreeMarker templates.

     * @see TextCreator#setTemplateDir(String)
     * @see TextCreator#getTemplateDir()
     * @throws IOException in case the {@link #directory} cannot be accessed.
     */
    @ManagedOperation
    public void reLoadTemplates() throws IOException {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Entering reLoadTemplates().");
            logger.debug("Using " + getTemplateDir() + " for reading the templates");
        }
        
        c = new Configuration();
        c.setDirectoryForTemplateLoading(new File(directory));
        c.setObjectWrapper(new DefaultObjectWrapper());
    }
    
    /**
     * 
     * @param update the {@link ClientDataTagValue} from which the information should be retrieved.
     * @return a proper string with information on the update  
     * @throws IOException 
     * @throws TemplateException 
     */
    public String getTextForUpdate(ClientDataTagValue update) throws IOException, TemplateException {
        logger.debug("Entering getTextForUpdate()");
        
        if (update == null) {
            throw new IllegalArgumentException("Pased argument for ClientDataTagValue update was null! ");
        }
        
        // Create the root hash
        HashMap<String, Object> root = new HashMap<String, Object>();
        
        
        int status = ((Integer) update.getValue()).intValue();
        if (status == 1 || status == 2) {
            root.put("notificationType", "PROBLEM");
        } else if (status == 0) {
            root.put("notificationType", "RECOVERY");
        } else {
            root.put("notificationType", "UNKNOWN");
        }
        
        ArrayList<String> ruleInputTags = new ArrayList<String>();
        for (Long l : update.getRuleExpression().getInputTagIds()) {
            ruleInputTags.add(Long.toString(l));
        }
        
        root.put("tagId", Long.toString(update.getId()));
        root.put("tagValue", Status.fromInt(status).toString());
        root.put("tagName", update.getName());
        root.put("tagDescription", update.getDescription());
        root.put("tagRuleExpression", update.getRuleExpression());
        root.put("tagServerTimestamp", update.getServerTimestamp());
        root.put("tagUnit", update.getUnit());
        root.put("tagValueDescription", update.getValueDescription());
        root.put("ruleExpression", update.getRuleExpression().getExpression());
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
     * 
     * @param list a list of {@link ClientDataTagValue} RULE objects.  
     * @return a string representation of the problematic metrics.
     * @throws IOException if there is a problem loading the template
     * @throws TemplateException in case there is a problem while setting the variables in the template.
     */
    public String getFreeTextMapForChildren(Collection<ClientDataTagValue> list) throws IOException, TemplateException {
        
//        HashSet<Long> tagIds = new HashSet<Long>();
//        for (ClientDataTagValue c : list) {
//            tagIds.addAll(c.getRuleExpression().getInputTagIds());
//        }
//        Collection<ClientDataTagValue> metrics = C2monServiceGateway.getTagManager().getDataTags(tagIds);
        
        
        logger.debug("Entering getFreeTextMapForChildren()");
        List<HashMap<String, Object>> children = new ArrayList<HashMap<String, Object>>();
    
        for (ClientDataTagValue c : list) {
            HashMap<String, Object> child = new HashMap<String, Object>();
            child.put("tagId", Long.toString(c.getId()));
            child.put("tagValue", c.getValue() == null ? "UNKNOWN" : c.getValue());
            child.put("tagName", c.getName() == null ? "UNKNOWN" : c.getName());
            child.put("tagDescription", c.getDescription() == null ? "UNKNOWN" : c.getDescription());
            child.put("tagServerTimestamp", c.getServerTimestamp() == null ? "UNKNOWN" : c.getServerTimestamp());
            child.put("tagUnit", c.getUnit() == null ? "UNKNOWN" : c.getUnit());
            child.put("tagValueDescription", c.getValueDescription() == null ? "UNKNOWN" : c.getValueDescription());
            children.add(child);
        }
        
        HashMap<String, Object> root = new HashMap<String, Object>();
        root.put("children", children);
        
        Template temp = c.getTemplate("ruleChildren.html"); 
        StringWriter out = new StringWriter();
        temp.process(root, out);
        out.flush();
        return out.toString();
    }
   

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    // 
    // -- INNER CLASSES -----------------------------------------------
    //
}


