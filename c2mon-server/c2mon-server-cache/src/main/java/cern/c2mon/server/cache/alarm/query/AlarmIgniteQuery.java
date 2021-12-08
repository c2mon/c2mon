package cern.c2mon.server.cache.alarm.query;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;
import cern.c2mon.shared.client.alarm.AlarmQueryFilter;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteClosure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmIgniteQuery implements AlarmQuery {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmIgniteQuery.class);

    private final IgniteCacheImpl cache;

    public AlarmIgniteQuery(final Ehcache cache){
        this.cache = (IgniteCacheImpl) cache;
    }

    @Override
    public List<Long> findAlarm(AlarmQueryFilter query) {

        ArrayList<IgniteBiPredicate<Long, Alarm>> predicates = new ArrayList<>();

        if (query.getFaultCode() != 0) {
            predicates.add((id, alarm) -> alarm.getFaultCode() == query.getFaultCode());
        }
        if (query.getFaultFamily() != null && !"".equals(query.getFaultFamily())) {
            Pattern pattern = Pattern.compile(replaceWildcardSymbols(query.getFaultFamily()), Pattern.CASE_INSENSITIVE);
            predicates.add((id, alarm) -> pattern.matcher(alarm.getFaultFamily()).matches());
        }
        if (query.getFaultMember() != null && !"".equals(query.getFaultMember())) {
            Pattern pattern = Pattern.compile(replaceWildcardSymbols(query.getFaultMember()), Pattern.CASE_INSENSITIVE);
            predicates.add((id, alarm) -> pattern.matcher(alarm.getFaultMember()).matches());
        }
        if (query.getPriority() != 0) {
            //TODO this attribute isn't used ? Does not appear in cern.c2mon.server.cache.dbaccess.AlarmMapper
        }
        if (query.getActive() != null) {
            predicates.add((id, alarm) -> alarm.isActive() == query.getActive());
        }
        if (query.getOscillating() != null) {
            predicates.add((id, alarm) -> alarm.isOscillating() == query.getOscillating());
        }

        IgniteBiPredicate<Long, Alarm> compositePredicate = predicates.stream().reduce((z, w) -> true, IgniteBiPredicate::and);

        List<Long> result = cache.getCache().query(new ScanQuery<>(compositePredicate),
                (IgniteClosure<Cache.Entry<Long, Alarm>, Long>) Cache.Entry::getKey).getAll();

        try(Stream<Long> stream = result.stream()) {
            result = stream.limit(query.getMaxResultSize()).collect(Collectors.toList());
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
