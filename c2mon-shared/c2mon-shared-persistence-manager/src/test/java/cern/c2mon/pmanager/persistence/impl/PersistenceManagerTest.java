/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.pmanager.persistence.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.mock.AlarmListenerImpl;
import cern.c2mon.pmanager.mock.DBHandlerImpl;
import cern.c2mon.pmanager.mock.FallbackImpl;

import static org.junit.Assert.assertEquals;

/**
 * JUnit test for the PersistenceManager class
 *
 * @author mruizgar
 *
 */
@RunWith(JUnit4.class)
public class PersistenceManagerTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder(new File("/tmp"));

  /** Instance of the class we want to test */
  private PersistenceManager<IFallback> persistenceManager;

  /** It sets all the objects needed for running the test */
  @Before
  public final void setUp() throws IOException {
    File fallbackFile = folder.newFile();
    persistenceManager = new PersistenceManager<>(new DBHandlerImpl(), fallbackFile.getAbsolutePath(), new AlarmListenerImpl(), new FallbackImpl());
  }

  /**
   * Tests the storeData([Collection]) method
   */
  @Test
  public final void testStoreObjectData() {
    int lines = persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();

    IFallback fallback = new FallbackImpl();
    persistenceManager.storeData(fallback);
    assertEquals(lines, persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines());
  }

  /**
   * Tests the storeData(IFallback) method
   */
  @Test
  public final void testStoreListData() {
    int lines = persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();
    List<IFallback> data = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      data.add(new FallbackImpl());
    }
    persistenceManager.storeData(data);
    assertEquals(lines, persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines());
  }

  /**
   * Tests the storeData(IFallback) method when writing to the DB fails
   */
  @Test
  public final void testStoreObjectConnectionFails() {
    FallbackImpl fallback = new FallbackImpl();
    int lines = persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();

    fallback.setObjectData(FallbackImpl.ERROR);
    persistenceManager.storeData(fallback);
    assertEquals(1, persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines() - lines);
  }

  /**
   * Tests the behavior of the storeData([Collection]) method when writing to the DB fails
   */
  @Test
  public final void testStoreListDataConnectionFails() {
    List<IFallback> data = new ArrayList<>();
    int lines = persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();
    for (int i = 0; i < 4; i++) {
      data.add(new FallbackImpl());
    }
    FallbackImpl fallback = new FallbackImpl();
    fallback.setObjectData(FallbackImpl.ERROR);
    data.add(fallback);
    persistenceManager.storeData(data);
    assertEquals(5, persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines() - lines);
  }


}
