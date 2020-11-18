package cern.c2mon.client.ext.dynconfig.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * A class associating a regular expression with exactly one C2MON Process and Equipment.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessEquipmentURIMapping {
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
