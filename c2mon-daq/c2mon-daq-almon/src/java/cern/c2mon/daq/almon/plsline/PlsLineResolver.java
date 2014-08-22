/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.plsline;

/**
 * @author wbuczak
 */
public interface PlsLineResolver {

    int PLS_LINE_UNDEFINED = -1;

    int resolve(String cycleName);
}
