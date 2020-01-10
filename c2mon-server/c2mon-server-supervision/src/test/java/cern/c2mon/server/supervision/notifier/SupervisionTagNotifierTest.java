///******************************************************************************
// * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
// *
// * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
// * C2MON is free software: you can redistribute it and/or modify it under the
// * terms of the GNU Lesser General Public License as published by the Free
// * Software Foundation, either version 3 of the license.
// *
// * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// * more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
// *****************************************************************************/
//package cern.c2mon.server.supervision.impl;
//
//import java.sql.Timestamp;
//import java.util.*;
//
//import cern.c2mon.cache.config.CacheConfigModuleRef;
//import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
//import cern.c2mon.server.common.config.CommonModule;
//import cern.c2mon.server.supervision.config.SupervisionModule;
//import org.easymock.EasyMock;
//import org.easymock.IMocksControl;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import cern.c2mon.server.common.datatag.DataTagCacheObject;
//import cern.c2mon.server.common.equipment.EquipmentCacheObject;
//import cern.c2mon.server.common.process.ProcessCacheObject;
//import cern.c2mon.server.common.rule.RuleTagCacheObject;
//import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
//import cern.c2mon.server.supervision.SupervisionNotifier;
//import cern.c2mon.shared.client.supervision.SupervisionEvent;
//import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
//import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
//import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
//
///**
// * Unit test of SupervisionTagNotifier class.
// *
// * @author Mark Brightwell
// *
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {
//    CommonModule.class,
//    CacheConfigModuleRef.class,
//    CacheDbAccessModule.class,
//    SupervisionModule.class
//})
//public class SupervisionTagNotifierTest {
//
//  /**
//   * Class to test.
//   */
//  private SupervisionTagNotifier supervisionTagNotifier;
//
//  /**
//   * Mocks
//   */
//  private IMocksControl mockControl = EasyMock.createControl();
//  private SupervisionNotifier supervisionNotifier;
//  private CacheProvider cacheProvider;
//  private ProcessCache processCache;
//  private EquipmentCache equipmentCache;
//  private SubEquipmentCache subEquipmentCache;
//  private TagLocationService tagLocationService;
//  private DataTagCache dataTagCache;
//  private RuleTagCache ruleTagCache;
//  private EquipmentFacade equipmentFacade;
//  private SubEquipmentFacade subEquipmentFacade;
//  private SupervisionAppender supervisionAppender;
//  private ProcessFacade processFacade;
//
//  /**
//   * Data objects.
//   */
//  private ProcessCacheObject process;
//  private EquipmentCacheObject equipment;
//  private SubEquipmentCacheObject subEquipment;
//  private DataTagCacheObject dataTag;
//  private DataTagCacheObject dataTag2;
//  private DataTagCacheObject dataTag3;
//  private DataTagCacheObject dataTag4;
//  private RuleTagCacheObject ruleTag;
//  private RuleTagCacheObject ruleTag2;
//  private RuleTagCacheObject ruleTag3;
//  private RuleTagCacheObject ruleTag4;
//  private RuleTagCacheObject ruleTag5;
//
//  @Before
//  public void setUp() {
//    supervisionNotifier = mockControl.createMock(SupervisionNotifier.class);
//    cacheProvider = mockControl.createMock(CacheProvider.class);
//    processCache = mockControl.createMock(ProcessCache.class);
//    equipmentCache = mockControl.createMock(EquipmentCache.class);
//    subEquipmentCache = mockControl.createMock(SubEquipmentCache.class);
//    tagLocationService = mockControl.createMock(TagLocationService.class);
//    dataTagCache = mockControl.createMock(DataTagCache.class);
//    ruleTagCache = mockControl.createMock(RuleTagCache.class);
//    equipmentFacade = mockControl.createMock(EquipmentFacade.class);
//    subEquipmentFacade = mockControl.createMock(SubEquipmentFacade.class);
//    supervisionAppender = mockControl.createMock(SupervisionAppender.class);
//    processFacade = mockControl.createMock(ProcessFacade.class);
//
//    EasyMock.expect(cacheProvider.getProcessCache()).andReturn(processCache);
//    EasyMock.expect(cacheProvider.getEquipmentCache()).andReturn(equipmentCache);
//    EasyMock.expect(cacheProvider.getSubEquipmentCache()).andReturn(subEquipmentCache);
//    EasyMock.expect(cacheProvider.getDataTagCache()).andReturn(dataTagCache);
//    EasyMock.expect(cacheProvider.getRuleTagCache()).andReturn(ruleTagCache);
//
//    supervisionTagNotifier = new SupervisionTagNotifier(supervisionNotifier, cacheProvider,
//                                                   tagLocationService, supervisionAppender, dataTagService, processFacade,
//                                                   equipmentFacade, subEquipmentFacade, dataTagCache, ruleTagCache);
//
//    process = new ProcessCacheObject(10L);
//    process.setEquipmentIds(new ArrayList<Long>(Arrays.asList(30L)));
//    equipment = new EquipmentCacheObject(30L);
//    equipment.setSubEquipmentIds(new LinkedList<>(Arrays.asList(50L)));
//    subEquipment = new SubEquipmentCacheObject(50L);
//    dataTag = new DataTagCacheObject(100L);
//    dataTag.setRuleIds(new ArrayList<Long>(Arrays.asList(200L, 201L)));
//    dataTag.setEquipmentId(30L);
//    dataTag.setProcessId(10L);
//    dataTag2 = new DataTagCacheObject(101L);
//    dataTag2.setRuleIds(new ArrayList<Long>(Arrays.asList(200L, 202L)));
//    dataTag2.setEquipmentId(30L);
//    dataTag2.setProcessId(10L);
//
//    dataTag3 = new DataTagCacheObject(102L);
//    dataTag3.setRuleIds(new ArrayList<Long>(Arrays.asList(203L, 204L)));
//    dataTag3.setSubEquipmentId(50L);
//    dataTag3.setEquipmentId(30L);
//    dataTag3.setProcessId(10L);
//
//    dataTag4 = new DataTagCacheObject(103L);
//    dataTag4.setRuleIds(new ArrayList<Long>(Arrays.asList(203L, 204L)));
//    dataTag4.setSubEquipmentId(50L);
//    dataTag4.setEquipmentId(30L);
//    dataTag4.setProcessId(10L);
//
//    ruleTag = new RuleTagCacheObject(200L);
//    Set<Long> eqIds = new HashSet<Long>();
//    eqIds.add(30L);
//    Set<Long> procIds = new HashSet<Long>();
//    procIds.add(10L);
//    ruleTag.setEquipmentIds(eqIds);
//    ruleTag.setProcessIds(procIds);
//    ruleTag2 = new RuleTagCacheObject(201L);
//    ruleTag2.setEquipmentIds(eqIds);
//    ruleTag2.setProcessIds(procIds);
//    ruleTag3 = new RuleTagCacheObject(202L);
//    ruleTag3.setEquipmentIds(eqIds);
//    ruleTag3.setProcessIds(procIds);
//
//    ruleTag4 = new RuleTagCacheObject(203L);
//    ruleTag4.setSubEquipmentIds(new HashSet<>(Arrays.asList(50L)));
//    ruleTag4.setEquipmentIds(eqIds);
//    ruleTag4.setProcessIds(procIds);
//
//    ruleTag5 = new RuleTagCacheObject(204L);
//    ruleTag5.setSubEquipmentIds(new HashSet<>(Arrays.asList(50L)));
//    ruleTag5.setEquipmentIds(eqIds);
//    ruleTag5.setProcessIds(procIds);
//  }
//
//  /**
//   * Test init call.
//   */
//  public void testRegistration() {
//    supervisionNotifier.registerAsListener(supervisionTagNotifier);
//
//    mockControl.replay();
//
//    supervisionTagNotifier.init();
//
//    mockControl.verify();
//  }
//
//  /**
//   * Tests notifySupervisionEvent for a process event.
//   */
//  @Test
//  @DirtiesContext
//  public void testNotifyProcessEvent() {
//    SupervisionEvent event = new SupervisionEventImpl(SupervisionEntity.PROCESS, 10L, "P_TEST", SupervisionStatus.DOWN, new Timestamp(System.currentTimeMillis()), "test message");
//
//    EasyMock.expect(processCache.get(10L)).andReturn(process);
//    //EasyMock.expect(equipmentFacade.getProcessForAbstractEquipment(30L)).andReturn(process);
//    EasyMock.expect(equipmentFacade.getDataTagIds(30L)).andReturn(Arrays.asList(100L, 101L));
//    EasyMock.expect(tagLocationService.get(100L)).andReturn(dataTag);
//    EasyMock.expect(tagLocationService.get(101L)).andReturn(dataTag2);
//    EasyMock.expect(tagLocationService.get(200L)).andReturn(ruleTag).times(2);
//    EasyMock.expect(tagLocationService.get(201L)).andReturn(ruleTag2);
//    EasyMock.expect(tagLocationService.get(202L)).andReturn(ruleTag3);
//    supervisionAppender.addSupervisionQuality(dataTag, event);
//    dataTagCache.notifyListenersOfSupervisionChange(dataTag);
//    supervisionAppender.addSupervisionQuality(dataTag2, event);
//    dataTagCache.notifyListenersOfSupervisionChange(dataTag2);
//    supervisionAppender.addSupervisionQuality(ruleTag,event);
//    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag); //only once although uses triggered by 2 different tags
//    supervisionAppender.addSupervisionQuality(ruleTag2,event);
//    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag2);
//    supervisionAppender.addSupervisionQuality(ruleTag3,event);
//    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag3);
//
//    mockControl.replay();
//
//    supervisionTagNotifier.notifySupervisionEvent(event);
//
//    mockControl.verify();
//  }
//
//  /**
//   * Tests notifySupervisionEvent for and equipment event.
//   */
//  @Test
//  @DirtiesContext
//  public void testNotifyEquipmentEvent() {
//    SupervisionEvent event = new SupervisionEventImpl(SupervisionEntity.EQUIPMENT, 30L, "E_TEST", SupervisionStatus.RUNNING, new Timestamp(System.currentTimeMillis()), "test message");
//    mockControl.reset();
//    //EasyMock.expect(equipmentFacade.getProcessForAbstractEquipment(30L)).andReturn(process);
//    EasyMock.expect(equipmentFacade.getDataTagIds(30L)).andReturn(Arrays.asList(100L, 101L));
//    EasyMock.expect(tagLocationService.get(100L)).andReturn(dataTag);
//    EasyMock.expect(tagLocationService.get(101L)).andReturn(dataTag2);
//    EasyMock.expect(tagLocationService.get(200L)).andReturn(ruleTag).times(2);
//    EasyMock.expect(tagLocationService.get(201L)).andReturn(ruleTag2);
//    EasyMock.expect(tagLocationService.get(202L)).andReturn(ruleTag3);
//    supervisionAppender.addSupervisionQuality(dataTag,event);
//    dataTagCache.notifyListenersOfSupervisionChange(dataTag);
//    supervisionAppender.addSupervisionQuality(dataTag2,event);
//    dataTagCache.notifyListenersOfSupervisionChange(dataTag2);
//    supervisionAppender.addSupervisionQuality(ruleTag,event);
//    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag); //only once although uses triggered by 2 different tags
//    supervisionAppender.addSupervisionQuality(ruleTag2,event);
//    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag2);
//    supervisionAppender.addSupervisionQuality(ruleTag3,event);
//    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag3);
//
//    mockControl.replay();
//
//    supervisionTagNotifier.notifySupervisionEvent(event);
//
//    mockControl.verify();
//  }
//
//  @Test
//  @DirtiesContext
//  public void testNotifySubEquipmentEvent() {
//    SupervisionEvent event = new SupervisionEventImpl(SupervisionEntity.SUBEQUIPMENT, 50L, "E_SUBTEST", SupervisionStatus.DOWN, new Timestamp(System.currentTimeMillis()),
//        "test message");
//    mockControl.reset();
//    EasyMock.expect(subEquipmentFacade.getDataTagIds(50L)).andReturn(Arrays.asList(102L, 103L));
//    EasyMock.expect(tagLocationService.get(102L)).andReturn(dataTag3);
//    EasyMock.expect(tagLocationService.get(103L)).andReturn(dataTag4);
//    EasyMock.expect(tagLocationService.get(203L)).andReturn(ruleTag4).times(2);
//    EasyMock.expect(tagLocationService.get(204L)).andReturn(ruleTag5).times(2);
//    supervisionAppender.addSupervisionQuality(dataTag3, event);
//    dataTagCache.notifyListenersOfSupervisionChange(dataTag3);
//    supervisionAppender.addSupervisionQuality(dataTag4, event);
//    dataTagCache.notifyListenersOfSupervisionChange(dataTag4);
//    supervisionAppender.addSupervisionQuality(ruleTag4, event);
//    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag4);
//    supervisionAppender.addSupervisionQuality(ruleTag5, event);
//    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag5);
//
//    mockControl.replay();
//
//    supervisionTagNotifier.notifySupervisionEvent(event);
//
//    mockControl.verify();
//  }
//}
