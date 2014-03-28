/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * This class holds C2MON configloader's configuration properties
 * 
 * @author wbuczak
 */
@Component("configLoaderConfiguration")
@ManagedResource(objectName = "cern.c2mon.client.configloader:name=C2MonConfigLoaderConfig", description = "C2MON configloader's configuration holder")
public class C2MonConfigLoaderConfig {

    @Value("${cern.c2mon.configloader.db.url}")
    private String dbUrl;

    @Value("${cern.c2mon.configloader.db.get_config_query:#{null}}")
    private String dbGetConfigQuery;

    @Value("${cern.c2mon.configloader.db.update_config_query:#{null}}")
    private String dbUpdateConfigQuery;

    @Value("${cern.c2mon.configloader.db.user:dmntest}")
    private String dbUserName;

    @Value("${cern.c2mon.configloader.db.password:#{null}}")
    private String dbPassword;

    @Value("#{${cern.c2mon.configloader.db.polling_period:900}}")
    private int dbPollingPeriod;

    @Value("${cern.c2mon.configloader.db.config_table:C2MON_CONFIG}")
    private String dbConfigTableName;

    @Value("${cern.c2mon.configloader.db.user:C2MONCONFLOADER}")
    private String loaderUserName;

    @ManagedAttribute
    public String getDbUrl() {
        return dbUrl;
    }

    @ManagedAttribute
    public String getDbUserName() {
        return dbUserName;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    /**
     * @return Returns the dbPollingPeriod.
     */
    @ManagedAttribute(description = "defines how frequently db is polled for configuration changes (in sec)")
    public int getDbPollingPeriod() {
        return dbPollingPeriod;
    }

    /**
     * @return Returns the db "get configurations" query.
     */
    @ManagedAttribute(description = "empty if default query is used")
    public String getDbConfigQuery() {
        return dbGetConfigQuery;
    }

    /**
     * @return Returns the db "update configuration" query.
     */
    @ManagedAttribute(description = "empty if default query is used")
    public String getDbUpdateConfigQuery() {
        return dbUpdateConfigQuery;
    }

    /**
     * @return Returns the dbConfigTableName.
     */
    @ManagedAttribute
    public String getDbConfigTableName() {
        return dbConfigTableName;
    }

    @ManagedAttribute(description = "user name the c2mon config loader identifies itself using this user name when "
            + "applying configurations")
    public String getLoaderUserName() {
        return loaderUserName;
    }

}