package cern.c2mon.server.eslog.structure.queries;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Alban Marguet.
 */
@Slf4j
public abstract class Query {
  protected Client client;
  protected HashMap<String, Object> parameters;
  protected final String TAG_IDS = "tagIds";
  protected final String TYPES = "types";
  protected final String INDICES = "indices";
  protected final String IS_TYPE_DEFINED = "isTypeDefined";
  protected final String FROM = "from";
  protected final String SIZE = "size";
  protected final String MIN = "min";
  protected final String MAX = "max";
  @Getter
  protected boolean parametersSet = false;

  public Query(Client client, List<String> indices, boolean isTypeDefined, List<String> types, List<Long> tagIds, int from, int size, int min, int max) {
    setClient(client);
    parameters = new HashMap<>();
    parameters.put(INDICES, indices);
    parameters.put(IS_TYPE_DEFINED, isTypeDefined);
    parameters.put(TYPES, types);
    parameters.put(TAG_IDS, tagIds);
    parameters.put(FROM, from);
    parameters.put(SIZE, size);
    parameters.put(MIN, min);
    parameters.put(MAX, max);
    parametersSet = true;
  }

  public Query(Client client) {
    setClient(client);
    parameters = new HashMap<>();
    parameters.put(INDICES, new ArrayList<String>());
    parameters.put(TYPES, new ArrayList<String>());
    parameters.put(TAG_IDS, new ArrayList<Long>());
  }

  /**
   * Utility method to convert the long[] to String[].
   * @param tagIds used for the routing.
   * @return tagIds as String for the query.
   */
  protected String[] getRouting(long[] tagIds) {
    if (tagIds != null) {
      String[] routing = new String[tagIds.length];

      for (int i = 0; i < tagIds.length; i++) {
        routing[i] = String.valueOf(tagIds[i]);
      }

      return routing;

    } else {
      return null;
    }
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public Client getClient() {
    return client;
  }

  public long[] tagIds() {
    if (parameters.get(TAG_IDS) != null) {
      List<Long> tagIds = new ArrayList<>();
      tagIds.addAll((List<Long>) parameters.get(TAG_IDS));
      long[] result = new long[tagIds.size()];

      for (int i = 0; i < tagIds.size(); i++) {
        result[i] = tagIds.get(i).longValue();
      }

      return result;

    } else {
      return null;
    }
  }

  public String[] indices() {
    List<String> indices = new ArrayList<>();
    indices.addAll((List<String>)parameters.get(INDICES));
    String[] result = new String[indices.size()];

    for (int i = 0; i < indices.size(); i++) {
      result[i] = indices.get(i);
    }

    return result;
  }

  public String[] types() {
    return (String[])((ArrayList<String>)parameters.get(TYPES)).toArray();
  }

  public boolean isTypeDefined() {
    return (boolean) parameters.get(IS_TYPE_DEFINED);
  }

  public int from() {
    return (int) parameters.get(FROM);
  }

  public int size() {
    return (int) parameters.get(SIZE);
  }

  public int min() {
    return (int) parameters.get(MIN);
  }

  public int max() {
    return (int) parameters.get(MAX);
  }
}