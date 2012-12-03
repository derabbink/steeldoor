package com.abbink.steeldoor.service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonProperty;

import com.yammer.dropwizard.config.Configuration;

public class SteelDoorConfiguration extends Configuration {
	@Valid
	@NotNull
	@JsonProperty
	private AWSConfiguration aws = new AWSConfiguration();
	
	public AWSConfiguration getAWSConfiguration() {
		return aws;
	}
}
