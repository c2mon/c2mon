package cern.c2mon.server.rule;

import static org.junit.Assert.*;

import org.junit.Test;

public class RuleTagServiceTest {

	@Test
	public void testSimpleSetParentSupervisionIds() {
		// Create two mock Data Tags and put them in the cache
		// Create a Rule Tag that depends on those (-> ruleTag.setRuleInputTagIds)  and put them in the cache
		
		// Invoke ruleTagService.setParentSupervisionIds(ruleTag.id)
		fail("Not yet implemented");
		
		// Assert that ruleTag.processIds is a non empty collection, that contains the expected process IDs
	}

	@Test
	public void testRecursiveSetParentSupervisionIds() {
		// Create two mock Data Tags and put them in the cache
		// Create a RuleTag that depends on those (-> ruleTag.setRuleInputTagIds)  and put them in the cache
		// Create a RuleTag2 that depends on the above and put it in the cache
		
		// Invoke ruleTagService.setParentSupervisionIds(ruleTag.id)
		fail("Not yet implemented");
		
		// Assert that RuleTag2.processIds is a non empty collection, that contains the expected process IDs (from the parent rule, which also is from the data tags)
	}
}
