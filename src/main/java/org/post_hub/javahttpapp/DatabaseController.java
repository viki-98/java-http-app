package org.post_hub.javahttpapp;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DatabaseController {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/db-check")
    public Map<String, Object> dbCheck() {
        String currentUser = jdbcTemplate.queryForObject("select current_user", String.class);
        String currentDatabase = jdbcTemplate.queryForObject("select current_database()", String.class);
        String currentTime = jdbcTemplate.queryForObject("select now()::text", String.class);
        Integer userCount = jdbcTemplate.queryForObject("select count(*) from test_users", Integer.class);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("currentUser", currentUser);
        response.put("currentDatabase", currentDatabase);
        response.put("databaseTime", currentTime);
        response.put("testUsersCount", userCount);

        return response;
    }

    @GetMapping("/api/users")
    public List<Map<String, Object>> users() {
        return jdbcTemplate.queryForList("select id, name from test_users order by id");
    }
}