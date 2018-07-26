package me.asu.test.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author suk
 */
public final class ExtClasspathLoader {

	private final Method addURL;
	private final URLClassLoader classloader;

	public ExtClasspathLoader() {
		this.addURL = initAddMethod();

		this.classloader = getClassLoader();
	}

	public void addURL(File file) {
		try {
			addURL(file.toURI().toURL());
		} catch (Exception localException) {
		}
	}

	public void addURL(String path) {
		File file = new File(path);
		addURL(file);
	}

	public void addURL(URL url) {
		try {
			this.addURL.invoke(this.classloader, new Object[]{url});
		} catch (Exception localException) {
		}
	}

	public void loadJarToClasspath(String filepath) {
		File file = new File(filepath);
		loopFiles(file);
	}

	public void loadResourceDirToClasspath(String filepath) {
		File file = new File(filepath);
		loopDirs(file);
	}

	private URLClassLoader getClassLoader() {
		return ((URLClassLoader) ClassLoader.getSystemClassLoader());
	}

	private Method initAddMethod() {
		try {
			Method add = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
			add.setAccessible(true);
			return add;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void loopDirs(File file) {
		if (file.isDirectory()) {
			addURL(file);
			File[] tmps = file.listFiles();
			File[] arrayOfFile1 = tmps;
			int i = arrayOfFile1.length;
			for (int j = 0; j < i; ++j) {
				File tmp = arrayOfFile1[j];
				loopDirs(tmp);
			}
		}
	}

	private void loopFiles(File file) {
		if (file.isDirectory()) {
			File[] tmps = file.listFiles();
			File[] arrayOfFile1 = tmps;
			int i = arrayOfFile1.length;
			for (int j = 0; j < i; ++j) {
				File tmp = arrayOfFile1[j];
				loopFiles(tmp);
			}
		} else {
			String jarSuffix = ".jar";
			String zipSuffix = ".zip";
			if ((file.getAbsolutePath().endsWith(jarSuffix)) || (file.getAbsolutePath()
					.endsWith(zipSuffix))) {
				addURL(file);
			}
		}
	}
}
