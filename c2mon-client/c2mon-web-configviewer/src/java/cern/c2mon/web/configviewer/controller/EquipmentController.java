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
package cern.c2mon.web.configviewer.controller;

import cern.c2mon.shared.common.datatag.SourceDataTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.web.configviewer.service.ProcessService;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class acts as a controller to handle requests to view specific
 * equipment.
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class EquipmentController {

  /**
   * Reference to the {@link ProcessService} bean.
   */
  @Autowired
  private ProcessService service;

  /**
   * View a specific equipment of a given process.
   *
   * @param processName the name of the process
   * @param id          the id of the equipment to view
   * @param model       the model to be passed to the JSP processor
   *
   * @return the name of the JSP page to be processed
   *
   * @throws Exception if no equipment with the specified id was found
   */
  @RequestMapping(value = "/process/{processName}/equipment/{id}", method = {RequestMethod.GET})
  public String viewEquipment(@PathVariable("processName") final String processName, @PathVariable("id") final Long id, final Model model) throws Exception {
    ProcessConfiguration process = service.getProcessConfiguration(processName);
    process.setProcessName(processName);
    EquipmentConfiguration equipment = process.getEquipmentConfiguration(id);

    if (equipment == null) {
      throw new Exception("No equipment with id " + id + " was found.");
    }

    // Epic hack to make sure the list of tags is sorted by ID
    Map<Long, SourceDataTag> sortedTags = new TreeMap<>(equipment.getDataTags());
    Field field = EquipmentConfiguration.class.getDeclaredField("sourceDataTags");
    field.setAccessible(true);
    field.set(equipment, sortedTags);

    model.addAttribute("title", "Equipment Viewer");
    model.addAttribute("process", process);
    model.addAttribute("equipment", equipment);
    return "equipment";
  }
}
