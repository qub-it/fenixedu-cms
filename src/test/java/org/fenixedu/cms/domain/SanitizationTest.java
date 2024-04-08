package org.fenixedu.cms.domain;

import java.util.Locale;

import org.fenixedu.commons.i18n.LocalizedString;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SanitizationTest {

    @Test
    public void testSanitizeRemovesScriptTagXSS() {
        String original = "<a href=\"javascript:alert('XSS')\">Malicious Link</a>";
        assertEquals("Malicious Link", Sanitization.sanitize(original));
    }

    @Test
    public void testStrictSanitizeRemovesScriptTagXSS() {
        String original = "<a href=\"javascript:alert('XSS')\">Malicious Link</a>";
        assertEquals("Malicious Link", Sanitization.strictSanitize(original));
    }

    @Test
    public void testSanitizeRemovesScriptTagXSSFromLocalizedString() {
        LocalizedString localizedString = new LocalizedString();
        localizedString = localizedString.with(Locale.ENGLISH, "<a href=\"javascript:alert('XSS')\">Malicious Link</a>");
        localizedString = localizedString.with(Locale.FRENCH, "<a href=\"javascript:alert('XSS')\">Lien malveillant</a>");

        LocalizedString expected = new LocalizedString();
        expected = expected.with(Locale.ENGLISH, "Malicious Link");
        expected = expected.with(Locale.FRENCH, "Lien malveillant");

        assertEquals(expected, Sanitization.sanitize(localizedString));
    }

    @Test
    public void testStrictSanitizeRemovesScriptTagXSSFromLocalizedString() {
        LocalizedString localizedString = new LocalizedString();
        localizedString = localizedString.with(Locale.ENGLISH, "<a href=\"javascript:alert('XSS')\">Malicious Link</a>");
        localizedString = localizedString.with(Locale.FRENCH, "<a href=\"javascript:alert('XSS')\">Lien malveillant</a>");

        LocalizedString expected = new LocalizedString();
        expected = expected.with(Locale.ENGLISH, "Malicious Link");
        expected = expected.with(Locale.FRENCH, "Lien malveillant");

        assertEquals(expected, Sanitization.strictSanitize(localizedString));
    }

    @Test
    public void testSanitizeRemovesSimpleXSSPayload() {
        String original = "<script>alert('XSS');</script>";
        assertEquals("", Sanitization.sanitize(original));
    }

    @Test
    public void testSanitizeRemovesComplexXSSPayload() {
        String original = "<IMG SRC=`javascript:alert(\"XSS\")`>";
        assertEquals("", Sanitization.sanitize(original));
    }

    @Test
    public void testSanitizeRemovesXSSWithEventHandlers() {
        String original = "<img src=x onerror=alert('XSS')>";
        assertEquals("<img src=\"x\" />", Sanitization.sanitize(original));
    }

    @Test
    public void testStrictSanitizeRemovesXSSWithDataURL() {
        String original = "<IMG SRC=\"javascript:alert('XSS');\">";
        assertEquals("", Sanitization.strictSanitize(original));
    }

    @Test
    public void testSanitizeRemovesXSSFromAnchorTagInLocalizedString() {
        LocalizedString localizedString = new LocalizedString();
        localizedString = localizedString.with(Locale.ENGLISH, "<a href=\"javascript:alert('XSS');\">Click me</a>");

        LocalizedString expected = new LocalizedString();
        expected = expected.with(Locale.ENGLISH, "Click me");

        assertEquals(expected, Sanitization.sanitize(localizedString));
    }

    @Test
    public void testStrictSanitizeRemovesXSSFromImageTagInLocalizedString() {
        LocalizedString localizedString = new LocalizedString();
        localizedString = localizedString.with(Locale.ENGLISH, "<IMG SRC=\"javascript:alert('XSS');\">");

        LocalizedString expected = new LocalizedString();
        expected = expected.with(Locale.ENGLISH, "");

        assertEquals(expected, Sanitization.strictSanitize(localizedString));
    }

    @Test
    public void testSanitizeHandlesEmptyInput() {
        assertEquals("", Sanitization.sanitize(""));
    }

    @Test
    public void testStrictSanitizeHandlesEmptyInput() {
        assertEquals("", Sanitization.strictSanitize(""));
    }

    @Test
    public void testSanitizePreservesSafeHTML() {
        String original = "<b>Safe Content</b>";
        assertEquals(original, Sanitization.sanitize(original));
    }

    @Test
    public void testStrictSanitizePreservesSafeHTML() {
        String original = "<i>Safe Content</i>";
        assertEquals("Safe Content", Sanitization.strictSanitize(original));
    }

    @Test
    public void testSanitizeHandlesNonASCIICharacters() {
        String original = "<p>Non-ASCII Characters: äöü</p>";
        assertEquals(original, Sanitization.sanitize(original));
    }

    @Test
    public void testStrictSanitizeHandlesNonASCIICharacters() {
        String original = "<p>Non-ASCII Characters: éèç</p>";
        assertEquals("Non-ASCII Characters: éèç", Sanitization.strictSanitize(original));
    }

    @Test
    public void testSanitizeHandlesHTMLEntities() {
        String original = "<p>HTML Entities: &lt; &gt; &amp;</p>";
        assertEquals(original, Sanitization.sanitize(original));
    }

    @Test
    public void testStrictSanitizeHandlesHTMLEntities() {
        String original = "<p>HTML Entities: &copy; &reg; &nbsp;</p>";
        assertEquals("HTML Entities: © ®  ", Sanitization.strictSanitize(original));
    }

    @Test
    public void testSanitizeHandlesMixedContent() {
        String original = "<p>Safe Content <script>alert('XSS');</script></p>";
        assertEquals("<p>Safe Content </p>", Sanitization.sanitize(original));
    }

    @Test
    public void testStrictSanitizeHandlesMixedContent() {
        String original = "<p>Safe Content <script>alert('XSS');</script></p>";
        assertEquals("Safe Content ", Sanitization.strictSanitize(original));
    }

    @Test
    public void testSanitizeHandlesInvalidHTMLStructure() {
        String original = "<p>Unclosed Tag";
        assertEquals("<p>Unclosed Tag</p>", Sanitization.sanitize(original));
    }

    @Test
    public void testStrictSanitizeHandlesInvalidHTMLStructure() {
        String original = "<p>Missing Closing Tag</div>";
        assertEquals("Missing Closing Tag", Sanitization.strictSanitize(original));
    }

    @Test
    public void testSanitizeRemovesScriptTag() {
        String original = "<script>alert('XSS')</script>";
        assertEquals("", Sanitization.sanitize(original));
    }

    @Test
    public void testSanitizeRemovesImageTagWithInvalidSrc() {
        String original = "<img src=x onerror=alert('XSS')>";
        assertEquals("<img src=\"x\" />", Sanitization.sanitize(original));
    }

    @Test
    public void testSanitizeRemovesIframeTag() {
        String original = "<iframe src=\"javascript:alert('XSS')\"></iframe>";
        assertEquals("<iframe></iframe>", Sanitization.sanitize(original));
    }

    @Test
    public void testSanitizeRemovesFontTagWithInvalidAttributes() {
        String original = "<font color=\"red\" size=\"5\" face=\"Arial\" style=\"background-color: yellow;\">Text</font>";
        assertEquals("<font color=\"red\" size=\"5\" face=\"Arial\" style=\"background-color:yellow\">Text</font>", Sanitization.sanitize(original));
    }

    @Test
    public void testSanitizeRemovesAudioTag() {
        String original = "<audio controls autoplay><source src=\"javascript:alert('XSS')\"></audio>";
        assertEquals("", Sanitization.sanitize(original));
    }

    @Test
    public void testStrictSanitizeRemovesScriptTag() {
        String original = "<script>alert('XSS')</script>";
        assertEquals("", Sanitization.strictSanitize(original));
    }

    @Test
    public void testStrictSanitizeRemovesImageTagWithInvalidSrc() {
        String original = "<img src=x onerror=alert('XSS')>";
        assertEquals("", Sanitization.strictSanitize(original));
    }

    @Test
    public void testStrictSanitizeRemovesIframeTag() {
        String original = "<iframe src=\"javascript:alert('XSS')\"></iframe>";
        assertEquals("", Sanitization.strictSanitize(original));
    }

    @Test
    public void testStrictSanitizeRemovesFontTagWithInvalidAttributes() {
        String original = "<font color=\"red\" size=\"5\" face=\"Arial\" style=\"background-color: yellow;\">Text</font>";
        assertEquals("Text", Sanitization.strictSanitize(original));
    }

    @Test
    public void testStrictSanitizeRemovesAudioTag() {
        String original = "<audio controls autoplay><source src=\"javascript:alert('XSS')\"></audio>";
        assertEquals("", Sanitization.strictSanitize(original));
    }
}