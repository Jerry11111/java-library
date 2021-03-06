package com.java.library.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class FileUtils {

	public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
	public static final long ONE_KB = 1024;
	public static final long ONE_MB = ONE_KB * ONE_KB;
	private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;
	public static final long ONE_GB = ONE_KB * ONE_MB;
	
	
	public static void write(File file, byte[]data) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			IOUtility.write(data, fos);
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			IOUtility.closeQuietly(fos);
		}
	}

	public static String readAsString(File file, String encoding) {
		InputStream in = null;
		try {
			in = openAsInputStream(file);
			return IOUtility.toString(in, encoding);
		} finally {
			IOUtility.closeQuietly(in);
		}
	}

	public static byte[] readAsByteArray(File file) {
		InputStream in = null;
		try {
			in = openAsInputStream(file);
			return IOUtility.toByteArray(in, file.length());
		} finally {
			IOUtility.closeQuietly(in);
		}
	}

	public static FileInputStream openAsInputStream(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new RuntimeException("File '" + file + "' exists but is a directory");
			}
			if (file.canRead() == false) {
				throw new RuntimeException("File '" + file + "' cannot be read");
			}
		} else {
			throw new RuntimeException("File '" + file + "' does not exist");
		}
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static FileOutputStream openAsOutputStream(File file, boolean append) {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new RuntimeException("File '" + file + "' exists but is a directory");
			}
			if (file.canWrite() == false) {
				throw new RuntimeException("File '" + file + "' cannot be written to");
			}
		} else {
			File parent = file.getParentFile();
			if (parent != null) {
				if (!parent.mkdirs() && !parent.isDirectory()) {
					throw new RuntimeException("Directory '" + parent + "' could not be created");
				}
			}
		}
		try {
			return new FileOutputStream(file, append);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static void touch(File file) {
		if (!file.exists()) {
			OutputStream out = openAsOutputStream(file, false);
			IOUtility.closeQuietly(out);
		}
		boolean success = file.setLastModified(System.currentTimeMillis());
		if (!success) {
			throw new RuntimeException("Unable to set the last modification time for " + file);
		}
	}

	public static void copyFileToDirectory(File srcFile, File destDir) {
		copyFileToDirectory(srcFile, destDir, true);
	}

	public static void copyFileToDirectory(File srcFile, File destDir, boolean preserveFileDate) {
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (destDir.exists() && destDir.isDirectory() == false) {
			throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
		}
		File destFile = new File(destDir, srcFile.getName());
		copyFile(srcFile, destFile, preserveFileDate);
	}

	public static void copyFile(File srcFile, File destFile) {
		copyFile(srcFile, destFile, true);
	}

	public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) {
		if (srcFile == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destFile == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (srcFile.exists() == false) {
			throw new RuntimeException("Source '" + srcFile + "' does not exist");
		}
		if (srcFile.isDirectory()) {
			throw new RuntimeException("Source '" + srcFile + "' exists but is a directory");
		}
		try {
			if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
				throw new RuntimeException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		File parentFile = destFile.getParentFile();
		if (parentFile != null) {
			if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
				throw new RuntimeException("Destination '" + parentFile + "' directory cannot be created");
			}
		}
		if (destFile.exists() && destFile.canWrite() == false) {
			throw new RuntimeException("Destination '" + destFile + "' exists but is read-only");
		}
		doCopyFile(srcFile, destFile, preserveFileDate);
	}

	private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) {
		if (destFile.exists() && destFile.isDirectory()) {
			throw new RuntimeException("Destination '" + destFile + "' exists but is a directory");
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel input = null;
		FileChannel output = null;
		try {
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			input = fis.getChannel();
			output = fos.getChannel();
			long size = input.size();
			long pos = 0;
			long count = 0;
			while (pos < size) {
				count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
				pos += output.transferFrom(input, pos, count);
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			IOUtility.closeQuietly(output);
			IOUtility.closeQuietly(fos);
			IOUtility.closeQuietly(input);
			IOUtility.closeQuietly(fis);
		}

		if (srcFile.length() != destFile.length()) {
			throw new RuntimeException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
		}
		if (preserveFileDate) {
			destFile.setLastModified(srcFile.lastModified());
		}
	}

}
