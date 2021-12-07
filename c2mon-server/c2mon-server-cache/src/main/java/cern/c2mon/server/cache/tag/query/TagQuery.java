package cern.c2mon.server.cache.tag.query;

import java.util.List;

public interface TagQuery<T> {

    List<T> findTagsByWildcard(String wildcard, int maxResults);

}
