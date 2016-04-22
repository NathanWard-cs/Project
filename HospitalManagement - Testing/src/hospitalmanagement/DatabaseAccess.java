package hospitalmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author nathan ward
 */
public class DatabaseAccess {

    private Connection connection = null;
    private ObservableList<Patient> patients;
    private ObservableList<Prescription> prescriptions;
    private ObservableList<Doctor> doctors;
    private ObservableList<Bed> beds;
    private ObservableList<Ward> wards;
    private ObservableList<Meal> meals;

    /**
     * Connects to the SQLite database
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public DatabaseAccess() throws SQLException, ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:hospitaldb.sqlite");
            //System.out.println("Connected");
        } catch (Exception error) {
            System.out.println(error);
        }
    }

    /**
     * Breaks the database connection
     * @throws SQLException
     */
    public void shutdown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     *
     * @param username
     * @param password
     * @return whether the login details exist
     * @throws SQLException
     */
    public boolean login(String username, String password) throws SQLException {

        if (connection != null) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM login");
            while (result.next()) {
                if ((result.getString("username").equals(username)) && (result.getString("password").equals(password))) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     *
     * @return a list of beds from the database
     * @throws SQLException
     */
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

    /**
     *
     * @return the list of meals from the database
     * @throws SQLException
     */
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

    /**
     *
     * @return the list of patients from the database
     * @throws SQLException
     */
    public ObservableList<Patient> getPatients() throws SQLException {
        int id;
        String forename, surname, symptoms, illness, timeArrived;
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
                timeArrived = result.getString("time_arrived");

                patient = new Patient(id, forename, surname, symptoms, illness, in_queue, timeArrived, getPrescriptionsForPatient(id), getMealsForPatient(id));
                //System.out.println(patient.getForename());
                patients.add(patient);
            }
            return patients;
        } else {
            System.out.println("oops");
            return null;
        }
    }

    /**
     *
     * @return the list of doctors from the database
     * @throws SQLException
     */
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

    /**
     *
     * @return the list of prescriptions from the database
     * @throws SQLException
     */
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

    /**
     *
     * @return the list of wards from the database
     * @throws SQLException
     */
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
    
    /**
     *
     * @return any unoccupied beds from the database
     * @throws SQLException
     */
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

