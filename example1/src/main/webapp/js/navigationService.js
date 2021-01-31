
var pages = {};
var currentPage = null;
var selectCallbacks = [];
var currentPageName = null;

function _switchPage(pageName,isPop) {

	// if not switching pages then do nothing
	if (pages[pageName]==currentPage) {
		return;
	}

	if (currentPage) {
		currentPage.event('hide');
		// To be uncommented when page templates are started
		currentPage.view.style.display = 'none';
		currentPage = null;
		currentPageName = null;
	}
	currentPage = pages[pageName];
	currentPageName = pageName;
	if (currentPage) {
		currentPage.event('show');
		// To be uncommented when page templates are started
		currentPage.view.style.display = '';
		if (!isPop) {
			window.history.pushState({
				show: pageName
			}, null,'?page='+pageName);
		}
		selectCallbacks.forEach((cb) => {
			cb(pageName);
		});
	}
}

window.onpopstate = function(event) {
	if ((event.state) && (event.state.show)) {
		//console.log('pop state switch to '+event.state.show);
		_switchPage(event.state.show,true);
	}
}

function _addPage(pageName,page) {
	if (pages[pageName]) {
		console.error('Added a second page named '+pageName);
	} else {
		pages[pageName] = page;
		page.view.style.display = 'none';
	}
}

function _refreshPage() {
	if (currentPage) {
		currentPage.event('refresh');
	}
}

function _selectCallback(callback) {
	selectCallbacks.push(callback);
}

function _currentPageName() {
	return currentPageName;
}

export const navigationService = {
	
	switchPage: _switchPage,
	addPage: _addPage,
	refreshPage: _refreshPage,
	selectCallback: _selectCallback,
	currentPageName: _currentPageName

};

