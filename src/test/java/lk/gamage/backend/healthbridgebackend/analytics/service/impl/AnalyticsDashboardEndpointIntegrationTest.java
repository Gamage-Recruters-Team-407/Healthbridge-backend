package lk.gamage.backend.healthbridgebackend.analytics.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsDashboardEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void analyticsDashboardEndpointReturnsMongoDataForEverySupportedPeriod() throws Exception {
        for (String period : new String[]{"today", "week", "month", "year"}) {
            var response = mockMvc.perform(get("/api/analytics/dashboard").param("period", period))
                    .andReturn()
                    .getResponse();

            Assertions.assertEquals(200, response.getStatus(), response.getContentAsString());
            var body = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(response.getContentAsString())
                    .path("data");
            Assertions.assertEquals(period, body.path("period").asText());
            Assertions.assertEquals("PARTIAL", body.path("dataAvailability").asText());
            Assertions.assertTrue(body.path("kpis").isArray());
            Assertions.assertTrue(body.path("patientTrends").isArray());
            Assertions.assertFalse(body.path("patientTrends").isEmpty());
            Assertions.assertTrue(body.path("revenueTrend").isArray());
            Assertions.assertFalse(body.path("revenueTrend").isEmpty());
            Assertions.assertTrue(body.path("departmentPerformance").isArray());
            Assertions.assertTrue(body.path("operationalSummary").isArray());
        }
    }
}
