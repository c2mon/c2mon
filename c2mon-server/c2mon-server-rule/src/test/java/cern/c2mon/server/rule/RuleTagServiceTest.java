package cern.c2mon.server.rule;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.CacheListenerManagerImpl;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.IgniteModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.rule.config.RuleModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static cern.c2mon.server.cache.test.CacheObjectCreation.*;
import static cern.c2mon.server.common.util.Java9Collections.setOf;
import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
	CommonModule.class,
	CacheActionsModuleRef.class,
	CacheConfigModuleRef.class,
	CacheDbAccessModule.class,
	IgniteModule.class,
	CacheLoadingModuleRef.class,
	RuleModule.class
})
public class RuleTagServiceTest {

	@Autowired
	private RuleTagService ruleTagService;

	@Autowired
	private C2monCache<DataTag> dataTagCache;

	@Autowired
	private C2monCache<RuleTag> ruleTagCache;

	@Before
	public void setUp() {
		dataTagCache.setCacheListenerManager(new CacheListenerManagerImpl<>());
		ruleTagCache.setCacheListenerManager(new CacheListenerManagerImpl<>());
	}

	@Test
	public void testSimpleSetParentSupervisionIds() {
		DataTag dataTag1 = apply(createTestDataTag(), t -> t.setProcessId(90470283L));
		DataTag dataTag2 = apply(createTestDataTag2(), t -> t.setProcessId(49998682L));
		dataTagCache.put(dataTag1.getId(), dataTag1);
		dataTagCache.put(dataTag2.getId(), dataTag2);
		assertEquals(2, dataTagCache.getKeys().size());

		RuleTag ruleTag1 = spy(createTestRuleTag());
		when(ruleTag1.getRuleInputTagIds()).thenReturn(setOf(dataTag1.getId(), dataTag2.getId()));
		ruleTagCache.put(ruleTag1.getId(), ruleTag1);

		ruleTagService.setParentSupervisionIds(ruleTag1.getId());

		ruleTag1 = ruleTagCache.get(ruleTag1.getId());
		assertEquals(2, ruleTag1.getProcessIds().size());
		assertTrue(ruleTag1.getProcessIds().containsAll(setOf(dataTag1.getProcessId(), dataTag2.getProcessId())));
	}

	@Test
	public void testRecursiveSetParentSupervisionIds() {
		DataTag dataTag1 = apply(createTestDataTag(), t -> t.setProcessId(16647539L));
		DataTag dataTag2 = apply(createTestDataTag2(), t -> t.setProcessId(27216180L));
		dataTagCache.put(dataTag1.getId(), dataTag1);
		dataTagCache.put(dataTag2.getId(), dataTag2);
		assertEquals(2, dataTagCache.getKeys().size());

		RuleTag realRuleTag1 = createTestRuleTag();
		RuleTag ruleTag1 = spy(realRuleTag1);
		when(ruleTag1.getRuleInputTagIds()).thenReturn(setOf(dataTag1.getId(), dataTag2.getId()));
		ruleTagCache.put(ruleTag1.getId(), ruleTag1);

		RuleTag ruleTag2 = spy(createTestRuleTag2());
		when(ruleTag2.getRuleInputTagIds()).thenReturn(setOf(realRuleTag1.getId()));
		ruleTagCache.put(ruleTag2.getId(), ruleTag2);

		ruleTagService.setParentSupervisionIds(ruleTag2.getId());

		ruleTag1 = ruleTagCache.get(ruleTag1.getId());
		assertEquals(2, ruleTag1.getProcessIds().size());
		assertTrue(ruleTag1.getProcessIds().containsAll(setOf(dataTag1.getProcessId(), dataTag2.getProcessId())));

		ruleTag2 = ruleTagCache.get(ruleTag2.getId());
		assertEquals(2, ruleTag2.getProcessIds().size());
		assertTrue(ruleTag2.getProcessIds().containsAll(setOf(dataTag1.getProcessId(), dataTag2.getProcessId())));
	}
}
