package sgms.service;

import sgms.dao.Db;
import sgms.dao.AssignmentDAO;
import sgms.dao.CourseDAO;
import sgms.dao.FeedbackDAO;
import sgms.dao.GradeDAO;
import sgms.dao.impl.UcanaccessAssignmentDAO;
import sgms.dao.impl.UcanaccessCourseDAO;
import sgms.dao.impl.UcanaccessFeedbackDAO;
import sgms.dao.impl.UcanaccessGradeDAO;
import sgms.dao.impl.UcanaccessReportDAO;
import sgms.model.Assignment;
import sgms.model.Course;
import sgms.model.Student;
import sgms.util.GradeCalculator;
import sgms.util.Grades;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service providing reporting features.
 */
public class ReportService {

    /** Container for a single term report row. */
    public static final class ReportRow {
        private int studentId;
        private String fullName;
        private String courseCode;
        private String assignmentTitle;
        private int maxMarks;
        private Integer mark;
        private Double percentage;

        public int getStudentId() { return studentId; }
        public void setStudentId(int studentId) { this.studentId = studentId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
        public String getAssignmentTitle() { return assignmentTitle; }
        public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
        public int getMaxMarks() { return maxMarks; }
        public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }
        public Integer getMark() { return mark; }
        public void setMark(Integer mark) { this.mark = mark; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }

        @Override
        public String toString() {
            return "ReportRow{" +
                    "studentId=" + studentId +
                    ", fullName='" + fullName + '\'' +
                    ", courseCode='" + courseCode + '\'' +
                    ", assignmentTitle='" + assignmentTitle + '\'' +
                    ", maxMarks=" + maxMarks +
                    ", mark=" + mark +
                    ", percentage=" + percentage +
                    '}';
        }
    }

    /** Returns the term report rows for the given term. */
    public ArrayList<ReportRow> termReport(int term) throws SQLException {
        ArrayList<ReportRow> list = new ArrayList<>();
        UcanaccessReportDAO dao = new UcanaccessReportDAO();
        try (Connection c = Db.get(); ResultSet rs = dao.queryTermReport(c, term)) {
            while (rs.next()) {
                ReportRow row = new ReportRow();
                row.setStudentId(rs.getInt("studentId"));
                String first = rs.getString("firstName");
                String last = rs.getString("lastName");
                row.setFullName(first + " " + last);
                row.setCourseCode(rs.getString("courseCode"));
                row.setAssignmentTitle(rs.getString("title"));
                row.setMaxMarks(rs.getInt("maxMarks"));
                int mark = rs.getInt("mark");
                if (rs.wasNull()) {
                    row.setMark(null);
                } else {
                    row.setMark(mark);
                }
                double pct = rs.getDouble("pct");
                if (rs.wasNull()) {
                    row.setPercentage(null);
                } else {
                    row.setPercentage(pct);
                }
                list.add(row);
            }
        }
        return list;
    }

    /** Builds merge data for a student's report card. */
    public Map<String, String> buildReportCardData(Student s) throws Exception {
        CourseDAO courseDAO = new UcanaccessCourseDAO();
        AssignmentDAO assignmentDAO = new UcanaccessAssignmentDAO();
        GradeDAO gradeDAO = new UcanaccessGradeDAO();
        FeedbackDAO feedbackDAO = new UcanaccessFeedbackDAO();

        Map<String, String> data = new HashMap<>();
        data.put("First_Name", s.getFirstName());
        data.put("Last_Name", s.getLastName());
        data.put("Grade", String.valueOf(s.getGradeLevel()));
        LocalDate now = LocalDate.now();
        data.put("Year", String.valueOf(now.getYear()));
        data.put("Date", now.toString());

        List<Course> courses = courseDAO.findByGrade(s.getGradeLevel());

        String[] names = {
            "English", "Afrikaans", "Mathematics", "Physics",
            "Business Studies", "Information Technology", "French", "Music"
        };
        Map<String, String> normalised = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            String base = name.replace(' ', '_');
            for (int t = 1; t <= 4; t++) {
                data.put(base + "_T" + t, "");
            }
            data.put(base + "_Final", "");
            data.put(base + "_Feedback", "");
            normalised.put(normalize(name), name);
        }

        int studentId = s.getStudentId();
        StringBuilder feedback = new StringBuilder();

        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            String name = normalised.get(normalize(c.getCourseName()));
            if (name == null) {
                String normCourse = normalize(c.getCourseName());
                for (Map.Entry<String, String> e : normalised.entrySet()) {
                    if (normCourse.contains(e.getKey())) {
                        name = e.getValue();
                        break;
                    }
                }
            }
            if (name == null) {
                continue;
            }
            String base = name.replace(' ', '_');
            List<Assignment> assigns = assignmentDAO.findByCourse(c.getCourseId());
            Map<Integer, Map<Integer, Integer>> gradesByStudent = gradeDAO.findByCourse(c.getCourseId());
            Map<Integer, Integer> grades = gradesByStudent.getOrDefault(studentId, new HashMap<Integer, Integer>());
            double[] termSum = new double[4];
            int[] termCount = new int[4];
            for (int aIndex = 0; aIndex < assigns.size(); aIndex++) {
                Assignment a = assigns.get(aIndex);
                Integer mark = grades.get(a.getAssignmentId());
                if (mark != null) {
                    int term = a.getTerm();
                    termSum[term - 1] += mark;
                    termCount[term - 1]++;
                }
            }
            Double[] termAvg = new Double[4];
            for (int t = 0; t < 4; t++) {
                String key = base + "_T" + (t + 1);
                if (termCount[t] > 0) {
                    termAvg[t] = termSum[t] / termCount[t];
                    data.put(key, String.valueOf(Math.round(termAvg[t])));
                } else {
                    termAvg[t] = null;
                }
            }
            Double finalAvg = GradeCalculator.calculateFinalGrade(termAvg[0], termAvg[1], termAvg[2], termAvg[3]);
            if (finalAvg != null) {
                int rounded = (int) Math.round(finalAvg);
                data.put(base + "_Final", String.valueOf(rounded));
                data.put(base + "_Grade", String.valueOf(Grades.symbolFor(rounded)));
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
}