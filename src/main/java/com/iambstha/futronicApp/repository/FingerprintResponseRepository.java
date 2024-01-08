package com.iambstha.futronicApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.iambstha.futronicApp.model.FingerprintResponse;

import jakarta.transaction.Transactional;

public interface FingerprintResponseRepository extends JpaRepository<FingerprintResponse, Long> {

	
	@Transactional
	@Modifying
	@Query(value = "UPDATE response_entity SET message = :msg WHERE id = 1", nativeQuery = true)
	public void updateResponseMessage(String msg);
	
	@Transactional
	@Query(value = "select * from response_entity", nativeQuery = true)
	public FingerprintResponse getResponseMessage();
	
	
}
