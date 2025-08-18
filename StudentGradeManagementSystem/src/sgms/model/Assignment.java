package sgms.model;

import java.sql.Date;

/**
 * One assignment or test for a course.
 * Stores the title, max marks (can be null), term (1..4), and due date (can be null).
 */
public class Assignment {

    private int assignmentId;
    private int courseId;
    private String title;
    private Integer maxMarks; // null means "not set"
    private int term;         // 1-4
    private Date dueDate;     // may be null

    /** Empty constructor for frameworks/tools. */
    public Assignment() { }

    /** Full constructor with all fields. */
    public Assignment(int assignmentId, int courseId, String title,
                      Integer maxMarks, int term, Date dueDate) {
        this.assignmentId = assignmentId;
        this.courseId = courseId;
        this.title = title;
        this.maxMarks = maxMarks;
        this.term = term;
        this.dueDate = dueDate;
    }

    /** Simple constructor when only course, title, and term are known. */
    public Assignment(int courseId, String title, int term) {
        this(0, courseId, title, null, term, null);
    }

    // --- getters and setters ---

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(Integer maxMarks) {
        this.maxMarks = maxMarks;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}
