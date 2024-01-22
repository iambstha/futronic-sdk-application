package com.iambstha.futronicApp.model.auth;

import com.iambstha.futronicApp.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

	private String firstname;
	
	private String lastname;
	
	private String username;
	
	private String password;
	
	private Role role;
	
}
