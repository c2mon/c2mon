package cern.c2mon.notification.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.TextCreator;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;
import freemarker.template.TemplateException;

/**
 * 
 * @author felixehm 
 */
public class TextCreatorTest {
    
    
    IMocksControl mockControl = EasyMock.createControl();
    
    @BeforeClass
    public static void initLog4J() {
        System.setProperty("log4j.configuration", TextCreatorTest.class.getResource("log4j.properties").toExternalForm());
        System.out.println(System.getProperty("log4j.configuration"));
    }
    
    
    ClientDataTagValue getClientDataTagMock(Long id, Object val) throws RuleFormatException {
        
        ClientDataTagValue result = mockControl.createMock(ClientDataTagValue.class);
        
        RuleExpression rule = RuleExpression.createExpression("#2>50 [1],#2>100 [2], [0]");
        
        EasyMock.expect(result.getId()).andReturn(id).anyTimes();
        EasyMock.expect(result.getName()).andReturn("DataTag #" + id).anyTimes();
        EasyMock.expect(result.getDescription()).andReturn("test-description").anyTimes();
        EasyMock.expect(result.getValue()).andReturn(val).anyTimes();
        EasyMock.expect(result.getUnit()).andReturn("integer").anyTimes();
        EasyMock.expect(result.getRuleExpression()).andReturn(rule).anyTimes();
        EasyMock.expect(result.getServerTimestamp()).andReturn(new Timestamp(System.currentTimeMillis())).anyTimes();
        EasyMock.expect(Boolean.valueOf(result.isRuleResult())).andReturn(Boolean.FALSE).anyTimes();
        
        
        EasyMock.expect(result.getValueDescription()).andReturn("datatag value description").anyTimes();
        
        return result;
    }
    
