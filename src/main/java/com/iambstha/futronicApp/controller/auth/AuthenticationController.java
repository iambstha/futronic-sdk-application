package com.iambstha.futronicApp.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iambstha.futronicApp.model.auth.AuthenticationRequest;
import com.iambstha.futronicApp.model.auth.AuthenticationResponse;
import com.iambstha.futronicApp.model.auth.RegisterRequest;
import com.iambstha.futronicApp.service.auth.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

	private final AuthService authService;

	@PostMapping(path = "/register", consumes = "application/json")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping(path = "/authenticate", consumes = "application/json")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
		return ResponseEntity.ok(authService.authenticate(request));
	}

}
