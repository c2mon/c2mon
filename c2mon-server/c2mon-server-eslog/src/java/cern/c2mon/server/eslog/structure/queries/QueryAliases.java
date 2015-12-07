package cern.c2mon.server.eslog.structure.queries;

/**
 * @author Alban Marguet.
 */
public class QueryAliases extends Query {
    public void addAlias(String indexMonth, String aliasName) {
        client.admin().indices().prepareAliases().addAlias(indexMonth, aliasName)
                .execute().actionGet();
    }
}
