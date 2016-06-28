package cern.c2mon.server.test;///******************************************************************************
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
//package cern.c2mon.server.test;
//
//import java.io.IOException;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.transaction.TransactionConfiguration;
//import org.springframework.transaction.annotation.Transactional;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration({"classpath:test-config/server-test-data.xml"})
//@TransactionConfiguration(transactionManager = "testTransactionManager", defaultRollback = true)
//@Transactional("testTransactionManager")
//public class TestDataInserterTest {
//
//  @Autowired
//  private TestDataInserter testDataInserter;
//
//  @Test
//  @DirtiesContext
//  public void testDataRemove() throws IOException {
//    testDataInserter.removeTestData();
//  }
//
//  @Test
//  @DirtiesContext
//  public void testDataInsert() throws IOException {
//    testDataInserter.removeTestData();
//    testDataInserter.insertTestData();
//  }
//
//}
