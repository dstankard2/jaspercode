package net.sf.jaspercode.patterns.files;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.SourceFile;

public class XmlFile implements SourceFile {

	private Document document = null;
	private String path = null;
	public static final DocumentFactory factory = new DocumentFactory();
	
	public XmlFile() {
		document = factory.createDocument();
	}
	
	public XmlFile(String path) {
		this();
		this.path = path;
	}
	
	public Document getDocument() {
		return document;
	}
	
	@Override
	public StringBuilder getSource() throws JasperException {
		StringBuilder build = new StringBuilder();
		build.append(document.asXML());
		return build;
	}

	@Override
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}
