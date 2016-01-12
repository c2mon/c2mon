package cern.c2mon.server.eslog.structure.queries;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Query to be launched against ElasticSearch to retrieve all the indices present in the cluster.
 * @author Alban Marguet.
 */
@Slf4j
public class QueryIndices extends Query {
  List<String> indices;

  public QueryIndices(Client client) {
    super(client);
    indices = new ArrayList<>();
  }

  /**
   * Simple query to get all the indices in the cluster.
   * @return List<String>: names of the indices.
   */
  public List<String> getListOfAnswer() {
    if (client != null) {
      String[] indicesFromCluster = getIndicesFromCluster();
      log.debug("getListOfAnswer() - got a list of indices, size=" + indicesFromCluster.length);
      return Arrays.asList(indicesFromCluster);
    }
    else {
      log.warn("getListOFAnswer() - Warning: client has value " + client + ".");
      return new ArrayList<>();
    }
  }

  public boolean initTest() {
    try {
      indices = getListOfAnswer();
      displayIndices(indices);
      return true;
    }
    catch(NoNodeAvailableException e) {
      log.error("initTest() - Error while creating client, could not find a connection to the ElasticSearch cluster, is it running?");
      return false;
    }
  }

  private void displayIndices(List<String> indices) {
    log.debug("displayIndices() - Indices present in the cluster:");
    for (String s : indices) {
      log.info(s);
    }
  }
}