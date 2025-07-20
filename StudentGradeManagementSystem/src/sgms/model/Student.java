package sgms.model;

/**
 * Plain Old Java Object that mirrors the tblStudents table.
 */
public class Student {

    // ── Fields mapped to DB columns ────────────────────────────────────────────
    private int    studentId;   // PRIMARY KEY  (0 means “not set yet”)
    private String firstName;
    private String lastName;
    private int    gradeLevel;  // 1 – 12

    // ── Constructors ───────────────────────────────────────────────────────────
    /** No-arg constructor required by frameworks and for manual set-up. */
    public Student() {}

    /** Convenience constructor when you do not yet have an ID. */
    public Student(String firstName, String lastName, int gradeLevel) {
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.gradeLevel = gradeLevel;
    }

    /** Full constructor (frameworks may call this when reading from DB). */
    public Student(int studentId, String firstName,
                   String lastName, int gradeLevel) {
        this(firstName, lastName, gradeLevel);
        this.studentId = studentId;
    }

    // ── Getters and setters (NetBeans: Alt + Insert ► Getter and Setter) ──────
    public int    getStudentId()            { return studentId; }
    public void   setStudentId(int id)      { this.studentId = id; }

    public String getFirstName()            { return firstName; }
    public void   setFirstName(String fn)   { this.firstName = fn; }

    public String getLastName()             { return lastName; }
    public void   setLastName(String ln)    { this.lastName = ln; }

    public int    getGradeLevel()           { return gradeLevel; }
    public void   setGradeLevel(int grade)  { this.gradeLevel = grade; }

    // ── Debug / logging helper ────────────────────────────────────────────────
    @Override
    public String toString() {
        return firstName + " " + lastName + " (Grade " + gradeLevel + ")";
    }
}