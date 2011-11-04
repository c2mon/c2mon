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

package cern.c2mon.client.common.video;


/**
 * This Class serves as container for the connection information
 * that are retrieved from the XML video configuration file.
 * 
 * @author Matthias Braeger
 */
public class VideoConnectionProperties {
	
	/**
   * Version number of the class used during serialization/deserialization.
   * This is to ensure that minor changes to the class do not prevent us
   * from reading back VideoConnectionProperties objects we have serialized 
   * earlier. If fields are added/removed from the class, the version 
   * number needs to change.
   */
  private static final long serialVersionUID = -989073437126133377L;

  /** ID variable is needed for iBatis */
  private int id;
  
  /** host name or IP address */
	private String host;
	
	 /** Camera image number */
  private int camera;
	
	/** Login name */
	private String login = "";
	
	/** Password */
	private String password = "";
	
	/** id of the data tag that queues the connection */
	private long queueingTagId = -1;
	
	/** id of the data tag that activates to put it on the main video viewer */
	private long activationTagId = -1;
	
	/** id of the data tag that updates the amount of taken keys */
	private long keysTakenTagId = -1;
		
	/** Description of the video connection */
	private String description = "";
	
	/** Amount of taken keys from the specific point */
	private int keysTaken = 0;

	
	/**
   * @return Amount of taken keys from the specific point
   */
  public final int getKeysTaken() {
    return keysTaken;
  }

  /**
   * @param keysTaken the keysTaken to set
   */
  public final void setKeysTaken(int keysTaken) {
    this.keysTaken = keysTaken;
  }

  /**
   * Default Constructor needed for IBatis.
   */
  public VideoConnectionProperties() {
    // Do nothing
  }
  
  /**
   * Constructor
   * @param host host name or IP address
   * @param camera Camera image number
   */
  public VideoConnectionProperties(final String host, final int camera) {
    this.host = host.toLowerCase();
    this.camera = camera;
  }
  
  /**
   * Constructor
   * @param host host name or IP address
   * @param camera Camera image number
   * @param activationTagId id of the data tag that activates to put it on the main video viewer
   * @param queueingTagId id of the data tag that queues the connection
   * @param keysTakenTagId id of the data tag that sends updates about the keys taken at the specific access point
   */
  public VideoConnectionProperties(final String host, final int camera, final int activationTagId, final int queueingTagId, final int keysTakenTagId) {
    this(host, camera);
    
    this.queueingTagId = queueingTagId;
    this.activationTagId = activationTagId;
    this.keysTakenTagId = keysTakenTagId;
  }
	
	/**
	 * @return the host
	 */
	public final String getHost() {
		return host;
	}

	/**
	 * @return the login
	 */
	public final String getLogin() {
		return login;
	}

	/**
	 * @param login the login to set
	 */
	public final void setLogin(final String login) {
		this.login = login;
	}

	/**
	 * @return the password
	 */
	public final String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public final void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * id of the data tag that queues the connection
	 * @return the data tag ID for the queueing request
	 */
	public final long getQueueingTagId() {
		return queueingTagId;
	}

	/**
	 * @return the camera
	 */
	public final int getCamera() {
		return camera;
	}

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public final boolean equals(final Object obj) {
    boolean retval = false;
    
    if (obj instanceof VideoConnectionProperties) {
      VideoConnectionProperties connProps = (VideoConnectionProperties) obj;
      
      if ( connProps.getCamera() == camera && 
           connProps.getHost().equalsIgnoreCase(host)) {
        retval = true;
      }
    }
    
    return retval;
  }

  /**
   * @return The description of the video connection
   */
  public final String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public final void setDescription(final String description) {
    this.description = description;
  }

  /**
   * @return The activationTagId id of the data tag that activates to put it on the main video viewer
   */
  public final long getActivationTagId() {
    return activationTagId;
  }

  /**
   * @return The keysTakenTagId id of the data tag that updates the amount of taken keys
   */
  public final long getKeysTakenTagId() {
    return keysTakenTagId;
  }

  /**
   * @return the id
   */
  public final int getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public final void setId(int id) {
    this.id = id;
  }

  /**
   * @param queueingTagId the queueingTagId to set
   */
  public final void setQueueingTagId(long queueingTagId) {
    this.queueingTagId = queueingTagId;
  }

  /**
   * @param activationTagId the activationTagId to set
   */
  public final void setActivationTagId(long activationTagId) {
    this.activationTagId = activationTagId;
  }

  /**
   * @param keysTakenTagId the keysTakenTagId to set
   */
  public final void setKeysTakenTagId(long keysTakenTagId) {
    this.keysTakenTagId = keysTakenTagId;
  }

  /**
   * @param host the host to set
   */
  public final void setHost(String host) {
    this.host = host;
  }

  /**
   * @param camera the camera to set
   */
  public final void setCamera(int camera) {
    this.camera = camera;
  }
}
