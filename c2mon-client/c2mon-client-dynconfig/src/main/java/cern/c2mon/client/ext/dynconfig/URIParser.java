package cern.c2mon.client.ext.dynconfig;

import cern.c2mon.client.ext.dynconfig.strategy.TagConfigStrategy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cern.c2mon.client.ext.dynconfig.strategy.TagConfigStrategy.*;

/**
 * Utility class to extract information from an URI in a structured form.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class URIParser {

    /**
     * A utility method that extract a value tagName from an URI if given as a query parameter
     * @param uri the uri to extract the tagName from
     * @return the tagName value if the uri contains a corresponding query parameter. Otherwise, the full uri is
     * returned.
     */
    public static String toTagName(URI uri) {
        if (uri.getQuery() != null && uri.getQuery().contains(TAG_NAME.getKeyName() + "=")) {
            return StringUtils.join(splitQuery(uri).get(TAG_NAME.getKeyName()), ", ");
        }
        return StringUtils.left(uri.toASCIIString(), MAX_COMMAND_TAG_NAME_LENGTH);
    }

    /**
     * A utility method that extracts the type of the Tag to be processed from an URI. Possible types are listed in
     * {@link cern.c2mon.client.ext.dynconfig.strategy.TagConfigStrategy.TagType}.
     * @param uri the uri to extract the tagType from
     * @return the type of the Tag to be processed. If no type is specified, {@link cern.c2mon.shared.client.configuration.api.tag.DataTag}
     * is used as default.
     */
    public static TagConfigStrategy.TagType getTagType(URI uri) {
        if (uri.getQuery() != null && uri.getQuery().contains(TAG_TYPE.getKeyName() + "=")) {
            final Optional<TagConfigStrategy.TagType> tagType = splitQuery(uri).get(TAG_TYPE.getKeyName()).stream()
                    .flatMap(tagValue -> Arrays.stream(TagConfigStrategy.TagType.values())
                            .filter(t -> tagValue.equalsIgnoreCase(t.name())))
                    .findFirst();
            if (tagType.isPresent()) {
                return tagType.get();
            }
        }
        return TagConfigStrategy.TagType.DATA;
    }

    /**
     * Splits the query parameters of a URI into a map of the query key String and a list of values.
     * @param uri the uri whose query parameters shall be split into a map.
     * @return A map of the uri's query parameters. Query values will be returned as list to support multiple values.
     */
    public static Map<String, List<String>> splitQuery(URI uri) {
        Map<String, List<String>> queryPairs = new ConcurrentHashMap<>();
        if (uri.getQuery() != null) {
            for (String queryEntry : uri.getQuery().split("[&?#]")) {
                AbstractMap.SimpleImmutableEntry<String, String> methodPair = splitBy(queryEntry, "=");
                queryPairs.putIfAbsent(methodPair.getKey(), new ArrayList<>());
                queryPairs.get(methodPair.getKey()).add(methodPair.getValue());
            }
        }
        return queryPairs;
    }

    private static AbstractMap.SimpleImmutableEntry<String, String> splitBy(String val, String... separators) {
        final int idx = Arrays.stream(separators).mapToInt(val::indexOf).filter(i -> i > 0).min().orElse(-1);
        String k = idx > 0 ? val.substring(0, idx) : val;
        String v = idx > 0 && val.length() > idx + 1 ? val.substring(idx + 1) : "";
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

}