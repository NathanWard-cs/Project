
package hospitalmanagement;

public class Prescription {

    private int id, frequency, number_of_days;
    private String medication;
    private boolean medicine_taken;
    private String time_to_take_medicine;

    public Prescription(int id, int frequency, int number_of_days, String medication, boolean medicine_taken, String time_to_take_medicine) {
        this.id = id;
        this.frequency = frequency;
        this.number_of_days = number_of_days;
        this.medication = medication;
        this.medicine_taken = medicine_taken;
        this.time_to_take_medicine = time_to_take_medicine;
    }
    
    public Prescription(String medication, int frequency, int number_of_days, String time_to_take_medicine, boolean medicine_taken) {
        this.frequency = frequency;
        this.number_of_days = number_of_days;
        this.medication = medication;
        this.medicine_taken = medicine_taken;
        this.time_to_take_medicine = time_to_take_medicine;
    }
    
    public Prescription() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getNumber_of_days() {
        return number_of_days;
    }

    public void setNumber_of_days(int number_of_days) {
        this.number_of_days = number_of_days;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public boolean isMedicine_taken() {
        return medicine_taken;
    }

    public void setMedicine_taken(boolean medicine_taken) {
        this.medicine_taken = medicine_taken;
    }

    public String getTime_to_take_medicine() {
        return time_to_take_medicine;
    }

    public void setTime_to_take_medicine(String time_to_take_medicine) {
        this.time_to_take_medicine = time_to_take_medicine;
    }
}
