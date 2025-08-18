package sgms.model;

/**
 * Holds one student's term marks and the final mark.
 * Term values can be null if there was no mark for that term.
 */
public class FinalGrade {

    private String firstName;
    private String lastName;
    private Integer term1;      // percent 0-100 or null
    private Integer term2;      // percent 0-100 or null
    private Integer term3;      // percent 0-100 or null
    private Integer term4;      // percent 0-100 or null
    private Integer finalGrade; // weighted final percent or null

    /** Make a row with the student's name and marks. */
    public FinalGrade(String firstName, String lastName,
                      Integer term1, Integer term2, Integer term3,
                      Integer term4, Integer finalGrade) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.term1 = term1;
        this.term2 = term2;
        this.term3 = term3;
        this.term4 = term4;
        this.finalGrade = finalGrade;
    }

    // --- getters ---

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getTerm1() {
        return term1;
    }

    public Integer getTerm2() {
        return term2;
    }

    public Integer getTerm3() {
        return term3;
    }

    public Integer getTerm4() {
        return term4;
    }

    public Integer getFinalGrade() {
        return finalGrade;
    }
}
