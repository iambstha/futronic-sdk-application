package com.iambstha.futronicApp.service.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iambstha.futronicApp.model.auth.AuthenticationRequest;
import com.iambstha.futronicApp.model.auth.AuthenticationResponse;
import com.iambstha.futronicApp.model.auth.RegisterRequest;
import com.iambstha.futronicApp.model.auth.User;
import com.iambstha.futronicApp.repository.UserRepository;
import com.iambstha.futronicApp.service.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthenticationResponse register(RegisterRequest request) {

		System.out.println("Username: " + request.getUsername());
		System.out.println("Password: " + request.getPassword());
		var user = User.builder().firstName(request.getFirstname()).lastName(request.getLastname())
				.username(request.getUsername()).password(passwordEncoder.encode(request.getPassword()))
				.role(request.getRole()).build();
		userRepository.save(user);
		var jwtToken = jwtService.generateJwtToken(user);
		return AuthenticationResponse.builder().token(jwtToken).build();
	}

	public AuthenticationResponse authenticate(AuthenticationRequest request) {

		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		var user = userRepository.findByUsername(request.getUsername()).orElseThrow();
		var jwtToken = jwtService.generateJwtToken(user);

		return AuthenticationResponse.builder().token(jwtToken).build();

	}

}
