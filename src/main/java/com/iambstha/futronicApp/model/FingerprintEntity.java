package com.iambstha.futronicApp.model;
/*
 * FingerprintEntity.java
 */
import java.util.Date;

import com.futronic.SDKHelper.FtrIdentifyRecord;
import com.iambstha.futronicApp.enums.IndividualType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


/**
 * Entity of fingerprint schema
 *
 * @author Bishal Shrestha
 */

@Entity
@Table(name = "fingerprint_entity")
@Data
@Getter
@Setter
public class FingerprintEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private byte[] m_Key;
	
	@Column
	private String first_name;
	
	@Column
	private String last_name;
	
	@Column
	private IndividualType individual_type;

	@Column
	private byte[] m_Template;
	
    @Column
    private Date timestamp;

	public FingerprintEntity() {
		first_name = "";
		last_name ="";
		individual_type = IndividualType.employee;
		m_Key = new byte[16];
		java.util.UUID guid = java.util.UUID.randomUUID();
		long itemHigh = guid.getMostSignificantBits();
		long itemLow = guid.getLeastSignificantBits();
		for (int i = 7; i >= 0; i--) {
			m_Key[i] = (byte) (itemHigh & 0xFF);
			itemHigh >>>= 8;
			m_Key[8 + i] = (byte) (itemLow & 0xFF);
			itemLow >>>= 8;
		}
		m_Template = null;
		timestamp = new Date();
	}

	public FtrIdentifyRecord getFtrIdentifyRecord() {
		FtrIdentifyRecord r = new FtrIdentifyRecord();
		r.m_KeyValue = m_Key;
		r.m_Template = m_Template;

		return r;
	}

}
