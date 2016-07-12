/*
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
 */

package cern.c2mon.shared.client.serializer;

import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;

import static cern.c2mon.shared.common.type.TypeConverter.cast;
import static cern.c2mon.shared.common.type.TypeConverter.getType;
import static cern.c2mon.shared.common.type.TypeConverter.isKnownClass;

/**
 * This class is responsible to serialize and deserialize {@link TagValueUpdate}s into json Strings.
 * The serialization is done through the Jackson parser.
 *
 * @author Franz Ritter
 */
@Slf4j
public class TransferTagSerializer {

  private static ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
  }

  public static ObjectMapper getJacksonParser(){
    return mapper;
  }

  /**
   * Converts the TagValue with into a json String. Jackson is used to do this.
   * @param tagUpdate The tag which needs to serialized into a json string.
   * @param <T>
   * @return The json String
   */
  public static <T extends TagValueUpdate> String toJson(T tagUpdate){
    String result = null;

    try {
      result = mapper.writeValueAsString(tagUpdate);
    } catch (JsonProcessingException e) {
      log.error("Serializing of tagUpdate failed: " + e.getMessage());
    }

    return result;
  }

  /**
   *
   * @param tagUpdateJson Converts the json String into a TagValueUpdate instance. By doing this the embedded Tag value will be casted into the given
   *                      class type of the tag.
   * @param dataType The data type of the tag in which the json string shall be serialized.
   * @param <T>
   * @return The instance of the TagValueUpdate with a converted TagValue.
   */
  public static <T extends TagValueUpdate> T fromJson(String tagUpdateJson, Class<T> dataType) {
    T tag = null;

    try {
      tag = mapper.readValue(tagUpdateJson, dataType);
      tag.setValue(convertTagValue(tag));
    } catch (IOException e) {
      log.warn("Error occurred while converting the json string back to an object: "+e.getMessage());
    }

    return tag;
  }

  /**
   *
   * @param tagUpdateJson Converts the json String into a collection of TagValueUpdate instances. By doing this the embedded TagValue of the tags
   *                      will be casted into the given class type of the tag.
   * @param dataType The data type of the tag in which the json string shall be serialized.
   * @param <T>
   * @param <U>
   * @return An instantiated collection of the TagValueUpdates which implements also the ClientRequestResult interface.
   */
  public static <T extends TagValueUpdate, U extends ClientRequestResult> Collection<U> fromCollectionJson(String tagUpdateJson, TypeReference dataType){
    Collection<T> result = null;

    try {
      result = mapper.readValue(tagUpdateJson, dataType);
      for (T resultElement : result) {
        resultElement.setValue(convertTagValue(resultElement));
      }
    } catch (IOException e) {
      log.error("Deserialization of tagUpdate failed: " + e.getMessage());
    }

    return (Collection<U>) result;
  }

  /**
   * Tries to convert the tagValue of an {@link TagValueUpdate} tag. If the value an instantiation of a LinkedHashMap
   * this method tries to instantiate an object of the classType described by the tag value with jackson.
   * Otherwise the type caster from {@link cern.c2mon.shared.common.type.TypeConverter} is used.
   * @param tag The tag with the tag value and the class name information.
   * @return The converted value.
   */
  public static Object convertTagValue(TagValueUpdate tag) {
    Object newTagValue = tag.getValue();

    if (isKnownClass(tag.getValueClassName())
        && getType(tag.getValueClassName()) != String.class
        && newTagValue != null
        && (newTagValue.getClass() == LinkedHashMap.class)
        || isJsonString(newTagValue)) {
      // determine which type the value have and try to convert the value to the correct type:
      if(newTagValue.getClass() == LinkedHashMap.class){
        newTagValue = hashMapToObject((LinkedHashMap) tag.getValue(), getType(tag.getValueClassName()));

      } else {
        newTagValue = stringToObject((String) tag.getValue(), getType(tag.getValueClassName()));
      }

      // if convention fails call the type converter cast method, because this mus be a normal type:
      newTagValue = newTagValue == null ? cast(tag.getValue(), tag.getValueClassName()) : newTagValue;

    } else if (isKnownClass(tag.getValueClassName())) {
      newTagValue = cast(newTagValue, tag.getValueClassName());
    }

    return newTagValue;

  }

  /**
   * takes a HashMap and creates an Object. The HashMap should be created through jackson,
   * so that jackson can deserialize it and instantiate an object.
   *
   * @param map The LinkedHashMap which holds the data for the object.
   * @param type the Type of the created object.
   * @param <T> The instantiation of the map.
   * @return The instantiated object based on the information of the map.
   */
  private static <T> T hashMapToObject(LinkedHashMap map, Class<T> type){
    try {
      return stringToObject(mapper.writeValueAsString(map), type);
    } catch (IOException e) {
      log.warn("Could not create a object of the class "+type.getName()+" out of the map "+map.toString()+" :"+e.getMessage());
      return null;
    }
  }

  /**
   * Takes a String and creates an Object. The string should be in json format,
   * so that jackson can deserialize it and instantiate an object.
   *
   * @param map The json string which holds the data for the object.
   * @param type the Type of the created object.
   * @param <T> The instantiation of the map.
   * @return The instantiated object based on the information of the string.
   */
  private static <T> T stringToObject(String jsonString, Class<T> type){
    try {
      return mapper.readValue(jsonString, type);
    } catch (IOException e) {
      log.warn("Could not create a object of the class"+type.getName()+" out of the value "+jsonString+": "+e.getMessage());
      return null;
    }
  }


  /**
   * Determine if the string is a valid json String
   * @param json String value which needs to be checked if its a json String.
   * @return True if the string is in the json format
   */
  private static boolean isJsonString(final Object json) {
    boolean valid = false;
    try {
      if (json instanceof String) {
        final JsonParser parser = mapper.getFactory().createParser((String) json);
        while (parser.nextToken() != null) {}
        valid = true;
      }
    } catch (IOException ignored) {}
    return valid;
  }
}
