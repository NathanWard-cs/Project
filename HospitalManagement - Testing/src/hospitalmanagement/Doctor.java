
package hospitalmanagement;

import javafx.collections.ObservableList;

public class Doctor {

    private int doctor_id;
    private String doctor_forename, doctor_surname, specialisation;
    private ObservableList<Patient> patients;

    public Doctor(int doctor_id, String doctor_forename, String doctor_surname, String specialisation, ObservableList<Patient> patients) {
        this.doctor_id = doctor_id;
        this.doctor_forename = doctor_forename;
        this.doctor_surname = doctor_surname;
        this.specialisation = specialisation;
        this.patients = patients;
    }
    
    public Doctor() {
    }

    public ObservableList<Patient> getPatients() {
        return patients;
    }

    public void setPatients(ObservableList<Patient> patients) {
        this.patients = patients;
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
    
    public String doctorToString() {
        return "Doctor Forename: "+doctor_forename+", Doctor Surname: "+doctor_surname+", Doctor Specialisation: "
                + ""+specialisation;
    }
}
