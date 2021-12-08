package cern.c2mon.server.cache.alarm.query;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;
import cern.c2mon.shared.client.alarm.AlarmQueryFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AlarmInMemoryQuery implements AlarmQuery {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmInMemoryQuery.class);

    private final InMemoryCache cache;

    public AlarmInMemoryQuery(final Ehcache cache){
        this.cache = (InMemoryCache) cache;
    }

    @Override
    public List<Long> findAlarm(AlarmQueryFilter query) {
        List<Long> result;

        ArrayList<Predicate<Alarm>> predicates = new ArrayList<>();

        if (query.getFaultCode() != 0) {
            predicates.add(alarm -> alarm.getFaultCode() == query.getFaultCode());
        }
        if (query.getFaultFamily() != null && !"".equals(query.getFaultFamily())) {
            Pattern pattern = Pattern.compile(replaceWildcardSymbols(query.getFaultFamily()), Pattern.CASE_INSENSITIVE);
            predicates.add(alarm -> pattern.matcher(alarm.getFaultFamily()).matches());
        }
        if (query.getFaultMember() != null && !"".equals(query.getFaultMember())) {
            Pattern pattern = Pattern.compile(replaceWildcardSymbols(query.getFaultMember()), Pattern.CASE_INSENSITIVE);
            predicates.add(alarm -> pattern.matcher(alarm.getFaultMember()).matches());
        }
        if (query.getPriority() != 0) {
            //TODO this attribute isn't used ? Does not appear in cern.c2mon.server.cache.dbaccess.AlarmMapper
        }
        if (query.getActive() != null) {
            predicates.add(alarm -> alarm.isActive() == query.getActive());
        }
        if (query.getOscillating() != null) {
            predicates.add(alarm -> alarm.isOscillating() == query.getOscillating());
        }

        Predicate<Alarm> compositePredicate = predicates.stream().reduce(w -> true, Predicate::and);

        try(Stream<Alarm> stream = cache.getCache().values().stream()) {
            result = stream.filter(compositePredicate).map(a -> a.getId()).limit(query.getMaxResultSize()).collect(Collectors.toList());
        }

        return result;
    }

    /**
     * Method to replace the character '*' by '.*' and '?' by '.?' to work with the Java Pattern
     * @param wildcard
     * @return
     */
    private String replaceWildcardSymbols(String wildcard){
        if(wildcard.contains("*") || wildcard.contains("?")) {
            String result = wildcard.replace("*", ".*").replace("?", ".?");
            LOG.debug("Replaced wildcard symbols on wildcard {}. Result: {}", wildcard, result);
            return result;
        }else{
            return wildcard;
        }
    }
}
