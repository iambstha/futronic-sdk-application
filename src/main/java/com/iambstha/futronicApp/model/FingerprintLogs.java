package com.iambstha.futronicApp.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "response_entity")
@Getter
@Setter
public class FingerprintLogs {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "message")
	private String message;
	
	@Column
	private Date timestamp;
	
	public FingerprintLogs() {
		timestamp = new Date();
	}
	
}
