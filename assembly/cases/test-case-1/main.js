load(current_test_case.getDir() + "/config.js")
//存放所有Case共用的函数。
load(current_test_case.getTestSuite().getLib() + "/yyy.js")
load(current_test_case.getLib() + "/xxx.js")
print(current_test_case.getLib() + "/xxx.js"+" is exists: "+ Files.exists(Paths.get(current_test_case.getLib() + "/xxx.js")))
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

