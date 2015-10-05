package com.github.naf.samples.jpa;

import java.util.Random;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Samples {
	@Id
	@GeneratedValue
	public UUID id;

	@Column
	public String key;

	@Column
	public String value;

	@Override
	public String toString() {
		return super.toString() + " i=" + id + " k=" + key + " v=" + value + " r=" + random;
	}

	public Samples() {
		System.err.println("SAMPLES INIT " + this);
	}

	static Random r = new Random();
	@Transient
	int random = r.nextInt();
}
