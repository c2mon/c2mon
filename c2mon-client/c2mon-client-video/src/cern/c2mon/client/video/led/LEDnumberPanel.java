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
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;


/**
 * This Panel can be used to display numbers with two digits
 * on an simulated LED panel.
 * @author Matthias Braeger
 *
 */
public class LEDnumberPanel extends JPanel {

  /**
   * 
   */
  private static final long serialVersionUID = 3286988954287789876L;
  
  // The grid has 5 columns for each digit + one extra free column in the middle 
  private static final int gridCols = 11;
	private static final int gridRows = 7;
	
	private static final boolean t = true;
	private static final boolean f = false;
	
	/**
	 * Default Constructor
	 * @param forground color of the digit
	 * @param background backgound color
	 */
	public LEDnumberPanel(Color forground, Color background) {
		super();
	
		setBackground(Color.darkGray);
		setLayout(new GridLayout(gridRows, gridCols));
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		  
		for ( int i = 0; i < (gridRows * gridCols); i++) {
			LED led = new LED(forground, background);
			add(led);
		}
	}
	
	/**
	 * Resets the LED panel;
	 */
	public void reset() {
		for (int i=0; i < (gridCols * gridRows); i++) {
			((LED)getComponent(i)).useSecondary();
		}
	}
	
	/**
	 * Sets the number that shall be displayed on the LED panel
	 * @param number A number between 0 and 99
	 * @throws NumberFormatException In case that 0 <= number <= 99 is not true
	 */
	public void setDisplayNumber(int number) {
	  if ( number < 0 || number > 99) 
	    throw new NumberFormatException("The given Number is not between 0 and 99 !");
	  else {
	    int leftDigit, rightDigit;
	    
	    if ( number <= 9) {
	      displayLeftDigit(0);
	      displayRightDigit(number);
	    }
	    else {
	      // Split the number in its digits
	      rightDigit = number % 10;
	      leftDigit  = number / 10;
	      
	      displayLeftDigit(leftDigit);
        displayRightDigit(rightDigit);
	    }
	      
	  }
	}
	
	/**
	 * Draws the digit on the left LED
	 * @param digit A number between 0 and 9
	 * @throws NumberFormatException In case that 0 <= digit <= 9 is not true
	 */
	private void displayLeftDigit(int digit) throws NumberFormatException {
	  if ( digit < 0 || digit > 9) 
	     throw new NumberFormatException("The given left digit is not between 0 and 9 !");
	  else {
	    switch ( digit ) {
	      case 0: displayZero(true); break;
	      case 1: displayOne(true); break;
	      case 2: displayTwo(true); break;
	      case 3: displayThree(true); break;
	      case 4: displayFour(true); break;
	      case 5: displayFive(true); break;
	      case 6: displaySix(true); break;
	      case 7: displaySeven(true); break;
	      case 8: displayEight(true); break;
	      case 9: displayNine(true);
	    }
	  }
	}
	
	/**
   * Draws the digit on the left LED
   * @param digit A number between 0 and 9
   * @throws NumberFormatException In case that 0 <= digit <= 9 is not true
   */
  private void displayRightDigit(int digit) throws NumberFormatException {
    if ( digit < 0 || digit > 9) 
      throw new NumberFormatException("The given right digit is not between 0 and 9 !");
    else {
      switch ( digit ) {
        case 0: displayZero(false); break;
        case 1: displayOne(false); break;
        case 2: displayTwo(false); break;
        case 3: displayThree(false); break;
        case 4: displayFour(false); break;
        case 5: displayFive(false); break;
        case 6: displaySix(false); break;
        case 7: displaySeven(false); break;
        case 8: displayEight(false); break;
        case 9: displayNine(false);
      }
    }
  }
	
	/**
   * Draws a 0 on the display
   */
	private void displayZero(boolean leftLED) {
    boolean[] gridCode = {f, t, t, t, f,
                          t, f, f, f, t,
                          t, f, f, t, t,
                          t, f, t, f, t,
                          t, t, f, f, t,
                          t, f, f, f, t,
                          f, t, t, t, f};

    drawDigit(gridCode, leftLED);
  }
	
	/**
	 * Draws a 1 on the display
	 */
	private void displayOne(boolean leftLED) {
		boolean[] gridCode = {f, f, t, f, f,
		                      f, t, t, f, f,
		                      f, f, t, f, f,
		                      f, f, t, f, f,
		                      f, f, t, f, f,
		                      f, f, t, f, f,
		                      f, t, t, t, f};
		
		drawDigit(gridCode, leftLED);
	}
	
