package cern.c2mon.client.config.shell;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.config.dynamic.DynConfigException;
import cern.c2mon.client.core.config.dynamic.DynConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Exposes methods to dynamically configure the C2MON server at runtime through the Spring Shell, JMX, or HTTP.
 */
@Slf4j
@Component
@ShellComponent
@ManagedResource(objectName = "cern.c2mon:type=Config,name=ClientConfigurationShell")
public class DynConfigServiceConnector {

    @Autowired
    private DynConfigService dynConfigService;

    /**
     * Delete the C2MON tag corresponding to a given URI if it exists.
     * @param uri the uri describing the address for which the corresponding C2MON tag shall be deleted
     */
    @ShellMethod(value = "Deletes a DataTag corresponding to the given URI.", key = "delete-tag")
    @ManagedOperation(description = "Deletes a DataTag corresponding to the given URI.")
    @ManagedOperationParameter(name = "uri", description = "A URI describing the DataTag to be deleted in the form: scheme://host[:port][/path][optionalAttribute=value].")
    public String deleteTagForURI(String uri) {
        try {
            if (dynConfigService.deleteTagForURI(URI.create(uri))) {
                return "Successfully deleted the Tag at " + uri;
            } else {
                return "No tag could be found at " + uri + ".";
            }
        } catch (DynConfigException e) {
            return "An exception occurred during the operation: " + e.getMessage();
        }
    }

    /**
     * For a set of URIs, query the corresponding tag, or create the tags if not found. If a Tag with a given itemName
     * already exists, the Tag the existing Tag is returned unchanged and no properties are overwritten.
     * @param uris the uris describing the address for which the corresponding C2MON tag shall be fetched or deleted
     * @return A C2MON tag that can be used to subscribe to data.
     */
    @ShellMethod(value = "Creates or displays a DataTag corresponding to the given URI.", key = "get-tags")
    @ManagedOperation(description = "Create or display one or more DataTags corresponding to the given URI.")
    @ManagedOperationParameter(name = "uris", description = "A URI describing the DataTag to be created in the form: scheme://host[:port][/path][optionalAttribute=value]. Multiple URIs can be given using a semicolons a separator.")
    public String getTagsForURI(String uris) {
        return Arrays.stream(uris.split(";"))
                .map(uri -> {
                    try {
                        Tag tag = dynConfigService.getTagForURI(URI.create(uri));
                        return tag.toString();
                    } catch (DynConfigException e) {
                        log.info("Failed", e);
                        return "Could not fetch or create a Tag at " + uri + ": \n" + e;
                    }
                }).collect(Collectors.joining("\n"));
    }
}
