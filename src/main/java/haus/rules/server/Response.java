package haus.rules;

import com.google.common.io.BaseEncoding;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Response {
	private final byte[] data;
	private final String etag;
	private final String mime;

	public Response(byte[] data, String mime) {
		this.mime = mime;
		this.data = data;
		try {
			etag = BaseEncoding.base64Url().encode(MessageDigest.getInstance("SHA-256").digest(data));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Failed to hash content.", e);
		}
	}

	String getMime() {
		return mime;
	}

	String getEtag() {
		return etag;
	}

	byte[] getData() {
		return data;
	}
}
