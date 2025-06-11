package xyz.sadiulhakim.util;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.List;

public class MarkdownUtils {
    private static final List<Extension> extensions = List.of(TablesExtension.create());
    private static final Parser parser = Parser.builder().extensions(extensions).build();
    private static final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();

    public static String toHtml(String markdown) {
        String html = renderer.render(parser.parse(markdown));
        return sanitizeHtml(html);
    }

    public static String sanitizeHtml(String unsafeHtml) {
        PolicyFactory policy = new HtmlPolicyBuilder()
                .allowElements("a", "b", "i", "u", "em", "strong", "code", "pre", "blockquote",
                        "ul", "ol", "li", "p", "br", "span",
                        "table", "thead", "tbody", "tr", "th", "td")
                .allowStandardUrlProtocols()
                .allowAttributes("href").onElements("a")
                .allowAttributes("class").onElements("code", "pre", "table", "span")
                .toFactory();

        return policy.sanitize(unsafeHtml);
    }
}
