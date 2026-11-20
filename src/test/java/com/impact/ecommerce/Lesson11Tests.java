package com.impact.ecommerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Lesson11Tests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(java.util.Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    @Test void refreshRotatesAndLogoutRevokes() throws Exception {
        var login = json.readTree(login("user@impact.md", "user123"));
        String old = login.get("refreshToken").asText();
        String request = json.writeValueAsString(java.util.Map.of("refreshToken", old));
        var result = mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk()).andReturn();
        String fresh = json.readTree(result.getResponse().getContentAsString()).get("refreshToken").asText();
        assertNotEquals(old, fresh);
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isUnauthorized());
        String logout = json.writeValueAsString(java.util.Map.of("refreshToken", fresh));
        mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content(logout)).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(logout)).andExpect(status().isUnauthorized());
    }

    @Test void productWritesRequireAdminAndDiscountIsValidated() throws Exception {
        String user = json.readTree(login("user@impact.md", "user123")).get("token").asText();
        String admin = json.readTree(login("admin@impact.md", "admin123")).get("token").asText();
        String body = "{\"name\":\"Test\",\"price\":100,\"stock\":1,\"discountPercentage\":25}";
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/products").header("Authorization", "Bearer " + user).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/products").header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.finalPrice").value(75.0));
        mvc.perform(post("/api/products").header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON).content(body.replace("25", "101")))
                .andExpect(status().isBadRequest());
    }

    @Test void frontendCanReadEnvironmentAndLesson10SecurityChecks() throws Exception {
        mvc.perform(get("/api/config/info")).andExpect(status().isOk())
                .andExpect(jsonPath("$.profile").value("default"))
                .andExpect(jsonPath("$.appName").value("Impact Shop"));
        mvc.perform(get("/api/products")).andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
        mvc.perform(get("/v3/api-docs")).andExpect(header().doesNotExist("X-Frame-Options"));
        mvc.perform(get("/api/products").param("category", "1 OR 1=1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
    }
}
