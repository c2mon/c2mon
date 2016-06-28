/*
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
 */

package arbitrary.type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.*;

/**
 * Testing class to create an arbitrary object for c2mon.
 *
 * @author Franz Ritter
 */
@Data
@Builder
@AllArgsConstructor
public class ComplexObject {

  private Long id;

  private String description;

  private boolean important;

  private Map<String, Long> nameToIds;

  private List<Integer> numberLists;

  private Integer[] intArray;

  private int[] primitiveArray;

  public ComplexObject(){

  }

  public static ComplexObject createRandomInstance(){
    Random rand = new Random();
    HashMap<String,Long> map = new HashMap<String, Long>();
    map.put("hello",rand.nextLong());

    return ComplexObject.builder()
        .id(rand.nextLong())
        .description("random complex object no. "+ rand.nextInt(100))
        .important(rand.nextBoolean())
        .nameToIds(map)
        .numberLists(Arrays.asList(rand.nextInt(100),rand.nextInt(100),rand.nextInt(100),rand.nextInt(100)))
        .intArray(new Integer[]{rand.nextInt(100), rand.nextInt(100), rand.nextInt(100)})
        .primitiveArray(new int[]{rand.nextInt(100), rand.nextInt(100), rand.nextInt(100)})
        .build();
  }
}
