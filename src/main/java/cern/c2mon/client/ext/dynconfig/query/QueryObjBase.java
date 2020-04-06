package cern.c2mon.client.ext.dynconfig.query;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import com.google.common.base.Splitter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.elasticsearch.common.util.ArrayUtils;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cern.c2mon.client.ext.dynconfig.DynConfigException.Context.INVALID_DATA_TYPE;
import static cern.c2mon.client.ext.dynconfig.DynConfigException.Context.URI_MISSING_REQUIRED_PROPERTIES;

/**
 * The abstract base holding information and logic common to all query object implementations.
 */
@Getter
public abstract class QueryObjBase implements IQueryObj {

    /**
     * The Keys enum stores the values to the standard syntax in the URI both for common and protocol-specific fields.
     */
    @AllArgsConstructor
    protected enum Keys {
        URI("uri"),
        ITEM_NAME("itemName"),
        DATA_TYPE("dataType"),
        TAG_NAME("tagName"),
        TAG_DESCRIPTION("tagDescription"),
        NAMESPACE("namespace"),
        COMMAND_TYPE("commandType"),
        COMMAND_PULSE("commandPulseLength");
        String propertyName;

    }
    static final String[] requiredProperties = {Keys.ITEM_NAME.propertyName};

    protected Map<String, String> properties;
    protected String tagName;
    protected String tagDescription;
    protected String itemName;
    protected Class<?> dataType;


    /**
     * checks whether the regular expression matches itemName, the only relevant field in the base.
     * @param regex the regular expression to check for
     * @return true if the regular expression matches the values of any of the relevant fields.
     */
    public boolean matches(String regex) {
        return itemName.matches(regex);
    }

    /**
     * At their base, query objects are considered equal if they are of the same type and their fields hold the same values.
     * @param o the object to compare to
     * @return true if the objects are of the same type and their fields hold the same values.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryObjBase that = (QueryObjBase) o;
        return Objects.equals(tagName, that.tagName) &&
                Objects.equals(tagDescription, that.tagDescription) &&
                Objects.equals(itemName, that.itemName) &&
                Objects.equals(dataType.getName(), that.dataType.getName());
    }

    /**
     * Override the hashCode method to fit the custom equals implementation
     * @return the hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(tagName, tagDescription, itemName, dataType.getName());
    }

    protected QueryObjBase(URI uri) throws DynConfigException {
        Map<String, String> propertyMap = splitQueryToMap(uri);
        checkRequiredProperties(propertyMap.keySet());
        this.itemName = propertyMap.getOrDefault(Keys.ITEM_NAME.propertyName, "");
        this.tagName = propertyMap.getOrDefault(Keys.TAG_NAME.propertyName, this.itemName);
        this.tagDescription = propertyMap.getOrDefault(Keys.TAG_DESCRIPTION.propertyName, "autoconfigured Tag");
        this.dataType = getDataTypeFromProperties(propertyMap);
        this.properties = propertyMap;
    }

    protected abstract String[] getRequiredPropertiesForProtocol();

    private Class<?> getDataTypeFromProperties(Map<String, String> properties) throws DynConfigException {
        String classString = properties.getOrDefault(Keys.DATA_TYPE.propertyName, "java.lang.Object");
        try {
            return Class.forName(classString);
        } catch (ClassNotFoundException e) {
            throw new DynConfigException(INVALID_DATA_TYPE, classString + " could not be cast to a class.", e);
        }
    }

    private void checkRequiredProperties(Set<String> keys) throws DynConfigException {
        for (String s : ArrayUtils.concat(requiredProperties, getRequiredPropertiesForProtocol())) {
            if (!keys.contains(s)) {
                throw new DynConfigException(URI_MISSING_REQUIRED_PROPERTIES, s);
            }
        }
    }

    protected static Map<String, String> splitQueryToMap(URI uri) {
        String query = uri.getQuery();
        return Splitter.onPattern("[?&]").trimResults().withKeyValueSeparator('=').split(query);
    }

    protected static String getSchemeAuthorityAndPath(URI uri) {
        return uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
    }

}
