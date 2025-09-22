package com.travelagent.app.controllers;

import com.travelagent.app.models.User;
import com.travelagent.app.models.Role;
import com.travelagent.app.services.RoleService;
import com.travelagent.app.services.UserService;
import com.travelagent.app.security.JwtUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final RoleService roleService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, RoleService roleService, JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // Set user fields for creation
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        // Set role field for user creation
        Role role = roleService.findByName("AGENT");
        user.setRole(role);
        userService.saveUser(user);

        return "User registered successfully!";
    }

    @PostMapping("/update")
    public String update(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        User user = userService.getUserByUsername(username);
        user.setPassword(password);
        userService.saveUser(user);
        return "User password updated successfully!";
    }

    @PostMapping("/delete")
    public String delete(@RequestBody String username) {
        userService.deleteUser(username);
        return "User deleted.";
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        User dbUser = userService.getUserByUsername(username);

        if (!passwordEncoder.matches(password, dbUser.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        System.out.println(dbUser.getRole().getName());
        String roleName = dbUser.getRole().getName();
        String token = jwtUtil.generateToken(dbUser.getUsername(), roleName);
        System.out.println(token);
        System.out.println(roleName);
        return Map.of(
                "token", token,
                "role", roleName,
                "username", dbUser.getUsername());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT doesn't require any server-side token invalidation since it's stateless.
        // Just return a success message to the client indicating a successful logout.
        // Handle the logout from the client.
        return ResponseEntity.ok().body("Logged out successfully.");
    }
}
