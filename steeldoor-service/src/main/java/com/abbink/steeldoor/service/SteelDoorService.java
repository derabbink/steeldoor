package com.abbink.steeldoor.service;

import com.abbink.steeldoor.service.resources.HtmlPageResource;
import com.google.common.cache.CacheBuilderSpec;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.bundles.AssetsBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

public class SteelDoorService extends Service<SteelDoorConfiguration> {
	public static void main(String[] args) throws Exception {
		new SteelDoorService().run(args);
	}
	
	private SteelDoorService() {
		super("steeldoor");
		
		addBundle(new ViewBundle());
		
		// By default a restart will be required to pick up any changes to assets.
		// Use the following spec to disable that behaviour, useful when developing.
		CacheBuilderSpec cacheSpec = CacheBuilderSpec.disableCaching();
		//CacheBuilderSpec cacheSpec = AssetsBundle.DEFAULT_CACHE_SPEC;
		addBundle(new AssetsBundle("/assets/", cacheSpec, "/assets/"));
	}
	
	@Override
	protected void initialize(SteelDoorConfiguration configuration, Environment environment) {
//		final String template = configuration.getMessageConfiguration().getTemplate();
//		final String defaultMessage = configuration.getMessageConfiguration().getDefaultMessage();
//		environment.addResource(new HelloWorldResource(template, defaultName));
//		environment.addHealthCheck(new TemplateHealthCheck(template));
		environment.addResource(new HtmlPageResource());
//		environment.addResource(new MessageResource(template, defaultMessage));
	}
}
