/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
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
 ******************************************************************************/
package cern.c2mon.shared.client.configuration.converter;

import java.sql.Timestamp;
import java.text.DateFormat;

import org.simpleframework.xml.transform.Transform;

/**
 * Enables deserialisation of timestamps inside configuration reports.
 *
 * @author Justin Lewis Salmon
 */
public class DateFormatConverter implements Transform<Timestamp> {
  private DateFormat dateFormat;

  public DateFormatConverter(DateFormat dateFormat) {
    this.dateFormat = dateFormat;
  }

  @Override
  public Timestamp read(String value) throws Exception {
    return new Timestamp(dateFormat.parse(value).getTime());
  }

  @Override
  public String write(Timestamp value) throws Exception {
    return dateFormat.format(value);
  }

}