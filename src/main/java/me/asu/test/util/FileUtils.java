package me.asu.test.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author victor.
 * @since 2018/7/5
 */
public abstract class FileUtils {

	public static void copyFolder(Path srcPath, Path destPath)
			throws IOException {
		long startTime = System.currentTimeMillis();
		if (Files.notExists(srcPath)) {
			throw new IOException(srcPath + " is not exists.");
		}
		if (Files.notExists(destPath)) {
			Files.createDirectories(destPath);
		}
		Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {
			@Override
			/** 文件处理，将文件夹也一并处理，简洁些 */
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Path dest = destPath.resolve(srcPath.relativize(file));
				if (Files.notExists(dest.getParent())) {
					Files.createDirectories(dest.getParent());
				}
				Files.copy(file, dest);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static void deleteFileOrFolder(Path start) throws IOException {
		if (Files.notExists(start)) {
			throw new IOException(start + " is not exists.");
		}
		if (Files.isRegularFile(start)) {
			Files.delete(start);
		} else if (Files.isDirectory(start)) {
			// walkFileTree is depth-first
			Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException e)
						throws IOException {
					if (e == null) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					} else {
						throw e;
					}
				}
			});
		} else {
			throw new IOException(start + " is not exists.");
		}

	}
}
