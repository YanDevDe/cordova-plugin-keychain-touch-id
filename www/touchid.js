var argscheck = require('cordova/argscheck'),
               exec = require('cordova/exec');
               
var touchID = {
	isAvailable: function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "TouchID", "isAvailable", []);
	},
	save: function(key,password, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "TouchID", "save", [key,password]);
	},
	verify: function(key,message,successCallback, errorCallback){
		exec(successCallback, errorCallback, "TouchID", "verify", [key,message]);
	},
	has: function(key,successCallback, errorCallback){
		exec(successCallback, errorCallback, "TouchID", "has", [key]);
	},
	delete: function(key,successCallback, errorCallback){
		exec(successCallback, errorCallback, "TouchID", "delete", [key]);
	}
};

module.exports = touchID;