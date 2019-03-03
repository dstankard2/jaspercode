package net.sf.jaspercode.patterns.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jaspercode.api.RuntimePlatform;
import net.sf.jaspercode.patterns.http.WebServiceDefinition;

public abstract class JavaWebappRuntimePlatform implements RuntimePlatform {
	protected String contextRoot = "";
	protected Map<String,String> servlets = new HashMap<>();
	protected Map<String,String> mappings = new HashMap<>();
	protected List<String> contextListeners = new ArrayList<>();
	protected Map<String,String> filters = new HashMap<>();
	protected Map<String,List<String>> filterMappings = new HashMap<>();
	protected Map<String,String> websocketEndpoints = new HashMap<>();
	protected List<WebServiceDefinition> services = new ArrayList<>();

	public List<String> getServletNames() {
		List<String> ret = new ArrayList<>();
		
		for(Entry<String,String> entry : servlets.entrySet()) {
			if (!ret.contains(entry.getKey())) {
				ret.add(entry.getKey());
			}
		}
		
		return ret;
	}
	
	public String getServletClass(String name) {
		return servlets.get(name);
	}
	
	public List<String> getMappings(String servletName) {
		List<String> ret = new ArrayList<>();
		
		for(Entry<String,String> entry : mappings.entrySet()) {
			if (entry.getValue().equals(servletName)) {
				ret.add(entry.getKey());
			}
		}
		
		return ret;
	}
	
	public void addServlet(String servletName, String servletClass) {
		servlets.put(servletName, servletClass);
	}

	public void addServletMapping(String uri, String servletName) {
		mappings.put(uri, servletName);
	}

	public void addServletContextListener(String className) {
		contextListeners.add(className);
	}

	public List<String> getServletContextListeners() {
		return contextListeners;
	}

	public void addFilter(String filterName,String className) {
		filters.put(filterName, className);
	}
	
	public Map<String,String> getFilters() {
		return filters;
	}

	public List<String> getFiltersForUri(String uri) {
		return filterMappings.get(uri);
	}
	
	public Map<String,List<String>> getFilterMappings() {
		return filterMappings;
	}

	public void addFilterMapping(String uri,String filter) {
		List<String> filters = filterMappings.get(uri);
		if (filters==null) {
			filters = new ArrayList<>();
			filterMappings.put(uri, filters);
		}
		filters.add(filter);
	}

	public void addWebsocketEndpoint(String url,String className) {
		websocketEndpoints.put(url, className);
	}
	public Map<String,String> getWebsocketEndpoints() {
		return websocketEndpoints;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}
	
	public String getContextRoot() {
		return this.contextRoot;
	}

}

