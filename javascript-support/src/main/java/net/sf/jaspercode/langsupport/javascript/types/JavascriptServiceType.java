package net.sf.jaspercode.langsupport.javascript.types;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.api.types.ServiceType;

public class JavascriptServiceType extends JavascriptType implements ServiceType {
	List<ServiceOperation> operations = new ArrayList<>();
	
	public JavascriptServiceType(String name) {
		super(name);
	}

	@Override
	public ServiceOperation getOperation(String name, String... params) {
		for(ServiceOperation op : operations) {
			if (op.getName().equals(name)) {
				if (op.getParamNames().size()!=params.length) continue;
				boolean match = true;
				for(int i=0;i<params.length;i++) {
					if (!op.getParamNames().get(i).equals(params[i])) {
						match = false;
						break;
					}
				}
				if (match) return op;
			}
		}
		return null;
	}

	@Override
	public void addOperation(ServiceOperation operation) {
		operations.add(operation);
	}

	@Override
	public List<ServiceOperation> getOperations() {
		return operations;
	}

}
