package net.sf.jaspercode.patterns.js.page;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptServiceType;

public class PageInfo {

	private String name = null;
	private String pageRendererObj = null;
	private ServiceOperation pageRendererRule = null;
	private List<PageFnDef> functions = new ArrayList<>();
	private PageModelType modelType = null;
	private JavascriptServiceType pageType = null;

	public String getPageRendererObj() {
		return pageRendererObj;
	}
	public void setPageRendererObj(String pageRendererObj) {
		this.pageRendererObj = pageRendererObj;
	}
	public ServiceOperation getPageRendererRule() {
		return pageRendererRule;
	}
	public void setPageRendererRule(ServiceOperation pageRendererRule) {
		this.pageRendererRule = pageRendererRule;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<PageFnDef> getFunctions() {
		return functions;
	}
	public void setFunctions(List<PageFnDef> functions) {
		this.functions = functions;
	}
	public PageModelType getModelType() {
		return modelType;
	}
	public void setModelType(PageModelType modelType) {
		this.modelType = modelType;
	}
	public JavascriptServiceType getPageType() {
		return pageType;
	}
	public void setPageType(JavascriptServiceType pageType) {
		this.pageType = pageType;
	}

}
