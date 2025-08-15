// FILE: src/sgms/util/ReportCardGenerator.java
package sgms.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import sgms.model.Student;
import sgms.service.ReportService;

/**
 * Utility to generate and open report cards from a DOCX template.
 * Supports:
 *  - Word MERGEFIELDs in both forms:
 *      (1) <w:fldSimple w:instr="... MERGEFIELD KEY ..."> ... </w:fldSimple>
 *      (2) Run-based begin / instrText " MERGEFIELD KEY " / ... / end sequences
 *  - Simple placeholders: {{KEY}}, ${KEY}, <<KEY>>, and «KEY»
 *  - mailMerge settings removal so Word does not prompt for a data source
 */
public final class ReportCardGenerator {

    private ReportCardGenerator() { }

    /**
     * Generates a DOCX by copying the bundled template and replacing placeholders
     * in document.xml (and headers/footers) with values from mergeData.
     *
     * @param mergeData map of field name → value (unescaped; this method escapes)
     * @return path to the generated .docx file
     * @throws IOException if the template cannot be read or file cannot be written
     */
    public static Path generateDocx(Map<String, String> mergeData) throws IOException {
        Path out = Files.createTempFile("report_card_", ".docx");

        try (InputStream templateIn = openTemplate();
             ZipInputStream zin = new ZipInputStream(templateIn);
             ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(out))) {

            ZipEntry inEntry;
            while ((inEntry = zin.getNextEntry()) != null) {
                String name = inEntry.getName();
                byte[] data = readAllBytes(zin);

                // Process document body and header/footer XMLs
                if ("word/document.xml".equals(name)
                        || name.startsWith("word/header")
                        || name.startsWith("word/footer")) {

                    String xml = new String(data, StandardCharsets.UTF_8);
                    String merged = applyMergeAll(xml, mergeData);
                    data = merged.getBytes(StandardCharsets.UTF_8);
                }
                // Remove mail merge settings so Word won’t prompt
                else if ("word/settings.xml".equals(name)) {
                    String xml = new String(data, StandardCharsets.UTF_8);
                    xml = xml.replaceAll("(?s)<w:mailMerge[\\s\\S]*?</w:mailMerge>", "");
                    data = xml.getBytes(StandardCharsets.UTF_8);
                }

                zout.putNextEntry(new ZipEntry(name));
                zout.write(data);
                zout.closeEntry();
                zin.closeEntry();
            }
        }

