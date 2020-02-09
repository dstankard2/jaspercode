package net.sf.jaspercode.patterns.js.template.parsing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;

public class TemplateParser {
	private String template;
	private ProcessorContext ctx;
	private String obj;
	private ServiceOperation serviceOperation = null;
	private List<Pair<String,String>> imports = new ArrayList<>();

	public TemplateParser(String template,ProcessorContext ctx,String obj,ServiceOperation fn) {
		this.template = template;
		this.ctx = ctx;
		this.obj = obj;
		this.serviceOperation = fn;
	}
	
	public JavascriptCode generateJavascriptCode(CodeExecutionContext execCtx) throws JasperException {
		StringBuilder b = new StringBuilder();
		JavascriptCode ret = new JavascriptCode();
		Document doc = null;
		b.append("var "+DirectiveUtils.DOCUMENT_REF+" = document;\n");

		String finalTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"><body>" + template + "</body>";
		doc = Jsoup.parse(finalTemplate, "", Parser.xmlParser());
		//doc = Jsoup.parseBodyFragment(template);
		
		List<Node> docNodeList = doc.childNodes();
		List<ElementParser> rootParsers = new ArrayList<ElementParser>();
		List<Node> bodyChildren = null;

		boolean hasElement = false;
		// First node is xml instruction.  Second is the wrapping body element
		Element bodyElement = (Element)docNodeList.get(1);
		bodyChildren = bodyElement.childNodes();

		/*
		boolean hasElement = false;
		// Root node is html.  Get right to the children
		List<Node> htmlChildren = docNodeList.get(0).childNodes();
		
		// Second child of html is body.  Get its children
		bodyChildren = htmlChildren.get(1).childNodes();
		*/
		
		for(Node node : bodyChildren) {
			if (node instanceof Element) {
				ElementParser p = new ElementParser((Element)node,ctx,null,obj,serviceOperation,new ArrayList<String>(),this);
				if ((p.isDomElement()) && (hasElement)) {
					throw new JasperException("A HTML template can only have one element at the root.");
				}
				if (p.isDomElement()) {
					hasElement = true;
				}
				rootParsers.add(p);
			} else if (node instanceof TextNode) {
				String text = ((TextNode)node).getWholeText();
				if (text.trim().length()>0) {
					throw new JasperException("A HTML template may not have text in the DOM root");
				}
			}
		}
		
		for(ElementParser p : rootParsers) {
			p.parseElement(execCtx);
			b.append(p.getElementCode());
			ret.getImportedModules().addAll(p.getImports());
		}

		b.append("return _e0;\n");
		ret.appendCodeText(b.toString());
		ret.getImportedModules().addAll(imports);
		return ret;
	}

	public String processChildNodeList(List<Node> nodeList,String containerVar,CodeExecutionContext execCtx) throws JasperException {
		StringBuilder b = new StringBuilder();
		List<String> children = new ArrayList<>();
		
		b.append("(function() {\n");

		CodeExecutionContext newCtx = new CodeExecutionContext(execCtx);
		for(Node node : nodeList) {
			if (node.toString().trim().length()>0) {
				b.append(parseNode(containerVar,node,newCtx,children));
			}
		}
		
		b.append("})();\n");
		
		return b.toString();
	}

	public String parseNode(String containerVar,Node node,CodeExecutionContext execCtx,List<String> previousEltVars) throws JasperException {
		String ret = null;
		
		if (node instanceof Element) {
			Element elt = (Element)node;
			ElementParser parser = new ElementParser(elt,ctx,containerVar,obj,serviceOperation,previousEltVars,this);
			parser.parseElement(execCtx);
			ret = parser.getElementCode();
			//List<ModuleImport> imports = parser.getImports();
			this.imports.addAll(parser.getImports());
			String var = parser.getEltVar();
			if (var!=null) previousEltVars.add(var);
		} else if (node instanceof TextNode) {
			ret = processTextNode((TextNode)node,containerVar,execCtx);
		} else {
			// TODO: Handle cases
			ret = "";
		}
		
		return ret;
	}

	protected static String processTextNode(TextNode n,String containerVar,CodeExecutionContext execCtx) throws JasperException {
		//String text = n.outerHtml().trim();
		String text = n.text().trim();
		String docRef = DirectiveUtils.DOCUMENT_REF;
		String finalRef = DirectiveUtils.parsePartialExpression(text, execCtx);
		String var = DirectiveUtils.newVarName("_t","DOMElement",execCtx);
		StringBuilder code = new StringBuilder();

		if (text.trim().length()==0) return null;

		code.append("try {\n");
		code.append("var "+var+" = "+docRef+".createTextNode(");
		code.append(finalRef);
		code.append(");\n");

		code.append(containerVar+".appendChild("+var+");\n");
		code.append("}catch(_err){}\n");
		return code.toString();
	}
	
}

