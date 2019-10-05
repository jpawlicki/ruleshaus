package haus.rules;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Handler implements HttpHandler {
	private static final Logger log = Logger.getLogger(Handler.class.getName());

	private final byte[] indexFile;

	public Handler() throws IOException {
		// need to switch to getResourceAsStream.
		indexFile = this.getClass().getResourceAsStream("/index.html").readAllBytes();
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if ("GET".equals(exchange.getRequestMethod())) {
			// TODO: HTTP→HTTPS pinning (needed for crypto.subtle).
			if ("/".equals(exchange.getRequestURI().getPath())) {
				exchange.sendResponseHeaders(200, indexFile.length);
				exchange.getResponseBody().write(indexFile);
			} else {
				// TODO: Nicer error pages.
				exchange.sendResponseHeaders(404, 0);
			}
		} else {
			exchange.sendResponseHeaders(405, 0);
		}
	}
}
