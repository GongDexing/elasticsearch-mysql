package cn.net.communion.sync.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcConnector {
    Logger logger = Logger.getLogger(JdbcConnector.class);
    private static JdbcTemplate template;

    @Autowired
    public JdbcConnector(JdbcTemplate jdbcTemplate) {
        template = jdbcTemplate;
    }

    // public void queryTest() {
    // List<Map<String, Object>> list =
    // jdbcTemplate.queryForList("select id,isbn from tbl_book limit 2");
    // // list.stream().map((m) -> logger.info(m.toString()));
    // list.forEach(m -> logger.info(m.toString()));
    // }

    public static List<Map<String, Object>> query(String sql) {
        return template.queryForList(sql);
    }

    public static List<Map<String, Object>> query(String sql, Object[] params, int[] paramTypes) {
        return template.queryForList(sql, params, paramTypes);
    }
}
