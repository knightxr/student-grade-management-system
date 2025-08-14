package sgms.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import sgms.dao.AssignmentDAO;
import sgms.dao.CourseDAO;
import sgms.dao.FeedbackDAO;
import sgms.dao.GradeDAO;
import sgms.model.Assignment;
import sgms.model.Course;
import sgms.model.Student;

/**
 * Utility class for generating a report card from the bundled Word template.
 * <p>
 * The implementation performs a very small subset of Mail Merge by replacing
 * merge fields in the template's <code>word/document.xml</code> with values
 * provided in a map. The resulting document is written to a temporary file
 * which can then be printed using the operating system's default application.
 * </p>
 */
public final class ReportCardGenerator {

    private ReportCardGenerator() {
    }

    /**
     * Generates a new DOCX report card with the provided merge data.
     *
     * @param mergeData mapping of merge field names to values
     * @return path to the generated DOCX file
     * @throws IOException if an I/O error occurs
     */
    public static Path generateDocx(Map<String, String> mergeData) throws IOException {
        Path out = Files.createTempFile("report_card_", ".docx");

        try (InputStream template = ReportCardGenerator.class.getResourceAsStream("/sgms/data/Report_Card.docx");
             ZipInputStream zin = new ZipInputStream(template);
             ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(out))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                byte[] data = zin.readAllBytes();
                String name = entry.getName();
                if ("word/document.xml".equals(name)) {
                    String xml = new String(data, StandardCharsets.UTF_8);
                    for (Map.Entry<String, String> e : mergeData.entrySet()) {
                        String field = Pattern.quote(e.getKey());
                        String value = escapeXml(e.getValue() == null ? "" : e.getValue());

                        String[] parts = value.split("\\n", -1);
                        StringBuilder run = new StringBuilder("<w:r>");
                        for (int i = 0; i < parts.length; i++) {
                            if (i > 0) {
                                run.append("<w:br/>");
                            }
                            run.append("<w:t>").append(parts[i]).append("</w:t>");
                        }
                        run.append("</w:r>");
                        String replacement = run.toString();

                        // Handle simple merge fields stored in <w:fldSimple>
                        String patternSimple =
                                "<w:fldSimple[^>]*MERGEFIELD\\s+\\\"?" + field + "\\\"?[^>]*>.*?</w:fldSimple>";
                        xml = xml.replaceAll(patternSimple, Matcher.quoteReplacement(replacement));

                        // Handle complex fields composed of w:fldChar and w:instrText
                        String patternComplex =
                                "<w:fldChar[^>]*/><w:instrText[^>]*>[^<]*MERGEFIELD\\s+" +
                                field + "[^<]*</w:instrText>.*?<w:fldChar[^>]*w:fldCharType=\\\"end\\\"[^>]*/>";
                        xml = xml.replaceAll(patternComplex, Matcher.quoteReplacement(replacement));

                        // Replace the human-readable placeholder text such as «Field_Name»
                        String placeholder = "\u00AB" + e.getKey() + "\u00BB";
                        xml = xml.replace(placeholder, value);
                    }
                    data = xml.getBytes(StandardCharsets.UTF_8);
                } else if ("word/settings.xml".equals(name)) {
                    String xml = new String(data, StandardCharsets.UTF_8);
                    // Remove mail merge settings so Word does not prompt for a data source
                    xml = xml.replaceAll("<w:mailMerge[\\s\\S]*?</w:mailMerge>", "");
                    data = xml.getBytes(StandardCharsets.UTF_8);
                }
                zout.putNextEntry(new ZipEntry(entry.getName()));
                zout.write(data);
                zout.closeEntry();
                zin.closeEntry();
            }
        }

        return out;
    }

    /**
     * Opens the given file using the desktop's default application. This is
     * used to launch the generated report card in Microsoft Word so the user
     * can review or export it to PDF.
     */
    public static void openFile(Path file) throws IOException {
        openFile(file.toFile());
    }

    /**
     * Variant accepting a {@link java.io.File} for callers compiled against
     * older versions of this utility.
     */
    public static void openFile(File file) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }

    /**
     * Builds merge data for the given student by querying the various DAOs for
     * grades and feedback. Attendance has been removed from the report card
     * template so it is no longer included here.
     */
    public static Map<String, String> buildData(Student s,
            CourseDAO courseDAO,
            AssignmentDAO assignmentDAO,
            GradeDAO gradeDAO,
            FeedbackDAO feedbackDAO) throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("First_Name", s.getFirstName());
        data.put("Last_Name", s.getLastName());
        data.put("Grade", String.valueOf(s.getGradeLevel()));
        LocalDate now = LocalDate.now();
        data.put("Year", String.valueOf(now.getYear()));
        data.put("Date", now.toString());

        List<Course> courses = courseDAO.findByGrade(s.getGradeLevel());

        // Pre‑populate merge fields for expected subjects so placeholders are
        // always replaced even if a course or grades are missing.
        String[] names = {
            "English", "Afrikaans", "Mathematics", "Physics",
            "Business Studies", "Information Technology", "French", "Music"
        };
        Map<String, String> normalised = new HashMap<>();
        for (String name : names) {
            String base = name.replace(' ', '_');
            for (int i = 1; i <= 4; i++) {
                data.put(base + "_T" + i, "");
            }
            data.put(base + "_Final", "");
            data.put(base + "_Feedback", "");
            normalised.put(normalize(name), name);
        }

        int studentId = s.getStudentId();
        StringBuilder feedback = new StringBuilder();

        for (Course c : courses) {
            String name = normalised.get(normalize(c.getCourseName()));
            if (name == null) {
                // attempt substring match for courses with extended names
                String normCourse = normalize(c.getCourseName());
                for (Map.Entry<String, String> e : normalised.entrySet()) {
                    if (normCourse.contains(e.getKey())) {
                        name = e.getValue();
                        break;
                    }
                }
            }
            if (name == null) {
                continue; // no placeholder for this course
            }

            String base = name.replace(' ', '_');
            List<Assignment> assigns = assignmentDAO.findByCourse(c.getCourseId());
            Map<Integer, Map<Integer, Integer>> gradesByStudent = gradeDAO.findByCourse(c.getCourseId());
            Map<Integer, Integer> grades = gradesByStudent.getOrDefault(studentId, java.util.Collections.emptyMap());

            double[] termSum = new double[4];
            int[] termCount = new int[4];
            for (Assignment a : assigns) {
                Integer mark = grades.get(a.getAssignmentId());
                if (mark != null) {
                    int term = a.getTerm();
                    termSum[term - 1] += mark;
                    termCount[term - 1]++;
                }
            }
            Double[] termAvg = new Double[4];
            for (int i = 0; i < 4; i++) {
                String key = base + "_T" + (i + 1);
                if (termCount[i] > 0) {
                    termAvg[i] = termSum[i] / termCount[i];
                    data.put(key, String.valueOf(Math.round(termAvg[i])));
                } else {
                    termAvg[i] = null; // remains blank
                }
            }
            Double finalAvg = GradeCalculator.calculateFinalGrade(termAvg[0], termAvg[1], termAvg[2], termAvg[3]);
            if (finalAvg != null) {
                data.put(base + "_Final", String.valueOf(Math.round(finalAvg)));
            }

            Map<Integer, String> fb = feedbackDAO.findByCourse(c.getCourseId());
            String note = fb.get(studentId);
            if (note != null) {
                note = note.trim();
                if (!note.isEmpty()) {
                    if (feedback.length() > 0) {
                        feedback.append('\n');
                    }
                    feedback.append(note);
                    data.put(base + "_Feedback", note);
                }
            }
        }
        data.put("Feedback", feedback.toString().trim());
        return data;
    }

    private static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}