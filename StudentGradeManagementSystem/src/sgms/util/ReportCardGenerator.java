package sgms.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import sgms.model.Student;
import sgms.service.ReportService;

/**
 * Builds a report card .docx from a template by replacing merge fields
 * with values (simple “mail merge” done in code).
 */
public final class ReportCardGenerator {

    private ReportCardGenerator() {}

    /**
     * Copy the template and replace merge fields/placeholders with values.
     * Looks in document.xml plus headers/footers and removes Word’s mailMerge
     * block so it won’t ask for a data source.
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

                // Replace fields in the main document and headers/footers
                if ("word/document.xml".equals(name)
                        || name.startsWith("word/header")
                        || name.startsWith("word/footer")) {
                    String xml = new String(data, StandardCharsets.UTF_8);
                    xml = applyMergeAll(xml, mergeData);
                    data = xml.getBytes(StandardCharsets.UTF_8);
                }
                // Remove the mailMerge settings so Word doesn’t prompt
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

    /** Open a file using the default desktop app (usually Word). */
    public static void openFile(Path file) throws IOException {
        openFile(file.toFile());
    }

    /** Same as above but accepts a File. */
    public static void openFile(File file) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }

    /** Ask the service layer to build the merge data for a student. */
    public static Map<String, String> buildData(Student s) throws Exception {
        ReportService service = new ReportService();
        return service.buildReportCardData(s);
    }

    // ----------------- merge helpers -----------------

    /**
     * Apply all supported replacements:
     *  - {{KEY}}, ${KEY}, <<KEY>>
     *  - Word MERGEFIELD (fldSimple)
     *  - Word MERGEFIELD (begin/instrText/end runs)
     *  - «KEY» chevrons
     */
    private static String applyMergeAll(String xml, Map<String, String> data) {
        if (xml == null || data == null || data.isEmpty()) return xml;

        String out = xml;
        out = applyPlaceholders(out, data);
        out = replaceFldSimple(out, data);
        out = replaceFieldCodeRuns(out, data);
        out = replaceChevrons(out, data);
        return out;
    }

    /** Replace simple text placeholders in the XML. */
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

    /** Replace <w:fldSimple ... MERGEFIELD KEY ...>...</w:fldSimple> with a plain run. */
    private static String replaceFldSimple(String xml, Map<String, String> data) {
        String out = xml;
        for (Map.Entry<String, String> e : data.entrySet()) {
            String key = e.getKey();
            if (key == null || key.trim().length() == 0) continue;

            String value = e.getValue() == null ? "" : e.getValue();
            String run = makeRun(value);

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
     * Replace the run-based MERGEFIELD (begin … instrText … end) with a single run.
     */
    private static String replaceFieldCodeRuns(String xml, Map<String, String> data) {
        String out = xml;
        for (Map.Entry<String, String> e : data.entrySet()) {
            String key = e.getKey();
            if (key == null || key.trim().length() == 0) continue;

            String value = e.getValue() == null ? "" : e.getValue();
            String run = makeRun(value);

            String pattern =
                "(?s)"
              + "<w:r[^>]*>\\s*<w:fldChar[^>]*w:type=\"begin\"[^>]*/>\\s*</w:r>"
              + ".*?"
              + "<w:instrText[^>]*>[^<]*MERGEFIELD\\s+"
              + java.util.regex.Pattern.quote(key)
              + "[^<]*</w:instrText>"
              + ".*?"
              + "<w:r[^>]*>\\s*<w:fldChar[^>]*w:type=\"end\"[^>]*/>\\s*</w:r>";

            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(out);
            out = m.replaceAll(java.util.regex.Matcher.quoteReplacement(run));
        }
        return out;
    }

    /** Replace «KEY» if it appears as plain text. */
    private static String replaceChevrons(String xml, Map<String, String> data) {
        String out = xml;
        for (Map.Entry<String, String> e : data.entrySet()) {
            String key = e.getKey();
            if (key == null || key.trim().length() == 0) continue;

            String value = e.getValue() == null ? "" : e.getValue();
            String escaped = escapeXmlWithLineBreaks(value);
            String chevron = "\u00AB" + key + "\u00BB";
            out = out.replace(chevron, escaped);
        }
        return out;
    }

    // ----------------- file/template helpers -----------------

    private static InputStream openTemplate() throws IOException {
        // 1) classpath (built resources)
        InputStream in = ReportCardGenerator.class.getResourceAsStream("/sgms/data/Report_Card.docx");
        if (in != null) return in;

        // 2) source tree (for running inside an IDE)
        Path p1 = Path.of("src", "sgms", "data", "Report_Card.docx");
        if (Files.exists(p1)) return Files.newInputStream(p1);

        // 3) plain folder (fallback)
        Path p2 = Path.of("sgms", "data", "Report_Card.docx");
        if (Files.exists(p2)) return Files.newInputStream(p2);

        throw new IOException("Report_Card.docx not found.");
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

    /** Turn plain text into a Word run, keeping new lines. */
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

    /** Escape XML and insert <w:br/> for new lines. */
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

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
