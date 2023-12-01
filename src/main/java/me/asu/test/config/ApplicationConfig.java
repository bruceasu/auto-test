package me.asu.test.config;

import lombok.extern.slf4j.Slf4j;
import me.asu.test.util.LangUtil;
import me.asu.test.util.PlaceholderUtils;
import me.asu.test.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;


/**
 * ApplicationConfig.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-09-19 16:17
 */
@Slf4j
public class ApplicationConfig extends LinkedHashMap<String, String> {

    //  whether utf-8 charset file
    private final boolean utf8;
    // ignore not found
    private boolean ignoreResourceNotFound = true;

    public ApplicationConfig() {
        this(true);
    }

    public ApplicationConfig(boolean utf8, String... paths) {
        this(utf8);
        this.setPaths(paths);
    }

    public ApplicationConfig(boolean utf8) {
        this.utf8 = utf8;
        Map<String, String> getenv = System.getenv();
        for (Map.Entry<String, String> e : getenv.entrySet()) {
            put("env." + e.getKey(), e.getValue());
        }
        Properties properties = System.getProperties();
        Enumeration<String> enumeration = (Enumeration<String>) properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            put(key, properties.getProperty(key));
        }
    }

    public ApplicationConfig(String... paths) {
        this(true);
        this.setPaths(paths);
    }

    public ApplicationConfig(InputStream in) {
        this(true);
        joinAndClose(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public ApplicationConfig(InputStream in, Charset cs) {
        this(StandardCharsets.UTF_8.equals(cs));
        joinAndClose(new InputStreamReader(in, cs));
    }

    /**
     * @param r text
     * @since 1.b.50
     */
    public ApplicationConfig(Reader r) {
        this(true);
        joinAndClose(r);
    }

    /**
     * load file/directories Properties
     * <p>
     * <b style=color:red>The last key will overwrite the earlier one.<b/>
     * </P>
     *
     * @param paths The files or directories
     */
    public void setPaths(String... paths) {
        clear();
        if (paths == null || paths.length == 0) return;
        try {
            final Charset cs = utf8 ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1;
            for (String path : paths) {
                Path p = Paths.get(path);
                if (Files.isRegularFile(p)) {
                    try (BufferedReader r = Files.newBufferedReader(p, cs)) {
                        joinAndClose(r);
                    }
                } else {
                    // try classpath
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    try (InputStream is =
                                 this.getClass().getClassLoader().getResourceAsStream(path)
                    ) {
                        if (is != null) {
                            BufferedReader r = new BufferedReader(new InputStreamReader(is, cs));
                            joinAndClose(r);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LangUtil.rethrowUnchecked(e);
        }
    }

    public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
        this.ignoreResourceNotFound = ignoreResourceNotFound;
    }


    public ApplicationConfig set(String key, String val) {
        put(key, val);
        return this;
    }

    public String check(String key) {
        String val = get(key);
        if (null == val) {
            throw LangUtil.makeThrow("Ioc.$conf expect property '%s'", key);
        }
        return val;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean dfval) {
        String val = get(key);
        if (StringUtils.isEmpty(val)) {
            return dfval;
        }
        return Boolean.parseBoolean(val);
    }

    public String get(String key, String defaultValue) {
        String v = get(key);
        return Optional.ofNullable(v).orElse(defaultValue);

    }

    public List<String> getList(String key) {
        return getList(key, ",");
    }

    public List<String> getList(String key, String separatorChar) {
        List<String> re = new ArrayList<String>();
        String keyVal = get(key);
        if (StringUtils.isNotEmpty(keyVal)) {
            String[] vlist = keyVal.split(separatorChar);
            for (String v : vlist) {
                if (StringUtils.trim(v).isEmpty()) continue;
                re.add(v.trim());
            }
        }
        return re;
    }

    public String trim(String key) {
        return StringUtils.trim(get(key));
    }

    public String trim(String key, String defaultValue) {
        return StringUtils.trim(get(key, defaultValue));
    }

    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getTrim(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long getLong(String key) {
        return getLong(key, -1);
    }

    public long getLong(String key, long dfval) {
        try {
            return Long.parseLong(getTrim(key));
        } catch (NumberFormatException e) {
            return dfval;
        }
    }

    public String getTrim(String key) {
        return StringUtils.trim(get(key));
    }

    public String getTrim(String key, String defaultValue) {
        return StringUtils.trim(get(key, defaultValue));
    }

    /**
     * add a Properties.
     *
     * @param r content of properties format
     * @return self
     */
    public ApplicationConfig joinAndClose(Reader r) {

        BufferedReader br;
        try {
            if (r instanceof BufferedReader) {
                br = (BufferedReader) r;
            }
            else {
                br = new BufferedReader(r);
            }
            br.lines().forEach(line -> {
                if (StringUtils.isEmpty(line)
                    || line.startsWith("#")
                    || line.startsWith("＃")) {
                    return;
                }

                final String[] split = line.split("=", 2);
                String k, v;
                if (split.length == 2) {
                    k = split[0];
                    v = split[1];
                } else {
                    k = split[0];
                    v = null;
                }
                put(k, v);
            });
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
        return this;
    }

    final Pattern pattern = Pattern.compile("\\$\\{.+?\\}");
    @Override
    public String put(String key, String value) {
        if (StringUtils.isNotEmpty(key) && pattern.matcher(key).find()) {
            value = PlaceholderUtils.resolvePlaceholders(key, this);
        }
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        m.forEach((k,v)->{
            put(k, v);
        });
    }


}
