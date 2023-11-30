package me.asu.test.util.email;

import javax.activation.DataSource;
import java.io.*;

public class ByteArrayDataSource implements DataSource {

	private byte[] bytes;
	private String name;
	private String contentType;

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(bytes);
	}

	public OutputStream getOutputStream() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		return new FilterOutputStream(baos) {
			public void close() throws IOException {
				baos.close();
				ByteArrayDataSource.this.setBytes(baos.toByteArray());
			}
		};
	}
}
