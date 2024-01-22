package com.iambstha.futronicApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.iambstha.futronicApp.model.FingerprintLogs;

import jakarta.transaction.Transactional;

public interface FingerprintLogsRepository extends JpaRepository<FingerprintLogs, Long> {

	
	@Transactional
	@Modifying
	@Query(value = "UPDATE response_entity SET message = :msg WHERE id = 1", nativeQuery = true)
	public void updateFingerprintMessage(String msg);
	
	@Transactional
	@Modifying
	@Query(value = "INSERT INTO response_entity (message, timestamp) VALUES (:msg, CURRENT_TIMESTAMP)", nativeQuery = true)
	public void logFingerprintMessage(String msg);
	
	@Transactional
	@Query(value = "select * from response_entity", nativeQuery = true)
	public FingerprintLogs getFingerprintLogs();
	
	
}
