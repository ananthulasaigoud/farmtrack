package com.farmtrack.farmtrack;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests of the REST API using the example from the project plan:
 * Sai grows rice, spends 2,000 + 3,000 + 5,000 + 1,500 = 11,500 and sells for 40,000, so profit = 28,500.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FarmTrackApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullFarmerFlow_calculatesProfitAndProtectsData() throws Exception {
        // ---- Register and log in ----
        String saiToken = register("Sai", "9876500001", "Warangal", "sai@example.com", "secret123");
        Long saiFarmerId = readLong(perform(get("/api/auth/me"), saiToken).andExpect(status().isOk()), "$.farmerId");

        perform(post("/api/auth/login").content("""
                {"identifier":"9876500001","password":"wrong-password"}"""), null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid phone/email or password"));

        perform(post("/api/auth/login").content("""
                {"identifier":"sai@example.com","password":"secret123"}"""), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.name").value("Sai"));

        // ---- Crop ----
        perform(post("/api/farmers/" + saiFarmerId + "/crops").content("""
                {"cropName":"Rice","season":"Kharif","sowingDate":"2026-07-01","expectedHarvestDate":"2026-06-01"}"""), saiToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Expected harvest date cannot be before the sowing date"));

        Long riceId = readLong(perform(post("/api/farmers/" + saiFarmerId + "/crops").content("""
                {"cropName":"Rice","landDetails":"2 acres near canal","season":"Kharif",
                 "sowingDate":"2026-06-15","expectedHarvestDate":"2026-10-30"}"""), saiToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cropName").value("Rice")), "$.id");

        Long cottonId = readLong(perform(post("/api/farmers/" + saiFarmerId + "/crops").content("""
                {"cropName":"Cotton","season":"Kharif","sowingDate":"2026-06-20"}"""), saiToken)
                .andExpect(status().isCreated()), "$.id");

        // ---- Expenses: 2000 + 3000 + 5000 + 1500 = 11500 ----
        addExpense(riceId, "SEEDS", "2000", "2026-06-10", saiToken);
        addExpense(riceId, "FERTILIZER", "3000", "2026-07-05", saiToken);
        Long labourId = addExpense(riceId, "LABOUR", "4000", "2026-08-01", saiToken);
        addExpense(riceId, "PESTICIDE", "1500", "2026-08-20", saiToken);
        addExpense(cottonId, "SEEDS", "1000", "2026-06-20", saiToken);

        perform(put("/api/expenses/" + labourId).content("""
                {"category":"LABOUR","amount":5000,"expenseDate":"2026-08-01","description":"Harvest labour"}"""), saiToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(5000));

        perform(post("/api/crops/" + riceId + "/expenses").content("""
                {"category":"SEEDS","amount":-5,"expenseDate":"2026-06-10"}"""), saiToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").value("Amount must be greater than 0"));

        // ---- Income: 20 quintals x 2000 = 40000 ----
        perform(post("/api/crops/" + riceId + "/incomes").content("""
                {"quantity":20,"unit":"quintal","pricePerUnit":2000,"saleDate":"2026-11-05","description":"Sold at market"}"""), saiToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(40000.0));

        // ---- Crop summary ----
        perform(get("/api/dashboard/crops/" + riceId), saiToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(11500.0))
                .andExpect(jsonPath("$.totalIncome").value(40000.0))
                .andExpect(jsonPath("$.profit").value(28500.0))
                .andExpect(jsonPath("$.status").value("PROFIT"));

        // ---- Dashboard: rice +28500, cotton -1000 => total profit 27500 ----
        perform(get("/api/dashboard/summary"), saiToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCrops").value(2))
                .andExpect(jsonPath("$.totalExpenses").value(12500.0))
                .andExpect(jsonPath("$.totalIncome").value(40000.0))
                .andExpect(jsonPath("$.profit").value(27500.0))
                .andExpect(jsonPath("$.expensesByCategory.SEEDS").value(3000.0))
                .andExpect(jsonPath("$.bestCrop.cropName").value("Rice"));

        perform(get("/api/dashboard/monthly?year=2026"), saiToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[5].totalExpenses").value(3000.0))   // June
                .andExpect(jsonPath("$[10].totalIncome").value(40000.0)); // November

        perform(get("/api/expenses?category=SEEDS"), saiToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        perform(get("/api/reports/download?type=crops"), saiToken)
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("farmtrack-crop-profit-report.csv")))
                .andExpect(content().string(containsString("\"Rice\",\"Kharif\",11500.00,40000.00,28500.00,\"PROFIT\"")));

        // ---- Another farmer cannot see Sai's data ----
        String raviToken = register("Ravi", "9876500002", "Karimnagar", null, "secret456");
        perform(get("/api/crops/" + riceId), raviToken).andExpect(status().isForbidden());
        perform(get("/api/farmers/" + saiFarmerId + "/crops"), raviToken).andExpect(status().isForbidden());
        perform(delete("/api/expenses/" + labourId), raviToken).andExpect(status().isForbidden());
        perform(get("/api/farmers"), raviToken).andExpect(status().isForbidden()); // admin only
        perform(get("/api/dashboard/summary"), raviToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCrops").value(0))
                .andExpect(jsonPath("$.bestCrop").doesNotExist());

        // ---- Deleting a crop removes its expenses and income ----
        perform(delete("/api/crops/" + riceId), saiToken).andExpect(status().isNoContent());
        perform(get("/api/crops/" + riceId), saiToken).andExpect(status().isNotFound());
        perform(get("/api/dashboard/summary"), saiToken)
                .andExpect(jsonPath("$.totalExpenses").value(1000.0))
                .andExpect(jsonPath("$.profit").value(-1000.0))
                .andExpect(jsonPath("$.status").value("LOSS"));
    }

    @Test
    void registrationValidationAndDuplicates() throws Exception {
        register("Lakshmi", "9876500010", "Nalgonda", null, "secret123");

        perform(post("/api/auth/register").content("""
                {"name":"Other","phone":"9876500010","village":"X","password":"secret123"}"""), null)
                .andExpect(status().isConflict());

        perform(post("/api/auth/register").content("""
                {"name":"","phone":"12345","village":"X","password":"123"}"""), null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.phone").value("Phone number must contain 10 digits"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void profilePage_updateChangePasswordAndDeleteAccount() throws Exception {
        String token = register("Anil", "9876500040", "Jangaon", null, "secret123");
        register("Suresh", "9876500041", "Jangaon", null, "secret123");
        Long farmerId = readLong(perform(get("/api/auth/me"), token), "$.farmerId");

        perform(get("/api/farmers/" + farmerId), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anil"));

        perform(put("/api/farmers/" + farmerId).content("""
                {"name":"Anil Kumar","phone":"9876500040","village":"Jangaon"}"""), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anil Kumar"));

        // Another farmer's phone number is rejected
        perform(put("/api/farmers/" + farmerId).content("""
                {"name":"Anil Kumar","phone":"9876500041","village":"Jangaon"}"""), token)
                .andExpect(status().isConflict());

        perform(post("/api/auth/change-password").content("""
                {"currentPassword":"wrong-one","newPassword":"newpass1"}"""), token)
                .andExpect(status().isBadRequest());
        perform(post("/api/auth/change-password").content("""
                {"currentPassword":"secret123","newPassword":"newpass1"}"""), token)
                .andExpect(status().isOk());

        // Delete account: the farmer, crops and login are all removed
        perform(post("/api/farmers/" + farmerId + "/crops").content("""
                {"cropName":"Maize"}"""), token).andExpect(status().isCreated());
        perform(delete("/api/farmers/" + farmerId), token).andExpect(status().isNoContent());
        perform(post("/api/auth/login").content("""
                {"identifier":"9876500040","password":"newpass1"}"""), null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void guestLogin_hasSampleDataAndAccountIsProtected() throws Exception {
        String guestToken = JsonPath.read(perform(post("/api/auth/login").content("""
                {"identifier":"9999999999","password":"guest123"}"""), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.guest").value(true))
                .andExpect(jsonPath("$.user.name").value("Guest Farmer"))
                .andReturn().getResponse().getContentAsString(), "$.token");
        Long guestFarmerId = readLong(perform(get("/api/auth/me"), guestToken), "$.farmerId");

        // Sample data: rice 40000-11500, cotton 30000-9000, maize 0-1200
        perform(get("/api/dashboard/summary"), guestToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCrops").value(3))
                .andExpect(jsonPath("$.totalIncome").value(70000.0))
                .andExpect(jsonPath("$.totalExpenses").value(21700.0))
                .andExpect(jsonPath("$.profit").value(48300.0))
                .andExpect(jsonPath("$.bestCrop.cropName").value("Rice"));

        // Account changes are blocked for the shared guest account
        perform(post("/api/auth/change-password").content("""
                {"currentPassword":"guest123","newPassword":"hacked1"}"""), guestToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("guest account")));
        perform(put("/api/farmers/" + guestFarmerId).content("""
                {"name":"Changed","phone":"9876500099","village":"X"}"""), guestToken)
                .andExpect(status().isBadRequest());
        perform(delete("/api/farmers/" + guestFarmerId), guestToken)
                .andExpect(status().isBadRequest());
        perform(post("/api/auth/forgot-password").content("""
                {"identifier":"9999999999"}"""), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resetToken").value(nullValue()));

        // ...but the guest can still try the farm features
        perform(get("/api/farmers/" + guestFarmerId + "/crops"), guestToken).andExpect(status().isOk());

        // A normal user is not a guest
        String normalToken = register("Mahesh", "9876500050", "Nizamabad", null, "secret123");
        perform(get("/api/auth/me"), normalToken).andExpect(jsonPath("$.guest").value(false));
    }

    @Test
    void webPagesArePublic() throws Exception {
        mockMvc.perform(get("/index.html")).andExpect(status().isOk())
                .andExpect(content().string(containsString("FarmTrack")));
        mockMvc.perform(get("/dashboard.html")).andExpect(status().isOk());
        mockMvc.perform(get("/js/common.js")).andExpect(status().isOk());
        mockMvc.perform(get("/css/style.css")).andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsNeedAValidToken() throws Exception {
        perform(get("/api/dashboard/summary"), null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Please log in to continue"));
        perform(get("/api/dashboard/summary"), "not-a-real-token")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgotAndResetPassword() throws Exception {
        register("Kavya", "9876500020", "Medak", null, "oldpass1");

        String resetToken = JsonPath.read(perform(post("/api/auth/forgot-password").content("""
                {"identifier":"9876500020"}"""), null)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.resetToken");

        perform(post("/api/auth/reset-password").content("""
                {"token":"wrong-token","newPassword":"newpass1"}"""), null)
                .andExpect(status().isBadRequest());

        perform(post("/api/auth/reset-password").content(
                "{\"token\":\"" + resetToken + "\",\"newPassword\":\"newpass1\"}"), null)
                .andExpect(status().isOk());

        perform(post("/api/auth/login").content("""
                {"identifier":"9876500020","password":"oldpass1"}"""), null)
                .andExpect(status().isUnauthorized());
        perform(post("/api/auth/login").content("""
                {"identifier":"9876500020","password":"newpass1"}"""), null)
                .andExpect(status().isOk());

        // A reset token works only once
        perform(post("/api/auth/reset-password").content(
                "{\"token\":\"" + resetToken + "\",\"newPassword\":\"another1\"}"), null)
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanListFarmers() throws Exception {
        register("Ramesh", "9876500030", "Siddipet", null, "secret123");
        String adminToken = JsonPath.read(perform(post("/api/auth/login").content("""
                {"identifier":"admin@farmtrack.test","password":"admin123"}"""), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andReturn().getResponse().getContentAsString(), "$.token");

        perform(get("/api/farmers"), adminToken)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ramesh")));

        perform(get("/api/dashboard/summary"), adminToken)
                .andExpect(status().isBadRequest()); // admin must choose a farmer with ?farmerId=
    }

    // ---------- helpers ----------

    private String register(String name, String phone, String village, String email, String password) throws Exception {
        String emailJson = email == null ? "" : ",\"email\":\"" + email + "\"";
        String body = "{\"name\":\"" + name + "\",\"phone\":\"" + phone + "\",\"village\":\"" + village + "\","
                + "\"password\":\"" + password + "\"" + emailJson + "}";
        String response = perform(post("/api/auth/register").content(body), null)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private Long addExpense(Long cropId, String category, String amount, String date, String token) throws Exception {
        String body = "{\"category\":\"" + category + "\",\"amount\":" + amount + ",\"expenseDate\":\"" + date + "\"}";
        return readLong(perform(post("/api/crops/" + cropId + "/expenses").content(body), token)
                .andExpect(status().isCreated()), "$.id");
    }

    private ResultActions perform(MockHttpServletRequestBuilder request, String token) throws Exception {
        request.contentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    private static Long readLong(ResultActions result, String path) throws Exception {
        Number value = JsonPath.read(result.andReturn().getResponse().getContentAsString(), path);
        return value.longValue();
    }
}
