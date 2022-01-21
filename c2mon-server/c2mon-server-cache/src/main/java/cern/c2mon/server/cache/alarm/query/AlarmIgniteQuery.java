package cern.c2mon.server.cache.alarm.query;

import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.IgniteCacheImpl;
import cern.c2mon.shared.client.alarm.AlarmQueryFilter;

import java.util.ArrayList;
import java.util.List;

import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
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

        List<String> conditions = new ArrayList<>();

        if (query.getFaultCode() != 0) {
            conditions.add("FAULTCODE = " + query.getFaultCode());
        }
        if (query.getFaultFamily() != null && !"".equals(query.getFaultFamily())) {
            conditions.add("upper(FAULTFAMILY) like upper('" + replaceWildcardSymbols(query.getFaultFamily()) + "')");
        }
        if (query.getFaultMember() != null && !"".equals(query.getFaultMember())) {
            conditions.add("upper(FAULTMEMBER) like upper('" + replaceWildcardSymbols(query.getFaultMember()) + "')");
        }
        if (query.getPriority() != 0) {
            //TODO this attribute isn't used ? Does not appear in cern.c2mon.server.cache.dbaccess.AlarmMapper
        }
        if (query.getActive() != null) {
            conditions.add("ACTIVE = " + query.getActive());
        }
        if (query.getOscillating() != null) {
            conditions.add("OSCILLATING = " + query.getOscillating());
        }

        StringBuilder findAlarmQuery = new StringBuilder();

        findAlarmQuery.append("select _key from AlarmCacheObject");

        for(int i = 0; i < conditions.size(); i++){
            if(i==0){
                findAlarmQuery.append(" where " + conditions.get(i));
            }else{
                findAlarmQuery.append(" and " + conditions.get(i));
            }
        }

        SqlFieldsQuery sql = new SqlFieldsQuery(findAlarmQuery.toString());

        List<Long> alarmIds = new ArrayList<>();

        try (QueryCursor<List<?>> cursor = cache.getCache().query(sql)) {
            for (List<?> row : cursor) {
                alarmIds.add((Long) row.get(0));
            }
        }
        return alarmIds;
    }

    /**
     * Method to replace the character '*' by '%' and '?' by '_' to work with the Java Pattern
     * @param wildcard
     * @return
     */
    private String replaceWildcardSymbols(String wildcard){
        if(wildcard.contains("*") || wildcard.contains("?")) {
            String result = wildcard.replace("*", "%").replace("?", "_");
            LOG.debug("Replaced wildcard symbols on wildcard {}. Result: {}", wildcard, result);
            return result;
        }else{
            return wildcard;
        }
    }
}