        return out;
    }

    /** Opens a DOCX using the OS default application (usually Word). */
    public static void openFile(Path file) throws IOException {
        openFile(file.toFile());
    }

    /** Variant accepting a File. */
    public static void openFile(File file) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }

    /**
     * Builds merge data for a student via your service layer.
     * Change the called method if your service uses a different name.
     */
    public static Map<String, String> buildData(Student s) throws Exception {
        ReportService service = new ReportService();
        return service.buildReportCardData(s);
    }

    // ----------------- merge helpers -----------------

    private static String applyMergeAll(String xml, Map<String, String> data) {
        if (xml == null || data == null || data.isEmpty()) return xml;

        String out = xml;
        out = applyPlaceholders(out, data);      // {{KEY}}, ${KEY}, <<KEY>>
        out = replaceFldSimple(out, data);       // <w:fldSimple ... MERGEFIELD KEY ...>
        out = replaceFieldCodeRuns(out, data);   // begin / instrText MERGEFIELD KEY / ... / end
        out = replaceChevrons(out, data);        // «KEY» human-readable placeholders
        return out;
    }

    /** Simple placeholders inside the XML text itself. */
    private static String applyPlaceholders(String xml, Map<String, String> data) {
        String out = xml;
        for (Map.Entry<String, String> e : data.entrySet()) {
            String key = e.getKey() == null ? "" : e.getKey().trim();
            if (key.length() == 0) continue;

            String value = e.getValue() == null ? "" : e.getValue();
            String escaped = escapeXmlWithLineBreaks(value);

            out = out.replace("{{" + key + "}}", escaped);
            out = out.replace("${" + key + "}", escaped);
            out = out.replace("<<" + key + ">>", escaped);
        }
        return out;
    }

    /** Replace <w:fldSimple ... w:instr="... MERGEFIELD KEY ..."> ... </w:fldSimple> with a plain run. */
    private static String replaceFldSimple(String xml, Map<String, String> data) {
        String out = xml;
        for (Map.Entry<String, String> e : data.entrySet()) {
            String key = e.getKey();
            if (key == null || key.trim().length() == 0) continue;

            String value = e.getValue() == null ? "" : e.getValue();
            String run   = makeRun(value); // builds <w:r><w:t>..</w:t></w:r> with <w:br/> for newlines

            String pattern =
                "(?s)<w:fldSimple[^>]*w:instr=\"[^\"]*MERGEFIELD\\s+"
                + java.util.regex.Pattern.quote(key)
                + "[^\"]*\"[^>]*>.*?</w:fldSimple>";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(out);
            out = m.replaceAll(java.util.regex.Matcher.quoteReplacement(run));
        }
        return out;
    }

    /**
     * Replace the run-based field code form:
     *   <w:r><w:fldChar w:type="begin"/></w:r> ... <w:instrText> MERGEFIELD KEY </w:instrText> ... <w:r><w:fldChar w:type="end"/></w:r>
     * with a single run containing the value.
     */
    private static String replaceFieldCodeRuns(String xml, Map<String, String> data) {
        String out = xml;
        for (Map.Entry<String, String> e : data.entrySet()) {
            String key = e.getKey();
            if (key == null || key.trim().length() == 0) continue;

            String value = e.getValue() == null ? "" : e.getValue();
            String run   = makeRun(value);

            // Conservative pattern: from a begin fldChar to a matching end fldChar
            // where an instrText containing MERGEFIELD KEY appears between them.
            String pattern =
                "(?s)"
              + "<w:r[^>]*>\\s*<w:fldChar[^>]*w:type=\"begin\"[^>]*/>\\s*</w:r>"
              + ".*?"
              + "<w:instrText[^>]*>[^<]*MERGEFIELD\\s+" + java.util.regex.Pattern.quote(key) + "[^<]*</w:instrText>"
              + ".*?"
              + "<w:r[^>]*>\\s*<w:fldChar[^>]*w:type=\"end\"[^>]*/>\\s*</w:r>";

            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(out);
            out = m.replaceAll(java.util.regex.Matcher.quoteReplacement(run));
        }
        return out;
    }

    /** Replace human-readable «KEY» placeholders if they survived as plain text. */
    private static String replaceChevrons(String xml, Map<String, String> data) {
        String out = xml;
        for (Map.Entry<String, String> e : data.entrySet()) {
            String key = e.getKey();
            if (key == null || key.trim().length() == 0) continue;

            String value = e.getValue() == null ? "" : e.getValue();
            String escaped = escapeXmlWithLineBreaks(value);
            String chevron = "\u00AB" + key + "\u00BB"; // «KEY»
            out = out.replace(chevron, escaped);
        }
        return out;
    }

    // ----------------- file/template helpers -----------------

    private static InputStream openTemplate() throws IOException {
        // Try from resources first
        InputStream in = ReportCardGenerator.class.getResourceAsStream("/sgms/data/Report_Card.docx");
        if (in != null) return in;

        // Fallback to project paths (useful when running from NetBeans)
        Path p1 = Path.of("src", "sgms", "data", "Report_Card.docx");
        if (Files.exists(p1)) return Files.newInputStream(p1);

        Path p2 = Path.of("sgms", "data", "Report_Card.docx");
        if (Files.exists(p2)) return Files.newInputStream(p2);

        throw new IOException("Report_Card.docx not found in resources or project folder.");
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copyStream(in, bos);
        return bos.toByteArray();
    }

    private static void copyStream(InputStream in, java.io.OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) != -1) {
            out.write(buf, 0, r);
        }
    }

    // ----------------- text helpers -----------------

    /** Builds a Word run for the given plain text (with <w:br/> for newlines); escapes XML. */
    private static String makeRun(String text) {
        String safe = text == null ? "" : text;
        String[] parts = safe.split("\\n", -1);
        StringBuilder run = new StringBuilder("<w:r>");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) run.append("<w:br/>");
            run.append("<w:t>").append(escapeXml(parts[i])).append("</w:t>");
        }
        run.append("</w:r>");
        return run.toString();
    }

    /** Escape XML and preserve newlines using <w:br/> where the caller inserts into text directly. */
    private static String escapeXmlWithLineBreaks(String s) {
        if (s == null || s.length() == 0) return "";
        String[] parts = s.split("\\n", -1);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) b.append("<w:br/>");
            b.append(escapeXml(parts[i]));
        }
        return b.toString();
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
