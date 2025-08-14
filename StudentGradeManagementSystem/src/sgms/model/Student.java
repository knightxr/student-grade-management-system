package sgms.model;

/**
 * Simple class that matches a row in the students table.
 * Fields are kept public through getters and setters so that
 * other parts of the program can use them easily.
 */
public class Student {

    // basic fields used in the database
    private int studentId;     // 0 means not yet saved to the database
    private String firstName;
    private String lastName;
    private int gradeLevel;    // grade 1 to 12

    /** Empty constructor for frameworks or manual setup. */
    public Student() {
    }

    /** Constructor used when creating a new student without an id. */
    public Student(String firstName, String lastName, int gradeLevel) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gradeLevel = gradeLevel;
    }

    /** Full constructor usually used when reading from the database. */
    public Student(int studentId, String firstName, String lastName, int gradeLevel) {
        this(firstName, lastName, gradeLevel);
        this.studentId = studentId;
    }

    // getters and setters
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

    // used for debugging or quick printing
    @Override
    public String toString() {
        return firstName + " " + lastName + " (Grade " + gradeLevel + ")";
    }
}