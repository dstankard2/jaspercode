package net.sf.jaspercode.api.resources;

import java.io.IOException;
import java.io.InputStream;

public interface ApplicationFile extends ApplicationResource {

	public InputStream getInputStream() throws IOException;
	
	public ApplicationFolder getFolder();
	
}
