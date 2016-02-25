
package hospitalmanagement;

public class Doctor {

    private int doctor_id;
    private String doctor_forename, doctor_surname, specialisation;

    public Doctor(int doctor_id, String doctor_forename, String doctor_surname, String specialisation) {
        this.doctor_id = doctor_id;
        this.doctor_forename = doctor_forename;
        this.doctor_surname = doctor_surname;
        this.specialisation = specialisation;
    }

    public int getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(int doctor_id) {
        this.doctor_id = doctor_id;
    }

    public String getDoctor_forename() {
        return doctor_forename;
    }

    public void setDoctor_forename(String doctor_forename) {
        this.doctor_forename = doctor_forename;
    }

    public String getDoctor_surname() {
        return doctor_surname;
    }

    public void setDoctor_surname(String doctor_surname) {
        this.doctor_surname = doctor_surname;
    }

    public String getSpecialisation() {
        return specialisation;
    }

    public void setSpecialisation(String specialisation) {
        this.specialisation = specialisation;
    }
    
    
}
