package com.abbink.steeldoor.service;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class AWSConfiguration {
	@NotEmpty
	@JsonProperty
	private String foo;
	
	public String getFoo() {
		return foo;
	}
}
