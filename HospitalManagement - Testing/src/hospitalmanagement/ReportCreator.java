
package hospitalmanagement;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author nathan ward
 */
public class ReportCreator {
    
    private DatabaseAccess db;
    ScheduledExecutorService s;
    Runnable runner;
    private HospitalManagement hm;
    
    /**
     * Initialises objects
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public ReportCreator() throws SQLException, ClassNotFoundException {
        db = new DatabaseAccess();
        hm = new HospitalManagement();
    }
    
    /**
     * Turns a time string into a Gregorian calendar
     * @param date
     * @return Gregorian calendar representation of a time
     */
    public GregorianCalendar stringToGreg(String date) {
        String[] hoursAndMins = date.split(":");

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int eventHour = Integer.parseInt(hoursAndMins[0]);
        int eventMinute = Integer.parseInt(hoursAndMins[1])+1;

        GregorianCalendar eventDate = new GregorianCalendar(currentYear, currentMonth, currentDay, eventHour, eventMinute);
        return eventDate;
    }

    /**
     *
     * @param date
     * @return Gregorian time in milliseconds
     */
    public long stringToDate(String date) {
        long timeMillis = stringToGreg(date).getTime().getTime();
        return timeMillis;
    }

    /**
     *
     * @return current time in Gregorian calendar format
     */
    public GregorianCalendar currentDateGreg() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);

        GregorianCalendar currentDate = new GregorianCalendar(currentYear, currentMonth, currentDay, currentHour, currentMinute);
        return currentDate;
    }

    /**
     *
     * @return current date in milliseconds
     */
    public long getCurrentDate() {
        long currentMillis = currentDateGreg().getTime().getTime();
        return currentMillis;
    }

    /**
     * Loads all data that has time constraints, if time data is later than the current time a report will be created, 
     * otherwise it will be put into a list.
     * @throws ParseException
     * @throws SQLException
     * @throws IOException
     */
    public void calculateDate() throws ParseException, SQLException, IOException {
        long difference = 0;
        ObservableList<Patient> pats = db.getPatients();
        for (Patient p : pats) {
            if (p.isIn_queue()) {
                difference = stringToDate(p.getTimeArrived()) - getCurrentDate();
                if (difference < 0) {
                    writeReport(p, null);
                } else {
                    hm.getGregTimes().add(difference);
                }
            }
            for (Prescription pre : p.getPresc()) {
                if (pre.isMedicine_taken() == false) {
                    difference = stringToDate(pre.getTime_to_take_medicine()) - getCurrentDate();
                    if (difference < 0) {
                        writeReport(p, pre);
                    } else {
                        hm.getGregTimes().add(difference);
                    }
                }
            }
            for (Meal m : p.getMeals()) {
                difference = stringToDate(m.getMeal_time()) - getCurrentDate();
                if (difference < 0) {
                    writeReport(p, m);
                } else {
                    hm.getGregTimes().add(difference);
                }
            }
        }

        if (!hm.getGregTimes().isEmpty()) {
            Collections.sort(hm.getGregTimes());
            timer();
        }
        
    }

    /**
     * 
     * @throws IOException
     * @throws SQLException
     */
    public void createReport() throws IOException, SQLException {
        ObservableList<Patient> patients = db.getPatients();
        ObservableList<Patient> patients2 = FXCollections.observableArrayList();
        ObservableList<Prescription> pres = FXCollections.observableArrayList();
        ObservableList<Meal> meals = FXCollections.observableArrayList();
        for (Patient p : patients) {
            if (stringToGreg(p.getTimeArrived()) == currentDateGreg()) {
                writeReport(p, null);
            }
            for (Prescription pr : p.getPresc()) {
                if (stringToGreg(pr.getTime_to_take_medicine()) == currentDateGreg()) {
                    writeReport(p, pr);
                    Prescription prescopy = pr;
                    pres.add(prescopy);
                }
            }
            for (Meal m : p.getMeals()) {
                if (stringToGreg(m.getMeal_time()) == currentDateGreg()) {
                    writeReport(p, m);
                    Meal mealcopy = m;
                    meals.add(mealcopy);
                }
            }
            Patient patcopy = p;
            p.setPresc(pres);
            p.setMeals(meals);
            patients2.add(patcopy);

        }
    }

    /**
     * Writes a report in a text file, contents is based on which object missed their time constraint.
     * @param pat
     * @param ob2
     * @throws IOException
     */
    public void writeReport(Patient pat, Object ob2) throws IOException {
        List<String> content = new ArrayList<>();
        Path p = null;
        if (ob2 instanceof Prescription) {
            content = Arrays.asList(
                    "Patient: " + pat.getForename() + " " + pat.getSurname() + " has missed their scheduled "
                    + "prescription of " + ((Prescription) ob2).getMedication() + " at " + ((Prescription) ob2).getTime_to_take_medicine(),
                    "");
            p = Paths.get("src/reports/"+pat.getForename() + "-" + ((Prescription) ob2).getMedication() + "-" + gregorianToString(currentDateGreg()) + ".txt");
        } else if (ob2 instanceof Meal) {
            content = Arrays.asList(
                    "Patient: " + pat.getForename() + " " + pat.getSurname() + " has missed their scheduled"
                    + " meal of " + ((Meal) ob2).getMeal_name() + " at " + ((Meal) ob2).getMeal_time(),
                    "");
            p = Paths.get("src/reports/"+pat.getForename() + "-" + ((Meal) ob2).getMeal_name() + "-" + gregorianToString(currentDateGreg()) + ".txt");
        } else if (ob2 == null) {
            content = Arrays.asList(
                    "Patient: " + pat.getForename() + " " + pat.getSurname() + " has been waiting " + stringToDate(pat.getTimeArrived()) / 60000 + " minutes which is over the 4 hour maximum wait for a bed", ""
            );
            p = Paths.get("src/reports/"+pat.getForename() + "-" + "waiting-since-" + pat.getTimeArrived() + ".txt");
        }

        //Files.write(p, content, Charset.forName("UTF-8"));
    }
    
    /**
     *
     * @return all reports in the reports folder as strings
     */
    public ObservableList<String> getAllReports() {
        ObservableList fileStrings = FXCollections.observableArrayList();
        File file = new File("src/reports/");
        File[] allFiles = file.listFiles();
        for (File f : allFiles) {
            fileStrings.add(f.getName());
        }
        return fileStrings;
    }

    /**
     *
     * @param gc
     * @return Gregorian time as string
     */
    public String gregorianToString(GregorianCalendar gc) {
        SimpleDateFormat sd = new SimpleDateFormat("HH-mm, dd-MM-YYYY");
        sd.setCalendar(gc);
        String date = sd.format(gc.getTime());
        return date;
    }

    /**
     * Changes the interval of the timer every time a time from the list is met. 
     */
    public void changeInterval() {
        if (hm.getGregTimes().size() > 1) {
            long difference = hm.getGregTimes().get(1) - hm.getGregTimes().get(0);
            hm.getGregTimes().remove(0);
            ScheduledFuture<?> newTask;
            newTask = s.schedule(runner, difference, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Begins the timer, starts running on program start-up
     */
    public void timer() {
        runner = () -> {
            try {
                createReport();
                if (hm.getGregTimes().size() > 1) {
                    changeInterval();
                }
            } catch (IOException | SQLException ex) {
                System.out.println(ex);
            }
        };
        //Make it so that this runs after each interval in the array
        s = Executors.newScheduledThreadPool(1);
        s.schedule(runner, hm.getGregTimes().get(0), TimeUnit.MILLISECONDS);
    }
    
    /**
     *
     * @return time scheduler
     */
    public ScheduledExecutorService getScheduled() {
        return s;
    }
}


