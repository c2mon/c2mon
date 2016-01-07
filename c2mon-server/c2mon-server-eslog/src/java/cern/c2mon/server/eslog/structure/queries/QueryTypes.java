package cern.c2mon.server.eslog.structure.queries;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;

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

  public List<String> getListOfAnswer(String index) {
    List<String> result = new ArrayList<>();

    if (index != null) {
      Iterator<ObjectCursor<String>> typesIt = getIndexWithMetadata(index).keys().iterator();
      addTypesToResult(typesIt, result);
      log.info("QueryTypes - Got a list of types, size= " + result.size());
    }
    return result;
  }

  private void addTypesToResult(Iterator<ObjectCursor<String>> typesIt, List<String> result) {
    while(typesIt.hasNext()) {
      result.add(typesIt.next().value);
    }
  }
}