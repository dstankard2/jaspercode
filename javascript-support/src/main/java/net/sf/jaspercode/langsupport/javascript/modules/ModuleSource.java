package net.sf.jaspercode.langsupport.javascript.modules;

public interface ModuleSource {

	public String getName();
	public StringBuilder getCode();
	// For use by the engine when managing source files.
	// The returned ModuleSource should be a copy of this one.
	public ModuleSource copy();

}

