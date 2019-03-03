
function EventDispatcher() {
	var _listeners = { };

	var _debug = false;
	var _count = 0;

	var _ret = {
			addEventListener: function(name,callback) {
				var inst = this;

				if (_listeners[name]==undefined) {
					_listeners[name] = [];
				}
				var list = _listeners[name];
				if (list.indexOf(callback)<0) {
					list.push(callback);
					_count++;
					if (_debug) {
						console.log('Attached a callback to event '+name+' and now there are '+_count+' listeners');
					}
					return function() {
						inst.removeListener(name,callback);
					};
				} else {
					return function() {};
				}
			},
			// Dispatches the given event name
			dispatch: function(name) {
				var list = _listeners[name];
				if ((list) && (list.length)) {
					for(var i=0;i<list.length;i++) {
						list[i]();
					}
				}
			},
			event: function(name,fn) {
				if (!name) return undefined;
				if (fn) {
					return _ret.addEventListener(name,fn);
				} else {
					return _ret.dispatch(name);
				}
			},
			debug: function(value) {
				_debug = value;
			},
			removeListener: function(event,callback) {
				var listeners = _listeners[event];
				if ((!listeners) || (!listeners.length)) return;
				for(var i=0;i<listeners.length;i++) {
					if (listeners[i]==callback) {
						listeners.splice(i,1);
						_count--;
						if (_debug) {
							console.log('removed a listener for event '+event+' and now there are '+_count+' listeners');
						}
						return;
					}
				}
			}
	};
	
	return _ret;

}

