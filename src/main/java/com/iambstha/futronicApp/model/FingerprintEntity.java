package com.iambstha.futronicApp.model;

import java.util.Date;

import com.futronic.SDKHelper.FtrIdentifyRecord;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	@Column
	private byte[] m_Key;
	
	@Column
	private String m_UserName;

	@Column
	private byte[] m_Template;
	
    @Column
    private Date timestamp;

	public FingerprintEntity() {
		m_UserName = "";
		// Generate user's unique identifier
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
