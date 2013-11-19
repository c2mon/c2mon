package cern.c2mon.shared.client.request;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.process.ProcessNameResponseImpl;
import cern.c2mon.shared.client.process.ProcessXmlResponse;
import cern.c2mon.shared.client.process.ProcessXmlResponseImpl;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.client.tag.TagConfigImpl;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.c2mon.shared.client.command.CommandExecuteRequest;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandReportImpl;
import cern.c2mon.shared.client.command.CommandTagHandle;
import cern.c2mon.shared.client.command.CommandTagHandleImpl;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.util.json.GsonFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

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
  @NotNull
  private final RequestType requestType;

  /** The expected result type */
  @NotNull
  private final ResultType resultType; 
  
  /** Requests from the Client API to the C2MON server have different timeouts depending on their type. 
   *  Timeout is set in milliseconds.
   **/
  private int requestTimeout;

  /** List of ids */
  @Size(min = 0)
  private final Collection<Long> tagIds = new HashSet<Long>();

  /** Request parameter */
  private String requestParameter; 
  
  /** Object parameter. Only used by EXECUTE_COMMAND_REQUEST so far */
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
   * determined by the constructor.
   * <br><br><b>Please note</b>, that the result type needs to be coherent with the
   * interface type <code>T</code>.
   * @param clazz Return type of the request 
   * @see ClientRequest.ResultType
   * @see #fromJsonResponse(String)
   */
  public ClientRequestImpl(final Class<T> clazz) {
    if (clazz == TagUpdate.class) {
      resultType = ResultType.TRANSFER_TAG_LIST;
      requestType = RequestType.TAG_REQUEST;
      requestTimeout = 10000;
    }
    else if (clazz == TagValueUpdate.class) {
      resultType = ResultType.TRANSFER_TAG_VALUE_LIST;
      requestType = RequestType.TAG_REQUEST;
      requestTimeout = 10000;
    }
    else if (clazz == TagConfig.class) { 
      resultType = ResultType.TRANSFER_TAG_CONFIGURATION_LIST;
      requestType = RequestType.TAG_CONFIGURATION_REQUEST;
      requestTimeout = 10000;
    }
    else if (clazz == AlarmValue.class) { 
      resultType = ResultType.TRANSFER_ALARM_LIST;
      requestType = RequestType.ALARM_REQUEST;
      requestTimeout = 10000;
    }
    else if (clazz == CommandTagHandle.class) {
      resultType = ResultType.TRANSFER_COMMAND_HANDLES_LIST;
      requestType = RequestType.COMMAND_HANDLE_REQUEST;
      requestTimeout = 10000;
    }    
    else if (clazz == CommandReport.class) {
      resultType = ResultType.TRANSFER_COMMAND_REPORT;
      requestType = RequestType.EXECUTE_COMMAND_REQUEST;
      requestTimeout = 10000;
    }        
    else if (clazz == ConfigurationReport.class) { 
      resultType = ResultType.TRANSFER_CONFIGURATION_REPORT;
      requestType = RequestType.APPLY_CONFIGURATION_REQUEST;
      requestTimeout = 120000;
    }
    else if (clazz == SupervisionEvent.class) {
      resultType = ResultType.SUPERVISION_EVENT_LIST;
      requestType = RequestType.SUPERVISION_REQUEST;
      requestTimeout = 10000;
    }
    else if (clazz == ProcessXmlResponse.class) {
      resultType = ResultType.TRANSFER_DAQ_XML;
      requestType = RequestType.DAQ_XML_REQUEST;
      requestTimeout = 120000;
    }
    else if (clazz == ProcessNameResponse.class) {
      resultType = ResultType.TRANSFER_PROCESS_NAMES;
      requestType = RequestType.PROCESS_NAMES_REQUEST;
      requestTimeout = 10000;
    }
    else {
      throw new UnsupportedOperationException(
          "The result type " + clazz + " is not supported by this class.");
    }
  }
  
  /**
   * This constructor can be used when the ResultType of the request is not
   * enough to define the request type (this occurs when two or more requests
   * can have the same ResultType).
   * @param pResultType Result type of the request.
   * @param pRequestType Return type of the request.
   * @param pTimeout Request timeout. 
   * @see ClientRequest.ResultType
   * @see #fromJsonResponse(String)
   */
  public ClientRequestImpl(final ResultType pResultType, final RequestType pRequestType
      ,int pTimeout) {
    
      resultType = pResultType;
      requestType = pRequestType;
      requestTimeout = pTimeout;
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
    if (requestType.equals(RequestType.SUPERVISION_REQUEST)) {
      throw new UnsupportedOperationException(
      "This method is not supported by a SUPERVISION_REQUEST");
    }

    if (tagId != null && tagId > 0) {
      return tagIds.add(tagId);
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


  /* (non-Javadoc)
   * @see cern.c2mon.shared.client.tag.TransferTagRequest#getTagIds()
   */
  @Override
  public Collection<Long> getTagIds() {
    return tagIds;
  }


  /**
   * This method returns the type of the client request
   * @return The type of request
   * @see RequestType
   */
  @Override
  public RequestType getRequestType() {
    return requestType;
  }


  /**
   * @return The expected <code>ResultType</code> of the response message
   * @see ResultType
   */
  @Override
  public ResultType getResultType() {
    return resultType;
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
  @SuppressWarnings("unchecked")
  public static final ClientRequest fromObject(final Object object) {
    if (object instanceof CommandExecuteRequest) {
      ClientRequestImpl<CommandReport> clientRequest = new ClientRequestImpl<CommandReport>(CommandReport.class);
      clientRequest.setObjectParameter(object);
      return clientRequest;
    }
    throw new UnsupportedOperationException("The object type is not supported by this class.");
  }

  /**
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
    JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
    jsonReader.setLenient(true);
    
    switch (resultType) {
      case TRANSFER_TAG_LIST:
        collectionType = new TypeToken<Collection<TransferTagImpl>>() { } .getType();
        return TransferTagImpl.getGson().fromJson(jsonReader, collectionType);
      case TRANSFER_TAG_VALUE_LIST:
        collectionType = new TypeToken<Collection<TransferTagValueImpl>>() { } .getType();
        return TransferTagValueImpl.getGson().fromJson(jsonReader, collectionType);
      case SUPERVISION_EVENT_LIST:
        collectionType = new TypeToken<Collection<SupervisionEventImpl>>() { } .getType();
        return getGson().fromJson(jsonReader, collectionType);
      case TRANSFER_DAQ_XML:
        collectionType = new TypeToken<Collection<ProcessXmlResponseImpl>>() { } .getType();
        return getGson().fromJson(jsonReader, collectionType);
      case TRANSFER_TAG_CONFIGURATION_LIST:
        collectionType = new TypeToken<Collection<TagConfigImpl>>() { } .getType();
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
      default:
        throw new JsonSyntaxException("Unknown result type specified");
    }
  }


  /**
   * @return the request parameter
   */
  @Override
  public String getRequestParameter() {
    return requestParameter;
  }


  /**
   * Only supported by DAQ_XML_REQUESTS so far.
   * @param requestParameter the requestParameter to set
   */
  public void setRequestParameter(final String requestParameter) {
    if (!requestType.equals(RequestType.DAQ_XML_REQUEST)) {
      throw new UnsupportedOperationException(
          "This method is not supported by requests of type " + requestType);
    }
    this.requestParameter = requestParameter;
  }
  
  /**
   * Only supported by EXECUTE_COMMAND_REQUESTS so far.
   * @param objectParameter the Object to set. 
   * In case of the EXECUTE_COMMAND_REQUEST this is a {@link CommandExecuteRequest}.
   */
  public void setObjectParameter(final Object objectParameter) {
    if (!requestType.equals(RequestType.EXECUTE_COMMAND_REQUEST)) {
      throw new UnsupportedOperationException(
          "This method is not supported by requests of type " + requestType);
    }
    else if (requestType.equals(RequestType.EXECUTE_COMMAND_REQUEST) 
             && !(objectParameter instanceof CommandExecuteRequest)) {
      throw new UnsupportedOperationException(
          "The request type " + requestType + " does not support object parameter of class " + objectParameter.getClass().getName());
    }
    
    this.objectParameter = objectParameter;
  }

  @Override
  public Object getObjectParameter() {
    
    return objectParameter;
  }

  @Override
  public boolean isObjectRequest() {
    // command handles are sent back as Objects
    // (in contrast with other responses which are sent as JsonTextMessages)
    return getRequestType() == RequestType.EXECUTE_COMMAND_REQUEST;
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
