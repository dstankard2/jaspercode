package net.sf.jaspercode.patterns.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a set of related web services available in a path on the 
 * @author DCS
 *
 */
public class WebServiceDefinition {

	private WebServiceContext context = null;
	private String name = null;
	private String uri = null;
	private List<WebServiceOperation> operations = new ArrayList<>();

	public WebServiceDefinition(WebServiceContext context) {
		this.context = context;
	}

	public Map<String,WebServiceOperation> getUrisAndOperations() {
		Map<String,WebServiceOperation> ret = new HashMap<>();

		String contextRoot = context.getContextRoot();
		String modulePath = uri;
		
		if (!contextRoot.startsWith("/")) contextRoot = "/"+contextRoot;
		if (contextRoot.endsWith("/")) contextRoot = contextRoot.substring(0, contextRoot.length());
		
		if (!modulePath.startsWith("/")) modulePath = "/" + modulePath;
		if (modulePath.endsWith("/")) modulePath = modulePath.substring(0, modulePath.length());
		
		for(WebServiceOperation op : operations) {
			StringBuilder b = new StringBuilder();
			b.append(contextRoot).append(modulePath);
			String u = op.getPath();
			if (!u.startsWith("/")) u = "/" + u;
			b.append(u);
			ret.put(b.toString(),op);
		}
		
		return ret;
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public List<WebServiceOperation> getOperations() {
		return operations;
	}

	public void setOperations(List<WebServiceOperation> operations) {
		this.operations = operations;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}

