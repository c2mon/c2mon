/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.supervision.impl;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.SupervisionAppender;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static cern.c2mon.server.common.util.Java9Collections.listOf;
import static cern.c2mon.server.common.util.Java9Collections.setOf;
import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test of SupervisionTagNotifier class.
 *
 * @author Mark Brightwell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  SupervisionModule.class,
  CacheActionsModuleRef.class,
  CacheLoadingModuleRef.class,
  C2monIgniteConfiguration.class
})
public class SupervisionTagNotifierTest {

  @Inject private SupervisionTagNotifier supervisionTagNotifier;

  @Inject private SupervisionAppender supervisionAppender;
  @Inject private TagCacheCollection tagLocationService;

  @Inject private C2monCache<Process> processCache;
  @Inject private C2monCache<Equipment> equipmentCache;
  @Inject private C2monCache<SubEquipment> subEquipmentCache;
  @Inject private C2monCache<DataTag> dataTagCache;
  @Inject private C2monCache<RuleTag> ruleTagCache;
  @Inject private C2monCache<SupervisionStateTag> stateTagCache;

  private ProcessCacheObject process;
  private EquipmentCacheObject equipment;
  private SubEquipmentCacheObject subEquipment;

  private DataTagCacheObject dataTag1;
  private DataTagCacheObject dataTag2;
  private DataTagCacheObject dataTag3;
  private DataTagCacheObject dataTag4;

  private RuleTagCacheObject ruleTag1;
  private RuleTagCacheObject ruleTag2;
  private RuleTagCacheObject ruleTag3;
  private RuleTagCacheObject ruleTag4;
  private RuleTagCacheObject ruleTag5;

  @Before
  public void setUp() {
    equipment = (EquipmentCacheObject) putAndGet(
      equipmentCache,
      apply(new EquipmentCacheObject(30L), e -> {
        SupervisionStateTag stateTag = createStateTag(30_000L, SupervisionEntity.EQUIPMENT, e);
        e.setStateTagId(stateTag.getId());
      })
    );

    subEquipment = (SubEquipmentCacheObject) putAndGet(
      subEquipmentCache,
      apply(new SubEquipmentCacheObject(50L), se -> {
        se.setParentId(equipment.getId());

        SupervisionStateTag stateTag = createStateTag(50_000L, SupervisionEntity.SUBEQUIPMENT, se);
        se.setStateTagId(stateTag.getId());
      })
    );

    process = (ProcessCacheObject) putAndGet(
      processCache,
      apply(new ProcessCacheObject(10L), p -> {
        p.setEquipmentIds(new ArrayList<>(listOf(equipment.getId())));

        SupervisionStateTag stateTag = createStateTag(10_000L, SupervisionEntity.SUBEQUIPMENT, p);
        p.setStateTagId(stateTag.getId());
      })
    );

    ruleTag1 = (RuleTagCacheObject) putAndGet(
      ruleTagCache,
      apply(new RuleTagCacheObject(200L), t -> {
        t.setEquipmentIds(setOf(equipment.getId()));
        t.setProcessIds(setOf(process.getId()));
        t.setDataTagQuality(createValidQuality());
      })
    );

    ruleTag2 = (RuleTagCacheObject) putAndGet(
      ruleTagCache,
      apply(new RuleTagCacheObject(201L), t -> {
        t.setEquipmentIds(setOf(equipment.getId()));
        t.setProcessIds(setOf(process.getId()));
        t.setDataTagQuality(createValidQuality());
      })
    );

    ruleTag3 = (RuleTagCacheObject) putAndGet(
      ruleTagCache,
      apply(new RuleTagCacheObject(202L), t -> {
        t.setEquipmentIds(setOf(equipment.getId()));
        t.setProcessIds(setOf(process.getId()));
        t.setDataTagQuality(createValidQuality());
      })
    );

    ruleTag4 = (RuleTagCacheObject) putAndGet(
      ruleTagCache,
      apply(new RuleTagCacheObject(203L), t -> {
        t.setSubEquipmentIds(setOf(subEquipment.getId()));
        t.setEquipmentIds(setOf(equipment.getId()));
        t.setProcessIds(setOf(process.getId()));
        t.setDataTagQuality(createValidQuality());
      })
    );

    ruleTag5 = (RuleTagCacheObject) putAndGet(
      ruleTagCache,
      apply(new RuleTagCacheObject(204L), t -> {
        t.setSubEquipmentIds(setOf(subEquipment.getId()));
        t.setEquipmentIds(setOf(equipment.getId()));
        t.setProcessIds(setOf(process.getId()));
        t.setDataTagQuality(createValidQuality());
      })
    );

    dataTag1 = (DataTagCacheObject) putAndGet(
      dataTagCache,
      apply(new DataTagCacheObject(100L), t -> {
        t.setRuleIds(listOf(ruleTag1.getId(), ruleTag2.getId()));
        t.setEquipmentId(equipment.getId());
        t.setProcessId(process.getId());
        t.setDataTagQuality(createValidQuality());
      })
    );

    dataTag2 = (DataTagCacheObject) putAndGet(
      dataTagCache,
      apply(new DataTagCacheObject(101L), t -> {
        t.setRuleIds(asList(ruleTag1.getId(), ruleTag3.getId()));
        t.setEquipmentId(equipment.getId());
        t.setProcessId(process.getId());
        t.setDataTagQuality(createValidQuality());
      })
    );

    dataTag3 = (DataTagCacheObject) putAndGet(
      dataTagCache,
      apply(new DataTagCacheObject(102L), t -> {
        t.setRuleIds(asList(ruleTag4.getId(), ruleTag5.getId()));
        t.setSubEquipmentId(subEquipment.getId());
        t.setEquipmentId(equipment.getId());
        t.setProcessId(process.getId());
        t.setDataTagQuality(createValidQuality());
      })
    );

    dataTag4 = (DataTagCacheObject) putAndGet(
      dataTagCache,
      apply(new DataTagCacheObject(103L), t -> {
        t.setRuleIds(asList(ruleTag4.getId(), ruleTag5.getId()));
        t.setSubEquipmentId(subEquipment.getId());
        t.setEquipmentId(equipment.getId());
        t.setProcessId(process.getId());
        t.setDataTagQuality(createValidQuality());
      })
    );
  }

