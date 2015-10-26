package cern.c2mon.client.ext.history.alarm.repository;

import cern.c2mon.client.ext.history.alarm.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.sql.Timestamp;
import java.util.List;

/**
 * Spring Data JPA repository for querying {@link Alarm}s from hte c2mon history database.
 *
 * @author Justin Lewis Salmon
 */
public interface AlarmRepository extends JpaRepository<Alarm, Long>, QueryDslPredicateExecutor<Alarm> {

  List<Alarm> findByTimestampBetween(Timestamp start, Timestamp end);
}
