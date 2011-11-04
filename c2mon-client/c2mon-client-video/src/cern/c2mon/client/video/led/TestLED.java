/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2008  CERN
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.client.video.led;

import java.awt.Color;
import javax.swing.JFrame;


public class TestLED extends JFrame {

	/**
   * 
   */
  private static final long serialVersionUID = -6573921285588154101L;
  private LEDnumberPanel ledPanel;
  
  public TestLED() {
		super("Test LED");
		setSize(200, 195);
		setLocation(200, 100);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		ledPanel = new LEDnumberPanel(Color.green, Color.black);
		getContentPane().add(ledPanel);
//		getContentPane().setBackground(Color.gray);
		
		setVisible(true);
		testDisplay();
	}
	
	private void testDisplay() {
    int sleepIntervall = 200;
    
	  while ( true ) {
  	  try {
//        ledPanel.setDisplayNumber((int)(Math.random() * 100));
  	    int count = 0;
  	    while ( count < 100 ) {
  	      ledPanel.setDisplayNumber(count);
  	      Thread.sleep(sleepIntervall);
  	      count++;
  	    }
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TestLED();
	}

}
