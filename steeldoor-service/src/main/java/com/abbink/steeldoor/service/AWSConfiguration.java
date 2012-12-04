package com.abbink.steeldoor.service;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class AWSConfiguration {
	@NotEmpty
	@JsonProperty
	private String dbEndpoint;
	
	@NotEmpty
	@JsonProperty
	private String dbInstanceId;
	
	@NotEmpty
	@JsonProperty
	private String dbMasterUser;
	
	@NotEmpty
	@JsonProperty
	private String dbMasterPassword;
	
	@NotEmpty
	@JsonProperty
	private String dbName;
	
	@Min(1)
	@Max(65535)
	@JsonProperty
	private int dbPort;
	
	public String getDbEndpoint() {
		return dbEndpoint;
	}
	
	public String getDbInstanceId() {
		return dbInstanceId;
	}
	
	public String getDbMasterUser() {
		return dbMasterUser;
	}
	
	public String getDbMasterPassword() {
		return dbMasterPassword;
	}
	
	public String getDbName() {
		return dbName;
	}
	
	public int getDbPort() {
		return dbPort;
	}
}
