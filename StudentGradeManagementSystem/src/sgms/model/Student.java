package sgms.model;

/**
 * One student record from the database.
 * Stores name, grade level, and the database ID.
 */
public class Student {

    // 0 means not saved yet (no DB ID)
    private int studentId;
    private String firstName;
    private String lastName;
    private int gradeLevel; // grade number

    /** Empty constructor */
    public Student() { }

    /** Create a new student without an ID. */
    public Student(String firstName, String lastName, int gradeLevel) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gradeLevel = gradeLevel;
    }

    /** Full constructor */
    public Student(int studentId, String firstName, String lastName, int gradeLevel) {
        this(firstName, lastName, gradeLevel);
        this.studentId = studentId;
    }

    // --- getters and setters ---

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int id) {
        this.studentId = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String fn) {
        this.firstName = fn;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String ln) {
        this.lastName = ln;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(int grade) {
        this.gradeLevel = grade;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (Grade " + gradeLevel + ")";
    }
}
