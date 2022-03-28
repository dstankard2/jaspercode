package net.sf.jaspercode.engine;

import java.io.File;
import java.io.FileReader;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.sf.jaspercode.api.config.ComponentSet;

public class ComponentFileReader {
	JAXBContext ctx = null;
	Unmarshaller um = null;
	
	public ComponentFileReader(Set<Class<?>> xmlClasses) throws EngineInitException {
		Class<?>[] classes = new Class<?>[xmlClasses.size()];
		xmlClasses.toArray(classes);
		try {
			ctx = JAXBContext.newInstance(classes);
			um = ctx.createUnmarshaller();
		} catch(JAXBException e) {
			throw new EngineInitException("Couldn't initialize component file reader", e);
		}
	}
	
	public ComponentSet readFile(File file) {
		ComponentSet ret = null;
		FileReader reader = null;

		try {
			reader = new FileReader(file);
			ret = (ComponentSet)um.unmarshal(reader);
		} catch(Exception e) {
			//e.printStackTrace();
			// no-op
		} finally {
			if (reader!=null) {
				try {
					reader.close();
				} catch(Exception e) { }
			}
		}
		if ((ret!=null) && (ret.getComponent().size()==0)) {
			System.out.println("WARN: Component File '"+file.getAbsolutePath()+"' had no components");
		}
		return ret;
	}

}
