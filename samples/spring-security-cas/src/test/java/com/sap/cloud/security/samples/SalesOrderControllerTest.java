package com.sap.cloud.security.samples;

import com.sap.cloud.security.cas.client.AdcService;
import com.sap.cloud.security.spring.context.support.WithMockOidcUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Support non-static @BeforeAll
public class SalesOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdcService adcService;

    @BeforeAll
    public void adcServiceRunning() {
        assumeTrue(adcService.ping());
    }

    @Test
    @WithMockOidcUser(name="Bob.noAuthorization@test.com")
    public void readWith_Bob_403() throws Exception {
        mockMvc.perform(get("/salesOrders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockOidcUser(name = "Alice_salesOrders@test.com", authorities = {"read:salesOrders"})
    public void readWith_Alice_salesOrders_200() throws Exception {
        mockMvc.perform(get("/salesOrders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockOidcUser(name = "Alice_salesOrdersBetween@test.com")
    public void readWith_Alice_italianSalesOrderWithId101_200() throws Exception {
        mockMvc.perform(get("/salesOrders/readByCountryAndId/IT/101"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockOidcUser(name = "Alice_salesOrdersBetween@test.com")
    public void readWith_Alice_italianSalesOrderWithId501_403() throws Exception {
        mockMvc.perform(get("/salesOrders/readByCountryAndId/IT/501"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockOidcUser(name ="Alice_countryCode@test.com")
    public void readWith_Alice_italianResource_200() throws Exception {
        mockMvc.perform(get("/salesOrders/readByCountry/IT"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockOidcUser(name ="Alice_countryCode@test.com")
    public void readWith_Alice_americanResource_403() throws Exception {
        mockMvc.perform(get("/salesOrders/readByCountry/US"))
                .andExpect(status().isForbidden());
    }

}


