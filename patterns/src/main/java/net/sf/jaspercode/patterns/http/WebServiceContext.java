package net.sf.jaspercode.patterns.http;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a web service API that is available to a HTTP client at a particular path.
 * The contextRoot is the root path on the server host to the web service endpoints 
 * contained in this context.
 * 
 * For instance if the contextRoot is "/app1" then all web services available in this context 
 * will begin with path "protocol://<host>/app1" when invoked by a HTTP client.
 * @author DCS
 *
 */
public class WebServiceContext {

	private String contextRoot = null;

	private List<WebServiceDefinition> webServices = new ArrayList<>();

	public WebServiceContext() {
	}

	public String getContextRoot() {
		return contextRoot;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}

	public List<WebServiceDefinition> getWebServices() {
		return webServices;
	}

	public void setWebServices(List<WebServiceDefinition> webServices) {
		this.webServices = webServices;
	}
	
	public WebServiceDefinition getWebServiceDefinition(String name) {
		for(WebServiceDefinition def : webServices) {
			if (def.getName().equals(name)) {
				return def;
			}
		}
		return null;
	}
	
}
