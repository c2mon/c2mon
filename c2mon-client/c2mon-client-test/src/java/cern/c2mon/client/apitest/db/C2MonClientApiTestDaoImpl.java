package cern.c2mon.client.apitest.db;

import java.util.List;

import cern.c2mon.client.apitest.MetricDef;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class C2MonClientApiTestDaoImpl implements C2MonClientApiTestDao {

    private SimpleJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    @Override
    public List<MetricDef> getAllMetrics() {

        StringBuilder sql = new StringBuilder(
                "select device || '/' || property || '/' || field metric_name, metric_rule_tag_id from dmn_metric_v where "
                        + "order by metric_rule_tag_id");

        Object[] args = null;

        RowMapper<MetricDef> mapper = new RowMapper<MetricDef>() {

            @Override
            public MetricDef mapRow(ResultSet rs, int arg1) throws SQLException {
                MetricDef def = new MetricDef(rs.getLong("metric_rule_tag_id"), rs.getString("metric_name"));
                return def;
            }
        };

        return this.jdbcTemplate.query(sql.toString(), mapper, args);

    }

    @Override
    public List<MetricDef> getAllDeviceRuleMetrics() {
        StringBuilder sql = new StringBuilder(
                "select equipment_name||':STATUS' metric_name, equipment_rule_tag_id from dmn_equipment_v order by equipment_rule_tag_id");

        Object[] args = null;

        RowMapper<MetricDef> mapper = new RowMapper<MetricDef>() {

            @Override
            public MetricDef mapRow(ResultSet rs, int arg1) throws SQLException {
                MetricDef def = new MetricDef(rs.getLong("equipment_rule_tag_id"), rs.getString("metric_name"));
                return def;
            }
        };

        return this.jdbcTemplate.query(sql.toString(), mapper, args);
    }

    @Override
    public List<MetricDef> getAllRuleMetrics() {

        StringBuilder sql = new StringBuilder(
                "select device || '/' || property || '/' || field metric_name, metric_rule_tag_id from dmn_metric_v where "
                        + "metric_rule_tag_id is not null order by metric_rule_tag_id");

        Object[] args = null;

        RowMapper<MetricDef> mapper = new RowMapper<MetricDef>() {

            @Override
            public MetricDef mapRow(ResultSet rs, int arg1) throws SQLException {
                MetricDef def = new MetricDef(rs.getLong("metric_rule_tag_id"), rs.getString("metric_name"));
                return def;
            }
        };

        return this.jdbcTemplate.query(sql.toString(), mapper, args);
    }

}
