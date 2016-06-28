package cern.c2mon.server.cache;///******************************************************************************
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
//package cern.c2mon.server.cache;
//
//import cern.c2mon.server.common.rule.RuleTag;
//import cern.c2mon.server.test.TestDataInserter;
//import org.springframework.beans.factory.annotation.Autowired;
//
///**
// * To be used in a Junit test class, needs persistence TestDataHelper in Spring context.
// * @author mbrightw
// *
// * @deprecated
// */
//public class TestCacheDataHelper {
//
//  @Autowired
//  TestDataInserter testDataInserter;
//
//  @Autowired
//  private DataTagCache dataTagCache;
//
//  @Autowired
//  private ControlTagCache controlTagCache;
//
//  @Autowired
//  private C2monCache<Long, RuleTag> ruleTagCache;
//
//  @Autowired
//  private ProcessCache processCache;
//
//  @Autowired
//  private EquipmentCache equipmentCache;
//
//  @Autowired
//  private SubEquipmentCache subEquipmentCache;
//
//  @Autowired
//  private AliveTimerCache aliveTimerCache;
//  //private CommFaultTagCache commFaultTagCache;
//
////  public void insertTestDataIntoCache() {
////    dataTagCache.put(testDataHelper.getDataTag().getId(), testDataHelper.getDataTag());
////    dataTagCache.put(testDataHelper.getDataTag2().getId(), testDataHelper.getDataTag2());
////    ruleTagCache.put(testDataHelper.getRuleTag().getId(), testDataHelper.getRuleTag());
////    controlTagCache.put(testDataHelper.getProcessAliveTag().getId(), testDataHelper.getProcessAliveTag());
////    controlTagCache.put(testDataHelper.getEquipmentAliveTag().getId(), testDataHelper.getEquipmentAliveTag());
////    controlTagCache.put(testDataHelper.getSubEquipmentAliveTag().getId(), testDataHelper.getSubEquipmentAliveTag());
////    processCache.put(testDataHelper.getProcess().getId(), testDataHelper.getProcess());
////    equipmentCache.put(testDataHelper.getEquipment().getId(), testDataHelper.getEquipment());
////    subEquipmentCache.put(testDataHelper.getSubEquipment().getId(), testDataHelper.getSubEquipment());
////  }
////
////  public void removeTestDataFromCache() {
////    dataTagCache.remove(testDataHelper.getDataTag2().getId());
////    dataTagCache.remove(testDataHelper.getDataTag().getId());
////    ruleTagCache.remove(testDataHelper.getRuleTag().getId());
////    controlTagCache.remove(testDataHelper.getProcessAliveTag().getId());
////    controlTagCache.remove(testDataHelper.getEquipmentAliveTag().getId());
////    controlTagCache.remove(testDataHelper.getSubEquipmentAliveTag().getId());
////    processCache.remove(testDataHelper.getProcess().getId());
////    equipmentCache.remove(testDataHelper.getEquipment().getId());
////    subEquipmentCache.remove(testDataHelper.getSubEquipment().getId());
////  }
//
//}
