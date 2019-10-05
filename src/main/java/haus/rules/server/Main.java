package haus.rules;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {
  public static void main(String[] args) throws IOException {
    HttpServer server =
				HttpServer.create(
						new InetSocketAddress(Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"))),
						0);
    server.createContext("/", new Handler());
    server.start();
  }
}
