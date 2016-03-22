package hospitalmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DatabaseAccess {

    private Connection connection = null;
    private ObservableList<Patient> patients;
    private ObservableList<Prescription> prescriptions;
    private ObservableList<Doctor> doctors;
    private ObservableList<Bed> beds;
    private ObservableList<Ward> wards;
    private ObservableList<Meal> meals;

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

    public boolean login(String username, String password) throws SQLException {

        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM login");
            while (result.next()) {
                if ((result.getString("username").equals(username)) && (result.getString("password").equals(password))) {
                    System.out.println("Logged in");
                    return true;
                }

            }
        }
        return false;
    }

    public ObservableList<Bed> getBeds() throws SQLException {
        int bedId, patientId;
        boolean bedCleaned;
        Patient p = null;
        patients = getPatients();
        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM bed");
            beds = FXCollections.observableArrayList();

            while (result.next()) {
                bedId = result.getInt("bed_id");
                bedCleaned = result.getBoolean("cleaned");
                patientId = result.getInt("patient_id");
                for (Patient patient : patients) {
                    if (patient.getId() == patientId) {
                        System.out.println("Yes");
                        p = patient;
                        break;
                    }
                }
                Bed bed = new Bed(bedId, bedCleaned, p);
                beds.add(bed);
            }
        }
        return beds;
    }

    public ObservableList<Meal> getMeals() throws SQLException {
        int id;
        String mealName, mealTime;
        boolean mealEaten;
        Meal meal = null;

        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM meal");
            meals = FXCollections.observableArrayList();
            while (result.next()) {
                id = result.getInt("meal_id");
                mealName = result.getString("meal_name");
                mealEaten = result.getBoolean("eaten");
                mealTime = result.getString("meal_time");
                meal = new Meal(id, mealName, mealEaten, mealTime);
                meals.add(meal);
            }
            return meals;
        } else {
            return null;
        }
    }

    public ObservableList<Patient> getPatients() throws SQLException {
        int id, time_waiting, doctor_id;
        String forename, surname, symptoms, illness;
        boolean in_queue;
        doctors = getDoctors();
        prescriptions = getPrescription();
        Patient patient = null;
        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM patient");

            patients = FXCollections.observableArrayList();

            while (result.next()) {
                id = result.getInt("id");
                forename = result.getString("patient_forename");
                surname = result.getString("patient_surname");
                symptoms = result.getString("symptoms");
                illness = result.getString("illness");
                in_queue = result.getBoolean("in_queue");
                time_waiting = result.getInt("time_waiting_minutes");

                patient = new Patient(id, forename, surname, symptoms, illness, in_queue, time_waiting, getPrescriptionsForPatient(id), getMealsForPatient(id));
                //System.out.println(patient.getForename());
                patients.add(patient);
            }
            return patients;
        } else {
            System.out.println("oops");
            return null;
        }
    }

    public ObservableList<Doctor> getDoctors() throws SQLException {
        int doctor_id;
        String doctor_forename, doctor_surname, specialisation;
        doctors = FXCollections.observableArrayList();

        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result2 = stmt.executeQuery("SELECT * FROM doctor");

            while (result2.next()) {
                doctor_id = result2.getInt("doctor_id");
                doctor_forename = result2.getString("doctor_forename");
                doctor_surname = result2.getString("doctor_surname");
                specialisation = result2.getString("specialisation");
                Doctor doctor = new Doctor(doctor_id, doctor_forename, doctor_surname, specialisation, getPatientsForDoctor(doctor_id));
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
        int id, frequency, numberOfDays, patientId;
        String medication, timeToTakeMedicine;
        boolean medicineTaken;
        Prescription presc = null;
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

                presc = new Prescription(id, frequency, numberOfDays, medication, medicineTaken, timeToTakeMedicine);
                prescriptions.add(presc);

            }
        }
        return prescriptions;
    }

    public ObservableList<Ward> getWard() throws SQLException {
        int wardId;
        String wardName;
        wards = FXCollections.observableArrayList();
        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM WARD");

            while (result.next()) {
                wardId = result.getInt("ward_id");
                wardName = result.getString("ward_name");
                Ward ward = new Ward(wardId, wardName, getBedsForWard(wardId));
                wards.add(ward);
            }

            return wards;
        } else {
            return null;
        }
    }
    
    public ObservableList<Bed> getUnoccupiedBeds() throws SQLException {
        ObservableList<Bed> unoccupied = FXCollections.observableArrayList();
        ObservableList<Bed> allBeds = getBeds();
        for (Bed b : allBeds) {
            if (b.getPatient_id() == null) {
                unoccupied.add(b);
            }
        }
        return unoccupied;
    }

    public void addBed(boolean cleaned, int ward_id) {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO BED (CLEANED, WARD_ID) VALUES ('" + cleaned + "', '" + ward_id + "');");
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public void addDoctor(String forename, String surname, String spec) throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO DOCTOR (DOCTOR_FORENAME, DOCTOR_SURNAME, SPECIALISATION) VALUES "
                    + "('" + forename + "', '" + surname + "', '" + spec + "');");
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public void addMeal(String mealName, boolean eaten, String mealTime, int patientId) {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO MEAL (MEAL_NAME, MEAL_TIME, EATEN, PATIENT_ID) VALUES ('" + mealName + "', '" + mealTime + "', "
                    + "'" + eaten + "', '" + patientId + "');");
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public void addPatient(String first_name, String last_name, String symptoms, String illness,
            boolean inQueue, int time_waiting, int doctor_id) throws SQLException {

        try {
            if (doctor_id != 0) {
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("INSERT INTO PATIENT (PATIENT_FORENAME, PATIENT_SURNAME, SYMPTOMS, ILLNESS, IN_QUEUE, TIME_WAITING_MINUTES, DOCTOR_ID) VALUES "
                        + "('" + first_name + "', '" + last_name + "', '" + symptoms + "', '" + illness + "', '" + inQueue + "', '" + time_waiting + "', '" + doctor_id + "');");
            } else {
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("INSERT INTO PATIENT (PATIENT_FORENAME, PATIENT_SURNAME, SYMPTOMS, ILLNESS, IN_QUEUE, TIME_WAITING_MINUTES, DOCTOR_ID) VALUES "
                        + "('" + first_name + "', '" + last_name + "', '" + symptoms + "', '" + illness + "', '" + inQueue + "', '" + time_waiting + "', '" + null + "');");
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public void addPrescripton(String medicationName, int frequencyPerDay, int numberOfDays,
            String timeToTakeMeds, boolean medecineTaken, int patientId) throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO PRESCRIPTION (MEDICATION_NAME, FREQUENCY_PER_DAY, NUMBER_OF_DAYS, TIME_TO_TAKE_MEDICINE, MEDICINE_TAKEN, PATIENT_ID) "
                    + "VALUES ('" + medicationName + "', '" + frequencyPerDay + "', '" + numberOfDays + "', '" + timeToTakeMeds + "', '" + medecineTaken + "', '" + patientId + "');");
        } catch (SQLException ex) {
            System.out.println(ex);

        }
    }

    public void addWard(String wardName) throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO WARD (WARD_NAME) VALUES ('" + wardName + "');");
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public void updatePatientPresId(Patient p, int newId) throws SQLException {
        String sql = "UPDATE PATIENT SET PRESCRIPTION_ID ='" + newId + "' WHERE ID = '" + p.getId() + "';";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void updateBedPatientId(Bed b, int newId) throws SQLException {
        String sql = "UPDATE BED SET PATIENT_ID ='" + newId + "' WHERE BED_ID = '" + b.getBed_id() + "';";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void updatePatientDoctorId(Patient p, int newId) throws SQLException {
        String sql = "UPDATE PATIENT SET DOCTOR_ID='" + newId + "' WHERE ID='" + p.getId() + "';";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void updateBed(ArrayList<Bed> b) throws SQLException {
        String sql = "";
        for (int i = 0; i < b.size(); i++) {
            sql += "UPDATE BED SET CLEANED='" + b.get(i).isCleaned() + "' WHERE BED_ID='" + b.get(i).getBed_id() + "';";
        }
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void updateDoctor(ArrayList<Doctor> d) throws SQLException {
        String sql = "";
        for (int i = 0; i < d.size(); i++) {
            sql += "UPDATE DOCTOR SET DOCTOR_FORENAME='" + d.get(i).getDoctor_forename() + "', DOCTOR_SURNAME='" + d.get(i).getDoctor_surname() + "', SPECIALISATION='" + d.get(i).getSpecialisation() + "'"
                    + " WHERE DOCTOR_ID='" + d.get(i).getDoctor_id() + "';\n";
        }
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void updatePatient(ArrayList<Patient> p) throws SQLException {
        String sql = "";
        for (int i = 0; i < p.size(); i++) {
            sql += "UPDATE PATIENT SET PATIENT_FORENAME='" + p.get(i).getForename() + "', PATIENT_SURNAME='" + p.get(i).getSurname() + "', SYMPTOMS='" + p.get(i).getSymptoms() + "', "
                    + "ILLNESS='" + p.get(i).getIllness() + "', IN_QUEUE='" + p.get(i).isIn_queue() + "', TIME_WAITING_MINUTES='" + p.get(i).getTime_waiting() + "'"
                    + " WHERE ID='" + p.get(i).getId() + "';\n";
        }

        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void updateMeal(ArrayList<Meal> m) throws SQLException {
        String sql = "";
        for (int i = 0; i < m.size(); i++) {
            sql += "UPDATE MEAL SET MEAL_NAME='" + m.get(i).getMeal_name() + "', MEAL_TIME='" + m.get(i).getMeal_time() + "', EATEN='" + m.get(i).isEaten() +
                    "' WHERE MEAL_ID='"+m.get(i).getMeal_id()+"';\n";
        }

        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void updatePrescription(ArrayList<Prescription> p) throws SQLException {
        //System.out.println(p.size());
        String sql = "";
        for (int i = 0; i < p.size(); i++) {
            //System.out.println(i);
            sql += "UPDATE PRESCRIPTION SET MEDICATION_NAME='" + p.get(i).getMedication() + "', FREQUENCY_PER_DAY='" + p.get(i).getFrequency() + "', NUMBER_OF_DAYS='" + p.get(i).getNumber_of_days() + "', "
                    + "TIME_TO_TAKE_MEDICINE='" + p.get(i).getTime_to_take_medicine() + "', MEDICINE_TAKEN='" + p.get(i).isMedicine_taken() + "' WHERE PRESCRIPTION_ID='" + p.get(i).getId() + "';\n";
        }

        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);

    }

    public void updateWard(ArrayList<Ward> w) throws SQLException {
        String sql = "";
        for (Ward ward : w) {
            sql += "UPDATE WARD SET WARD_NAME='" + ward.getWard_name() + "' WHERE WARD_ID='" + ward.getWard_id() + "';";
        }
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void deleteBed(ObservableList<Bed> b) {
        String sql = "";
        for (Bed d : b) {
            sql += "DELETE FROM BED WHERE BED_ID ='" + d.getBed_id() + "';";
        }
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException err) {
            System.out.println(err);
        }
    }

    public void deleteDoctor(ObservableList<Doctor> d) {
        String sql = "";
        for (int i = 0; i < d.size(); i++) {
            sql += "DELETE FROM DOCTOR WHERE DOCTOR_ID ='" + d.get(i).getDoctor_id() + "';";
        }

        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException err) {
            System.out.println(err);
        }
    }

    public void deleteMeal(ObservableList<Meal> m) {
        String sql = "";
        for (int i = 0; i < m.size(); i++) {
            sql += "DELETE FROM MEAL WHERE MEAL_ID='" + m.get(i).getMeal_id() + "';";
        }
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException err) {
            System.out.println(err);
        }
    }

    public void deletePatient(ObservableList<Patient> p) {
        String sql = "";
        for (int i = 0; i < p.size(); i++) {
            sql += "DELETE FROM PATIENT WHERE ID='" + p.get(i).getId() + "';";
        }

        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException err) {
            System.out.println(err);
        }
    }

    public void deletePrescription(ObservableList<Prescription> p) {
        String sql = "";
        for (int i = 0; i < p.size(); i++) {
            sql += "DELETE FROM PRESCRIPTION WHERE PRESCRIPTION_ID='" + p.get(i).getId() + "';";
        }

        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException err) {
            System.out.println(err);
        }
    }

    public void deleteWard(ObservableList<Ward> w) {
        String sql = "";
        for (Ward ward : w) {
            sql += "DELETE FROM WARD WHERE WARD_ID='" + ward.getWard_id() + "';";
        }

        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException err) {
            System.out.println(err);
        }
    }

    public ObservableList<Meal> getMealsForPatient(int id) throws SQLException {
        ObservableList<Meal> patMeals = FXCollections.observableArrayList();
        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM meal WHERE PATIENT_ID='" + id + "';");
            while (result.next()) {
                int mealId = result.getInt("meal_id");
                String mealName = result.getString("meal_name");
                String mealTime = result.getString("meal_time");
                boolean eaten = result.getBoolean("eaten");
                Meal meal = new Meal(mealId, mealName, eaten, mealTime);
                patMeals.add(meal);
            }
        }
        return patMeals;
    }

    public ObservableList<Prescription> getPrescriptionsForPatient(int id) throws SQLException {
        ObservableList<Prescription> patPres = FXCollections.observableArrayList();
        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result2 = stmt.executeQuery("SELECT * FROM prescription WHERE PATIENT_ID='" + id + "';");

            while (result2.next()) {
                int presid = result2.getInt("prescription_id");
                int frequency = result2.getInt("frequency_per_day");
                int numberOfDays = result2.getInt("number_of_days");
                String medication = result2.getString("medication_name");
                String timeToTakeMedicine = result2.getString("time_to_take_medicine");
                boolean medicineTaken = result2.getBoolean("medicine_taken");
                Prescription pres = new Prescription(presid, frequency, numberOfDays, medication, medicineTaken, timeToTakeMedicine);
                patPres.add(pres);
            }

        }
        return patPres;
    }

    public ObservableList<Patient> getPatientsForDoctor(int id) throws SQLException {
        ObservableList<Patient> docPats = FXCollections.observableArrayList();
        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM patient WHERE DOCTOR_ID='" + id + "';");
            while (result.next()) {
                int patid = result.getInt("id");
                String forename = result.getString("patient_forename");
                String surname = result.getString("patient_surname");
                String symptoms = result.getString("symptoms");
                String illness = result.getString("illness");
                boolean in_queue = result.getBoolean("in_queue");
                int time_waiting = result.getInt("time_waiting_minutes");
                Patient patient = new Patient(patid, forename, surname, symptoms, illness, in_queue, time_waiting, getPrescriptionsForPatient(patid), getMealsForPatient(patid));
                docPats.add(patient);
            }
        }
        return docPats;
    }

    public ObservableList<Bed> getBedsForWard(int id) throws SQLException {
        Patient patient = null;
        patients = getPatients();
        ObservableList<Bed> wardBeds = FXCollections.observableArrayList();
        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM bed WHERE WARD_ID='" + id + "'");
            while (result.next()) {
                int bedid = result.getInt("bed_id");
                boolean cleaned = result.getBoolean("cleaned");
                int patientid = result.getInt("patient_id");
                for (Patient p : patients) {
                    if (p.getId() == patientid) {
                        patient = p;
                    }
                }
                Bed bed = new Bed(bedid, cleaned, patient);
                wardBeds.add(bed);
            }

        }
        return wardBeds;
    }
}
