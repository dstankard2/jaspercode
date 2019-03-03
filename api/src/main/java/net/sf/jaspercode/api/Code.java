package net.sf.jaspercode.api;

/**
 * Represents a snippet of generated code.  The language of the code is not specified.
 * @author DCS
 */
public interface Code {

	/**
	 * Returns the inderlying code.
	 * @return Generated code.
	 * @throws JavascribeException If there is a problem.
	 */
	public String getCodeText();
	
	/**
	 * Appends the specified string to the end of this code piece.
	 * @param s Code to append.
	 * @throws JavascribeException If there is a problem.
	 */
	public void appendCodeText(String s);

	public void append(Code append);

}

