package com.impact.ecommerce.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {
    private final Environment environment;
    private final String displayName;
    public ConfigController(Environment environment, @Value("${app.display-name}") String displayName) {
        this.environment = environment;
        this.displayName = displayName;
    }
    @GetMapping("/api/config/info")
    public Map<String, Object> info() {
        String[] profiles = environment.getActiveProfiles();
        return Map.of("displayName", displayName, "appName", displayName,
                "profile", profiles.length == 0 ? "default" : profiles[0], "profiles", List.of(profiles));
    }
    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "UP"); }
}
