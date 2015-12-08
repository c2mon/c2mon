package cern.c2mon.server.eslog.logger;

/**
 * Handles the bulk indexing settings.
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
