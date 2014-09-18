/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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