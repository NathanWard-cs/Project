
package hospitalmanagement;

public class Bed {
    
    private int bed_id, patient_id;
    private boolean cleaned;
    //private int patient_id;

    public Bed(int bed_id, boolean cleaned, int patient_id) {
        this.bed_id = bed_id;
        this.cleaned = cleaned;
        this.patient_id = patient_id;
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

    public int getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(int patient_id) {
        this.patient_id = patient_id;
    }
    
    
}
