package net.sf.jaspercode.api.resources;

/**
 * 
 * @author DCS
 */
public interface ApplicationResource {

	/**
	 * Returns the name of this folder or file
	 * @return File name
	 */
	public String getName();
	
	/**
	 * Returns the path of this DefinitionResource under the application root directory.
	 * @return Absolute path to this file
	 */
	public String getPath();
	
}
