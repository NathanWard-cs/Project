package hospitalmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class DatabaseAccess {

    private Connection connection = null;
    private ObservableList<Patient> patients;
    private ObservableList<Prescription> prescriptions;
    //Doctor[] doctors;
    //private Patient[] patients1;

    public DatabaseAccess() throws SQLException, ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:hospitaldb.sqlite");
            //System.out.println("Connected");
        } catch (Exception error) {
            System.out.println(error);
        }
    }

    public void shutdown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public ObservableList<Patient> getIndexList() throws SQLException {
        int id, time_waiting, doctor_id, prescription_id;
        String forename, surname, symptoms, illness;
        boolean in_queue;
        ArrayList<Doctor> doctors = getDoctors();
        Prescription presc;
        Patient patient = null;
        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM patient");
            patients = FXCollections.observableArrayList();

            while (result.next()) {

                forename = result.getString("patient_forename");
                surname = result.getString("patient_surname");
                symptoms = result.getString("symptoms");
                illness = result.getString("illness");
                in_queue = result.getBoolean("in_queue");
                time_waiting = result.getInt("time_waiting_minutes");
                doctor_id = result.getInt("doctor_id");
                prescription_id = result.getInt("prescription_id");

                //System.out.println(doctors);
                for (int i = 0; i < doctors.size(); i++) {
                    if (doctors.get(i).getDoctor_id() == doctor_id) {
                        patient = new Patient(forename, surname, symptoms, illness, in_queue, time_waiting, doctors.get(i), null);
                    }
                }
                //System.out.println(patient.getForename());
                patients.add(patient);
            }
            return patients;
        } else {
            System.out.println("oops");
            return null;
        }
    }

//    public ObservableList<Patient> getPatients() throws SQLException {
//        if (connection != null) {
//            Statement stmt = connection.createStatement();
//            ResultSet result = stmt.executeQuery("SELECT * FROM patient");
//            patients = FXCollections.observableArrayList();
//        }
//    }
    public ArrayList<Doctor> getDoctors() throws SQLException {
        int doctor_id;
        String doctor_forename, doctor_surname, specialisation;
        ArrayList doctors = new ArrayList<>();

        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result2 = stmt.executeQuery("SELECT * FROM doctor");

            while (result2.next()) {
                doctor_id = result2.getInt("doctor_id");
                doctor_forename = result2.getString("doctor_forename");
                doctor_surname = result2.getString("doctor_surname");
                specialisation = result2.getString("specialisation");
                Doctor doctor = new Doctor(doctor_id, doctor_forename, doctor_surname, specialisation);
                //System.out.println(doctors);
                doctors.add(doctor);
            }
            return doctors;
        } else {
            System.out.println("oops");
            return null;
        }
    }

    public ObservableList<Prescription> getPrescription() throws SQLException {
        int id, frequency, numberOfDays;
        String medication, timeToTakeMedicine;
        boolean medicineTaken;
        prescriptions = FXCollections.observableArrayList();

        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result2 = stmt.executeQuery("SELECT * FROM prescription");

            while (result2.next()) {
                id = result2.getInt("prescription_id");
                frequency = result2.getInt("frequency_per_day");
                numberOfDays = result2.getInt("number_of_days");
                medication = result2.getString("medication_name");
                timeToTakeMedicine = result2.getString("time_to_take_medicine");
                medicineTaken = result2.getBoolean("medicine_taken");
                Prescription presc = new Prescription(id, frequency, numberOfDays, medication, medicineTaken, timeToTakeMedicine);
                //System.out.println(doctors);
                prescriptions.add(presc);
            }
            return prescriptions;
        } else {
            System.out.println("oops");
            return null;
        }
    }

    public void addPatient(String first_name, String last_name, String symptoms, String illness, boolean inQueue, int time_waiting, int doctor_id, int prescription_id) throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeQuery("INSERT INTO PATIENT (PATIENT_FORENAME, PATIENT_SURNAME, SYMPTOMS, ILLNESS, IN_QUEUE, TIME_WAITING_MINUTES, DOCTOR_ID, PRESCRIPTION_ID) VALUES "
                    + "('" + first_name + "', '" + last_name + "', '" + symptoms + "', '" + illness + "', '" + inQueue + "', '" + time_waiting + "', '" + doctor_id + "', '" + prescription_id + "')");
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public void addPrescripton(String medicationName, int frequencyPerDay, int numberOfDays,
            String timeToTakeMeds, boolean medecineTaken) throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("INSERT INTO PRESCRIPTION (PRESCRIPTION_ID, MEDICATION_NAME, FREQUENCY_PER_DAY, NUMBER_OF_DAYS, TIME_TO_TAKE_MEDICINE, MEDICINE_TAKEN) "
                    + "VALUES ('1', '" + medicationName + "', '" + frequencyPerDay + "', '" + numberOfDays + "', '" + timeToTakeMeds + "', '" + medecineTaken + "')");
        } catch (SQLException ex) {
            System.out.println(ex);

        }
    }

    public void updatePrescription(ArrayList<Prescription> p) throws SQLException {
        //System.out.println(p.size());
        String sql = "";
        for (int i = 0; i < p.size(); i++) {
            System.out.println(i);
            sql += "UPDATE PRESCRIPTION SET MEDICATION_NAME='" + p.get(i).getMedication() + "', FREQUENCY_PER_DAY='" + p.get(i).getFrequency() + "', NUMBER_OF_DAYS='" + p.get(i).getNumber_of_days() + "', "
                    + "TIME_TO_TAKE_MEDICINE='" + p.get(i).getTime_to_take_medicine() + "', MEDICINE_TAKEN='" + p.get(i).isMedicine_taken() + "' WHERE PRESCRIPTION_ID='" + p.get(i).getId() + "';\n";
        }
        
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        
    }

    public void deletePrescription(ObservableList<Prescription> p) {
        String sql = "";
        for (int i = 0; i < p.size(); i++) {
            sql += "DELETE FROM PRESCRIPTION WHERE PRESCRIPTION_ID=" + p.get(i).getId() + ";";
        }

        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException err) {
            System.out.println(err);
        }
    }

}
