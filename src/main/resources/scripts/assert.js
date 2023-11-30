// env_context is a global container, will pass through all test case.
if (!Array.isArray) {
	Array.isArray = function (arg) {
		return Object.prototype.toString.call(arg) === '[object Array]'
	}
}

var Assert = {};

Assert.assertEmpty = function(actual, message) {
	if (actual === undefined || actual === null || actual === "" ) {
		return;
	}
	if (message == undefined || message == '') {
		throw "Expectation is empty, actuality is" + actual;
	} else {
		throw message;
	}
}

Assert.assertNotEmpty = function(actual, message) {
	if (!(actual === undefined || actual === null || actual === "")) {
    		return;
    }
	if (message == undefined || message == '') {
		throw "Expectation is not empty, actuality is empty";
	} else {
		throw message;
	}
}

Assert.assertTrue = function(actual, message) {
	if (actual) {
		return;
	}
	if (message == undefined || message == '') {
		throw "Expectation is true, actuality is" + actual;
	} else {
		throw message;
	}
}

Assert.assertFalse = function(actual, message) {
	if (!actual) {
		return;
	}
	if (message == undefined || message == '') {
		throw "Expectation is false, actuality is" + actual;
	} else {
		throw message;
	}
}

Assert.assertEquals = function(expected, actual, message) {
	if (expected == actual) {
		return;
	}
	if (message == undefined || message == '') {
		throw "Expectation is " + expected + ", actuality is" + actual;
	} else {
		throw message;
	}
}

Assert.assertNotEquals = function(expected, actual, message) {
	if (expected != actual) {
		return;
	}
	if (message == undefined || message == '') {
		throw "Expectation" + expected + ", actuality" + actual;
	} else {
		throw message;
	}
}

Assert.assertArrayEquals = function(expected, actual, message) {
	if (expected == undefined && actual == undefined) {
		return;
	}
	if (expected == undefined || actual == undefined) {
		if (message == undefined || message == '') {
			throw "Expect " + expected + " and " + actual + " to be equal, but not actually equal.";
		} else {
			throw message;
		}
	}
	if (expected.sort().toString() !== actual.sort().toString()) {
		if (message == undefined || message == '') {
			throw "Expect " + expected + " and " + actual + " to be equal, but not actually equal.";
		} else {
			throw message;
		}
	}
}

Assert.assertArrayNotEquals = function(expected, actual, message) {
	if (expected == undefined && actual == undefined) {
		if (message == undefined || message == '') {
			throw "Expect " + expected + " and " + actual + " not to be equal, but actually equal.";
		} else {
			throw message;
		}
	}
	if (expected == undefined || actual == undefined) {
		return;
	}

	if (expected.sort().toString() === actual.sort().toString()) {
		if (message == undefined || message == '') {
			throw "Expect " + expected + " and " + actual + "not to be equal, but actually equal.";
		} else {
			throw message;
		}
	}
}

Assert.fail = function(message) {
	if (message == undefined || message == '') {
		throw "Fail."
	} else {
		throw message;
	}
}

Assert.assertContains = function(expected, actual, message) {
	var flag = actual.indexOf(expected) != -1
	if (flag) {
		return flag;
	}
	if (message == undefined || message == '') {
		throw actual + " is not include " + expected + " 。";
	} else {
		throw message;
	}
}

Assert.assertNotContains = function(expected, actual, message) {
	var flag = actual.indexOf(expected) == -1
	if (flag) {
		return flag;
	}
	if (message == undefined || message == '') {
		throw actual + " contains " + expected + ".";
	} else {
		throw message;
	}
}