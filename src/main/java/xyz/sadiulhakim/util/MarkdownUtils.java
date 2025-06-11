package xyz.sadiulhakim.util;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class MarkdownUtils {
    private static final Parser parser = Parser.builder().build();
    private static final HtmlRenderer renderer = HtmlRenderer.builder().build();

    public static String toHtml(String markdown) {
        String html = renderer.render(parser.parse(markdown));
        return sanitizeHtml(html);
    }

    public static String sanitizeHtml(String unsafeHtml) {
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS).and(Sanitizers.IMAGES);
        return policy.sanitize(unsafeHtml);
    }
}
