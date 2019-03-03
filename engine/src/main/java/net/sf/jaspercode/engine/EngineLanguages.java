package net.sf.jaspercode.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.jaspercode.api.langsupport.LanguageSupport;

public class EngineLanguages {
	Map<String,LanguageSupport> languages = new HashMap<>();
	private PluginManager pluginManager = null;

	public EngineLanguages(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public void findLanguages() throws EngineInitException {
		try {
			Set<Class<LanguageSupport>> classes = pluginManager.getPluginSubclasses(LanguageSupport.class);
			for(Class<LanguageSupport> cl : classes) {
				LanguageSupport lang = cl.newInstance();
				languages.put(lang.getLanguageName(), lang);
			}
		} catch(Exception e) {
			throw new EngineInitException("Couldn't initialize language support",e);
		}
	}
	
	public LanguageSupport getLanguageSupport(String lang) {
		return languages.get(lang);
	}
	
}
