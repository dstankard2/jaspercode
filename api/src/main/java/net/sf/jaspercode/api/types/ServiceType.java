package net.sf.jaspercode.api.types;

import java.util.List;

import net.sf.jaspercode.api.Code;

public interface ServiceType extends VariableType {

	public ServiceOperation getOperation(String name,String... params);
	public void addOperation(ServiceOperation operation);
	public Code instantiate(String ref);
	public List<ServiceOperation> getOperations();

}
