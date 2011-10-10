/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.history.gui.components.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

/**
 * This model can be used by a {@link JList} to easily do filtering and
 * searching in a list.
 * 
 * @author vdeila
 * 
 * @param <T>
 *          the type of the search
 */
public class SearchListModel<T> extends AbstractListModel implements ListModel {

  /** serialVersionUID */
  private static final long serialVersionUID = 7495128039046024322L;
  
  /** All the data */
  private final Collection<Object> allData;
  
  /** The data currently showing to the user */
  private final List<Object> effectiveData;
  
  /** The lock for {@link #effectiveData} */
  private final ReentrantReadWriteLock effectiveDataLock;
  
  /**
   * The matcher which decides in what level the search is matching a list
   * object
   */
  private final Matcher<T> matcher;
  
  public SearchListModel(final Object[] listData, final Matcher<T> matcher) {
    this.allData = Arrays.asList(listData);
    this.matcher = matcher;
    this.effectiveData = new ArrayList<Object>(this.allData);
    this.effectiveDataLock = new ReentrantReadWriteLock();
  }

  /**
   * 
   * @param search the search object
   */
  public void search(final T search) {
    final List<MatchedListObject> matches = new ArrayList<MatchedListObject>();
    
    for (Object listObj : this.allData) {
      final int match = matcher.matches(listObj, search);
      if (match >= 0) {
        matches.add(new MatchedListObject(listObj, match));
      }
    }
    
    Collections.sort(matches, new Comparator<MatchedListObject>() {
      @Override
      public int compare(final MatchedListObject o1, final MatchedListObject o2) {
        if (o1.getMatch() < o2.getMatch()) {
          return -1;
        } 
        else if (o1.getMatch() > o2.getMatch()) {
          return 1;
        }
        return 0;
      }
    });
    
    final int oldSize;
    this.effectiveDataLock.writeLock().lock();
    try {
      oldSize = this.effectiveData.size();
      effectiveData.clear();
      for (MatchedListObject match : matches) {
        effectiveData.add(match.getListObject());
      }
    }
    finally {
      this.effectiveDataLock.writeLock().unlock();
    }
    
    if (oldSize > 0) {
      fireIntervalRemoved(this, 0, oldSize - 1);
    }
    if (getSize() > 0) {
      fireIntervalAdded(this, 0, getSize() - 1);
    }
  }
  
  /**
   * Resets the search
   */
  public void resetSearch() {
    final int oldSize;
    this.effectiveDataLock.writeLock().lock();
    try {
      oldSize = this.effectiveData.size();
      this.effectiveData.clear();
      this.effectiveData.addAll(allData);
    }
    finally {
      this.effectiveDataLock.writeLock().unlock();
    }
    
    if (oldSize > 0) {
      fireIntervalRemoved(this, 0, oldSize - 1);
    }
    if (getSize() > 0) {
      fireIntervalAdded(this, 0, getSize() - 1);
    }
  }
  
  @Override
  public Object getElementAt(int index) {
    this.effectiveDataLock.readLock().lock();
    try {
      return effectiveData.get(index);
    }
    finally {
      this.effectiveDataLock.readLock().unlock();
    }
  }

  @Override
  public int getSize() {
    this.effectiveDataLock.readLock().lock();
    try {
      return effectiveData.size();
    }
    finally {
      this.effectiveDataLock.readLock().unlock();
    }
  }
  
  

}
