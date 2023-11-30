var PlaceholderUtils = Packages.me.asu.test.util.PlaceholderUtils

var JsBase64 = {
    _keyStr: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

    encode: function(input) {
        var output = "";
        var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
        var i = 0;

        input = Base64._utf8_encode(input);

        while (i < input.length) {
            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);

            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;

            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }

            output = output + this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) + this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);
        }

        return output;
    },

    decode: function(input) {
        var output = "";
        var chr1, chr2, chr3;
        var enc1, enc2, enc3, enc4;
        var i = 0;

        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

        while (i < input.length) {
            enc1 = this._keyStr.indexOf(input.charAt(i++));
            enc2 = this._keyStr.indexOf(input.charAt(i++));
            enc3 = this._keyStr.indexOf(input.charAt(i++));
            enc4 = this._keyStr.indexOf(input.charAt(i++));

            chr1 = (enc1 << 2) | (enc2 >> 4);
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            chr3 = ((enc3 & 3) << 6) | enc4;

            output = output + String.fromCharCode(chr1);

            if (enc3 != 64) {
                output = output + String.fromCharCode(chr2);
            }
            if (enc4 != 64) {
                output = output + String.fromCharCode(chr3);
            }
        }

        output = Base64._utf8_decode(output);

        return output;
    },

    _utf8_encode: function(string) {
        string = string.replace(/\r\n/g, "\n");
        var utftext = "";

        for (var n = 0; n < string.length; n++) {
            var c = string.charCodeAt(n);

            if (c < 128) {
                utftext += String.fromCharCode(c);
            } else if ((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            } else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }
        }

        return utftext;
    },

    _utf8_decode: function(utftext) {
        var string = "";
        var i = 0;
        var c = 0;
        var c1 = 0;
        var c2 = 0;

        while (i < utftext.length) {
            c = utftext.charCodeAt(i);

            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            } else if ((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i + 1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            } else {
                c2 = utftext.charCodeAt(i + 1);
                var c3 = utftext.charCodeAt(i + 2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }
        }

        return string;
    }
};


function toJsonObject(cotent) {
    return JSON.parse(cotent);
}

function toJsonString(obj) {
    return JSON.stringify(obj);
}

function toJsonPrettyString(obj) {
    return JSON.stringify(obj, null, 2);
}

function selectFromGlobal(name) {
    return env_context[name];
}

function setToGlobal(name, value) {
   env_context[name] = value;
}

function selectFromLocal(name) {
    return test_case_context[name];
}

function setToLocal(name, value) {
    test_case_context[name] = value;
}


function ObjectMerger() {
    this.target = {};
    this.overwrite = true;
    this.excludeKeys = null; // 使用排除键列表
}

ObjectMerger.prototype.into = function(target) {
    this.target = target || {};
    return this;
};

ObjectMerger.prototype.setOverwrite = function(overwrite) {
    this.overwrite = overwrite;
    return this;
};

ObjectMerger.prototype.setExcludeKeys = function(keys) {
    this.excludeKeys = keys;
    return this;
};

ObjectMerger.prototype.merge = function() {
    for (var i = 0; i < arguments.length; i++) {
        var source = arguments[i];
        if (typeof source === 'object' && source !== null) {
            for (var key in source) {
                if (source.hasOwnProperty(key)) {
                    // 如果当前键在排除列表中，跳过
                    if (this.excludeKeys !== null && this.excludeKeys.indexOf(key) !== -1) {
                        continue;
                    }

                    // 如果设置为不覆盖，且键已存在于目标中，则跳过
                    if (!this.overwrite && this.target.hasOwnProperty(key)) {
                        continue;
                    }

                    this.target[key] = source[key];
                }
            }
        }
    }
    return this.target;
};

// 使用示例
//var merger = new ObjectMerger();
//var result = merger.into(obj1).setOverwrite(false).setExcludeKeys(['b', 'd']).merge(obj2, obj3);


var Base64 = Java.type('java.util.Base64');
var StandardCharsets = Java.type('java.nio.charset.StandardCharsets');
var Charset = Java.type('java.nio.charset.Charset');

function toCharset(obj) {
    if (obj instanceof Charset) {
        // 如果 obj 已經是 Charset，直接返回
        return obj;
    } else if (typeof obj === 'string' || obj instanceof String) {
        // 如果 obj 是 JavaScript 字符串，將其轉換為 Charset
        return Charset.forName(obj);
    } else {
        // 如果都不是，返回 null 或者拋出異常
        return null; // 或者 throw new Error("不支持的类型");
    }
}

// 使用示例
var charsetObj = toCharset("UTF-8"); // 從字符串 "UTF-8" 獲取 Charset 對象
var anotherCharsetObj = toCharset(Charset.forName("UTF-8")); // 直接傳入 Charset 對象

// Base64 編碼
function encodeB64(text) {
    return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
}

// Base64 解碼
function decodeB64(encodedText) {
    var decodedBytes = Base64.getDecoder().decode(encodedText);
    return new java.lang.String(decodedBytes, StandardCharsets.UTF_8);
}

// Base64 Mime 編碼
function encodeB64(text) {
    return Base64.getMimeEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
}

// Base64 mime 解碼
// return JavaArray
function decodeB64(encodedText, asString, cs) {
    var decodedBytes = Base64.getMimeDecoder().decode(encodedText);
    if (asString){
        var charset = toCharset(cs)
        if (charset == undefined || charset == null) {
            return new java.lang.String(decodedBytes, StandardCharsets.UTF_8);
        } else {
            return new java.lang.String(decodedBytes, charset);
        }

    } else {
        return decodedBytes;
    }

}



// 使用示例
//var originalText = "Hello World";
//var encodedText = encodeB64(originalText);
//var decodedText = decodeB64(encodedText);
//
//print('Original: ' + originalText);
//print('Encoded: ' + encodedText);
//print('Decoded: ' + decodedText);


var Files = Java.type('java.nio.file.Files');
var Paths = Java.type('java.nio.file.Paths');

function base64File(path) {
    var file = Paths.get(path);
    // 讀取文件為二進制數據
    var fileContent = Files.readAllBytes(file);
    // 將二進制數據轉換為 Base64
    var encodedFileContent = Base64.getEncoder().encodeToString(fileContent);

    return encodedFileContent;

}

function isEmpty(obj, isBlankAsEmpty) {
    // 检查 null 或 undefined
    if (obj == null || obj == undefined) return true;

    // 检查字符串
    if (typeof obj === 'string') {
        return isBlankAsEmpty ? obj.trim() === '' : obj === '';
    }

    // 检查对象 (Array, Object)
    if (typeof obj === 'object') {
        // 如果是数组，检查长度
        if (Array.isArray(obj)) return obj.length === 0;

        // 如果是对象，检查属性
        for (var prop in obj) {
            if (Object.prototype.hasOwnProperty.call(obj, prop)) {
                if (isBlankAsEmpty && isEmpty(obj[prop], isBlankAsEmpty)) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    // 其他类型 (例如：boolean)
    return false;
}

// 使用示例
//console.log(isEmpty("", true)); // true
//console.log(isEmpty(" ", true)); // true
//console.log(isEmpty(" ", false)); // false
//console.log(isEmpty(0)); // true
//console.log(isEmpty({})); // true
//console.log(isEmpty({ a: "" }, true)); // true
//console.log(isEmpty({ a: " " }, true)); // true
//console.log(isEmpty({ a: 1 }, true)); // false
