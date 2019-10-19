package haus.rules;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

public class PageListTest {
	@Test public void testLoads() throws Exception {
		assertNotNull(PageList.loadPages("/sitemap.json"));
	}

	@Test public void testDeadLinks() throws Exception {
		Pattern linker = Pattern.compile("\\[.*?\\]\\((/.*?)\\)");
		Map<String, Page> pages = PageList.loadPages("/sitemap.json");
		List<String> errors = new ArrayList<>();
		for (Map.Entry<String, Page> e : pages.entrySet()) {
			Matcher m = linker.matcher(e.getValue().text);
			while (m.find()) {
				if (!pages.containsKey(m.group(1))) errors.add(e.getKey() + " has dead link to \"" + m.group(1) + "\"");
			}
		}
		for (String s : errors) System.out.println(s);
		assertTrue(errors.isEmpty());
	}

	@Test public void testSelfLinks() throws Exception {
		Pattern linker = Pattern.compile("\\[.*?\\]\\((/.*?)\\)");
		List<String> errors = new ArrayList<>();
		for (Map.Entry<String, Page> e : PageList.loadPages("/sitemap.json").entrySet()) {
			Matcher m = linker.matcher(e.getValue().text);
			while (m.find()) {
				if (m.group(1).equals(e.getKey())) errors.add(e.getKey() + " has a self-link");
			}
		}
		for (String s : errors) System.out.println(s);
		assertTrue(errors.isEmpty());
	}

	@Test public void testUrlStyle() throws Exception {
		Pattern allowed = Pattern.compile("/|(/[a-z0-9_]+)+");
		List<String> errors = new ArrayList<>();
		for (String url : PageList.loadPages("/sitemap.json").keySet()) {
			if (!allowed.matcher(url).matches()) errors.add(url + " doesn't match the allowed pattern (a-z, 0-9, and underscores only)");
		}
		for (String s : errors) System.out.println(s);
		assertTrue(errors.isEmpty());
	}

	@Test public void testCommonTypos() throws Exception {
		Map<String, String> typos = new HashMap<>();
		List<String> errors = new ArrayList<>();
		typos.put("role-playing", "roleplaying");
		typos.put("table-top", "tabletop");
		for (Map.Entry<String, Page> e : PageList.loadPages("/sitemap.json").entrySet()) {
			for (Map.Entry<String, String> typo : typos.entrySet()) {
				if (e.getValue().text.contains(typo.getKey())) errors.add(e.getKey() + " contains \"" + typo.getKey() + "\", use \"" + typo.getValue() + "\" instead");
			}
		}
		for (String s : errors) System.out.println(s);
		assertTrue(errors.isEmpty());
	}
}
