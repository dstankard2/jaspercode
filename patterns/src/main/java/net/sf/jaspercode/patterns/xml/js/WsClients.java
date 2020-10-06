package net.sf.jaspercode.patterns.xml.js;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.PatternPriority;

@XmlConfig
@Plugin
@XmlRootElement(name="wsClients")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="wsClients",propOrder={ "webService" })
public class WsClients extends Component {

	@XmlElement
	private List<WebService> webService = new ArrayList<>();
	@XmlAttribute
	private String buildContextName = "";
	@XmlTransient
	private String preprocessing = "";
	@XmlTransient
	private String ajaxProvider = "";
	@XmlAttribute
	private String urlPrefix = "";
	
	public List<WebService> getWebService() {
		return webService;
	}

	public void setWebService(List<WebService> webService) {
		this.webService = webService;
	}

	public String getBuildContextName() {
		return buildContextName;
	}

	public void setBuildContextName(String buildContextName) {
		this.buildContextName = buildContextName;
	}

	public int getPriority() {
		return PatternPriority.WS_CLIENT;
	}

	@ConfigProperty(required = false, name = "wsClient.requestPreprocess",
			description = "API to invoke before a AJAX web service call", example = "window.service.preprocessRequest")
	public void setPreprocessing(String preprocessing) {
		this.preprocessing = preprocessing;
	}
	
	public String getPreprocessing() {
		return this.preprocessing;
	}
	
	@ConfigProperty(required = false, name = "wsClient.ajaxProvider",
			description = "Name of provider for WS Client processing (defaults to 'XMLHttpRequest')", example = "'Fetch' or 'XMLHttpRequest'")
	public void setAjaxProvider(String ajaxProvider) {
		this.ajaxProvider = ajaxProvider;
	}
	
	public String getAjaxProvider() {
		return this.ajaxProvider;
	}

	public String getUrlPrefix() {
		return urlPrefix;
	}

	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}

	@Override
	public String getComponentName() {
		return "WsClients["+urlPrefix+"/*]";
	}

}

