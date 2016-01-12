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

package cern.c2mon.daq.almon.plsline.impl;

import static cern.japc.ext.tgm.TgmUtil.cycleName2LineNumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import cern.c2mon.daq.almon.plsline.PlsLineResolver;

/**
 * This pls line resolver implementation uses japc-ext-tgm to resolve pls line codes
 * 
 * @author wbuczak
 */
public class PlsLineResolverImpl implements PlsLineResolver {

    private static final Logger LOG = LoggerFactory.getLogger(PlsLineResolverImpl.class);

    @Value("#{conf.maxPlsLine}")
    private int maxPlsLine;

    @Override
    public int resolve(String cycleName) {

        int plsLine = PLS_LINE_UNDEFINED;

        if (!cycleName.isEmpty()) {
            try {
                plsLine = cycleName2LineNumber(cycleName);
            } catch (Exception ex) {
                LOG.error("Exception in converting cycle: {} to pls line", cycleName);
                LOG.debug("exception trace: ", ex);
                return PLS_LINE_UNDEFINED;
            }
            LOG.debug("Converted cycle '" + cycleName + "' to pls line " + plsLine);
            if (plsLine < 1 || plsLine > maxPlsLine) {
                LOG.error("Failed to convert cycle: {} to pls line", cycleName);

                return PLS_LINE_UNDEFINED;
            }
        }

        return plsLine;
    }

}
