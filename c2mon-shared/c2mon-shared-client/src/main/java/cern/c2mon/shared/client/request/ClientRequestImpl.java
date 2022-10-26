/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.shared.client.request;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.Getter;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.command.*;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.device.*;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.process.ProcessNameResponseImpl;
import cern.c2mon.shared.client.process.ProcessXmlResponse;
import cern.c2mon.shared.client.process.ProcessXmlResponseImpl;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import cern.c2mon.shared.client.statistics.TagStatisticsResponse;
import cern.c2mon.shared.client.statistics.TagStatisticsResponseImpl;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.client.tag.*;
import cern.c2mon.shared.util.json.GsonFactory;

/**
 * This class implements the <code>ClientRequest</code> interface
 * which used as transfer object for requesting information from the
 * C2MON server.
 *
 * @param <T> Specifies the <code>Interface</code> for the result type.
 * @author Matthias Braeger
 * @see #fromJsonResponse(String)
 */
public class ClientRequestImpl<T extends ClientRequestResult> implements ClientRequest, JsonRequest<T> {

  /** Gson instance */
  private static transient Gson gson = null;

  /** The request type */
  @Getter
  private final RequestType requestType;

  /** The expected result type */
  @Getter
  private final ResultType resultType;

  /** Requests from the Client API to the C2MON server have different timeouts depending on their type.
   *  Timeout is set in milliseconds.
   **/
  private int requestTimeout;

  /** List of ids. Please note this field should one day be renamed to ids. */
  @Getter
  private final Collection<Long> tagIds = new HashSet<>();

  @Getter
  private final Collection<Long> tagProcessIds = new HashSet<>();

  @Getter
  private final Collection<Long> tagEquipmentIds = new HashSet<>();

  @Getter
  private final Collection<Long> tagSubEquipmentIds = new HashSet<>();


  /** List of regular expressions, which is e.g. used to search via tag name */
  @Getter
  private final Collection<String> regexList = new HashSet<>();

  /** Request parameter */
  @Getter
  private String requestParameter;

  /** Object parameter. Only used by EXECUTE_COMMAND_REQUEST and DEVICE_REQUEST so far */
  @Getter
  private Object objectParameter;

  
  /**
   * Hidden constructor for Json
   */
  @SuppressWarnings("unused")
  private ClientRequestImpl() {
    requestType = null;
    resultType = null;
  }
  
  /**
   * Default Constructor needs specifying the result type of the response message.
   * The request type and the requestTimeout are then automatically
   * determined by the constructor. The default request timeout is 10 seconds
   * <p/>
   * <b>Please note</b>, that the result type needs to be coherent with the
   * interface type <code>T</code>.
   * @param clazz Return type of the request
   * @see ClientRequest.ResultType
   * @see #fromJsonResponse(String)
   */
  public ClientRequestImpl(final Class<T> clazz) {
    this(clazz, 10_000);
  }


