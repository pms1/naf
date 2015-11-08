package com.github.naf.samples.jpa;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class S2 {
	@Id
	@GeneratedValue
	private UUID id;

	public UUID getId() {
		return id;
	}

	@Column
	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
