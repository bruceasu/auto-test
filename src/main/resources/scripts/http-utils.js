var http = {};
http.Http = Packages.org.nutz.http.Http;
http.Sender = Packages.org.nutz.http.Sender;
http.Request = Packages.org.nutz.http.Request;
http.Response = Packages.org.nutz.http.Response;
http.Header = Packages.org.nutz.http.Header;
http.Cookie = Packages.org.nutz.http.Cookie;
http.FilePostSender = Packages.org.nutz.http.sender.FilePostSender;
http.PostSender = Packages.org.nutz.http.sender.PostSender;
http.GetSender = Packages.org.nutz.http.sender.GetSender;


/*
 * 发送请求，并把http.Response对象存到全局变量resp中。
 * var resp;        // http.Response
 * var type;        // string GET or POST
 * var url;         // string
 * var headers;     // [{name, value}];
 * var cookies;     // [{name, value}];
 * var parameters;  // string , json or form
 * var contentType; // string
 */
function process() {
    var method = type == "GET" ? http.Request.METHOD.GET
        : http.Request.METHOD.POST
    var _url = url;
    if (type == "GET" && parameters != "") {
        if (url.indexOf("?") == -1) {
            _url += "?" + parameters;
        } else {
            _url += "&" + parameters;
        }
    }
    print("url: " + url)
    var request = http.Request.create(_url, method);
    if (headers != null && headers.length > 0) {
        for (var i = 0; i < headers.length; i++) {
            request.header(headers[i]["name"], headers[i]["value"]);
        }
    }
    request.header("Content-Type", contentType);

    if (cookies != null && cookies.length > 0) {
        var _cookie = new http.Cookie();
        for (var i = 0; i < cookies.length; i++) {
            _cookie.set(cookies[i]["name"], cookies[i]["value"]);
        }
        request.setCookie(_cookie);
    }

    if (type != "GET" && parameters != "") {
        request.setData(parameters)
    }

    var sender = http.Sender.create(request);
    var resp = sender.send();
    test_case_context["response.resp"] = resp;
    test_case_context["response.status"] = resp.getStatus();
    test_case_context["response.header"] = resp.getHeader();
    test_case_context["response.cookie"] = resp.getCookie();
    test_case_context["response.protocal"] = resp.getProtocal();
    test_case_context["response.detail"] = resp.getDetail();
    try {
        test_case_context["response.content"] = resp.getContent();
    } catch(e) {
        test_case_context["response.content"] = "";
    }
    return resp;
}

var PlaceholderUtils = Packages.me.asu.test.util.PlaceholderUtils

function convertResponseToMapFromJson() {
    test_case_context["response.json"] = JSON.parse(
        test_case_context["response.content"]);
    // test_case_context["response.json"] = JSON.parse('{"code": "OK", "result":{"kw": "guava"}}');
    // print ("json >>>>>>>>>> " + JSON.stringify(test_case_context["response.json"]));
}

function selectFromContent(name) {
    eval("var x = test_case_context[\"response.json\"]." + name + ";")
    // print (">>>>>>>>> " + x)
    return x;
}

function selectFromHeader(name) {
    return test_case_context["response.header"].get(name);
}