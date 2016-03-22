
package hospitalmanagement;

public class Bed {
    
    private int bed_id;
    private boolean cleaned;
    private Patient patient;

    public Bed(int bed_id, boolean cleaned, Patient patient) {
        this.bed_id = bed_id;
        this.cleaned = cleaned;
        this.patient = patient;
    }
    
    public Bed(boolean cleaned, Patient patient) {
        this.cleaned = cleaned;
        this.patient = patient;
    }
    
    public Bed() {
    }

    public int getBed_id() {
        return bed_id;
    }

    public void setBed_id(int bed_id) {
        this.bed_id = bed_id;
    }

    public boolean isCleaned() {
        return cleaned;
    }

    public void setCleaned(boolean cleaned) {
        this.cleaned = cleaned;
    }

    public Patient getPatient_id() {
        return patient;
    }

    public void setPatient_id(Patient patient) {
        this.patient = patient;
    }
    
    
}
