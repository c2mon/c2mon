/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
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
package cern.c2mon.japc.publisher;

import org.junit.Test;

import cern.japc.AcquiredParameterValue;
import cern.japc.Parameter;
import cern.japc.ParameterException;
import cern.japc.ParameterValueListener;
import cern.japc.SubscriptionHandle;
import cern.japc.factory.ParameterFactory;
import cern.japc.spi.SelectorImpl;

public class JapcClient implements ParameterValueListener {
  static{
    System.setProperty("cern.japc.ext.valcompl.disabled", "true");
  }
  
  private static SubscriptionHandle handle;

  public JapcClient() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    // System.setProperty("wildcard.selector.resolver","cern.japc.ext.subscription.TgmWildcardSelectorResolver");
   
    try {
      
      JapcClient tester = new JapcClient();
      ParameterFactory parameterFactory = ParameterFactory.newInstance();

      
      Parameter parameter = parameterFactory
      .newParameter("no://tim/TIM_DEVICE/148528");

      SelectorImpl selector = new SelectorImpl("");
          
      handle = parameter.createSubscription(selector, tester);
      handle.startMonitoring();
      
      Thread.sleep(4000);
      
      synchronized (tester) {
        tester.wait();
      }
    } catch (Throwable ex) {
      ex.printStackTrace();
    }
  }

  public void valueReceived(String paramName, AcquiredParameterValue param) {
    System.out.println(param);
  }

  public void exceptionOccured(String arg0, String arg1,
      ParameterException arg2) {
    arg2.printStackTrace();
  }

}