  /**
   * Default Constructor needs specifying the result type of the response message.
   * The request type and the requestTimeout are then automatically
   * determined by the constructor.
   * <p>
   * <b>Please note</b>, that the result type needs to be coherent with the
   * interface type <code>T</code>.
   * @param clazz Return type of the request
   * @param requestTimeout Requests from the Client API to the C2MON server have different 
   *                       timeouts depending on their type. Timeout is set in milliseconds.
   * @see ClientRequest.ResultType
   * @see #fromJsonResponse(String)
   */
  public ClientRequestImpl(final Class<T> clazz, int requestTimeout) {
    if (clazz == TagUpdate.class) {
      this.resultType = ResultType.TRANSFER_TAG_LIST;
      this.requestType = RequestType.TAG_REQUEST;
      this.requestTimeout = requestTimeout;
    } else if (clazz == TagValueUpdate.class) {
      this.resultType = ResultType.TRANSFER_TAG_VALUE_LIST;
      this.requestType = RequestType.TAG_REQUEST;
      this.requestTimeout = requestTimeout;
    } else if (clazz == TagConfig.class) {
      this.resultType = ResultType.TRANSFER_TAG_CONFIGURATION_LIST;
      this.requestType = RequestType.TAG_CONFIGURATION_REQUEST;
      this.requestTimeout = requestTimeout;
    } else if (clazz == AlarmValue.class) {
      this.resultType = ResultType.TRANSFER_ALARM_LIST;
      this.requestType = RequestType.ALARM_REQUEST;
      this.requestTimeout = requestTimeout;
    } else if (clazz == CommandTagHandle.class) {
      this.resultType = ResultType.TRANSFER_COMMAND_HANDLES_LIST;
      this.requestType = RequestType.COMMAND_HANDLE_REQUEST;
      this.requestTimeout = requestTimeout;
    } else if (clazz == CommandReport.class) {
      this.resultType = ResultType.TRANSFER_COMMAND_REPORT;
      this.requestType = RequestType.EXECUTE_COMMAND_REQUEST;
      this.requestTimeout = requestTimeout;
    } else if (clazz == ConfigurationReport.class) {
      this.resultType = ResultType.TRANSFER_CONFIGURATION_REPORT;
      this.requestType = RequestType.APPLY_CONFIGURATION_REQUEST;
      this.requestTimeout = 30 * requestTimeout;
    } else if (clazz == SupervisionEvent.class) {
      this.resultType = ResultType.SUPERVISION_EVENT_LIST;
      this.requestType = RequestType.SUPERVISION_REQUEST;
      this.requestTimeout = requestTimeout;
    } else if (clazz == ProcessXmlResponse.class) {
      this.resultType = ResultType.TRANSFER_DAQ_XML;
      this.requestType = RequestType.DAQ_XML_REQUEST;
      this.requestTimeout = 12 * requestTimeout;
    } else if (clazz == ProcessNameResponse.class) {
      this.resultType = ResultType.TRANSFER_PROCESS_NAMES;
      this.requestType = RequestType.PROCESS_NAMES_REQUEST;
      this.requestTimeout = requestTimeout;
    } else if (clazz == DeviceClassNameResponse.class) {
      this.resultType = ResultType.TRANSFER_DEVICE_CLASS_NAMES;
      this.requestType = RequestType.DEVICE_CLASS_NAMES_REQUEST;
      this.requestTimeout = requestTimeout;
    } else if (clazz == TransferDevice.class) {
      this.resultType = ResultType.TRANSFER_DEVICE_LIST;
      this.requestType = RequestType.DEVICE_REQUEST;
      this.requestTimeout = requestTimeout;
    } else if (clazz == TagStatisticsResponse.class) {
      this.resultType = ResultType.TRANSFER_TAG_STATISTICS;
      this.requestType = RequestType.TAG_STATISTICS_REQUEST;
      this.requestTimeout = requestTimeout;
    } else {
      throw new UnsupportedOperationException(
          "The result type " + clazz + " is not supported by this class.");
    }
  }

  /**
   * This constructor can be used when the ResultType of the request is not
   * enough to define the request type (this occurs when two or more requests
   * can have the same ResultType).
   * @param resultType Result type of the request.
   * @param requestType Return type of the request.
   * @param requestTimeout Request timeout.
   * @see ClientRequest.ResultType
   * @see #fromJsonResponse(String)
   */
  public ClientRequestImpl(final ResultType resultType, final RequestType requestType ,int requestTimeout) {
      this.resultType = resultType;
      this.requestType = requestType;
      this.requestTimeout = requestTimeout;
  }
  
  @Override
  public Collection<Long> getIds() {
    return this.tagIds;
  }

  @Override
  public Collection<Long> getTagProcessIds() {
    return this.tagProcessIds;
  }

  @Override
  public Collection<Long> getTagEquipmentIds() {
    return this.tagEquipmentIds;
  }

  @Override
  public Collection<Long> getTagSubEquipmentIds() {
    return this.tagSubEquipmentIds;
  }

  /**
   * @return The Gson parser singleton instance
   */
  protected static synchronized Gson getGson() {
    if (gson == null) {
      gson = GsonFactory.createGson();
    }

    return gson;
  }


  /**
   * Adds the given tag id to this request.
   * @param tagId A tag id
   * @return <code>true</code>, if the request did not already contain the
   *         specified tag id and if the tag id is bigger than <code>zero</code>.
   */
  public boolean addTagId(final Long tagId) {
    if (tagId != null && tagId > 0) {
      return tagIds.add(tagId);
    }

    return false;
  }

  /**
   * Adds the given process id to this request.
   * @param processId A tag id
   * @return <code>true</code>, if the request did not already contain the
   *         specified tag id and if the tag id is bigger than <code>zero</code>.
   */
  public boolean addTagProcessId(final Long processId) {
    if (processId != null && processId > 0) {
      return tagProcessIds.add(processId);
    }

    return false;
  }

  /**
   * Adds the given equipment id to this request.
   * @param equipmentId A tag id
   * @return <code>true</code>, if the request did not already contain the
   *         specified tag id and if the tag id is bigger than <code>zero</code>.
   */
  public boolean addTagEquipmentId(final Long equipmentId) {
    if (equipmentId != null && equipmentId > 0) {
      return tagEquipmentIds.add(equipmentId);
    }

    return false;
  }

