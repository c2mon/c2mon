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
				"select equipment_name, equipment_rule_tag_id from dmn_equipment_v order by 1");

		Object[] args = null;

		RowMapper<MetricDef> mapper = new RowMapper<MetricDef>() {

			@Override
			public MetricDef mapRow(ResultSet rs, int arg1) throws SQLException {
				MetricDef def = new MetricDef(
						rs.getLong("equipment_rule_tag_id"),
						rs.getString("equipment_name"));
				return def;
			}
		};

		return this.jdbcTemplate.query(sql.toString(), mapper, args);

	}


	@Override
	public List<MetricDef> getAllDeviceRuleMetrics() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<MetricDef> getAllRuleMetrics() {
		// TODO Auto-generated method stub
		return null;
	}

}
