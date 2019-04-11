package net.sf.jaspercode.patterns.files;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXReader;
import net.sf.jaspercode.api.SourceFile;

public class XmlFile implements SourceFile {

	private Document document = null;
	public static final DocumentFactory factory = new DocumentFactory();
	private String path = null;
	
	public XmlFile(String path) {
		this.path = path;
		document = factory.createDocument();
	}
	
	private XmlFile(String path,Document document) {
		this(path);
		this.document = document;
	}
	
	public String getPath() {
		return path;
	}

	public Document getDocument() {
		return document;
	}
	
	@Override
	public StringBuilder getSource() {
		StringBuilder build = new StringBuilder();
		build.append(document.asXML());
		return build;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public SourceFile copy() {
		try {
			StringWriter writer = new StringWriter();
			Document copy = null;
			XmlFile ret = null;

			this.document.write(writer);
			writer.flush();
			StringBuffer buf = writer.getBuffer();
			SAXReader reader = new SAXReader();
			copy = reader.read(new ByteArrayInputStream(buf.toString().getBytes()));
			
			ret = new XmlFile(path,copy);

			return ret;
		} catch(DocumentException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
