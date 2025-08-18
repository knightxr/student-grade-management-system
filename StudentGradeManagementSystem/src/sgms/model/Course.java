package sgms.model;

/**
 * One course at school.
 * Keeps the code, name, grade level, and (optionally) how many students are in it.
 */
public class Course {

    private int courseId;        // 0 means not saved yet
    private String courseCode;   // e.g. IT12
    private String courseName;   // e.g. Information Technology
    private int gradeLevel;      // e.g. 12
    private int studentCount;    // used for display; can be 0

    /** Empty constructor for tools or manual setup. */
    public Course() { }

    /**
     * Quick constructor used by UI lists that only need an ID and a name.
     */
    public Course(int courseId, String courseName) {
        this.courseId = courseId;
        this.courseName = courseName;
    }

    /** Main constructor without student count. */
    public Course(int courseId, String courseCode, String courseName, int gradeLevel) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.gradeLevel = gradeLevel;
    }

    /** Constructor that also includes the student count (for display). */
    public Course(int courseId, String courseCode, String courseName, int gradeLevel, int studentCount) {
        this(courseId, courseCode, courseName, gradeLevel);
        this.studentCount = studentCount;
    }

    // --- getters and setters ---

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

    /** Useful when showing a course in a combo box. */
    @Override
    public String toString() {
        return courseName;
    }
}
