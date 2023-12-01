package me.asu.test.config;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import me.asu.test.util.PlaceholderUtils;

/**
 * EnvContext.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-30 11:27
 */
public class EnvContext implements Map<String, Object> {

	Map<String, Object> cache = new ConcurrentHashMap<>();

	@Getter
	ApplicationConfig appCfg;

	public EnvContext() {
		Map<String, String> getenv = System.getenv();
		for (Entry<String, String> e : getenv.entrySet()) {
			cache.put("env." + e.getKey(), e.getValue());
		}
		Properties properties = System.getProperties();
		Enumeration<String> enumeration = (Enumeration<String>) properties.propertyNames();
		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();
			cache.put(key, properties.getProperty(key));
		}
	}

	public void setAppCfg(ApplicationConfig cfg) {
		if (cfg == null) {
			return;
		}
		Set<String> strings = cfg.keySet();
		for (String key : strings) {
			String s = cfg.get(key);
			s = PlaceholderUtils.resolvePlaceholders(s, cache);
			cfg.set(key, s);
			cache.put(key, s);
		}
		this.appCfg = cfg;
	}

	public boolean hasCfg(String key) {
		return appCfg.containsKey(key);
	}

	public boolean getCfgBoolean(String key) {
		return appCfg.getBoolean(key);
	}

	public boolean getCfgBoolean(String key, boolean dfval) {
		return appCfg.getBoolean(key, dfval);
	}

	public String getCfg(String key) {
		return appCfg.get(key);
	}

	public String getCfg(String key, String defaultValue) {
		return appCfg.get(key, defaultValue);
	}

	public List<String> getCfgList(String key) {
		return appCfg.getList(key);
	}

	public List<String> getCfgList(String key, String separatorChar) {
		return appCfg.getList(key, separatorChar);
	}

	public String cfgTrim(String key) {
		return appCfg.trim(key);
	}

	public String cfgTrim(String key, String defaultValue) {
		return appCfg.trim(key, defaultValue);
	}

	public int getCfgInt(String key) {
		return appCfg.getInt(key);
	}

	public int getCfgInt(String key, int defaultValue) {
		return appCfg.getInt(key, defaultValue);
	}

	public long getCfgLong(String key) {
		return appCfg.getLong(key);
	}

	public long getCfgLong(String key, long dfval) {
		return appCfg.getLong(key, dfval);
	}

	public String getCfgTrim(String key) {
		return appCfg.getTrim(key);
	}

	public String getCfgTrim(String key, String defaultValue) {
		return appCfg.getTrim(key, defaultValue);
	}

	@Override
	public int size() {
		return cache.size();
	}

	@Override
	public boolean isEmpty() {
		return cache.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return cache.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return cache.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return cache.get(key);
	}

	@Override
	public Object put(String key, Object value) {
		return cache.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return cache.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		cache.putAll(m);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public Set<String> keySet() {
		return cache.keySet();
	}

	@Override
	public Collection<Object> values() {
		return cache.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return cache.entrySet();
	}

	@Override
	public boolean equals(Object o) {
		return cache.equals(o);
	}

	public Map<String, Object> getCache() {
		return cache;
	}

	public static void main(String[] args) {
		EnvContext envContext = new EnvContext();
		for (String key : envContext.cache.keySet()) {
			System.out.println(key + ": " + envContext.get(key));
		}
	}
}
