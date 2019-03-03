package net.sf.jaspercode.patterns.js.page;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.PatternPriority;

@Plugin
public class PageBuilderComponent extends Component {

	public int getPriority() {
		return PatternPriority.PAGE_BUILDER;
	}

	private PageInfo pageInfo = null;
	
	public PageBuilderComponent(PageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}

	public PageInfo getPageInfo() {
		return pageInfo;
	}
	
	@Override
	public String getComponentName() {
		return "PageBuilder[page=\""+pageInfo.getName()+"\"]";
	}

}

