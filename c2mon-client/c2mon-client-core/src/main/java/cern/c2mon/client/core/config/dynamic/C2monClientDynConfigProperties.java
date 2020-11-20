package cern.c2mon.client.core.config.dynamic;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Configuration class of the DynConfig module. Specifies the processes and equipments that a Tag will be created for
 * if none exists.
 */
@Data
@ConfigurationProperties(prefix = "c2mon.client.dynconfig")
public class C2monClientDynConfigProperties {
    /**
     * A list of {@link ProcessEquipmentURIMapping} object which map a regular expression to a specific C2MON Process and Equipment.
     */
    private List<ProcessEquipmentURIMapping> mappings = new ArrayList<>();

    @Data
    public static class ProcessEquipmentURIMapping {
        /**
         * The ID of the C2MON process - only used when creating a new process must be created
         */
        private Long processId;

        /**
         * The name of the C2MON process. If a process of the given name is already running, a tag will be created for that
         * process even if they have different IDs.
         */
        private String processName;

        /**
         * The name of the C2MON equipment
         */
        private String equipmentName;

        /**
         * The description of the C2MON process
         */
        private String processDescription = "DynConfig Process";

        /**
         * The description of the C2MON equipment
         */
        private String equipmentDescription;

        /**
         * The regular expression associated to the process and equipment
         */
        private String uriPattern;

        /**
         * Optional metadata for the process and equipment.
         */
        private Map<String, String> metadata;
    }
}