  @Test
  public void testNotifySupervisionEventForProcessEvent() {
    testEventNotification(
      new SupervisionEventImpl(
        SupervisionEntity.PROCESS,
        process.getId(),
        "P_TEST",
        SupervisionStatus.DOWN,
        new Timestamp(System.currentTimeMillis()),
        "test message"
      ),
      setOf(
        dataTag1, dataTag2, dataTag3, dataTag4,
        ruleTag1, ruleTag2, ruleTag3, ruleTag4, ruleTag5
      )
    );
  }

  @Test
  public void testNotifySupervisionEventForEquipmentEvent() {
    testEventNotification(
      new SupervisionEventImpl(
        SupervisionEntity.EQUIPMENT,
        equipment.getId(),
        "E_TEST",
        SupervisionStatus.RUNNING,
        new Timestamp(System.currentTimeMillis()),
        "test message"
      ),
      setOf(
        dataTag1, dataTag2, dataTag3, dataTag4,
        ruleTag1, ruleTag2, ruleTag3, ruleTag4, ruleTag5
      )
    );
  }

  @Test
  public void testNotifySubEquipmentEvent() {
    testEventNotification(
      new SupervisionEventImpl(
        SupervisionEntity.SUBEQUIPMENT,
        subEquipment.getId(),
        "E_SUBTEST",
        SupervisionStatus.DOWN,
        new Timestamp(System.currentTimeMillis()),
        "test message"
      ),
      setOf(
        dataTag3, dataTag4,
        ruleTag4, ruleTag5
      )
    );
  }

  private void testEventNotification(SupervisionEvent event, Set<Tag> expectedTags) {
    assertTrue(expectedTags.stream().allMatch(Tag::isValid));

    List<Tag> notifiedTags = new ArrayList<>();

    dataTagCache.getCacheListenerManager().registerListener(notifiedTags::add, CacheEvent.SUPERVISION_CHANGE);
    ruleTagCache.getCacheListenerManager().registerListener(notifiedTags::add, CacheEvent.SUPERVISION_CHANGE);

    supervisionTagNotifier.notifySupervisionEvent(event);

    await()
      .atMost(250, TimeUnit.MILLISECONDS)
      .until(() -> notifiedTags.size() == expectedTags.size());

    assertEquals(
      notifiedTags.stream().map(Tag::getId).collect(toSet()),
      expectedTags.stream().map(Tag::getId).collect(toSet())
    );
    assertTrue(notifiedTags.stream().noneMatch(Tag::isValid));
  }

  private <S extends Cacheable> SupervisionStateTag createStateTag(Long tagId, SupervisionEntity supervisedEntity, S supervisedObject) {
    return putAndGet(
      stateTagCache,
      apply(new SupervisionStateTag(tagId), t -> {
        t.setSupervisedEntity(supervisedEntity);
        t.setSupervisedId(supervisedObject.getId());
      })
    );
  }

  private static <C extends C2monCache<T>, T extends Cacheable> T putAndGet(C cache, T thing) {
    cache.put(thing.getId(), thing);
    return cache.get(thing.getId());
  }

  private static DataTagQuality createValidQuality() {
    DataTagQuality dataTagQuality = new DataTagQualityImpl();
    dataTagQuality.validate();
    return dataTagQuality;
  }
}
