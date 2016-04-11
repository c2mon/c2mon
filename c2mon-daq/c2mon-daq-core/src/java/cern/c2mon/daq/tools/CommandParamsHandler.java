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
package cern.c2mon.daq.tools;

import cern.c2mon.daq.config.Options;

import java.util.Hashtable;

/**
 * This class is used for handling command line parameters. The class validates
 * the parameters, and remembers the (param-name,param-value) pairs so that they
 * can be easly accessed later during program's execution.
 *
 * @deprecated use {@link Options} instead
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
         if (paramName.startsWith("-")) {
           return paramsTable.containsKey(paramName);
         }
         else {
           return paramsTable.containsKey("-" + paramName);
         }
    }

    /**
     * @param paramName The parameter name
     * @return the value of the specified parameter
     */
    public String getParamValue(String paramName) {
      if (paramName.startsWith("-")) {
        return paramsTable.get(paramName);
      }
      else {
        return paramsTable.get("-" + paramName);
      }
    }
}
