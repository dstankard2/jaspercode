package net.sf.jaspercode.patterns.js.page;

import java.util.ArrayList;
import java.util.List;

public class PageFnDef {

	private String name = null;
	private List<String> params = new ArrayList<>();
	private String returnType = null;
	private String code = null;
	private String event = null;

	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getParams() {
		return params;
	}
	public void setParams(List<String> params) {
		this.params = params;
	}
	public String getReturnType() {
		return returnType;
	}
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

}
