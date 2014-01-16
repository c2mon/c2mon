package cern.c2mon.server.cache;

import cern.c2mon.server.common.tag.Tag;

/**
 * CommonTagFacade implementation that can be used on any {@link Tag}
 * cache object (DataTag, ControlTag and RuleTag). It locates the correct
 * facade bean to call according to the object passed. 
 * @author Mark Brightwell
 *
 */
public interface TagFacadeGateway extends CommonTagFacade<Tag> {

}
