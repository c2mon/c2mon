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
package cern.c2mon.client.history.gui.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cern.c2mon.client.ext.history.common.SavedHistoryEvent;
import cern.c2mon.client.history.gui.components.model.Matcher;

/**
 * A {@link Matcher} to match {@link SavedHistoryEvent}s with each other.
 * 
 * @author vdeila
 * 
 */
class SavedHistoryEventSearchMatcher implements Matcher<String> {

  /** Date formatter used when searching */
  private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("d.MM.yyyy HH:mm");

  /** The expression used when splitting the string into arrays */
  private static final String SPLIT_EXPRESSION = "[^\\w:\\./]+";
  
  @Override
  public int matches(final Object listObj, final String search) {
    if (search == null || search.length() == 0) {
      return 0;
    }

    if (listObj != null && listObj instanceof SavedHistoryEvent) {
      final SavedHistoryEvent event = (SavedHistoryEvent) listObj;

      final String[] searchWords = search.split(SPLIT_EXPRESSION);
      int match = 10000;

      for (String searchWord : searchWords) {
        if (searchWord.length() == 0) {
          continue;
        }
        final int wordMatch = calculateMatchWeight(searchWord, event);
        if (wordMatch == 0) {
          return -1;
        }
        match -= wordMatch;
      }

      if (match < 0) {
        match = 0;
      }
      if (match >= 10000) {
        match = -1;
      }
      return match;
    }
    else {
      return -1;
    }
  }

  /**
   * 
   * @param word
   *          the word to search for
   * @param event
   *          the event to search in
   * @return the weighted of the matches
   */
  private static int calculateMatchWeight(final String word, final SavedHistoryEvent event) {
    int matchingWeight = 0;
    if (event.getName() != null) {
      matchingWeight += 40 * countWordMatches(word, event.getName(), true);
    }

    matchingWeight += 400 * countWordMatches(word, "" + event.getId(), false);

    if (event.getDescription() != null) {
      matchingWeight += 15 * countWordMatches(word, event.getDescription(), true);
    }
    if (event.getStartDate() != null) {
      matchingWeight += 40 * countWordMatches(word, DATE_FORMATTER.format(event.getStartDate()), true);
    }
    if (event.getEndDate() != null) {
      matchingWeight += 40 * countWordMatches(word, DATE_FORMATTER.format(event.getEndDate()), true);
    }
    return matchingWeight;
  }
  
  /**
   * @param searchWords
   *          the search words to search for
   * @param str
   *          the string to search in
   * @param useContainsMethod
   *          <code>true</code> to use the {@link String#contains(CharSequence)}
   *          method, <code>false</code> to use
   *          {@link String#compareToIgnoreCase(String)}.
   * @return the number of matches in the <code>str</code>
   */
  private static int countWordMatches(final String searchWord, final String str, final boolean useContainsMethod) {
    final String[] strWords = str.split(SPLIT_EXPRESSION);
    int matches = 0;
    if (searchWord.length() > 0) {
      final String searchWordLowerCase = searchWord.toLowerCase();
      for (String strWord : strWords) {
        if (strWord.length() == 0) {
          continue;
        }
        if (useContainsMethod) {
          final String strWordLowerCase = strWord.toLowerCase();
          if (strWordLowerCase.contains(searchWordLowerCase)) {
            matches++;
          }
        }
        else {
          if (strWord.compareToIgnoreCase(searchWordLowerCase) == 0) {
            matches++;
          }
        }
      }
    }
    return matches;
  }
}
