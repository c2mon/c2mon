package cern.c2mon.client.apitest.db.impl;

import java.util.Arrays;
import java.util.List;

import cern.c2mon.client.apitest.EquipmentDef;
import cern.c2mon.client.apitest.MetricDef;
import cern.c2mon.client.apitest.db.C2MonClientApiTestDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class C2MonClientApiTestDaoImpl implements C2MonClientApiTestDao {

    private SimpleJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    @Override
    public List<MetricDef> getProcessMetrics(String processName) {

        StringBuilder sql = new StringBuilder(
                "select metric_data_tag_id, metric_rule_tag_id, metric_name, display_name, data_type, test_id, description from dmn_metric_v mv "
                        + "where mv.equipment_id in ( select equipment_id from dmn_equipment_v where process_id="
                        + "(select process_id from dmn_process_v where process_name=?)) "
                        + "order by metric_data_tag_id");

        RowMapper<MetricDef> mapper = new RowMapper<MetricDef>() {

            @Override
            public MetricDef mapRow(ResultSet rs, @SuppressWarnings("unused") int arg1) throws SQLException {
                MetricDef def = new MetricDef(rs.getLong("metric_data_tag_id"), rs.getLong("metric_rule_tag_id"), rs
                        .getString("metric_name"), rs.getString("display_name"), rs.getString("data_type"), rs
                        .getInt("test_id"), rs.getString("description"));
                return def;
            }
        };

        return this.jdbcTemplate.query(sql.toString(), mapper, new Object[] { processName });
    }

    @Override
    public List<MetricDef> getEquipmentMetrics(String equipmentName) {

        StringBuilder sql = new StringBuilder(
                "select metric_data_tag_id, metric_rule_tag_id, metric_name, display_name, data_type, test_id, description from dmn_metric_v mv "
                        + "inner join dmn_equipment_v ev on mv.equipment_id = ev.equipment_id where ev.equipment_name=?");

        RowMapper<MetricDef> mapper = new RowMapper<MetricDef>() {

            @Override
            public MetricDef mapRow(ResultSet rs, @SuppressWarnings("unused") int arg1) throws SQLException {
                MetricDef def = new MetricDef(rs.getLong("metric_data_tag_id"), rs.getLong("metric_rule_tag_id"), rs
                        .getString("metric_name"), rs.getString("display_name"), rs.getString("data_type"), rs
                        .getInt("test_id"), rs.getString("description"));
                return def;
            }
        };

        return this.jdbcTemplate.query(sql.toString(), mapper, new Object[] { equipmentName });
    }

    @Override
    public List<EquipmentDef> getEquipments(String... processNames) {

        StringBuilder sql = new StringBuilder(
                "select e.equipment_name as equipment_name, p.equipment_type_name as equipment_type_name, e.equipment_rule_tag_id as equipment_rule_tag_id "
                        + "from dmn_equipment_v e inner join dmn_process_v p on e.process_id = p.process_id where e.process_id in "
                        + "(select process_id from dmn_process_v where process_name in (:pnames))");

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("pnames", Arrays.asList(processNames));

        RowMapper<EquipmentDef> mapper = new RowMapper<EquipmentDef>() {

            @Override
            public EquipmentDef mapRow(ResultSet rs, @SuppressWarnings("unused") int arg1) throws SQLException {
                EquipmentDef def = new EquipmentDef(rs.getLong("equipment_rule_tag_id"),
                        rs.getString("equipment_name"), rs.getString("equipment_type_name"));
                return def;
            }
        };

        return this.jdbcTemplate.query(sql.toString(), mapper, parameters);

    }
    
}
