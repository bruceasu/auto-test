
var http = {};
http.ActionForm = Packages.me.asu.test.util.ActionForm;
http.StringUtils = Packages.me.asu.test.util.StringUtils;

// 发送请求，并把http.Response对象存到全局变量resp中。
//var resp;

//var type;        // string GET or POST
//var url;         // string
//var headers;     // {name: value};
//var cookies;     // [{name: value}];
//var parameters;  // string , json or form
//var data; // [{name, value}];
//var contentType; // string, 覆盖 headers的值

var ContentTypes = {
    JSON: "application/json;charset=UTF-8",
    FORM: "application/x-www-form-urlencoded;charset=UTF-8",
    FILE: "application/multipart/form-data"
}
function mapListToCookieString(cookieList) {
  var _cookie = []
  for (var i = 0; i < cookieList.length; i++) {
    var k = cookieList[i]["name"];
    var v = cookieList[i]["value"];
     _cookie.push(encodeURIComponent(k) + "=" + encodeURIComponent(v));
  }
  return _cookie.join(";");
}


function cookieStringToMapList(cookieList) {
    var mapList = [];
    for(var i = 0; i< cookieList.length; i++ ) {
        var cookie = cookieList[i];
        var map = {};
        var cookieArray = cookie.split(';');
        for (var i = 0; i < cookieArray.length; i++) {
            var cookie = cookieArray[i];
            var parts = cookie.split('=');
            var key = parts[0];
            var value = parts.length > 1 ? parts[1] : undefined;
            if (key && value) {
//                if ("expires|path|domain|Secure|HttpOnly|priority|SameSite".indexOf(key) !== -1) continue;
                map.set(decodeURIComponent(key.trim()), decodeURIComponent(value.trim()));
            }

        }
        mapList.push(map);
    }

    return mapList;
}

function process() {
    var request = selectFromLocal("request")
    var method = request.type;
    var _url = request.url;
//    print("url: " + _url);
    var actionFormBuilder = http.ActionForm.createBuilder();
    actionFormBuilder.url(_url).connectTimeout(10000).readTimeout(15000).encoding("utf-8");
    var actionForm = actionFormBuilder.build();
    var action;
    switch(method) {
        case "POST": action = actionForm.post(); break;
        case "PUT": action = actionForm.put(); break;
        case "PATCH": action = actionForm.patch(); break;
        case "DELETE": action = actionForm.delete(); break;
        case "GET": action = actionForm.get(); break;
        case "FILE": action = actionForm.file().withMultipartType(); break;
        default:
            action = actionForm.get();
            break;
    }

//    print("action:", action.getClass());
    if (request.headers) {
        request.headers["Content-Type"] = request.contentType;
    } else {
        request.headers = {};
        request.headers["Content-Type"] = request.contentType;
    }


    if (request.cookies != null && request.cookies.length > 0) {
        request.headers["Cookie"] = mapListToCookieString(request.cookies);
    }

    if (request.headers) {
        action.withInitHeaders(request.headers);
    }

    if (request.parameters) {
        action.withParams(request.parameters);
    }

    if (request.data != undefined && request.data != null) {
        action.withData(request.data);
    }

    var resp = action.send();

    setToLocal("response.resp", resp);
    setToLocal("response.status",resp.getStatusCode());
    setToLocal("response.headers", resp.getHeaders());
    var cookieList = resp.getHeaders().get("Set-Cookie");
    if (cookieList != null && cookieList.length > 0) {
        setToLocal("response.cookie", cookieStringToMapList(cookieList));
    }

    try {
        setToLocal("response.content", resp.getContent());
    } catch(e) {
        setToLocal("response.content", "");
    }
    return resp;
}

var PlaceholderUtils = Packages.me.asu.test.util.PlaceholderUtils

function convertResponseToMapFromJson() {
    var content = selectFromLocal("response.content");
    setToLocal("response.json", toJsonObject(content));
}

function selectFromContent(name) {
    eval("var x = test_case_context[\"response.json\"]." + name + ";")
    // print (">>>>>>>>> " + x)
    return x;
}

function selectFromHeader(name) {
    return selectFromLocal("response.headers").get(name);
}