    /**
     * Inserts a bed into the database
     * @param cleaned
     * @param ward_id
     */
    public void addBed(boolean cleaned, int ward_id) {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO BED (CLEANED, WARD_ID) VALUES ('" + cleaned + "', '" + ward_id + "');");
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Inserts a doctor into the database
     * @param forename
     * @param surname
     * @param spec
     * @throws SQLException
     */
    public void addDoctor(String forename, String surname, String spec) throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO DOCTOR (DOCTOR_FORENAME, DOCTOR_SURNAME, SPECIALISATION) VALUES "
                    + "('" + forename + "', '" + surname + "', '" + spec + "');");
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Inserts a meal into the database
     * @param mealName
     * @param eaten
     * @param mealTime
     * @param patientId
     */
    public void addMeal(String mealName, boolean eaten, String mealTime, int patientId) {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO MEAL (MEAL_NAME, MEAL_TIME, EATEN, PATIENT_ID) VALUES ('" + mealName + "', '" + mealTime + "', "
                    + "'" + eaten + "', '" + patientId + "');");
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Inserts a patient into the database
     * @param first_name
     * @param last_name
     * @param symptoms
     * @param illness
     * @param inQueue
     * @param timeArrived
     * @param doctor_id
     * @throws SQLException
     */
    public void addPatient(String first_name, String last_name, String symptoms, String illness,
            boolean inQueue, String timeArrived, int doctor_id) throws SQLException {

        try {
            if (doctor_id != 0) {
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("INSERT INTO PATIENT (PATIENT_FORENAME, PATIENT_SURNAME, SYMPTOMS, ILLNESS, IN_QUEUE, TIME_ARRIVED, DOCTOR_ID) VALUES "
                        + "('" + first_name + "', '" + last_name + "', '" + symptoms + "', '" + illness + "', '" + inQueue + "', '" + timeArrived + "', '" + doctor_id + "');");
            } else {
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("INSERT INTO PATIENT (PATIENT_FORENAME, PATIENT_SURNAME, SYMPTOMS, ILLNESS, IN_QUEUE, TIME_ARRIVED, DOCTOR_ID) VALUES "
                        + "('" + first_name + "', '" + last_name + "', '" + symptoms + "', '" + illness + "', '" + inQueue + "', '" + timeArrived + "', '" + null + "');");
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Inserts a prescription into the database
     * @param medicationName
     * @param frequencyPerDay
     * @param numberOfDays
     * @param timeToTakeMeds
     * @param medecineTaken
     * @param patientId
     * @throws SQLException
     */
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

    /**
     * Inserts a ward into the database
     * @param wardName
     * @throws SQLException
     */
    public void addWard(String wardName) throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO WARD (WARD_NAME) VALUES ('" + wardName + "');");
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Updates bed when inserting patient to bed
     * @param b
     * @param newId
     * @throws SQLException
     */
    public void updateBedPatientId(Bed b, int newId) throws SQLException {
        String sql = "UPDATE BED SET PATIENT_ID ='" + newId + "' WHERE BED_ID = '" + b.getBed_id() + "';";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    

    /**
     * Updates bed in database
     * @param b
     * @throws SQLException
     */
    public void updateBed(ArrayList<Bed> b) throws SQLException {
        String sql = "";
        for (int i = 0; i < b.size(); i++) {
            sql += "UPDATE BED SET CLEANED='" + b.get(i).isCleaned() + "' WHERE BED_ID='" + b.get(i).getBed_id() + "';";
        }
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    /**
     * Updates doctor in database
     * @param d
     * @throws SQLException
     */
    public void updateDoctor(ArrayList<Doctor> d) throws SQLException {
        String sql = "";
        for (int i = 0; i < d.size(); i++) {
            sql += "UPDATE DOCTOR SET DOCTOR_FORENAME='" + d.get(i).getDoctor_forename() + "', DOCTOR_SURNAME='" + d.get(i).getDoctor_surname() + "', SPECIALISATION='" + d.get(i).getSpecialisation() + "'"
                    + " WHERE DOCTOR_ID='" + d.get(i).getDoctor_id() + "';\n";
        }
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    /**
     * Updates patient in database
     * @param p
     * @throws SQLException
     */
    public void updatePatient(ArrayList<Patient> p) throws SQLException {
        String sql = "";
        for (int i = 0; i < p.size(); i++) {
            sql += "UPDATE PATIENT SET PATIENT_FORENAME='" + p.get(i).getForename() + "', PATIENT_SURNAME='" + p.get(i).getSurname() + "', SYMPTOMS='" + p.get(i).getSymptoms() + "', "
                    + "ILLNESS='" + p.get(i).getIllness() + "', IN_QUEUE='" + p.get(i).isIn_queue() + "', TIME_ARRIVED='" + p.get(i).getTimeArrived() + "'"
                    + " WHERE ID='" + p.get(i).getId() + "';\n";
        }

        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    /**
     * Updates meal in database 
     * @param m
     * @throws SQLException
     */
    public void updateMeal(ArrayList<Meal> m) throws SQLException {
        String sql = "";
        for (int i = 0; i < m.size(); i++) {
            sql += "UPDATE MEAL SET MEAL_NAME='" + m.get(i).getMeal_name() + "', MEAL_TIME='" + m.get(i).getMeal_time() + "', EATEN='" + m.get(i).isEaten() +
                    "' WHERE MEAL_ID='"+m.get(i).getMeal_id()+"';\n";
        }

        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    /**
     * Updates prescription in database
     * @param p
     * @throws SQLException
     */
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

    /**
     * Update ward in database
     * @param w
     * @throws SQLException
     */
    public void updateWard(ArrayList<Ward> w) throws SQLException {
        String sql = "";
        for (Ward ward : w) {
            sql += "UPDATE WARD SET WARD_NAME='" + ward.getWard_name() + "' WHERE WARD_ID='" + ward.getWard_id() + "';";
        }
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    /**
     * Removes bed from database
     * @param b
     */
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

    /**
     * Removes doctor from database
     * @param d
     */
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

    /**
     * Removes meal from database
     * @param m
     */
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

    /**
     * Removes patient from database
     * @param p
     */
    public void deletePatient(ObservableList<Patient> p) {
        String sql = "";
        for (int i = 0; i < p.size(); i++) {
            deletePrescription(p.get(i).getPresc());
            deleteMeal(p.get(i).getMeals());
                
            sql += "DELETE FROM PATIENT WHERE ID='" + p.get(i).getId() + "';";
        }

        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException err) {
            System.out.println(err);
        }
    }

    /**
     * Removes prescription from database
     * @param p
     */
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

    /**
     * Removes ward from database
     * @param w
     */
    public void deleteWard(ObservableList<Ward> w) {
        String sql = "";
        for (int i = 0; i < w.size(); i++) {
            deleteBed(w.get(i).getBeds());
            sql += "DELETE FROM WARD WHERE WARD_ID='" + w.get(i).getWard_id() + "';";
        }

        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException err) {
            System.out.println(err);
        }
    }

    /**
     * Gets a specified patients meals
     * @param id
     * @return
     * @throws SQLException
     */
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

    /**
     * Gets a specified patients prescriptions
     * @param id
     * @return
     * @throws SQLException
     */
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

    /**
     * Gets a specified doctors patients
     * @param id
     * @return
     * @throws SQLException
     */
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
                String timeArrived = result.getString("time_arrived");
                Patient patient = new Patient(patid, forename, surname, symptoms, illness, in_queue, timeArrived, getPrescriptionsForPatient(patid), getMealsForPatient(patid));
                docPats.add(patient);
            }
        }
        return docPats;
    }

    /**
     * Gets a specified wards beds
     * @param id
     * @return
     * @throws SQLException
     */
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
    
    /**
     * Removes a patient from a bed
     * @param b
     * @throws SQLException
     */
    public void removePatientFromBed(Bed b) throws SQLException {
        if (connection != null) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("UPDATE BED SET PATIENT_ID='' WHERE BED_ID="+b.getBed_id());
        }
    }
    
    /**
     * Removes a patient from a doctor
     * @param d
     * @throws SQLException
     */
    public void removePatientFromDoctor(Doctor d) throws SQLException {
        if (connection != null) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("UPDATE DOCTOR SET PATIENT_ID='' WHERE DOCTOR_ID="+d.getDoctor_id());
        }
    }
}
