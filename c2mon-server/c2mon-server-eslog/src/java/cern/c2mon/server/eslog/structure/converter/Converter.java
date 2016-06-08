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
package cern.c2mon.server.eslog.structure.converter;

/**
 * Defines behaviour for transforming a source instance
 * to it's corresponding target one.
 *
 * @param <S> The source instance
 * @param <T> The target instance
 */
public interface Converter<S, T> {

  /**
   * Converts a provided instance to the target one.
   *
   * @param source the source instance that works
   *               as information provider for the process
   * @return the target instance that has derived from the
   * information provided by the source one
   */
  T convert(S source);

}