	/**
   * Draws a 2 on the display
   */
	private void displayTwo(boolean leftLED) {
	  boolean[] gridCode = {f, t, t, t, f,
                          t, f, f, f, t,
                          f, f, f, f, t,
                          f, f, f, t, f,
                          f, f, t, f, f,
                          f, t, f, f, f,
                          t, t, t, t, t};

	  drawDigit(gridCode, leftLED);
	}
	
	/**
   * Draws a 3 on the display
   */
	private void displayThree(boolean leftLED) {
    boolean[] gridCode = {t, t, t, t, t,
                          f, f, f, t, f,
                          f, f, t, f, f,
                          f, f, f, t, f,
                          f, f, f, f, t,
                          t, f, f, f, t,
                          f, t, t, t, f};

    drawDigit(gridCode, leftLED);
  }
	
	/**
   * Draws a 4 on the display
   */
  private void displayFour(boolean leftLED) {
    boolean[] gridCode = {f, f, f, t, f,
                          f, f, t, t, f,
                          f, t, f, t, f,
                          t, f, f, t, f,
                          t, t, t, t, t,
                          f, f, f, t, f,
                          f, f, f, t, f};

    drawDigit(gridCode, leftLED);
  }
    
  /**
   * Draws a 5 on the display
   */
  private void displayFive(boolean leftLED) {
    boolean[] gridCode = {t, t, t, t, t,
                          t, f, f, f, f,
                          t, t, t, t, f,
                          f, f, f, f, t,
                          f, f, f, f, t,
                          t, f, f, f, t,
                          f, t, t, t, f};
  
    drawDigit(gridCode, leftLED);
  }  
	
  /**
   * Draws a 6 on the display
   */
  private void displaySix(boolean leftLED) {
    boolean[] gridCode = {f, f, t, t, f,
                          f, t, f, f, f,
                          t, f, f, f, f,
                          t, t, t, t, f,
                          t, f, f, f, t,
                          t, f, f, f, t,
                          f, t, t, t, f};

    drawDigit(gridCode, leftLED);
  }
  
  /**
   * Draws a 7 on the display
   */
  private void displaySeven(boolean leftLED) {
    boolean[] gridCode = {t, t, t, t, t,
                          f, f, f, f, t,
                          f, f, f, t, f,
                          f, f, t, f, f,
                          f, t, f, f, f,
                          f, t, f, f, f,
                          f, t, f, f, f};
  
    drawDigit(gridCode, leftLED);
  }
  
  /**
   * Draws a 8 on the display
   */
  private void displayEight(boolean leftLED) {
    boolean[] gridCode = {f, t, t, t, f,
                          t, f, f, f, t,
                          t, f, f, f, t,
                          f, t, t, t, f,
                          t, f, f, f, t,
                          t, f, f, f, t,
                          f, t, t, t, f};
  
    drawDigit(gridCode, leftLED);
  }
  
  /**
   * Draws a 9 on the display
   */
  private void displayNine(boolean leftLED) {
    boolean[] gridCode = {f, t, t, t, f,
                          t, f, f, f, t,
                          t, f, f, f, t,
                          f, t, t, t, t,
                          f, f, f, f, t,
                          f, f, f, t, f,
                          f, t, t, f, f};
  
    drawDigit(gridCode, leftLED);
  }
  
	/**
	 * Private method to draw a single digit on the LED display
	 * @param gridCode An array of booleans, that represents the LEDs to be set
	 * @param leftLED true, if the digit shall be displayed on the left side of the LED panel.
	 */
	private void drawDigit(boolean[] gridCode, boolean leftLED) {
	  int gridCodeCounter = 0;
	  
	  if ( leftLED ) {
  	  for ( int counter = 0; counter < ( gridRows * gridCols ); counter++) {
        if ( ((counter + 6) % 11 ) == 0 ) {
         // Jump over 6 fields
         counter += 5;
        }
        else if ( gridCode[gridCodeCounter++] )
          ((LED)getComponent(counter)).usePrimary();
        else
          ((LED)getComponent(counter)).useSecondary();
      }
  	}
	  else {
	    // We want to draw the digit on the right side
	    for ( int counter = 6; counter < ( gridRows * gridCols ); counter++) {
        if ( ( counter % 11 ) == 0 ) {
         // Jump over 6 fields
         counter += 6;
        }
        
        if ( gridCode[gridCodeCounter++] )
          ((LED)getComponent(counter)).usePrimary();
        else
          ((LED)getComponent(counter)).useSecondary();
      }
	  }
	}
}
