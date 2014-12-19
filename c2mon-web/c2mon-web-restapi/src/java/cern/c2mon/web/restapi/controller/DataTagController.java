package cern.c2mon.web.restapi.controller;

import static cern.c2mon.web.restapi.version.ApiVersion.API_V1;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.web.restapi.exception.UnknownResourceException;
import cern.c2mon.web.restapi.service.DataTagService;

/**
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class DataTagController {

  /**
   *
   */
  private static final String DATATAG_VALUE_MAPPING = "/datatags/{id}";

  /**
   *
   */
  @Autowired
  private DataTagService service;

  /**
   *
   * @param id
   * @return
   * @throws UnknownResourceException
   */
  @RequestMapping(value = DATATAG_VALUE_MAPPING, method = GET, produces = { API_V1 })
  @ResponseBody
  public ClientDataTagValue getDataTagValue(@PathVariable final Long id) throws UnknownResourceException {
    return service.getDataTagValue(id);
  }
}
