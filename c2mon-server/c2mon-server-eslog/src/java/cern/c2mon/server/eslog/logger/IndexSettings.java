package cern.c2mon.server.eslog.logger;

/**
 * Handles the settings of the indices in ElasticSearch.
 * By default: 10 shards and 0 replica for monthly indices.
 * By default: 1 shards and 0 replica for daily indices.
 * TODO: MUST give replica in order to insure consistency in the future.
 * @author Alban Marguet.
 */
public enum IndexSettings {

  INDEX_MONTH_SETTINGS(10, 0),
  INDEX_DAILY_SETTINGS(1, 0);

  private final int shards;
  private final int replica;

  IndexSettings(int shards, int replica) {
    this.shards = shards;
    this.replica = replica;
  }

  public int getShards() {
    return shards;
  }

  public int getReplica() {
    return replica;
  }

  public int[] getSettings() {
    return new int[]{shards, replica};
  }
}