  /**
   * Adds the given sub equipment id to this request.
   * @param subEquipmentId A tag id
   * @return <code>true</code>, if the request did not already contain the
   *         specified tag id and if the tag id is bigger than <code>zero</code>.
   */
  public boolean addTagSubEquipmentId(final Long subEquipmentId) {
    if (subEquipmentId != null && subEquipmentId > 0) {
      return tagSubEquipmentIds.add(subEquipmentId);
    }

    return false;
  }


  /**
   * Adds the given tag ids to this request.
   *
   * <br><br><b>Please note</b>, that the list is only considered for a
   * <code>TAG_REQUEST</code>. When requesting the full actual list of
   * <code>SupervisionEvent</code> objects this list of tag ids is ignored.
   *
   * @param tagIds A list of tag ids
   * @see ClientRequestImpl#addTagId(Long)
   */
  public void addTagIds(final Collection<Long> tagIds) {
    if (tagIds != null) {
      for (Long tagId : tagIds) {
        addTagId(tagId);
      }
    }
  }

  /**
   * Adds (tag) name or a regular search expression
   * @param regex (tag) name or a regular search expression
   * @return <code>true</code>, if the request did not already contain the
   *         specified string.
   */
  public boolean addRegex(final String regex) {
    if (regex != null && !regex.isEmpty()) {
      return regexList.add(regex);
    }

    return false;
  }


  /**
   * Adds the given tag ids to this request.
   *
   * <br><br><b>Please note</b>, that the list is only considered for a
   * <code>TAG_REQUEST</code>. When requesting the full actual list of
   * <code>SupervisionEvent</code> objects this list of tag ids is ignored.
   *
   * @param tagIds A list of tag ids
   * @see ClientRequestImpl#addTagId(Long)
   */
  public void addRegexList(final Collection<String> regexList) {
    if (regexList != null) {
      this.regexList.addAll(regexList);
    }
  }

  /* (non-Javadoc)
   * @see cern.c2mon.shared.client.request.JsonRequest#toJson()
   */
  @Override
  public String toJson() {
    return getGson().toJson(this);
  }

  /**
   * Used to recreate a <code>ClientRequest</code> from a de-serialized <code>ClientRequest</code>
   * <code>objectParameter</code> field. This method is only used on the server side!
   * <br>
   * Please note, that this method supports currently only {@link CommandExecuteRequest} objects.
   * You have to change the implementation in case you want to support other client request objects.
   * @param object The object to put into the client request
   * @return A <code>ClientRequest</code> instance
   * @throws UnsupportedOperationException In case the object is not a supported <code>objectParameter</code>
   */
  public static final ClientRequest fromObject(final Object object) {
    if (object instanceof CommandExecuteRequest) {
      ClientRequestImpl<CommandReport> clientRequest = new ClientRequestImpl<>(CommandReport.class);
      clientRequest.setObjectParameter(object);
      return clientRequest;
    } else if (object instanceof Set) {
      ClientRequestImpl<TransferDevice> clientRequest = new ClientRequestImpl<>(TransferDevice.class);
      clientRequest.setObjectParameter(object);
      return clientRequest;
    }
    throw new UnsupportedOperationException("The object type is not supported by this class.");
  }

  /**
   * Factory method to deserialize from json back to {@link ClientRequest}
   * @param json Json string representation of a <code>TransferTagRequestImpl</code> class
   * @return The deserialized Json message
   * @throws JsonSyntaxException This exception is raised when Gson attempts to read
   *                             (or write) a malformed JSON element.
   */
  public static final ClientRequest fromJson(final String json) {
    JsonReader jsonReader = new JsonReader(new StringReader(json));
    jsonReader.setLenient(true);
    return getGson().fromJson(jsonReader, ClientRequestImpl.class);
  }


