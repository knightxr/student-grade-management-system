package sgms.model;

import java.sql.Date;

/**
 * Represents an assignment or test for a course.
 */
public class Assignment {

    private int assignmentId;
    private int courseId;
    private String title;
    private Integer maxMarks;
    private Date dueDate;

    public Assignment() {
    }

    public Assignment(int assignmentId, int courseId, String title,
                      Integer maxMarks, Date dueDate) {
        this.assignmentId = assignmentId;
        this.courseId = courseId;
        this.title = title;
        this.maxMarks = maxMarks;
        this.dueDate = dueDate;
    }

    public Assignment(int courseId, String title) {
        this(0, courseId, title, null, null);
    }

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

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}e