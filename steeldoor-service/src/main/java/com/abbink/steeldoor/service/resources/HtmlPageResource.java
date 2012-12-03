package com.abbink.steeldoor.service.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.abbink.steeldoor.service.views.IndexView;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class HtmlPageResource {
	
	@GET
	public IndexView index() {
		return new IndexView();
	}
}
