package cern.c2mon.server.eslog.logger;

/**
 * Handles the settings of the indices in ElasticSearch. By default: 10 shards per Month and 0 replicas.
 * TODO: MUST give replica in order to insure consistency in the future.
 * @author Alban Marguet.
 */
public enum IndexMonthSettings {
	SHARDS (10),
	REPLICA (0);

	private final int setting;

	IndexMonthSettings(int setting) {
		this.setting = setting;
	}

	public int getSetting() {
		return setting;
	}
}