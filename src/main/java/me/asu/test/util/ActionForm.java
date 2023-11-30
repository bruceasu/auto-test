package me.asu.test.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import static me.asu.test.util.StringUtils.isEmpty;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;


/**
 * Created by Administrator on 2019/5/3.
 *
 * @author Victor
 * @date 2019-05-03
 */
@Slf4j
public class ActionForm {

    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_PATCH = "PATCH";
    public static final String METHOD_HEAD = "HEAD";
    public static final String DATA_TYPE_FORM = "form";
    public static final String DATA_TYPE_JSON = "json";
    public static final String DATA_TYPE_MULTIPART = "multipart";
    public static final byte[] SEPARATOR = "\r\n".getBytes();

    public static final String APPLICATION_JSON_VALUE = "application/json";
    public static final String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";
    public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";

    protected int connectTimeout = 10000;
    protected int readTimeout = 15000;
    protected String encoding;
    protected String url;


    protected static String encodeFormData(Map<String, Object> params)
    throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (String s : params.keySet()) {
                //@formatter:off
                Object o = params.get(s);
                if (o == null) {
                    sb.append(s).append('=').append('&');
                } else {
                    String value = URLEncoder.encode(o.toString(), "UTF-8");
                    sb.append(s).append('=').append(value) .append('&');
                }
                //@formatter:on
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }


    /**
     * 处理实际业务.
     *
     * @param url            地址
     * @param headers        头信息
     * @param content        待发送内容
     * @param connectTimeout 连接超时设置
     * @param readTimeout    读超时设置
     * @param method         方法
     * @return 内容
     * @throws Exception 异常
     */
    public ActionFormResponse handle(String url,
            Map<String, String> headers,
            byte[] content,
            int connectTimeout,
            int readTimeout,
            String method) throws Exception {
        throw new Exception("Not implement yet!");
    }

    public ActionGet get() {
        return new ActionGet();
    }

    public ActionDelete delete() {
        return new ActionDelete();
    }

    public ActionPut put() {
        return new ActionPut();
    }

    public ActionPatch patch() {
        return new ActionPatch();
    }

    public ActionPost post() {
        return new ActionPost();
    }

    public ActionPostJson postJson() {
        return new ActionPostJson();
    }

    public ActionFile file() {
        return new ActionFile();
    }

    @Data
    public abstract class Action<T> {
        protected int connectTimeout = ActionForm.this.connectTimeout;
        protected int readTimeout = ActionForm.this.readTimeout;
        protected String encoding = ActionForm.this.encoding;
        protected String url = ActionForm.this.url;
        protected Map<String, Object> params;
        protected Map<String, String> initHeaders = new HashMap<>();
        protected Object data;

        public T withUrl(String url) {
            this.url = url;
            return (T) this;
        }

        public T withEncoding(String encoding) {
            this.encoding = encoding;
            return (T) this;
        }

        public T withReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return (T) this;
        }

