package sgms.model;

/**
 * Represents a student's term and final grades.
 */
public class FinalGrade {

    private String firstName;
    private String lastName;
    private Integer term1;
    private Integer term2;
    private Integer term3;
    private Integer term4;
    private Integer finalGrade;

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
