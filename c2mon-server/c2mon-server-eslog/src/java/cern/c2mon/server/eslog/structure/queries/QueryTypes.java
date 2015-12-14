package cern.c2mon.server.eslog.structure.queries;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Alban Marguet.
 */
@Slf4j
public class QueryTypes extends Query {
	public QueryTypes(Client client) {
		super(client);
	}

	public QueryTypes(Client client, List<String> indices, boolean isTypeDefined, List<String> types, List<Long> tagIds, int from, int size, int min, int max) {
		super(client, indices, isTypeDefined, types, tagIds, from, size, min, max);
	}

	public List<String> getListOfAnswer() {
		List<String> result = new ArrayList<>();
		Iterator<ObjectCursor<IndexMetaData>> indicesIt = client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().indices().values().iterator();

		while(indicesIt.hasNext()) {
			Iterator<ObjectCursor<MappingMetaData>> mappings = indicesIt.next().value.getMappings().values().iterator();

			while(mappings.hasNext()) {
				result.add(mappings.next().value.type());
			}
		}

		return result;
	}
}