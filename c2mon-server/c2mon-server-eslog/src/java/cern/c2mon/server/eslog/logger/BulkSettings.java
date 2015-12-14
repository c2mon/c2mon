package cern.c2mon.server.eslog.logger;

/**
 * Handles the bulk indexing settings.
 * Default is %600 actions before flush OR 5 seconds. We have an interval of 1 second between the flushes and have at
 * most 1 concurrent bulkProcessors.
 * @author Alban Marguet.
 */
public enum BulkSettings {
	BULK_ACTIONS (5600),
	BULK_SIZE (5),
	FLUSH_INTERVAL (1),
	CONCURRENT (1);

	private final int setting;

	BulkSettings(int setting) {
		this.setting = setting;
	}

	public int getSetting() {
		return setting;
	}
}