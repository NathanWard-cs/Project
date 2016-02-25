
package hospitalmanagement;

import java.sql.Time;

public class Meal {

    private int meal_id, patient_id;
    private String meal_name;
    private boolean eaten;
    private Time meal_time;

    public Meal(int meal_id, int patient_id, String meal_name, boolean eaten, Time meal_time) {
        this.meal_id = meal_id;
        this.patient_id = patient_id;
        this.meal_name = meal_name;
        this.eaten = eaten;
        this.meal_time = meal_time;
    }

    public int getMeal_id() {
        return meal_id;
    }

    public void setMeal_id(int meal_id) {
        this.meal_id = meal_id;
    }

    public int getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(int patient_id) {
        this.patient_id = patient_id;
    }

    public String getMeal_name() {
        return meal_name;
    }

    public void setMeal_name(String meal_name) {
        this.meal_name = meal_name;
    }

    public boolean isEaten() {
        return eaten;
    }

    public void setEaten(boolean eaten) {
        this.eaten = eaten;
    }

    public Time getMeal_time() {
        return meal_time;
    }

    public void setMeal_time(Time meal_time) {
        this.meal_time = meal_time;
    }

    
    
}
