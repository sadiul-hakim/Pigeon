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

    public static String toHtml(String input) {
        if (isLikelyMarkdown(input)) {
            String html = renderer.render(parser.parse(input));
            return sanitizeHtml(html);
        } else {
            // Just sanitize & escape plain text (preserve line breaks as <br>)
            String escaped = sanitizeHtml(escapeHtml(input));
            return escaped.replace("\n", "<br>");
        }
    }

    public static String sanitizeHtml(String unsafeHtml) {
        PolicyFactory policy = new HtmlPolicyBuilder()
                .allowElements("a", "b", "i", "u", "em", "strong", "code", "pre", "blockquote",
                        "ul", "ol", "li", "p", "br", "span",
                        "table", "thead", "tbody", "tr", "th", "td", "div", "h1", "h2", "h3", "h4", "h5", "h6", "img",
                        "video", "audio")
                .allowStandardUrlProtocols()
                .allowAttributes("href").onElements("a")
                .allowAttributes("src").onElements("img", "video", "audio")
                .allowAttributes("class").onElements(
                        "code", "pre", "table", "span", "div"
                )
                .toFactory();

        return policy.sanitize(unsafeHtml);
    }

    private static boolean isLikelyMarkdown(String text) {
        if (text == null || text.isBlank()) return false;

        // Common Markdown indicators
        return text.contains("**") || text.contains("* ")
                || text.contains("```") || text.contains("__")
                || text.contains("#") || text.contains("- ")
                || text.contains(">") || text.contains("|")
                || text.contains("`") || text.contains("~~")
                || text.contains("[ ]") || text.contains("[x]");
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
