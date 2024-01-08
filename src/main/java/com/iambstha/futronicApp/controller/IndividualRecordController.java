package com.iambstha.futronicApp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iambstha.futronicApp.model.FingerprintEntity;
import com.iambstha.futronicApp.service.IndividualRecordService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class IndividualRecordController {

	@Autowired
	private final IndividualRecordService individualRecordService;
	
	
	@GetMapping("/records")
	public ResponseEntity<List<FingerprintEntity>> getAllIndividualRecords(){
		return ResponseEntity.ok().body(individualRecordService.getAllIndividualRecords());
	}
}
