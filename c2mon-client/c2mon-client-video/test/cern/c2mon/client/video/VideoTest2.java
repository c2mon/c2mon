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

package cern.c2mon.client.video;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.video.NetvuMainPanel;
import cern.c2mon.client.video.VideoMainHandler;

public class VideoTest2 extends JFrame implements ActionListener {
  
  private VideoMainHandler videoHandler = null;
  
  List<Integer> addedPositions;
  List<Integer> removedPositions;
  
  private int counter = 1;
  
  private final String host;
  private final String login;
  private final String password;

	public VideoTest2(String host, String login, String password) {
		super("TIM Video Support Test");
		setSize(800, 800);
		setLocation(200, 100);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().add(createVideoPanel());
		
		this.host = host;
		this.login = login;
		this.password = password;
		
		createMenu();
		
		addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
//        videoPanel.getVideoPanelManager().closeAllConnections();
        System.exit(0);
      }
    });
		
		addedPositions = new ArrayList<Integer>();
		removedPositions = new ArrayList<Integer>();
		for (int i = 1; i < 6; i++) {
		  removedPositions.add(new Integer(i));
    }
		
		setVisible(true);
	}
	
	private void createMenu() {
	  MenuBar menuBar = new MenuBar();
	  Menu actionMenu = new Menu("Actions"); 
	  
	  menuBar.add(actionMenu);
	  
	  MenuItem addCamera = new MenuItem("Add Camera View");
	  addCamera.addActionListener(this);
	  addCamera.setActionCommand("add");
	  MenuItem remCamera = new MenuItem("Remove Camera View");
	  remCamera.addActionListener(this);
    remCamera.setActionCommand("remove");
    MenuItem mainCamera = new MenuItem("Set new Main Camera");
    mainCamera.addActionListener(this);
    mainCamera.setActionCommand("setMainVideo");
    
    MenuItem closeAllConnections = new MenuItem("Close all Connections");
    closeAllConnections.addActionListener(this);
    closeAllConnections.setActionCommand("closeAllConnections");
    
	  actionMenu.add(addCamera);
	  actionMenu.add(remCamera);
	  actionMenu.add(mainCamera);
	  actionMenu.addSeparator();
	  actionMenu.add(closeAllConnections);
	  
	  setMenuBar(menuBar);
	}
	
	private JComponent createVideoPanel() {
		NetvuMainPanel videoPanel = new NetvuMainPanel("Access PS Doors", true, 4);
		videoHandler = new VideoMainHandler(videoPanel);
		return videoPanel;
	}

	/**
	 * The serial Version UID of the class. Needed for Java 1.6
	 */
	private static final long serialVersionUID = -7687938210179552069L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	  if ( args.length == 3 ) {
	    new VideoTest2(args[0].trim(), args[1].trim(), args[2].trim());
	  }
	  else {
	    System.err.println("You didn't provide enough arguments! "+ args.length);
	    System.exit(1);
	  }
	}

  

	public void actionPerformed(ActionEvent e) {
    if ( e.getActionCommand().equalsIgnoreCase("add") ) {
        // Another server: muxvidsr1
        Integer cameraPosition = removedPositions.remove((int)Math.round(Math.random() * (removedPositions.size() - 1)));
        addedPositions.add(cameraPosition);
        VideoConnectionProperties props = new VideoConnectionProperties(host, cameraPosition.intValue());
        props.setLogin(login);
        props.setPassword(password);
        props.setKeysTaken( (int)Math.round(Math.random() * 99));
        props.setDescription("Access point number: YEA02=102:\nAccess point description:This is the test access point for the TIM video!");
        
        videoHandler.addVideoRequest(props);
    }
    else if ( e.getActionCommand().equalsIgnoreCase("remove") ) {
      Integer cameraPosition = addedPositions.remove((int)Math.round(Math.random() * (addedPositions.size() - 1)));
      removedPositions.add(cameraPosition);
      System.out.println("camera position to remove: "+ cameraPosition);
      if ( videoHandler.closeVideoConnection( new VideoConnectionProperties(host, cameraPosition.intValue())) )
//      if ( videoHandler.closeVideoConnection( new VideoConnectionProperties(host, 1, 0)) )
        counter--;
    }
    else if ( e.getActionCommand().equalsIgnoreCase("setMainVideo") ) {
      Integer cameraPosition = addedPositions.get((int)Math.round(Math.random() * (addedPositions.size() - 1)));
      VideoConnectionProperties props = new VideoConnectionProperties(host, cameraPosition.intValue());
      props.setLogin(login);
      props.setPassword(password);
      props.setKeysTaken( (int)Math.round(Math.random() * 99));
      props.setDescription("Access point number: YEA02=102:\nAccess point description:This is the test access point for the TIM video!");
      videoHandler.setActiveVideoConncetion(props);
    }
    else if ( e.getActionCommand().equalsIgnoreCase("closeAllConnections") ) {
      videoHandler.closeAllConnections();
    }
  }

}
