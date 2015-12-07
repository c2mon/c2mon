package cern.c2mon.server.eslog.logger;

/**
 * Handles the settings of the indices in ElasticSearch.
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
