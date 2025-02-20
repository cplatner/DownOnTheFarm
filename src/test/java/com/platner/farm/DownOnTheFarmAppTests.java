package com.platner.farm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platner.farm.models.Actions;
import com.platner.farm.models.FarmAction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DownOnTheFarmAppTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getPing() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/ping").accept(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("pong")));
    }

    @Test
    @Disabled(value = "Regular call is tested in the rate-limit test")
    public void getADisaster() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/v1/farm")
                        .content(mapper.writeValueAsString(new FarmAction(Actions.harvest)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                // The status string is random, so just check for an ok http status for now
                .andExpect(status().isOk());
    }

    @Test
    public void performInvalidAction() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/v1/farm")
                        .content("{ \"action\": \"invalid\" }")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                // The status string is random, so just check for an ok http status for now
                .andExpect(status().is(400));
    }

    @Test
    public void getHealth() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/health").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    public void verifyRateLimiting() throws Exception {
        // First will work fine
        mvc.perform(MockMvcRequestBuilders.post("/api/v1/farm")
                        .content(mapper.writeValueAsString(new FarmAction(Actions.harvest)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.post("/api/v1/farm")
                        .content(mapper.writeValueAsString(new FarmAction(Actions.harvest)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
        // Rate limited
        mvc.perform(MockMvcRequestBuilders.post("/api/v1/farm")
                        .content(mapper.writeValueAsString(new FarmAction(Actions.harvest)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(429));
    }
}
