package net.sf.jaspercode.patterns;

public class PatternPriority {

	public static final int EMBED_TOMCAT_JAR = 0;

	public static final int STATIC_CONTENT_APP = 0;

	public static final int JAVA_ENUM = 0;

	public static final int TABLE_SET = 5000;

	public static final int PERSISTENCE_UNIT = 7500;

	public static final int THREAD_LOCAL_ENTITY_MANAGER = 8750;
	
	public static final int JPA_DAO_FACTORY = 10000;

	public static final int SEARCH_INDEX = 12500;

	public static final int CLASSIFICATION = 13750;

	public static final int DATA_OBJECT = 15000;

	public static final int JSON_OBJECT = 16250;

	public static final int APPLY_CLASSIFICATION = 18125;

	public static final int DOMAIN_DATA_RULES = 18750;

	public static final int DOMAIN_SERVICE = 20000;

	public static final int DOMAIN_DATA_TRANSLATOR = 22500;

	public static final int DOMAIN_RULE = 25000;

	public static final int HANDWRITTEN_DOMAIN_RULE = 27500;
	
	public static final int BUSINESS_SERVICE = 30000;

	public static final int SERVLET_FILTER = 35000;

	public static final int SERVLET_FILTER_GROUP = 37500;

	public static final int SERVLET_MODULE = 38750;

	public static final int SERVLET_ENDPOINT = 40000;

	public static final int SERVLET_ENDPOINT_BUILDER = 50000;

	public static final int HANDWRITTEN_JS_MODULE = 52500;

	public static final int WS_CLIENT = 55000;

	public static final int PAGE = 60000;

	public static final int PAGE_MODEL = 65000;

	public static final int PAGE_FN = 67500;

	public static final int HTML_TEMPLATE = 70000;

	public static final int PAGE_BUILDER = 75000;

	public static final int SASS_FILES = 80000;

}
