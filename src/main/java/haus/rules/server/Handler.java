package haus.rules;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.URI;
import java.net.URISyntaxException;

public class Handler implements HttpHandler {
	private static final Logger log = Logger.getLogger(Handler.class.getName());

	private final byte[] indexFile;
	private final byte[] errorPage;
	private final Map<String, ByteArray> responses = new HashMap<>();

	public Handler() throws IOException {
		indexFile = this.getClass().getResourceAsStream("/index.html").readAllBytes();
		errorPage = new byte[0]; // TODO: populate
		for (Map.Entry<String, Page> e : PageList.loadPages("/sitemap.json").entrySet()) {
			responses.put(e.getKey(), new ByteArray(e.getValue().toJson().getBytes(StandardCharsets.UTF_8)));
		}
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		URI uri = exchange.getRequestURI();
		if ("http".equals(uri.getScheme()) && !"localhost".equals(uri.getHost())) {
			try {
				exchange.getResponseHeaders().set("Location", new URI("https", uri.getHost(), uri.getPath(), uri.getFragment()).toString());
				exchange.sendResponseHeaders(301, 0);
			} catch (URISyntaxException e) {
				throw new IOException("Failed to create Location URI.", e);
			}
			return;
		}
		if ("GET".equals(exchange.getRequestMethod())) {
			String path = exchange.getRequestURI().getPath();
			if ("/".equals(path)) {
				respond(exchange, 200, indexFile);
			} else if (path.startsWith("/json/") && responses.containsKey(path.substring("/json".length()))) {
				respond(exchange, 200, responses.get(path.substring("/json".length())).array);
			} else {
				respond(exchange, 404, errorPage);
			}
		} else {
			exchange.sendResponseHeaders(405, 0);
		}
	}

	private static void respond(HttpExchange exchange, int status, byte[] body) throws IOException {
		exchange.sendResponseHeaders(status, body.length);
		try (OutputStream o = exchange.getResponseBody()) {
			o.write(body);
		}
	}

	private static class ByteArray {
		final byte[] array;
		ByteArray(byte[] array) { this.array = array; }
	}
}
