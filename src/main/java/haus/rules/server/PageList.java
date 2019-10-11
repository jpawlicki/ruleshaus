package haus.rules;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class PageList {
	public static Map<String, Page> loadPages(String resource) throws IOException {
		Entry root = new Gson().fromJson(new InputStreamReader(PageList.class.getResourceAsStream(resource), StandardCharsets.UTF_8), Entry.class);
		Map<String, Page> map = new HashMap<>();
		ArrayList<Page.Spec> ancestry = new ArrayList<>();
		addMap(root, map, ancestry);
		return map;
	}

	private static void addMap(Entry node, Map<String, Page> map, ArrayList<Page.Spec> ancestors) throws IOException {
		List<Page.Spec> childspecs = node.pages.stream().map(c -> new Page.Spec(c.title, c.url)).collect(Collectors.toList());
		Page.Spec thisSpec = new Page.Spec(node.title, node.url);
		ancestors.add(thisSpec);
		String url = ancestors.stream().map(s -> s.url).collect(Collectors.joining("/"));
		String text;
		try (InputStream resource = PageList.class.getResourceAsStream(url + "/_.md")) {
			if (resource == null) throw new IOException("Failed to open resource file: " + url + "/_.md");
			text = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
		}
		if ("".equals(url)) url = "/";
		map.put(url, new Page(node.title, node.source, childspecs, ancestors, text));
		for (Entry child : node.pages) addMap(child, map, ancestors);
		ancestors.remove(thisSpec);
	}

	private static final class Entry {
		String source;
		String title;
		String url;
		List<Entry> pages = new ArrayList<>();
	}

	private PageList() {}
}
