package cern.c2mon.server.eslog.structure.queries;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alban Marguet.
 */
public class QueryPaginated extends Query {
    protected SearchResponse getResponse() {
        return null;
    }

    public List<String> getListOfAnswer() {
        return null;
    }
}
