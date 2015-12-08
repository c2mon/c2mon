package cern.c2mon.server.eslog.structure.queries;

import org.elasticsearch.client.Client;

import java.util.HashMap;

/**
 * @author Alban Marguet.
 */
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


    public Query(Client client, String[] indices, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
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
    }

    /**
     * Utility method to convert the long[] to String[].
     * @param tagIds used for the routing.
     * @return tagIds as String for the query.
     */
    protected String[] getRouting(long[] tagIds) {
        String[] routing = new String[tagIds.length];
        for (int i = 0; i < tagIds.length; i++) {
            routing[i] = String.valueOf(tagIds[i]);
        }
        return routing;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public long[] tagIds() {
        return (long[]) parameters.get(TAG_IDS);
    }

    public String[] indices() {
        return (String[]) parameters.get(INDICES);
    }

    public String[] types() {
        return (String[]) parameters.get(TYPES);
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
