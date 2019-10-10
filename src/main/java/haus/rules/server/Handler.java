package haus.rules;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Handler implements HttpHandler {
	private static final Logger log = Logger.getLogger(Handler.class.getName());

	private final Response indexFile;
	private final Response faviconFile;
	private final Map<String, Response> responses = new HashMap<>();

	public Handler() throws IOException {
		indexFile = new Response(this.getClass().getResourceAsStream("/index.html").readAllBytes(), "text/html");
		faviconFile = new Response(this.getClass().getResourceAsStream("/favicon.png").readAllBytes(), "image/png");
		for (Map.Entry<String, Page> e : PageList.loadPages("/sitemap.json").entrySet()) {
			responses.put(e.getKey(), new Response(e.getValue().toJson().getBytes(StandardCharsets.UTF_8), "application/json"));
		}
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		URI uri = exchange.getRequestURI();
		if ("GET".equals(exchange.getRequestMethod())) {
			String path = exchange.getRequestURI().getPath();
			if (path.startsWith("/json/") && responses.containsKey(path.substring("/json".length()))) {
				respond(exchange, responses.get(path.substring("/json".length())));
			} else if ("/favicon.png".equals(path)) {
				respond(exchange, faviconFile);
			} else {
				respond(exchange, indexFile);
			}
		} else {
			exchange.sendResponseHeaders(405, -1);
		}
	}

	private static void respond(HttpExchange exchange, Response body) throws IOException {
		String etag = body.getEtag();
		exchange.getResponseHeaders().add("Cache-Control", "public,max-age=600");
		exchange.getResponseHeaders().add("Content-Type", body.getMime());
		exchange.getResponseHeaders().add("ETag", body.getEtag());
		List<String> noneMatch = exchange.getRequestHeaders().get("If-None-Match");
		if (noneMatch != null && noneMatch.contains(etag)) {
			exchange.sendResponseHeaders(204, -1);
		} else {
			exchange.sendResponseHeaders(200, body.getData().length);
			try (OutputStream o = exchange.getResponseBody()) {
				o.write(body.getData());
			}
		}
	}
}
