package com.iambstha.futronicApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iambstha.futronicApp.model.FingerprintEntity;

@Repository
public interface FingerprintRepository extends JpaRepository<FingerprintEntity, byte[]> {

}
