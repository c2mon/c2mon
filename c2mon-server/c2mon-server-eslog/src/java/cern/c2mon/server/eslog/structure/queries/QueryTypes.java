package cern.c2mon.server.eslog.structure.queries;

import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Alban Marguet.
 */
public class QueryTypes extends Query {
    public QueryTypes(Client client, String[] indices, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
        super(client, indices, isTypeDefined, types, tagIds, from, size, min, max);
    }

    public List<String> getListOfAnswer() {
        List<String> result = new ArrayList<>();
        Set<String> set = client.admin().cluster().prepareState().execute().actionGet().
                getState().getMetaData().getAliasAndIndexLookup().keySet();
        result.addAll(set);
        return result;
    }
}
