package net.sf.jaspercode.engine.processing;

import java.lang.reflect.Method;

import net.sf.jaspercode.api.annotation.ConfigProperty;

public abstract class ConfigurableProcessable implements Processable {

	protected ProcessorLog log = null;

	protected abstract String getProperty(String name);
	
	protected boolean populateConfigurations(Object target) {
		Class<?> compClass = target.getClass();
		Method[] methods = compClass.getMethods();
		
		for(Method method : methods) {
			ConfigProperty prop = method.getDeclaredAnnotation(ConfigProperty.class);
			if (prop!=null) {
				if (!handleConfigProperty(prop, method, target)) {
					return false;
				}
			}
		}
		return true;
	}
	// Returns true if preprocessing is successful, false otherwise
	private boolean handleConfigProperty(ConfigProperty property, Method setter, Object target) {
		//boolean ret = false;
		String configValue = null;
		Class<?>[] params = setter.getParameterTypes();
		Object valueObj = null;

		if (params.length!=1) {
			this.log.error("A method annotated with @ConfigProperty must take a single parameter which is either string or integer or boolean");
			return false;
		}
		
		boolean required = property.required();
		String name = property.name();

		configValue = getProperty(name);
		if ((configValue==null) && (required)) {
			this.log.error("Configuration property '"+name+"' is required");
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
					this.log.error("Configuration '"+name+"' must be an integer");
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
					this.log.error("Found invalid value '"+configValue+"' for boolean configuration '"+name+"'");
					return false;
				}
			}
		}
		
		try {
			setter.invoke(target, valueObj);
		} catch(Exception e) {
			this.log.error("Couldn't call setter for config property '"+name+"'", e);
		}

		return true;
	}

}
