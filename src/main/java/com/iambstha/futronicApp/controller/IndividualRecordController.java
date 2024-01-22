package com.iambstha.futronicApp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iambstha.futronicApp.model.FingerprintEntity;
import com.iambstha.futronicApp.service.IndividualRecordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/securedapi")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class IndividualRecordController {

	@Autowired
	private final IndividualRecordService individualRecordService;

	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Endpoint accessible only by users with the role 'ROLE_USER'")
	@GetMapping("/hello")
	public ResponseEntity<String> sayHello(Authentication auth) {
		return ResponseEntity.ok().body("Hello " + auth.getName() + " with authority " + auth.getAuthorities()
				+ " Principal" + auth.getPrincipal());
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Endpoint accessible only by users with the role 'ROLE_ADMIN'")
	@GetMapping("/records")
	public ResponseEntity<List<FingerprintEntity>> getAllIndividualRecords() {
		return ResponseEntity.ok().body(individualRecordService.getAllIndividualRecords());
	}
}
