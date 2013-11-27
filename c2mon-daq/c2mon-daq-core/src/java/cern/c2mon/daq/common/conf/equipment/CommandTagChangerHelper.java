/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
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
package cern.c2mon.daq.common.conf.equipment;

import cern.c2mon.shared.daq.command.ISourceCommandTag;

/**
 * Helper class which can be used by all command tag changer it has 
 * helper methods to check if command tag fields have changed.
 * 
 * @author Andreas Lang
 *
 */
public abstract class CommandTagChangerHelper 
        extends TagChangerHelper
        implements ICommandTagChanger {

    /**
     * Checks if the name has changed between the two command tags.
     * 
     * @param sourceCommandTag The new source command tag.
     * @param oldSourceCommandTag The old source command tag.
     * @return True if the nam has changed else false.
     */
    public boolean hasNameChanged(final ISourceCommandTag sourceCommandTag,
            final ISourceCommandTag oldSourceCommandTag) {
        return !sourceCommandTag.getName().equals(oldSourceCommandTag.getName());
    }
    
    /**
     * Checks if the source timeout has changed between the two command tags.
     * 
     * @deprecated This method will be removed when the source timeout moves to the
     * hardware address.
     * @param sourceCommandTag The new source command tag.
     * @param oldSourceCommandTag The old source command tag.
     * @return True if the source timeout has changed else false.
     */
    public boolean hasSourceTimeoutChanged(final ISourceCommandTag sourceCommandTag,
            final ISourceCommandTag oldSourceCommandTag) {
        return sourceCommandTag.getSourceTimeout() != oldSourceCommandTag.getSourceTimeout();
    }
}
