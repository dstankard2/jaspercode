package net.sf.jaspercode.patterns.js.template.parsing;

public abstract class AttributeDirectiveBase implements AttributeDirective,Comparable<AttributeDirective> {

	@Override
	public int getPriority() { return 10; }

	@Override
	public int compareTo(AttributeDirective other) {
		return getPriority() - other.getPriority();
	}

}

