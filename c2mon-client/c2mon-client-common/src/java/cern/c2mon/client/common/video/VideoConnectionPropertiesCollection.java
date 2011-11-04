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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import cern.tim.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * A serializable collection of VideoConnectionProperties objects
 * The VideoConnectionPropertiesCollection is modelled on the 
 * java.util.Collection interface although it does not implement it.
 * 
 * @author Matthias Braeger
 */
public class VideoConnectionPropertiesCollection {
  
  /**
   * Version number of the class used during serialization/deserialization.
   * This is to ensure that minor changes to the class do not prevent us
   * from reading back VideoConnectionPropertiesCollection objects we have serialized 
   * earlier. If fields are added/removed from the class, the version 
   * number needs to change.
   */
  private static final long serialVersionUID = -6154967324183669354L;
  
  /**
   * ArrayList for storing the VideoConnectionProperties objects
   */
  protected ArrayList connections;
  
  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  /**
   * Constructor to create a VideoConnectionPropertiesCollection from a simple Collection
   * of VideoConnectionProperties objects.
   */
  public VideoConnectionPropertiesCollection(Collection connections) {
    this.connections = new ArrayList(connections);
  }

  /**
   * Default Constructor creating an empty VideoConnectionPropertiesCollection
   */
  public VideoConnectionPropertiesCollection() {
    this.connections = new ArrayList();
  }

  /**
   * Constructor creating an empty VideoConnectionPropertiesCollection initialised for a 
   * certain number of commandHandles
   */
  public VideoConnectionPropertiesCollection(int size) {
    this.connections = new ArrayList(size);
  }

  /**
   * Get the number of VideoConnectionProperties objects in the list.
   */
  public int size() {
    return this.connections.size();
  }

  /**
   * Get an iterator over the objects contained in the VideoConnectionPropertiesCollection
   */
  public Iterator iterator() {
    return this.connections.iterator();
  }

  /**
   * Remove a VideoConnectionProperties from the collection.
   */
  public boolean remove(VideoConnectionProperties connection) {
    return this.connections.remove(connection);
  }

  /**
   * Remove all VideoConnectionProperties objects in the specified Collection from the
   * VideoConnectionPropertiesCollection.
   */
  public boolean removeAll(Collection connections) {
    return this.connections.removeAll(connections);
  }

  /**
   * Retain all VideoConnectionProperties objects in the specified Collection and remove
   * all the others from the VideoConnectionPropertiesCollection.
   */
  public boolean retainAll(Collection connections) {
    return this.connections.retainAll(connections);
  }

  /**
   * Convert the collection to an array of VideoConnectionProperties objects.
   */
  public VideoConnectionProperties[] toArray() {
    return (VideoConnectionProperties[]) this.connections.toArray(new VideoConnectionProperties[0]);
  }

  /**
   * Check whether the VideoConnectionPropertiesCollection is empty.
   */
  public boolean isEmpty() {
    return this.connections.isEmpty();
  }

  /**
   * Return a hash code for the object.
   */
  public int hashCode() {
    return this.connections.hashCode();
  }

  /**
   * Check whether the VideoConnectionPropertiesCollection contains the specified 
   * VideoConnectionProperties object.
   */
  public boolean contains(VideoConnectionProperties connection) {
    return this.connections.contains(connection);
  }

  /**
   * Add a new VideoConnectionProperties to the collection.
   */
  public boolean add(VideoConnectionProperties connection) {
    return this.connections.add(connection);
  }

  /**
   * Add several VideoConnectionProperties objects to the collection.
   */
  public boolean addAll(Collection connections) {
    return this.connections.addAll(connections);
  }

  /**
   * Remove all VideoConnectionProperties objects from the collection.
   */
  public void clear() {
    this.connections.clear();
  }

  /**
   * Check whether the VideoConnectionPropertiesCollection contains ALL specified 
   * VideoConnectionProperties objects.
   */
  public boolean containsAll(Collection tags) {
    return this.connections.containsAll(tags);
  }

  public static VideoConnectionPropertiesCollection fromJsonResponse(String text) {
    
    return GSON.fromJson(text, VideoConnectionPropertiesCollection.class);
  }

  public String toJson() {
    
    return GSON.toJson(this);
  }
}
