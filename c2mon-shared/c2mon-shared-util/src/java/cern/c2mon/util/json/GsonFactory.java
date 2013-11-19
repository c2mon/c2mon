package cern.c2mon.util.json;

import java.lang.reflect.Type;
import java.sql.Timestamp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Factory class for creating a customized GSON builder that is used for
 * serializing/deserializing C2MON client messages. 
 *
 * @author Matthias Braeger
 */
public abstract class GsonFactory {
  
  /**
   * Hidden default constructor
   */
  private GsonFactory() {
    // Do nothing
  }
  
  /**
   * Creates a Gson Builder that converts by default a time stamp to a long
   * value. Furthermore, in case of an <code>Object</code> field it does not
   * try to deserialize the object but stores instead the Json String to that
   * field. The deserialization has then to be handled in an extra step once the
   * final Object class information is available. 
   * @return The Gson Builder to serialize/deserialize Json messages
   */
  public static GsonBuilder createGsonBuilder() {
    GsonBuilder gsonBuilder = new GsonBuilder()
      .registerTypeAdapter(Timestamp.class, new TimestampSerializer())
      .registerTypeAdapter(Timestamp.class, new TimestampDeserializer())
      .registerTypeAdapter(Object.class, new TagValueDeserializer());
    
    return gsonBuilder;
  }
  
  
  /**
   * Creates a Gson instance that converts by default a time stamp to a long
   * value. Furthermore, in case of an <code>Object</code> field it does not
   * try to deserialize the object but stores instead the Json String to that
   * field. The deserialization has then to be handled in an extra step once the
   * final Object class information is available.
   * @return An instance of Gson configured with the options currently set in this builder
   * @see #createGsonBuilder()
   */
  public static final Gson createGson() {    
    return createGsonBuilder().create();
  }
  
  
  /**
   * This private class is used by Gson to serialize correctly the Timestamp objects
   *
   * @author Matthias Braeger
   */
  private static final class TimestampSerializer implements JsonSerializer<Timestamp> {
    @Override
    public JsonElement serialize(final Timestamp src, final Type typeOfSrc, final JsonSerializationContext context) {
      return new JsonPrimitive(src.getTime());
    }
  }


  /**
   * This private class is used by Gson to deserialize correctly the Timestamp objects
   *
   * @author Matthias Braeger
   */
  private static final class TimestampDeserializer implements JsonDeserializer<Timestamp> {
    @Override
    public Timestamp deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
        throws JsonParseException {
      return new Timestamp(json.getAsJsonPrimitive().getAsLong());
    }
  }
  
  
  /**
   * This private class assures that the <code>tagValue</code> Json represenetation
   * is not deserialized by Json. This is not possible at this stage since the Object
   * type information gets lost while serializing. But we stored it in the
   * <code>valueClassName</code> variable and deserialize then later in the 
   * <code>getValue()</code> method.
   *
   * @author Matthias Braeger
   * @see TransferTagValueImpl#getValue()
   */
  private static final class TagValueDeserializer implements JsonDeserializer<String> {
    @Override
    public String deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
        throws JsonParseException {
      return json.toString().trim();
    }
  }
}
