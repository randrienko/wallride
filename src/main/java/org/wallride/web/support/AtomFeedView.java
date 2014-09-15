package org.wallride.web.support;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;
import org.springframework.web.util.UriComponentsBuilder;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.Setting;
import org.wallride.core.service.BlogService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class AtomFeedView extends AbstractAtomFeedView {

	private BlogService blogService;

	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	protected void buildFeedMetadata(
			Map<String, Object> model,
			Feed feed, 
			HttpServletRequest request) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		String language = LocaleContextHolder.getLocale().getLanguage();

		feed.setTitle(blog.getTitle(language));
		Content info = new Content();
		info.setValue(blog.getTitle(language));
		feed.setInfo(info);

		ArrayList<Link> links = new ArrayList<>();
		Link link = new Link();
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		link.setHref(builder.buildAndExpand().toUriString());
		links.add(link);
		feed.setOtherLinks(links);
//		feed.setIcon("http://" + settings.getAsString(Setting.Key.SITE_URL) + "resources/default/img/favicon.ico");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected List<Entry> buildFeedEntries(
			Map<String, Object> model,
			HttpServletRequest request,
			HttpServletResponse response)
	throws Exception {
		Set<Article> articles = (Set<Article>)model.get("articles");
		List<Entry> entries = new ArrayList<>(articles.size());
		for (Article article : articles) {
			Entry entry = new Entry();
			entry.setTitle(article.getTitle());
			entry.setPublished(article.getDate().toDate());
			Content content = new Content();
			content.setValue(article.getBody());
			entry.setSummary(content);

			Link link = new Link();
			link.setHref(link(article));
			List<Link> links = new ArrayList<Link>();
			links.add(link);
			entry.setAlternateLinks(links);
			entries.add(entry);
		}
		return entries;
	}

	private String link(Article article) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		Map<String, Object> params = new HashMap<>();

		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		if (blog.getLanguages().size() > 1) {
			builder.path("/{language}");
			params.put("language", LocaleContextHolder.getLocale().getLanguage());
		}
		builder.path("/{year}/{month}/{day}/{code}");
		params.put("year", String.format("%04d", article.getDate().getYear()));
		params.put("month", String.format("%02d", article.getDate().getMonthOfYear()));
		params.put("day", String.format("%02d", article.getDate().getDayOfMonth()));
		params.put("code", article.getCode());
		return builder.buildAndExpand(params).encode().toUriString();
	}
}
