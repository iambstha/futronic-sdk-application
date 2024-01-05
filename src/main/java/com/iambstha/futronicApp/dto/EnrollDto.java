package com.iambstha.futronicApp.dto;

import com.iambstha.futronicApp.enums.IndividualType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrollDto {

	private String firstName;
	private String lastName;
	private IndividualType individualType ;
	
}
