package com.server.app.controllers;

import com.server.app.config.JsonWebToken;
import com.server.app.dto.user.UserCreateDto;
import com.server.app.dto.user.UserLoginDto;
import com.server.app.dto.user.UserUpdateDto;
import com.server.app.entities.User;
import com.server.app.services.UserService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JsonWebToken jsonWebToken;

    public AuthController(UserService userService, JsonWebToken jsonWebToken) {
        this.userService = userService;
        this.jsonWebToken = jsonWebToken;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signUp(@Valid @RequestBody UserCreateDto dto) {
        return ResponseEntity.ok(userService.create(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDto dto) {
        User user = userService.login(dto.getUsername(), dto.getPassword());

        String token = jsonWebToken.createToken(user);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "data", user
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        int userId = getAuthenticatedUserId();
        return ResponseEntity.ok(userService.findById(userId));
    }

    @PutMapping("/update/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserUpdateDto dto) {
        int userId = getAuthenticatedUserId();
        User user = userService.updateProfile(userId, dto);

        return ResponseEntity.ok(Map.of(
                "token", jsonWebToken.createToken(user),
                "data", user
        ));
    }

    @PutMapping("/update/password")
    public ResponseEntity<User> updatePassword(@RequestBody Map<String, String> body) {
        int userId = getAuthenticatedUserId();
        userService.updatePassword(userId, body.get("oldpassword"), body.get("newpassword"));
        return ResponseEntity.ok(userService.findById(userId));
    }

    private int getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User) {
            return ((User) principal).getId();
        }

        String principalStr = principal.toString();

        try {
            String idStr = principalStr.split("id=")[1].split(",")[0];
            return Integer.parseInt(idStr);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo extraer el ID del usuario del token");
        }
    }
}
