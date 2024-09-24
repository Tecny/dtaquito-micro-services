package com.dtaquito_backend.dtaquito_backend.dtaquito.api.rest;

import com.dtaquito_backend.dtaquito_backend.config.TestSecurityConfig;
import com.dtaquito_backend.dtaquito_backend.iam.domain.model.queries.GetAllRolesQuery;
import com.dtaquito_backend.dtaquito_backend.iam.domain.services.RoleQueryService;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.RolesController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;

@WebMvcTest(RolesController.class)
@Import(TestSecurityConfig.class)
public class RolesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleQueryService roleQueryService;

    @BeforeEach
    public void setup() {}

    @Test
    public void testGetAllRoles() throws Exception {
        when(roleQueryService.handle(any(GetAllRolesQuery.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}