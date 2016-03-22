
package hospitalmanagement;

import java.sql.Time;

public class Meal {

    private int meal_id;
    private String meal_name;
    private boolean eaten;
    private String meal_time;

    public Meal(int meal_id, String meal_name, boolean eaten, String meal_time) {
        this.meal_id = meal_id;
        //this.patient_id = patient_id;
        this.meal_name = meal_name;
        this.eaten = eaten;
        this.meal_time = meal_time;
    }
    
    public Meal() {}

    public int getMeal_id() {
        return meal_id;
    }

    public void setMeal_id(int meal_id) {
        this.meal_id = meal_id;
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

    public String getMeal_time() {
        return meal_time;
    }

    public void setMeal_time(String meal_time) {
        this.meal_time = meal_time;
    }

    
    
}
