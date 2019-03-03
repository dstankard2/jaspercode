package net.sf.jaspercode.langsupport.java.types.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.api.types.ServiceType;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.types.JavaVariableTypeBase;

public class JavaServiceType extends JavaVariableTypeBase implements ServiceType {
	private List<ServiceOperation> operations = new ArrayList<>();
	private List<String> dependencyNames = new ArrayList<>();

	@Override
	public List<ServiceOperation> getOperations() {
		return operations;
	}

	public JavaServiceType(String className,String im,BuildContext buildCtx) {
		super(className,im,buildCtx);
	}
	
	@Override
	public JavaCode instantiate(String name) {
		return new JavaCode(name+" = new "+getClassName()+"();\n");
	}

	public List<ServiceOperation> getOperations(String name) {
		List<ServiceOperation> ret = new ArrayList<>();
		
		for(ServiceOperation op : operations) {
			if (op.getName().equals(name)) {
				ret.add(op);
			}
		}
		
		return ret;
	}

	@Override
	public ServiceOperation getOperation(String name, String... params) {
		for(ServiceOperation op : operations) {
			if (op.getName().equals(name)) {
				List<String> paramNames = op.getParamNames();
				if (paramNames.size()!=params.length) continue;
				for(int i=0;i<params.length;i++) {
					if (!params[i].equals(paramNames.get(i))) {
						continue;
					}
				}
				return op;
			}
		}
		return null;
	}

	@Override
	public void addOperation(ServiceOperation operation) {
		operations.add(operation);
	}

	public void addDependency(String name) {
		dependencyNames.add(name); 
	}

	public List<String> getDependencyNames() {
		return dependencyNames;
	}
	
}
