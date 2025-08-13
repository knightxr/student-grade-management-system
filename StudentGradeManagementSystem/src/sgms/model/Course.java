package sgms.model;

/**
 * Simple POJO representing a course.
 */
public class Course {

    private int    courseId;
    private String courseCode;
    private String courseName;
    private int    gradeLevel;
    private int    studentCount;

    public Course() {}

    /**
     * Convenience constructor used in existing parts of the UI that only need
     * an ID and name, e.g. the course filter combo box.
     */
    public Course(int courseId, String courseName) {
        this.courseId = courseId;
        this.courseName = courseName;
    }

    public Course(int courseId, String courseCode, String courseName, int gradeLevel) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.gradeLevel = gradeLevel;
    }

    public Course(int courseId, String courseCode, String courseName, int gradeLevel, int studentCount) {
        this(courseId, courseCode, courseName, gradeLevel);
        this.studentCount = studentCount;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(int gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    @Override
    public String toString() {
        return courseName;
    }
}
