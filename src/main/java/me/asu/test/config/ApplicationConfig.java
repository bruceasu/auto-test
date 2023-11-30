package me.asu.test.config;

import lombok.extern.slf4j.Slf4j;
import me.asu.test.util.LangUtil;
import me.asu.test.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * ApplicationConfig.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-09-19 16:17
 */
@Slf4j
public class ApplicationConfig extends Properties {

    // 是否为UTF8格式的Properties文件
    private final boolean utf8;
    // 是否忽略无法加载的文件
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
     * @param r 文本输入流
     * @since 1.b.50
     */
    public ApplicationConfig(Reader r) {
        this(true);
        joinAndClose(r);
    }

    /**
     * 加载指定文件/文件夹的Properties文件,合并成一个Properties对象
     * <p>
     * <b style=color:red>如果有重复的key,请务必注意加载的顺序!!<b/>
     * </P>
     *
     * @param paths 需要加载的Properties文件路径
     */
    public void setPaths(String... paths) {
        clear();
        if (paths == null || paths.length == 0) return;
        try {
            final Charset cs = utf8 ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1;
            for (String path : paths) {
                Path p = Paths.get(path);
                if (Files.isRegularFile(p)) {
                    try (Reader r = Files.newBufferedReader(p, cs)) {
                        Properties prop = new Properties();
                        prop.load(r);
                        putAll(prop);
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
                            Properties prop = new Properties();
                            prop.load(new InputStreamReader(is, cs));
                            putAll(prop);
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

    /**
     * @param key 键
     * @return 是否包括这个键
     * @since 1.b.50
     */
    public boolean has(String key) {
        return containsKey(key);
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
        return getProperty(key, defaultValue);
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

    public List<String> getKeys() {
        List<String> list = new ArrayList<>();
        final Enumeration<Object> keys = keys();
        while (keys.hasMoreElements()) {
            final Object o = keys.nextElement();
            if (o == null) continue;
            if (o instanceof String) {
                list.add((String) o);
            } else {
                list.add(o.toString());
            }
        }
        return list;
    }

    public Collection<String> getValues() {
        final Collection<Object> vs = values();
        if (vs == null || vs.isEmpty()) return Collections.emptyList();
        List<String> values = new ArrayList<>();
        for (Object v : vs) {
            if (v == null) continue;
            if (v instanceof String) {
                values.add((String) v);
            } else {
                values.add(v.toString());
            }
        }
        return values;
    }


    /**
     * 将另外一个 Properties 文本加入本散列表.
     *
     * @param r 文本输入流
     * @return 自身
     */
    public ApplicationConfig joinAndClose(Reader r) {
        Properties mp = new Properties();
        try {
            mp.load(r);
        } catch (IOException e) {
            LangUtil.rethrowUnchecked(e);
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
        this.putAll(mp);
        return this;
    }


    public String get(String key) {
        return super.getProperty(key);
    }


}
