var AppDispatcher = require('../dispatcher/AppDispatcher');
var EventEmitter = require('events').EventEmitter;
var AppConstants = require('../constants/AppConstants');
var assign = require('object-assign');
var _ = require('lodash');

var _data = {};

var CHANGE_EVENT = 'change';

var NodeSlowLogsStore = assign({}, EventEmitter.prototype, {
    getSlowLogs: function(clusterName) {
        return _data[clusterName];
    },
    emitChange: function() {
        this.emit(CHANGE_EVENT);
    },
    addChangeListener: function(callback) {
        this.on(CHANGE_EVENT, callback);
    },
    removeChangeListener: function(callback) {
        this.removeListener(CHANGE_EVENT, callback);
    }
});

AppDispatcher.register(function(action) {
    switch(action.actionType) {
        case AppConstants.GET_NODE_SLOWLOGS:
            var newData = {};
            newData[action.clusterName] = action.data;
            _data = newData;
            NodeSlowLogsStore.emitChange();
            break;
        default:
            // no operation
    }
});

module.exports = NodeSlowLogsStore;
