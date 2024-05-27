import java.io.Serializable;

public class Participant implements Serializable {
    private String name;
    private String familyName;
    private String placeOfWork;
    private String reportTitle;
    private String email;

    public Participant(String name, String familyName, String placeOfWork, String reportTitle, String email) {
        this.name = name;
        this.familyName = familyName;
        this.placeOfWork = placeOfWork;
        this.reportTitle = reportTitle;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getPlaceOfWork() {
        return placeOfWork;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public String getEmail() {
        return email;
    }
}
