/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hospitalmanagement;

import javafx.collections.ObservableList;

public class Patient {
    
    private int id;
    private String forename, surname, symptoms, illness, timeArrived;
    private boolean in_queue;
    private ObservableList<Prescription> presc;
    private ObservableList<Meal> meals;
    
    public Patient(String forename, String surname, String symptoms, 
            String illness, boolean in_queue, String timeArrived, ObservableList<Prescription> presc,
            ObservableList<Meal> meals) {
        this.forename = forename;
        this.surname = surname;
        this.symptoms = symptoms;
        this.illness = illness;
        this.in_queue = in_queue;
        this.timeArrived = timeArrived;
        this.presc = presc;
        this.meals = meals;
    }
    
    public Patient(int id, String forename, String surname, String symptoms, 
            String illness, boolean in_queue, String timeArrived, ObservableList<Prescription> presc,
            ObservableList<Meal> meals) {
        this.id = id;
        this.forename = forename;
        this.surname = surname;
        this.symptoms = symptoms;
        this.illness = illness;
        this.in_queue = in_queue;
        this.timeArrived = timeArrived;
        this.presc = presc;
        this.meals = meals;
    }
    
    public Patient() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ObservableList<Prescription> getPresc() {
        return presc;
    }

    public void setPresc(ObservableList<Prescription> presc) {
        this.presc = presc;
    }
    
    public ObservableList<Meal> getMeals() {
        return meals;
    }

    public void setMeals(ObservableList<Meal> meals) {
        this.meals = meals;
    }

    public String getTimeArrived() {
        return timeArrived;
    }

    public void setTimeArrived(String timeArrived) {
        this.timeArrived = timeArrived;
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
    
    
    
    public String patientToString() {
        return "Forename: "+forename+", Surname: "+surname+", Symptoms: "+symptoms+", Illness: "+illness+", In Queue: "+in_queue+", Time waiting: "+timeArrived;
    }
    
}
