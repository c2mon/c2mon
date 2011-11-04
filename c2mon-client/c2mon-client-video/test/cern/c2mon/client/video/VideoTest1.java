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

import javax.swing.JFrame;
import javax.swing.JPanel;

import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.video.viewers.NetvuViewer;

public class VideoTest1 extends JFrame implements ActionListener {
  
  private NetvuViewer videoViewer = null;
  
  private int counter = 1;
  
  private boolean changeServer = true;
  
  private final String host;
  private final String login;
  private final String password;

	public VideoTest1(String host, String login, String password) {
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
        videoViewer.getVideoViewerManager().closeAllConnections();
        System.exit(0);
      }
    });
		
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
	  actionMenu.add(addCamera);
	  actionMenu.add(remCamera);
	  
	  setMenuBar(menuBar);
	}
	
	private JPanel createVideoPanel() {
		videoViewer = new NetvuViewer(false, true, true);
		return videoViewer;
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
      new VideoTest1(args[0].trim(), args[1].trim(), args[2].trim());
    }
    else {
      System.err.println("You didn't provide enough arguments! "+ args.length);
      System.exit(1);
    }
	}

  

	public void actionPerformed(ActionEvent e) {
    if ( e.getActionCommand().equalsIgnoreCase("add") ) {
//      if ( changeServer ) {
        VideoConnectionProperties props = new VideoConnectionProperties(host, counter++);
        props.setPassword(password);
        props.setLogin(login);
        videoViewer.getVideoViewerManager().connectCamera(props);
//      }
//      else {
//        videoPanel.getVideoPanelManager().connectCamera(new VideoConnectionProperties(host, counter++, 0));
//      }
//      changeServer = !changeServer;
    }
    else if ( e.getActionCommand().equalsIgnoreCase("remove") ) {
      if ( videoViewer.getVideoViewerManager().closeCameraConn(new VideoConnectionProperties(host, (counter - 1))) )
        counter--;
//      videoPanel.getVideoPanelManager().closeCameraConn(new VideoConnectionProperties(host, --counter, 0));
    }
  }

}