  /* (non-Javadoc)
   * @see cern.c2mon.shared.client.request.JsonRequest#fromJsonResponse(java.lang.String)
   */
  @Override
  public final Collection<T> fromJsonResponse(final String jsonString) throws JsonSyntaxException {
    Type collectionType;
    TypeReference jacksonCollectionType;
    JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
    jsonReader.setLenient(true);

    try {
      switch (resultType) {
        case TRANSFER_TAG_LIST:
          jacksonCollectionType =  new TypeReference<Collection<TransferTagImpl>>() { };
          return TransferTagSerializer.fromCollectionJson(jsonString, jacksonCollectionType);
        case TRANSFER_TAG_VALUE_LIST:
          jacksonCollectionType =  new TypeReference<Collection<TransferTagValueImpl>>() { };
          return TransferTagSerializer.fromCollectionJson(jsonString, jacksonCollectionType);
        case SUPERVISION_EVENT_LIST:
          collectionType = new TypeToken<Collection<SupervisionEventImpl>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_DAQ_XML:
          collectionType = new TypeToken<Collection<ProcessXmlResponseImpl>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_TAG_CONFIGURATION_LIST:
          collectionType = new TypeToken<Collection<TagConfigImpl>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_CONFIGURATION_REPORT_HEADER:
          collectionType = new TypeToken<Collection<ConfigurationReportHeader>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_CONFIGURATION_REPORT:
          collectionType = new TypeToken<Collection<ConfigurationReport>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_ACTIVE_ALARM_LIST:
          collectionType = new TypeToken<Collection<AlarmValueImpl>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_ALARM_LIST:
          collectionType = new TypeToken<Collection<AlarmValueImpl>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_COMMAND_HANDLES_LIST:
          collectionType = new TypeToken<Collection<CommandTagHandleImpl>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_COMMAND_REPORT:
          collectionType = new TypeToken<Collection<CommandReportImpl>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_PROCESS_NAMES:
          collectionType = new TypeToken<Collection<ProcessNameResponseImpl>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_DEVICE_CLASS_NAMES:
          collectionType = new TypeToken<Collection<DeviceClassNameResponseImpl>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_DEVICE_LIST:
          collectionType = new TypeToken<Collection<TransferDeviceImpl>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        case TRANSFER_TAG_STATISTICS:
          collectionType = new TypeToken<Collection<TagStatisticsResponseImpl>>() { } .getType();
          return getGson().fromJson(jsonReader, collectionType);
        default:
          throw new JsonSyntaxException("Unknown result type specified");
      }
    } finally  {
      try { jsonReader.close(); } catch (IOException e) {}
    }
  }

  /**
   * Only supported by DAQ_XML_REQUEST, DEVICE_REQUEST and RETRIEVE_CONFIGURATION_REQUEST
   * @param requestParameter the requestParameter to set
   */
  public void setRequestParameter(final String requestParameter) {
    if (!requestType.equals(RequestType.DAQ_XML_REQUEST) && !requestType.equals(RequestType.DEVICE_REQUEST)
        && !requestType.equals(RequestType.RETRIEVE_CONFIGURATION_REQUEST)) {
      throw new UnsupportedOperationException("This method is not supported by requests of type " + requestType);
    }
    this.requestParameter = requestParameter;
  }

  /**
   * Only supported by EXECUTE_COMMAND_REQUESTS and DEVICE_REQUEST so far.
   *
   * @param objectParameter the Object to set. In case of the
   *          EXECUTE_COMMAND_REQUEST this is a {@link CommandExecuteRequest}.
   *          In case of a DEVICE_REQUEST, it is a set of {@link DeviceInfo}
   *          objects.
   */
  public void setObjectParameter(final Object objectParameter) {
    if (!requestType.equals(RequestType.EXECUTE_COMMAND_REQUEST) && !requestType.equals(RequestType.DEVICE_REQUEST)) {
      throw new UnsupportedOperationException(
          "This method is not supported by requests of type " + requestType);
    }
    else if (requestType.equals(RequestType.EXECUTE_COMMAND_REQUEST)
             && !(objectParameter instanceof CommandExecuteRequest)) {
      throw new UnsupportedOperationException(
          "The request type " + requestType + " does not support object parameter of class " + objectParameter.getClass().getName());
    }
    else if (requestType.equals(RequestType.DEVICE_REQUEST) && !(objectParameter instanceof Set)) {
      throw new UnsupportedOperationException(
          "The request type " + requestType + " does not support object parameter of class " + objectParameter.getClass().getName());
    }

    this.objectParameter = objectParameter;
  }

  @Override
  public boolean isObjectRequest() {
    // command handles are sent back as Objects
    // (in contrast with other responses which are sent as JsonTextMessages)
    return getRequestType() == RequestType.EXECUTE_COMMAND_REQUEST || (getRequestType() == RequestType.DEVICE_REQUEST && objectParameter != null);
  }

  @Override
  public boolean requiresObjectResponse() {
    // command handles are sent back as Objects
    // (in contrast with other responses which are sent as JsonTextMessages)
    return getRequestType() == RequestType.COMMAND_HANDLE_REQUEST;
  }

  @Override
  public int getTimeout() {
    return requestTimeout;
  }
}