    ClientDataTagValue getRuleTagMock(Long id, int status, String ruleValueDescription, long [] ruleInputTags) throws RuleFormatException {
     
        ClientDataTagValue result = mockControl.createMock(ClientDataTagValue.class);
        EasyMock.expect(result.getId()).andReturn(id).anyTimes();
        EasyMock.expect(result.getName()).andReturn("RuleTag #" + id).anyTimes();
        EasyMock.expect(result.getDescription()).andReturn("a rule with single datatag").anyTimes();
        EasyMock.expect(result.getValue()).andReturn(new Integer(status)).anyTimes();
        EasyMock.expect(result.getUnit()).andReturn("integer").anyTimes();
        EasyMock.expect(Boolean.valueOf(result.isRuleResult())).andReturn(Boolean.TRUE).anyTimes();
        EasyMock.expect(result.getServerTimestamp()).andReturn(new Timestamp(System.currentTimeMillis())).anyTimes();
        
        
        DataTagQuality dtq3 = mockControl.createMock(DataTagQuality.class);
        EasyMock.expect(dtq3.isExistingTag()).andReturn(true).anyTimes();
        EasyMock.expect(dtq3.isValid()).andReturn(true).anyTimes();
        EasyMock.expect(dtq3.isAccessible()).andReturn(true).anyTimes();
        EasyMock.expect(dtq3.getDescription()).andReturn("Cool").anyTimes();
        EasyMock.expect(dtq3.getInvalidQualityStates()).andReturn(new HashMap<TagQualityStatus,String>()).anyTimes();
        EasyMock.expect(result.getDataTagQuality()).andReturn(dtq3).anyTimes();
        
        String ruleTxt = ""; 
        for (long l : ruleInputTags) {
            ruleTxt += "#" + l + ">50,";
        }
        ruleTxt = ruleTxt.substring(0,  ruleTxt.length() - 1);
        
        ruleTxt += " [1], [0]";
        //"#2>50 [1],#2>100 [2], [0]"
        RuleExpression rule = RuleExpression.createExpression(ruleTxt);
        EasyMock.expect(result.getRuleExpression()).andReturn(rule).anyTimes();
        EasyMock.expect(result.getValueDescription()).andReturn(ruleValueDescription).anyTimes();
        
        
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public Collection<ClientDataTagValue> getClientDataTagChildren() {
        Collection<ClientDataTagValue> children = new ArrayList<ClientDataTagValue>();
        
        for (int i = 0; i < 5; i++) {
            ClientDataTagValue value = mockControl.createMock(ClientDataTagValue.class);
            EasyMock.expect(value.getId()).andReturn(new Long(i)).atLeastOnce();
            EasyMock.expect(value.getName()).andReturn("error-child-tag-" + i).atLeastOnce();
            EasyMock.expect(value.getValue()).andReturn(1).atLeastOnce();
            EasyMock.expect(value.getType()).andReturn((Class) Integer.class).atLeastOnce();
            EasyMock.expect(value.isRuleResult()).andReturn(false).atLeastOnce();
            EasyMock.replay();
            children.add(value);
        }
        return children;
    }
    
    
    
    
    @Test
    public void testSimpleTagUpdate() throws IOException, TemplateException, RuleFormatException {
        TextCreator creator = new TextCreator();
        
        
        ClientDataTagValue rule = getRuleTagMock(new Long(1), 2, "Additional info for failing rule", new long [] {2});

        ClientDataTagValue dataTag = getClientDataTagMock(new Long(2), new Long(4));
        mockControl.replay();
        
        Tag t = new Tag (rule.getId(), true);
        Tag child = new Tag(dataTag.getId(), false);
        t.update(rule);
        child.update(dataTag);
        t.addChildTag(child);

        MyCache c = new MyCache();
        c.put(t);
        c.put(child);
        
        String text = creator.getTextForRuleUpdate(t, c);
        
        System.out.println(text);
        
    }
    
   
    
    
    
    
    @Test
    public void testMetricChangeUpdate() {
//        Tag t1 = new Tag(1L, true);
//        ClientDataTagValue value = mockControl.createMock(ClientDataTagValue.class);
//        EasyMock.expect(value.getName()).andReturn("TheParent").once();
//        EasyMock.expect(value.getValue()).andReturn(new Double(1L)).once();
//        EasyMock.expect(value.getDataTagQuality()).andReturn(new DataTagQualityImpl()).once();
//        EasyMock.expect(value.isRuleResult()).andReturn(true).once();
//        
//        t1.update(value);
//        
//        Tag t2 = new Tag(2L, false);
//        t2.update(getClientDataTagMock());
//        
//        TextCreator creator = new TextCreator();
//        System.out.println(creator.getTextForMetricUpdate(t2, t1));
        
    }
    
    @Test
    public void testBooleanDatatag() throws Exception {
        ClientDataTagValue rootRule = getRuleTagMock(new Long(1), 2, "root rule", new long [] {2});
        ClientDataTagValue childRule = getRuleTagMock(new Long(2), 2, "child rule", new long [] {3});
        ClientDataTagValue childRuleDataTag = getClientDataTagMock(new Long(3), Boolean.TRUE);
        mockControl.replay();
        
        Tag root = new Tag (rootRule.getId(), true);
        Tag child = new Tag(childRule.getId(), true);
        Tag dataTag = new Tag(childRuleDataTag.getId(), false);
        
        root.addChildTag(child);
        child.addChildTag(dataTag);
        
        
        root.update(rootRule);
        child.update(childRule);
        dataTag.update(childRuleDataTag);
        
        MyCache c = new MyCache();
        c.put(root);
        c.put(child);
        c.put(dataTag);
        
        
        TextCreator creator = new TextCreator();
        
        Set<Tag> interestingChildren = new HashSet<Tag>();
        interestingChildren.add(child);
        String text = creator.getReportForTag(root, interestingChildren, c);
        
     
        System.out.println(text);

    }
    
    
    private class MyCache extends TagCache {
        public void put(Tag tag) {
            super.cache.put(tag.getId(), tag);
        }
    }
    
}

