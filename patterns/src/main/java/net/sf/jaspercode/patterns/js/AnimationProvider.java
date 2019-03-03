package net.sf.jaspercode.patterns.js;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;

public interface AnimationProvider {

	public JavascriptCode show(String div,String animation,String duration,String onComplete) throws JasperException;
	public JavascriptCode hide(String elementId,String animation,String duration,String onComplete) throws JasperException;
	public String getName();

}

