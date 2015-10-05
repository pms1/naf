package com.github.naf.samples.jpa;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.h2.util.DateTimeUtils;

public class C {
	public static void main(String[] args) {
		// Date x = new Date(2015 - 1900, 10 - 1, 4, 13, 14, 15);

		Timestamp x = new Timestamp(new Date().getTime());

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
		System.err.println(DateTimeUtils.convertTimestamp(x, calendar));
		calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		System.err.println(DateTimeUtils.convertTimestamp(x, calendar));
	}
}