        public T withConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return (T) this;
        }

        public T withParams(Map<String, Object> params) {
            this.params = params;
            return (T) this;
        }

        public T withInitHeaders(Map<String, String> initHeaders) {
            if (isNotEmpty(initHeaders)) this.initHeaders.putAll(initHeaders);
            return (T) this;
        }


        public T withData(Object data) {
            this.data = data;
            return (T) this;
        }

        public T withJsonType() {
            initHeaders.put("Content-Type", APPLICATION_JSON_VALUE);
            return (T) this;
        }

        public T withMultipartType() {
            initHeaders.put("Content-Type", MULTIPART_FORM_DATA_VALUE);
            return (T) this;
        }

        public T withFormType(final String charset) {
            if (isEmpty(charset)) {
                initHeaders.put("Content-Type",
                                APPLICATION_FORM_URLENCODED_VALUE + "; charset=utf-8");
            } else {
                initHeaders.put("Content-Type",
                                APPLICATION_FORM_URLENCODED_VALUE + "; charset=" + charset);
            }
            return (T) this;
        }

        protected void appendParamToUrl()
        throws UnsupportedEncodingException {
            if (params != null && !params.isEmpty()) {
                if (url.contains("?")) {
                    url = url + "&" + encodeFormData(params);
                } else {
                    url = url + "?" + encodeFormData(params);
                }

            }
        }

        protected byte[] createBody(Map<String, String> headers)
        throws UnsupportedEncodingException {
            byte[] content = null;
            if (data == null) {
                content = null;
            } else if (data instanceof String) {
                content = Bytes.toBytes((String) data);
            } else {
                String dataType = getDataType(headers);
                content = dataToBytes(data, dataType);
            }
            return content;
        }

        protected Map<String, String> initHeaders(Map<String, String> initHeaders) {
            Map<String, String> headers;
            if (isEmpty(initHeaders)) {
                headers = new HashMap<>();
                headers.put("Accept-Encoding", "gzip, deflate");
                headers.put("Connection", "Close");
                headers.put("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");
            } else {
                headers = new HashMap<>();
                headers.putAll(initHeaders);
            }
            return headers;
        }


        protected byte[] dataToBytes(Object data, String dataType)
        throws UnsupportedEncodingException {
            byte[] content = null;
            if (data == null) {
                content = null;
            } else if (DATA_TYPE_JSON.equals(dataType)) {
                content = JacksonUtils.serializeAsBytes(data);
            } else if (DATA_TYPE_FORM.equals(dataType)) {
                if (data instanceof Map) {
                    content = Bytes.toBytes(encodeFormData((Map) data));
                } else {
                    final Map map = JacksonUtils.convertToMap(data);
                    content = Bytes.toBytes(encodeFormData(map));

                }
            } else if (DATA_TYPE_MULTIPART.equals(dataType)) {
                // 参数不足无法处理。
                log.warn("Unsupported data type of {}", dataType);
            }
            return content;
        }

        protected String getDataType(Map<String, String> initHeaders) {
            String s = initHeaders.get("Content-Type");
            String dataType;
            if (isEmpty(s)) {
                dataType = DATA_TYPE_FORM;
                initHeaders.put("Content-Type", APPLICATION_FORM_URLENCODED_VALUE);
            } else if (s.contains(APPLICATION_JSON_VALUE)) {
                dataType = DATA_TYPE_JSON;
            } else if (s.contains(APPLICATION_FORM_URLENCODED_VALUE)) {
                dataType = DATA_TYPE_FORM;
            } else if (s.contains(MULTIPART_FORM_DATA_VALUE)) {
                dataType = DATA_TYPE_MULTIPART;
            } else {
                dataType = DATA_TYPE_FORM;
                initHeaders.put("Content-Type", APPLICATION_FORM_URLENCODED_VALUE);
            }
            return dataType;
        }


        protected abstract String getMethod();

    }

    public class ActionGet extends Action<ActionGet> {
        @Override
        protected String getMethod() {
            return METHOD_GET;
        }

        public ActionFormResponse send() throws Exception {
            Map<String, String> headers = initHeaders(getInitHeaders());
            appendParamToUrl();
            return handle(getUrl(), headers, null, getConnectTimeout(),
                          getReadTimeout(), getMethod());
        }


    }


    public class ActionDelete extends Action<ActionDelete> {
        @Override
        protected String getMethod() {
            return METHOD_DELETE;
        }

        public ActionFormResponse send() throws Exception {
            Map<String, String> headers = initHeaders(getInitHeaders());

            appendParamToUrl();
            return handle(getUrl(), headers, null, getConnectTimeout(),
                          getReadTimeout(), getMethod());
        }

    }

    public class ActionPut extends Action<ActionPut> {

        @Override
        protected String getMethod() {
            return METHOD_PUT;
        }

        public ActionFormResponse send() throws Exception {
            Map<String, String> headers = initHeaders(initHeaders);
            appendParamToUrl();
            byte[] content = createBody(headers);
            return handle(getUrl(), headers, content, getConnectTimeout(),
                          getReadTimeout(), getMethod());
        }

    }

    public class ActionPatch extends Action<ActionPatch> {
        @Override
        protected String getMethod() {
            return METHOD_PATCH;
        }

        public ActionFormResponse send() throws Exception {
            Map<String, String> headers = initHeaders(getInitHeaders());
            appendParamToUrl();
            byte[] content = createBody(headers);
            return handle(getUrl(), headers, content, getConnectTimeout(),
                          getReadTimeout(), getMethod());
        }

    }


    public class ActionPost extends Action<ActionPost> {
        @Override
        protected String getMethod() {
            return METHOD_POST;
        }

        public ActionFormResponse send() throws Exception {
            Map<String, String> headers = initHeaders(getInitHeaders());

            appendParamToUrl();
            byte[] content = createBody(headers);
            return handle(getUrl(), headers, content, getConnectTimeout(), getReadTimeout(),
                          getMethod());
        }
    }


    public class ActionPostJson extends Action<ActionPostJson> {
        @Override
        protected String getMethod() {
            return METHOD_POST;
        }

        public ActionPostJson() {
            withJsonType();
        }

        public ActionFormResponse send() throws Exception {
            Map<String, String> headers = initHeaders(getInitHeaders());

            appendParamToUrl();
            byte[] content = createBody(headers);
            return handle(getUrl(), headers, content, getConnectTimeout(), getReadTimeout(),
                          getMethod());
        }
    }

    public class ActionFile extends Action<ActionFile> {
        public ActionFile() {
            withMultipartType();
        }

        @Override
        protected String getMethod() {
            return METHOD_POST;
        }

        public ActionFormResponse send() throws Exception {
            Map<String, String> headers = initHeaders(initHeaders);
            appendParamToUrl();
            UUID uuid = UUID.randomUUID();
            String boundary = "------FormBoundary" + uuid.toString();
            headers.put("Content-Type", "multipart/form-data;boundary=" + boundary);
            byte[] content = createSendFileContent(data, boundary);
            return handle(getUrl(), headers, content, connectTimeout, readTimeout, getMethod());
        }

        private byte[] createSendFileContent(Object data, String boundary)
        throws IOException {
            Map<String, Object> params =
                    data instanceof Map ? (Map) data : JacksonUtils.convertToMap(data);
            ByteArrayOutputStream binary = new ByteArrayOutputStream();
            for (Entry<String, Object> entry : params.entrySet()) {
                final String key = entry.getKey();
                Object val = entry.getValue();
                if (val == null) {
                    val = "";
                }

                binary.write(Bytes.toBytes("--" + boundary));
                binary.write(SEPARATOR);

                if (val instanceof File) {
                    readFile((File) val, key, binary);
                } else {
                    String namePart = "Content-Disposition: form-data; name=\"" + key + "\"";
                    binary.write(Bytes.toBytes(namePart));
                    binary.write(SEPARATOR);
                    binary.write(SEPARATOR);

                    binary.write(Bytes.toBytes(String.valueOf(val)));
                    binary.write(SEPARATOR);
                }
            }

            binary.write(Bytes.toBytes("--" + boundary + "--"));
            binary.write(SEPARATOR);

            return binary.toByteArray();
        }

        private void readFile(File f, String key, OutputStream out) throws IOException {
            String fileNamePart = "Content-Disposition: form-data; name=\"" + key + "\";filename=\""
                                  + f.getName() + "\"";
            out.write(Bytes.toBytes(fileNamePart));
            out.write(SEPARATOR);
            String contentTypePart = "Content-Type: application/octet-stream";
            out.write(Bytes.toBytes(contentTypePart));
            out.write(SEPARATOR);
            out.write(SEPARATOR);

            byte[] bytes = Files.readAllBytes(f.toPath());
            out.write(bytes);
            out.write(SEPARATOR);
        }

    }


    /**
     * @author victor.
     * @since 2018/7/12
     */
    @Slf4j
    public static class HttpHandler extends ActionForm {

        private static HttpURLConnection getHttpConnection(URL url, String method)
        throws IOException {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setDoInput(true);
            if (METHOD_POST.equals(method)) {
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("Accept", "*/*");
            return conn;
        }

        /**
         * 处理实际业务.
         *
         * @param url            地址
         * @param headers        头信息
         * @param content        待发送内容
         * @param connectTimeout 连接超时设置
         * @param readTimeout    读超时设置
         * @param method         方法
         * @return 内容
         * @throws Exception 异常
         */
        @Override
        public ActionFormResponse handle(String url,
                Map<String, String> headers,
                byte[] content,
                int connectTimeout,
                int readTimeout,
                String method) throws Exception {
            log.info("{} {}", method, url);
            HttpURLConnection conn = null;
            OutputStream out = null;
            ActionFormResponse httpResponse = new ActionFormResponse();
            try {
                conn = getHttpConnection(new URL(url), method);
                conn.setConnectTimeout(connectTimeout);
                conn.setReadTimeout(readTimeout);
                if (headers != null && !headers.isEmpty()) {
                    for (Entry<String, String> entry : headers.entrySet()) {
                        conn.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
                //如果是post交易，写入请求参数
                switch (method) {
                    case METHOD_POST:
                    case METHOD_PUT:
                    case METHOD_PATCH:
                        if (content != null) {
                            out = conn.getOutputStream();
                            out.write(content);
                        }
                        break;
                }
                httpResponse.fillResponse(conn);

            } catch (IOException e) {
                HttpHandler.log.error(e.getMessage(), e);
                throw e;
            } finally {
                if (out != null) {
                    out.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return httpResponse;
        }


    }

    /**
     * @author victor.
     * @since 2018/7/12
     */
    @Slf4j
    public static class HttpsHandler extends ActionForm {

        private static class DefaultTrustManager implements X509TrustManager {

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }

        private static HttpsURLConnection getHttpsConnection(URL url, String method)
        throws IOException {
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setDoInput(true);
            if (!ActionForm.METHOD_GET.equals(method)) {
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("Accept", "*/*");
            return conn;
        }

        /**
         * 处理实际业务.
         *
         * @param url            地址
         * @param headers        头信息
         * @param content        待发送内容
         * @param connectTimeout 连接超时设置
         * @param readTimeout    读超时设置
         * @param method         方法
         * @return 内容
         * @throws Exception 异常
         */
        @Override
        public ActionFormResponse handle(String url,
                Map<String, String> headers,
                byte[] content,
                int connectTimeout,
                int readTimeout,
                String method) throws Exception {
            log.info("{} {}", method, url);
            HttpsURLConnection conn = null;
            OutputStream out = null;
            String rsp = null;
            ActionFormResponse httpResponse = new ActionFormResponse();
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                KeyManager[] kms = new KeyManager[0];
                TrustManager[] tms = new TrustManager[]{new DefaultTrustManager()};
                SecureRandom random = new SecureRandom();
                ctx.init(kms, tms, random);
                SSLContext.setDefault(ctx);
                conn = getHttpsConnection(new URL(url), method);
                conn.setConnectTimeout(connectTimeout);
                conn.setReadTimeout(readTimeout);
                if (headers != null && !headers.isEmpty()) {
                    for (Entry<String, String> entry : headers.entrySet()) {
                        conn.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
                conn.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                //如果不是get交易，写入请求参数
                switch (method) {
                    case METHOD_POST:
                    case METHOD_PUT:
                    case METHOD_PATCH:
                        if (content != null) {
                            out = conn.getOutputStream();
                            out.write(content);
                        }
                        break;
                }
                httpResponse.fillResponse(conn);
            } catch (IOException e) {
                HttpsHandler.log.error(e.getMessage(), e);
                throw e;
            } finally {
                if (out != null) {
                    out.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return httpResponse;
        }

    }

    /**
     * @author victor.
     * @since 2018/7/12
     */
    @Data
    public class ActionFormResponse {
        private static final long MAX_LENGTH = 5242880L;

        int statusCode;
        Map<String, List<String>> headers;
        String content;
        byte[] bodyBytes;
        boolean storeToTmpFile = false;
        boolean isDownloadFile = false;
        Path tmpPath;
        String charset = encoding;

        public void fillResponse(HttpURLConnection conn)
        throws IOException {

            setStatusCode(conn.getResponseCode());
            setHeaders(conn.getHeaderFields());

            // 获取内容长度
            int contentLength = conn.getContentLength();
            if (contentLength == -1 || contentLength > MAX_LENGTH) {
                storeToTmpFile = true;
                tmpPath = Files.createTempFile("dl-", ".tmp");
            }

            // 检查是否是一个下载的响应
            String contentDisposition = conn.getHeaderField("Content-Disposition");
            if (contentDisposition != null && contentDisposition.contains("attachment")) {
                isDownloadFile = true;
                storeToTmpFile = true;
                tmpPath = Files.createTempFile("dl-", ".tmp");

            }

            if (isDownloadFile) {
                storeToFile(conn.getInputStream(), tmpPath);
                return;
            }

            List<String> encodings = getHeaders().get("Content-Encoding");
            boolean isGzip = false;
            if (encodings != null && encodings.size() > 0) {
                isGzip = encodings.contains("gzip");
            }

            if (storeToTmpFile) {
                if (isGzip) {
                    GZIPInputStream gi = new GZIPInputStream(conn.getInputStream());
                    storeToFile(gi, tmpPath);
                } else {
                    storeToFile(conn.getInputStream(), tmpPath);
                }
            } else {
                byte[] bytes = readResponseToBytes(conn);
                if (isGzip) {
                    GZIPInputStream gi = new GZIPInputStream(new ByteArrayInputStream(bytes));
                    bytes = readStreamToBytes(gi);
                } else {
                    setBodyBytes(bytes);
                }
                List<String> strings = getHeaders().get("Content-Type");
                if (strings != null && !strings.isEmpty()) {
                    String ct = strings.get(0);
                    if (ct.contains("text") || ct.contains("json") || ct.contains("xml")) {
                        // 这是文本内容
                        charset = getResponseCharset(ct);
                        setContent(new String(bytes, charset));
                    }
                } else {
                    // 通常是文本，我们就假定它是文本了。
                    setContent(new String(bytes, charset));
                }
            }

        }


        private void storeToFile(InputStream stream, Path path) throws IOException {
            if (stream == null) return;
            byte[] buffer = new byte[4096];
            try (final OutputStream out = Files.newOutputStream(path,
                                                                StandardOpenOption.CREATE,
                                                                StandardOpenOption.WRITE)
            ) {
                int bytesRead = -1;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

        }

        private byte[] readResponseToBytes(HttpURLConnection conn) throws IOException {
            InputStream es = conn.getErrorStream();
            if (es == null) {
                InputStream inputStream = null;
                try {
                    inputStream = conn.getInputStream();
                } catch (IOException e) {
                    return new byte[0];
                }
                return readStreamToBytes(inputStream);
            } else {
                return readStreamToBytes(es);
            }
        }


        private byte[] readStreamToBytes(InputStream stream) throws IOException {
            if (stream == null) {
                return new byte[0];
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = stream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }


        /**
         * 获取流的编码。先从头信息中查找，如果没有就取默认值。
         */
        private String getResponseCharset(String contentType) {
            if (!isEmpty(contentType)) {
                String[] params = contentType.split(";");
                for (String param : params) {
                    param = param.trim();
                    if (param.startsWith("charset")) {
                        String[] pair = param.split("=", 2);
                        if (pair.length == 2 && !isEmpty(pair[1])) {
                            charset = pair[1].trim();
                        }
                        break;
                    }
                }
            }

            return charset;
        }


        /**
         * 假设content是json数据, 如果不是json数据则抛JSONException异常。
         */
        public Map getAsJson() {
            if (isDownloadFile || isStoreToTmpFile()) {
                try {
                    final byte[] bytes = Files.readAllBytes(tmpPath);
                    if (bytes == null || bytes.length == 0) return Collections.emptyMap();
                    return JacksonUtils.deserializeToMap(bytes);

                } catch (Exception e) {
                    log.error("", e);
                    return Collections.emptyMap();
                }
            } else {
                if (isEmpty(content)) {
                    return Collections.emptyMap();
                }
                return JacksonUtils.deserializeToMap(content);
            }
        }

        /**
         * 假设content是json数据, 如果不是json数据则抛JSONException异常。
         */
        public <T> T getAsJson(Class<T> klass) {
            if (isDownloadFile || isStoreToTmpFile()) {
                try {
                    final byte[] bytes = Files.readAllBytes(tmpPath);
                    if (bytes == null || bytes.length == 0) return null;
                    return JacksonUtils.deserialize(bytes, klass);

                } catch (Exception e) {
                    log.error("", e);
                    return null;
                }
            } else {
                if (isEmpty(content)) {
                    return null;
                }
                return JacksonUtils.deserialize(content, klass);
            }
        }

        public String getContent() {
            if (isDownloadFile || isStoreToTmpFile()) {
                try {
                    if (Files.isRegularFile(tmpPath)) {
                        bodyBytes = Files.readAllBytes(tmpPath);
                        if (bodyBytes == null || bodyBytes.length == 0) return "";
                        return new String(bodyBytes, encoding);
                    }

                    return "";
                } catch (Exception e) {
                    log.error("", e);
                    return "";
                }
            } else {
                if (isEmpty(content)) {
                    return content;
                }
                if (bodyBytes != null) {
                    try {
                        return new String(bodyBytes, encoding);
                    } catch (UnsupportedEncodingException e) {
                        return new String(bodyBytes);
                    }
                }
                return "";
            }
        }

        public InputStream getInputStream() {
            if (isDownloadFile || isStoreToTmpFile()) {
                try {
                    return Files.newInputStream(tmpPath);

                } catch (Exception e) {
                    log.error("", e);
                    return null;
                }
            } else {

                if (bodyBytes != null) {
                    return new ByteArrayInputStream(bodyBytes);
                }
                if (isEmpty(content)) {
                    return new ByteArrayInputStream(StringUtils.isNotEmpty(charset)
                                                    ? Bytes.toBytes(content, charset)
                                                    : (StringUtils.isNotEmpty(encoding)
                                                       ? Bytes.toBytes(content, encoding)
                                                       : Bytes.toBytes(content))
                    );
                }
                return null;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            // 注意，不一定会被调用。也不确定什么时候被调用。
            super.finalize();
            if (tmpPath != null && Files.isRegularFile(tmpPath)) {
                Files.deleteIfExists(tmpPath);
            }
        }
    }


    public static ActionFormBuilder createBuilder() {
        return new ActionFormBuilder();
    }


    /**
     * @author victor.
     * @since 2018/7/12
     */
    public static class ActionFormBuilder {

        int connectTimeout = 10000;
        int readTimeout = 15000;
        String encoding;
        String url;


        public ActionForm build() {
            return createClient(url, encoding, readTimeout, connectTimeout);
        }

        public ActionForm createClient(
                String url, String encoding, int readTimeout, int connectTimeout) {
            ActionForm client = null;
            if (isHttps(url)) {
                client = new HttpsHandler();
            } else {
                client = new HttpHandler();
            }
            client.encoding = encoding;
            client.connectTimeout = connectTimeout;
            client.readTimeout = readTimeout;
            client.url = url;
            return client;
        }

        /**
         * 是否为https地址.
         *
         * @param url 地址
         * @return true or false.
         */
        boolean isHttps(String url) {
            return url.startsWith("https") || url.startsWith("HTTPS");
        }

        public ActionFormBuilder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public ActionFormBuilder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public ActionFormBuilder encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public ActionFormBuilder url(String url) {
            this.url = url;
            return this;
        }
    }


}
