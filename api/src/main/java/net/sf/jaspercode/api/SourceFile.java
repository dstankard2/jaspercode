package net.sf.jaspercode.api;

import net.sf.jaspercode.api.exception.JasperException;

public interface SourceFile {

	/**
	 * Returns the source code for this file.  The framework will not invoke 
	 * this until all components have been processed.
	 * @return Source as it will be put into the file.
	 */
	public StringBuilder getSource();
	
	/**
	 * Query the file for its path relative to the output directory.
	 * @return Path relative to the output directory.
	 */
	public String getPath();
	
	/**
	 * FOR ENGINE'S INTERNAL USE ONLY.<br/>
	 * Create a copy of this source file for a component processor to make uncommited changes.
	 * @return
	 */
	public SourceFile copy();

}

