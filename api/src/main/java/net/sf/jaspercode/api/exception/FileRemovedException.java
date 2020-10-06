package net.sf.jaspercode.api.exception;

/**
 * Thrown by a source file if the file is obsolete based on component definitions.<br/>
 * All components that originate the source file will be unloaded and re-evaluated.<br/>
 * This happens if a component is unloaded which 
 * @author DCS
 *
 */
public class FileRemovedException extends JasperException {
	private static final long serialVersionUID = 1L;

	public FileRemovedException() {
		// no-op
	}

}

