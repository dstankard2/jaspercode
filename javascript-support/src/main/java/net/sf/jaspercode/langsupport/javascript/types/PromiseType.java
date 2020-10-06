package net.sf.jaspercode.langsupport.javascript.types;

public abstract class PromiseType extends JavascriptServiceType {

	public PromiseType() {
		super("Promise");
	}
	
	public abstract String getResolveTypeName();

	public abstract JavascriptType getResolveType();

	protected void name(String name) {
		this.name = name;
	}

	public static PromiseType noResultPromise(String typeName) {
		PromiseType ret = new PromiseType() {
			
			@Override
			public JavascriptType getResolveType() {
				return null;
			}

			@Override
			public String getResolveTypeName() {
				return null;
			}

		};
		ret.name(typeName);
		return ret;
	}

	public static PromiseType getPromise(JavascriptType resolveType,String typeName) {
		PromiseType ret = new PromiseType() {

			@Override
			public JavascriptType getResolveType() {
				return resolveType;
			}

			@Override
			public String getResolveTypeName() {
				return resolveType.getName();
			}

		};
		ret.name(typeName);
		return ret;
	}
	
}

