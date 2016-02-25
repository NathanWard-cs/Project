/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hospitalmanagement;

public class Patient {
    
    private int time_waiting;
    private String forename, surname, symptoms, illness;
    private boolean in_queue;
    private Doctor doctor;
    private Prescription presc;
    
    public Patient(String forename, String surname, String symptoms, 
            String illness, boolean in_queue, int time_waiting, Doctor doctor, Prescription presc) {
        this.forename = forename;
        this.surname = surname;
        this.symptoms = symptoms;
        this.illness = illness;
        this.in_queue = in_queue;
        this.time_waiting = time_waiting;
        this.doctor = doctor;
        this.presc = presc;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Prescription getPresc() {
        return presc;
    }

    public void setPresc(Prescription presc) {
        this.presc = presc;
    }

    public int getTime_waiting() {
        return time_waiting;
    }

    public void setTime_waiting(int time_waiting) {
        this.time_waiting = time_waiting;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getIllness() {
        return illness;
    }

    public void setIllness(String illness) {
        this.illness = illness;
    }

    public boolean isIn_queue() {
        return in_queue;
    }

    public void setIn_queue(boolean in_queue) {
        this.in_queue = in_queue;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
    
}
