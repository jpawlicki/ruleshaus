package haus.rules;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public final class Page {
	static final class Spec {
		final String title;
		final String url;

		Spec(String title, String url) {
			this.title = title;
			this.url = url;
		}
	}

	final String title;
	final String source;
	final List<Spec> subpages;
	final List<Spec> path;
	final String text;

	Page(String title, String source, List<Spec> subpages, List<Spec> path, String text) {
		this.title = title;
		this.source = source;
		this.subpages = new ArrayList<>(subpages);
		this.path = new ArrayList<>(path);
		this.text = text;
	}

	public String toJson() {
		return new Gson().toJson(this);
	}
}
