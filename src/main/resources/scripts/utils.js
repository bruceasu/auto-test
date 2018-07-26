var PlaceholderUtils = Packages.me.asu.test.util.PlaceholderUtils

function toJsonObject(cotent) {
    return JSON.parse(cotent);
}

function toJsonString(obj) {
    return JSON.stringify(obj);
}

function selectFromGlobal(name) {
    if (name in env_context) {
        return env_context[name];
    } else {
        return null;
    }
}

function selectFromLocal(name) {
    if (name in test_case_context) {
        return test_case_context[name];
    } else {
        return null;
    }
}