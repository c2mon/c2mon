/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.tools;

import java.util.Hashtable;

/**
 * This class is used for handling command line parameters. The class validates
 * the parameters, and remembers the (param-name,param-value) pairs so that they
 * can be easly accessed later during program's execution.
 */
public class CommandParamsHandler {

    /**
     * The TIM hashtable used for storing parsed (param-name,param-value) pairs
     */
    private final Hashtable<String, String> paramsTable;

    // to be removed once moved to Spring and change initialize method to
    // private
    public CommandParamsHandler() {
        paramsTable = new Hashtable<String, String>();
    }

    /**
     * The constructor
     */
    public CommandParamsHandler(String[] params) {
        paramsTable = new Hashtable<String, String>();
        initialise(params);
    }

    /**
     * This method parses the command line array of arguments and tries to fill
     * the hashtable with (param-name, param-value). In case of some troubles
     * (inpropper number of tokens found etc..) it throws the
     * IncorrectCommandLineParamsException
     */
    public void initialise(String[] params) {

        for (int i = 0; i < params.length; i++) {
            if (params[i].charAt(0) == '-') {
                if (i < params.length - 1) {
                    if (params[i + 1].charAt(0) == '-') {
                        paramsTable.put(params[i], "null");
                    } else {
                        paramsTable.put(params[i], params[i + 1]);
                    }
                }// if
                else {
                    paramsTable.put(params[i], "null");
                }
            }// if
        }// for
    }

    /**
     * This method checks if specified parameter is registered or not
     */
    public boolean hasParam(String paramName) {
        return paramsTable.containsKey(paramName);
    }

    /**
     * This method returns the value of the specified parameter
     */
    public String getParamValue(String paramName) {
        return (String) paramsTable.get(paramName);
    }

}