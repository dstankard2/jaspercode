package net.sf.jaspercode.engine.processing;

import java.lang.reflect.Method;
import java.util.Map;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.config.Property;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;
import net.sf.jaspercode.engine.files.ComponentFile;

public class ProcessingUtilities {

	public static Map<String,String> getConfigs(ComponentFile file, Component component) {
		Map<String,String> ret = null;
		ApplicationFolderImpl folder = file.getFolder();
		
		ret = folder.getProperties();
		//ret = folder.getJasperProperties();

		// Override folder properties with properties defined in the component file.
		for(Property prop : file.getComponentSet().getProperty()) {
			ret.put(prop.getName(), prop.getValue());
		}
		
		// Override with component-level configurations
		for(Property prop: component.getProperty()) {
			ret.put(prop.getName(), prop.getValue());
		}
		
		return ret;
	}
	
	public static boolean populateConfigurations(Object target,ProcessorLog log,Map<String,String> configs) {
		Class<?> compClass = target.getClass();
		Method[] methods = compClass.getMethods();
		
		for(Method method : methods) {
			ConfigProperty prop = method.getDeclaredAnnotation(ConfigProperty.class);
			if (prop!=null) {
				if (!handleConfigProperty(prop, method, target, log, configs)) {
					return false;
				}
			}
		}
		return true;
	}
	// Returns true if preprocessing is successful, false otherwise
	private static boolean handleConfigProperty(ConfigProperty property, Method setter, Object target, ProcessorLog log, Map<String,String> configs) {
		//boolean ret = false;
		String configValue = null;
		Class<?>[] params = setter.getParameterTypes();
		Object valueObj = null;

		if (params.length!=1) {
			log.error("A method annotated with @ConfigProperty must take a single parameter which is either string or integer or boolean");
			return false;
		}
		
		boolean required = property.required();
		String name = property.name();

		configValue = configs.get(name);
		if ((configValue==null) && (required)) {
			log.error("Configuration property '"+name+"' is required");
			return false;
		}

		if (configValue!=null) {
			if (params[0]==String.class) {
				valueObj = configValue;
			}
			else if (params[0]==Integer.class) {
				try {
					valueObj = Integer.parseInt(configValue);
				} catch(NumberFormatException e) {
					log.error("Configuration '"+name+"' must be an integer");
					return false;
				}
			}
			else if (params[0]==Boolean.class) {
				valueObj = Boolean.FALSE;
				if (configValue.equalsIgnoreCase("true")) valueObj = Boolean.TRUE;
				else if (configValue.equalsIgnoreCase("T")) valueObj = Boolean.TRUE;
				else if (configValue.equalsIgnoreCase("Y")) valueObj = Boolean.TRUE;
				else if (configValue.equalsIgnoreCase("false")) valueObj = Boolean.FALSE;
				else if (configValue.equalsIgnoreCase("F")) valueObj = Boolean.TRUE;
				else if (configValue.equalsIgnoreCase("N")) valueObj = Boolean.TRUE;
				else {
					log.error("Found invalid value '"+configValue+"' for boolean configuration '"+name+"'");
					return false;
				}
			}
		}
		
		try {
			setter.invoke(target, valueObj);
		} catch(Exception e) {
			log.error("Couldn't call setter for config property '"+name+"'", e);
			return false;
		}

		return true;
	}
	
}
