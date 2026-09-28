package com.travelagent.app.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.travelagent.app.models.Role;
import com.travelagent.app.models.User;
import com.travelagent.app.repositories.UserRepository;
import com.travelagent.app.security.JwtUtil;
import com.travelagent.app.security.SecurityConfig;
import com.travelagent.app.services.RoleService;
import com.travelagent.app.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;

/**
 * Account management under /api/auth must be admin-only; login and logout stay public.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private RoleService roleService;

    private static final String UPDATE = "{\"username\":\"victim\",\"password\":\"new-password\"}";
    private final User victim = user("victim", "AGENT");

    @BeforeEach
    void setUp() {
        victim.setPassword(BCrypt.hashpw("old-password", BCrypt.gensalt(4)));
        when(jwtUtil.extractUsername("admin-token")).thenReturn("admin");
        when(jwtUtil.extractUsername("agent-token")).thenReturn("agent");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user("admin", "ADMIN")));
        when(userRepository.findByUsername("agent")).thenReturn(Optional.of(user("agent", "AGENT")));
        when(userService.getUserByUsername("victim")).thenReturn(victim);
    }

    @Test
    void anonymousCannotRegisterUpdateOrDeleteAccounts() throws Exception {
        mvc.perform(json(post("/api/auth/register"), "{\"username\":\"intruder\",\"password\":\"x\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(json(post("/api/auth/update"), UPDATE)).andExpect(status().isForbidden());
        mvc.perform(json(post("/api/auth/delete"), "victim")).andExpect(status().isForbidden());

        verify(userService, never()).saveUser(any());
        verify(userService, never()).deleteUser(any());
    }

    @Test
    void agentsCannotManageAccounts() throws Exception {
        mvc.perform(json(post("/api/auth/update"), UPDATE).header("Authorization", "Bearer agent-token"))
                .andExpect(status().isForbidden());
        mvc.perform(json(post("/api/auth/delete"), "victim").header("Authorization", "Bearer agent-token"))
                .andExpect(status().isForbidden());

        verify(userService, never()).saveUser(any());
        verify(userService, never()).deleteUser(any());
    }

    @Test
    void adminUpdateStoresAHashNotThePlainPassword() throws Exception {
        mvc.perform(json(post("/api/auth/update"), UPDATE).header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());

        verify(userService).saveUser(argThat(u -> !"new-password".equals(u.getPassword())
                && BCrypt.checkpw("new-password", u.getPassword())));
    }

    @Test
    void blankPasswordIsRejected() throws Exception {
        mvc.perform(json(post("/api/auth/update"), "{\"username\":\"victim\",\"password\":\" \"}")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).saveUser(any());
    }

    @Test
    void loginStaysPublic() throws Exception {
        when(userService.getUserByUsername("victim")).thenReturn(victimWithProfile());
        when(jwtUtil.generateToken("victim", "AGENT")).thenReturn("token");

        mvc.perform(json(post("/api/auth/login"), "{\"username\":\"victim\",\"password\":\"old-password\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout")).andExpect(status().isOk());
    }

    private User victimWithProfile() {
        victim.setEmail("victim@example.com");
        victim.setFullName("Victim");
        victim.setPhoneNumber("+30 000");
        return victim;
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static User user(String username, String roleName) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setUsername(username);
        user.setRole(role);
        return user;
    }
}
