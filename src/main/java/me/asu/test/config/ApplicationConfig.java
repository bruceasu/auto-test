package me.asu.test.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.nutz.castor.Castors;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.inject.Injecting;
import org.nutz.lang.util.MultiLineProperties;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.resource.NutResource;
import org.nutz.resource.Scans;

/**
 * ApplicationConfig.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-09-19 16:17
 */
public class ApplicationConfig extends MultiLineProperties {

	private static final Log log = Logs.get();

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
		try {
			load(new InputStreamReader(in));
		} catch (IOException e) {
			throw Lang.wrapThrow(e);
		} finally {
			Streams.safeClose(in);
		}
	}

	/**
	 * @param r 文本输入流
	 * @since 1.b.50
	 */
	public ApplicationConfig(Reader r) {
		this(true);
		try {
			load(r);
		} catch (IOException e) {
			throw Lang.wrapThrow(e);
		} finally {
			Streams.safeClose(r);
		}
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
		try {
			List<NutResource> list = getResources(paths);
			if (utf8) {
				for (NutResource nr : list) {
					Reader r = nr.getReader();
					try {
						load(nr.getReader(), false);
					} finally {
						Streams.safeClose(r);
					}
				}
			} else {
				Properties p = new Properties();
				for (NutResource nr : list) {
					// 用字符流来读取文件
					BufferedReader bf = new BufferedReader(
							new InputStreamReader(nr.getInputStream()));
					try {
						p.load(bf);
					} finally {
						Streams.safeClose(bf);
					}
				}
				putAll(p);
			}
		} catch (IOException e) {
			throw Lang.wrapThrow(e);
		}
	}

	/**
	 * 加载指定文件/文件夹的Properties文件.
	 *
	 * @param paths 需要加载的Properties文件路径
	 * @return 加载到的Properties文件Resource列表
	 */
	private List<NutResource> getResources(String... paths) {
		List<NutResource> list = new ArrayList<NutResource>();
		for (String path : paths) {
			try {
				List<NutResource> resources = Scans.me().loadResource("^.+[.]properties$", path);
				list.addAll(resources);
			} catch (Exception e) {
				if (ignoreResourceNotFound) {
					if (log.isWarnEnabled()) {
						log.warn("Could not load resource from " + path + ": " + e.getMessage());
					}
				} else {
					throw Lang.wrapThrow(e);
				}
			}
		}
		return list;
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
			throw Lang.makeThrow("Ioc.$conf expect property '%s'", key);
		}
		return val;
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean dfval) {
		String val = get(key);
		if (Strings.isBlank(val)) {
			return dfval;
		}
		return Castors.me().castTo(val, Boolean.class);
	}

	public String get(String key, String defaultValue) {
		return Strings.sNull(get(key), defaultValue);
	}

	public List<String> getList(String key) {
		return getList(key, ",");
	}

	public List<String> getList(String key, String separatorChar) {
		List<String> re = new ArrayList<String>();
		String keyVal = get(key);
		if (Strings.isNotBlank(keyVal)) {
			String[] vlist = Strings.splitIgnoreBlank(keyVal, separatorChar);
			for (String v : vlist) {
				re.add(v.trim());
			}
		}
		return re;
	}

	public String trim(String key) {
		return Strings.trim(get(key));
	}

	public String trim(String key, String defaultValue) {
		return Strings.trim(get(key, defaultValue));
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
		return Strings.trim(get(key));
	}

	public String getTrim(String key, String defaultValue) {
		return Strings.trim(get(key, defaultValue));
	}

	public List<String> getKeys() {
		return keys();
	}

	public Collection<String> getValues() {
		return values();
	}

	public Properties toProperties() {
		Properties p = new Properties();
		p.putAll(this);
		return p;
	}


	/**
	 * 将另外一个 Properties 文本加入本散列表.
	 *
	 * @param r 文本输入流
	 * @return 自身
	 */
	public ApplicationConfig joinAndClose(Reader r) {
		MultiLineProperties mp = new MultiLineProperties();
		try {
			mp.load(r);
		} catch (IOException e) {
			throw Lang.wrapThrow(e);
		} finally {
			Streams.safeClose(r);
		}
		this.putAll(mp);
		return this;
	}

	public Map<String, String> toMap() {
		return new LinkedHashMap<String, String>(this);
	}

	public String get(String key) {
		return super.get(key);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> T make(Class<T> klass, String prefix) {
		Mirror<T> mirror = Mirror.me(klass);
		T t = mirror.born();
		Map map = toMap();
		map = Lang.filter(map, prefix, null, null, null);
		for (Entry<String, Object> en : ((Map<String, Object>) map).entrySet()) {
			String name = en.getKey();
			Injecting setter = mirror.getInjecting(name);
			setter.inject(t, en.getValue());
		}
		return t;
	}

}
