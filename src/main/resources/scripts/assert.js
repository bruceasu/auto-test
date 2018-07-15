// env_context is a global container, will pass through all test case.
if (!Array.isArray) {
	Array.isArray = function (arg) {
		return Object.prototype.toString.call(arg) === '[object Array]'
	}
}

var Assert = {};

Assert.assertTrue = function(actual, message) {
	if (actual) {
		return;
	}
	if (message == undefined || message == '') {
		throw "期望是true, 实际是" + actual;
	} else {
		throw message;
	}
}

Assert.assertFalse = function(actual, message) {
	if (!actual) {
		return;
	}
	if (message == undefined || message == '') {
		throw "期望是false, 实际是" + actual;
	} else {
		throw message;
	}
}

Assert.assertEquals = function(expected, actual, message) {
	if (expected == actual) {
		return;
	}
	if (message == undefined || message == '') {
		throw "期望是" + expected + ", 实际是" + actual;
	} else {
		throw message;
	}
}

Assert.assertNotEquals = function(expected, actual, message) {
	if (expected != actual) {
		return;
	}
	if (message == undefined || message == '') {
		throw "期望是" + expected + ", 实际是" + actual;
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
			throw "期望 " + expected + " 和 " + actual + " 相等，实际不等。";
		} else {
			throw message;
		}
	}
	if (expected.sort().toString() !== actual.sort().toString()) {
		if (message == undefined || message == '') {
			throw "期望 " + expected + " 和 " + actual + " 相等，实际不等。";
		} else {
			throw message;
		}
	}
}

Assert.assertArrayNotEquals = function(expected, actual, message) {
	if (expected == undefined && actual == undefined) {
		if (message == undefined || message == '') {
			throw "期望 " + expected + " 和 " + actual + " 不等，实际相等。";
		} else {
			throw message;
		}
	}
	if (expected == undefined || actual == undefined) {
		return;
	}

	if (expected.sort().toString() === actual.sort().toString()) {
		if (message == undefined || message == '') {
			throw "期望 " + expected + " 和 " + actual + " 不等，实际相等。";
		} else {
			throw message;
		}
	}
}

Assert.fail = function(message) {
	if (message == undefined || message == '') {
		throw "失败了。"
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
		throw actual + " 不包含 " + expected + " 。";
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
		throw actual + " 包含了 " + expected + " 。";
	} else {
		throw message;
	}
}