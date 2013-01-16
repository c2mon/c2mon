package cern.c2mon.notification;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.TextCreator;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.rule.RuleExpression;
import cern.tim.shared.rule.RuleFormatException;
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
    
    
    public ClientDataTagValue getClientDataTagMock() throws RuleFormatException {
        
        ClientDataTagValue value = mockControl.createMock(ClientDataTagValue.class);
        
        RuleExpression rule = RuleExpression.createExpression("#1>50 [1],#1>100 [2], [0]");
        
        EasyMock.expect(value.getId()).andReturn(1L).atLeastOnce();
        EasyMock.expect(value.getName()).andReturn("test-tag").atLeastOnce();
        EasyMock.expect(value.getDescription()).andReturn("test-description").atLeastOnce();
        EasyMock.expect(value.getValue()).andReturn(0).atLeastOnce();
        EasyMock.expect(value.getUnit()).andReturn("integer").atLeastOnce();
        EasyMock.expect(value.getRuleExpression()).andReturn(rule).anyTimes();
        EasyMock.expect(value.getServerTimestamp()).andReturn(new Timestamp(0)).atLeastOnce();
        EasyMock.expect(value.isRuleResult()).andReturn(false).atLeastOnce();
        EasyMock.expect(value.getDataTagQuality()).andReturn(new DataTagQualityImpl()).atLeastOnce();
        
        EasyMock.expect(value.getType()).andReturn((Class) Integer.class).atLeastOnce();
        EasyMock.expect(value.getValueDescription()).andReturn("test-value-description").atLeastOnce();
        EasyMock.replay(value);
        return value;
    }
    
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
        ClientDataTagValue cdtv = getClientDataTagMock();
        
        Tag t = new Tag (cdtv.getId(), true);
        t.update(cdtv);
        
        String text = creator.getTextForRuleUpdate(t);
        System.out.println(text);
        
    }
    
    
    
    
    
    
    @Test
    public void testMetricChangeUpdate() throws RuleFormatException, IOException {
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
    public void testCreateChildrenList() throws IOException, TemplateException {
        TextCreator creator = new TextCreator();
        Collection<ClientDataTagValue> children = new ArrayList<ClientDataTagValue>();
        
//        for (int i = 0; i < 5; i++) {
//            ClientDataTagValue value = mockControl.createMock(ClientDataTagValue.class);
//            EasyMock.expect(value.getId()).andReturn(new Long(i)).atLeastOnce();
//            EasyMock.expect(value.getName()).andReturn("error-child-tag-" + i).atLeastOnce();
//            EasyMock.expect(value.getValue()).andReturn(1).atLeastOnce();
//            EasyMock.expect(value.getType()).andReturn((Class) Integer.class).atLeastOnce();
//            EasyMock.expect(value.isRuleResult()).andReturn(false).atLeastOnce();
//            children.add(value);
//        }
//        String text = creator.getFreeTextMapForChildren(children);
//        
//        System.out.println(text);
        
    }
}
