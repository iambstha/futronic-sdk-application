package com.iambstha.futronicApp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iambstha.futronicApp.model.FingerprintEntity;
import com.iambstha.futronicApp.repository.FingerprintRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IndividualRecordServiceImpl implements IndividualRecordService {

	@Autowired
	private final FingerprintRepository fingerprintRepository;
	
	@Override
	public List<FingerprintEntity> getAllIndividualRecords() {
		return fingerprintRepository.findAll();
	}

}
