load(current_test_case.getDir() + "/config.js")
load(current_test_case.getTestSuite().getLib() + "/xxx.js")
load(current_test_case.getLib() + "/yyy.js")

function printStartMessage() {
	print("this is test case 1");
	print(JSON.stringify(config));
}

function main() {
	printStartMessage();
	Assert.assertEquals("OK", "OK");
	current_test_case.setResult(true);
}

main();

