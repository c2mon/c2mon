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
 * Controller entry point for data tag API requests.
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class DataTagController {

  /**
   * The URL mapping to be used for retrieving data tags.
   */
  private static final String DATATAG_VALUE_MAPPING = "/datatags/{id}";

  /**
   * Reference to the data tag service bean.
   */
  @Autowired
  private DataTagService service;

  /**
   * Spring MVC request mapping entry point for requests to the URL defined by
   * DATATAG_VALUE_MAPPING.
   *
   * <p>
   * Note: only GET requests are allowed to this URL.
   * </p>
   *
   * @param id the path variable representing the ID of the data tag to be
   *          retrieved
   * @return the {@link ClientDataTagValue} object itself, which will be
   *         automatically serialised by Spring
   *
   * @throws UnknownResourceException if no data tag was found with the given ID
   */
  @RequestMapping(value = DATATAG_VALUE_MAPPING, method = GET, produces = { API_V1 })
  @ResponseBody
  public ClientDataTagValue getDataTagValue(@PathVariable final Long id) throws UnknownResourceException {
    return service.getDataTagValue(id);
  }
}
