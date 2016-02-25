/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hospitalmanagement;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class HospitalManagement extends Application {

    Scene scene1, scene2, scene3;
    Connection conn = null;
    TableView<Patient> table;
    DatabaseAccess db;
    ArrayList<Long> gregTimes = new ArrayList<Long>();
    //ArrayList<Integer> test = new ArrayList<Integer>(Arrays.asList(2, 6, 14));
    ScheduledExecutorService s;
    Runnable runner;

    public HospitalManagement() throws SQLException {
    }

    @Override
    public void start(Stage primaryStage) throws Exception, SQLException {
        
        db = new DatabaseAccess();
        calculateDate();

        primaryStage.setOnCloseRequest(e -> {
            if (gregTimes.size() != 0)
                s.shutdown();
                });
        primaryStage.setTitle("Hospital Management");

        //Menu
        MenuItem addPatient = new MenuItem("Patient");

        MenuItem addBed = new MenuItem("Bed");
        MenuItem addWard = new MenuItem("Ward");
        MenuItem addPrescription = new MenuItem("Prescription");

        MenuItem editPatient = new MenuItem("Patient");
        MenuItem editPrescription = new MenuItem("Prescription");
        MenuItem editBed = new MenuItem("Bed");
        MenuItem editWard = new MenuItem("Ward");

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> primaryStage.close());

        MenuItem home = new MenuItem("Home");
        home.setOnAction(e -> {
            scene1.lookup("menuBar");
            primaryStage.setScene(scene1);
        });

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(home, exit);

        Menu addMenu = new Menu("Add");
        addMenu.getItems().addAll(addPatient, addBed, addWard, addPrescription);

        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(editPatient, editPrescription, editBed, editWard);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, addMenu, editMenu);

        //Creates the add patient form
        addPatient.setOnAction(e -> addPatient(primaryStage, menuBar));

        //Creates the add prescription form
        addPrescription.setOnAction(e -> addPrescription(primaryStage, menuBar));

        editPrescription.setOnAction(e -> {
            try {
                primaryStage.setScene(editPrescription("Edit Prescription", menuBar));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        //Home screen title
        Label title = new Label("Hospital Management");

        //Home screen input box test
        Label searchLabel = new Label("Search:");
        TextField searchField = new TextField();

        //Home screen buttons
        Button submit = new Button("Search");
        submit.setOnAction(e -> System.out.println("This will search"));

        //Home screen table
        TableColumn<Patient, String> forenameCol = new TableColumn<>("Forename");
        forenameCol.setCellValueFactory(new PropertyValueFactory<>("forename"));
        forenameCol.setMinWidth(20);
        //System.out.println(forenameCol.getCellData(0));

        TableColumn<Patient, String> surnameCol = new TableColumn<>("Surname");
        surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));
        surnameCol.setMinWidth(20);

        TableColumn<Patient, String> symptomsCol = new TableColumn<>("Symptoms");
        symptomsCol.setCellValueFactory(new PropertyValueFactory<>("symptoms"));
        symptomsCol.setMinWidth(20);

        TableColumn<Patient, String> illnessCol = new TableColumn<>("Illness");
        illnessCol.setCellValueFactory(new PropertyValueFactory<>("illness"));
        illnessCol.setMinWidth(20);

        TableColumn<Patient, String> inQueueCol = new TableColumn<>("In Queue");
        inQueueCol.setCellValueFactory(new PropertyValueFactory<>("in_queue"));
        inQueueCol.setMinWidth(20);

        TableColumn<Patient, String> doctorForenameCol = new TableColumn<>("Doctors Forename");
        doctorForenameCol.setCellValueFactory(new PropertyValueFactory<>("doctor_forename"));
        doctorForenameCol.setMinWidth(20);

        table = new TableView<>();
        //System.out.println(db.getPeople().get(0).getForename());
        table.setItems(db.getIndexList());
        table.getColumns().addAll(forenameCol, surnameCol, symptomsCol, illnessCol, inQueueCol, doctorForenameCol);

        //Home screen layout
        BorderPane homeLayout = new BorderPane();
        GridPane content = new GridPane();
        //layout1.setPadding(new Insets(10, 10, 10, 10));
        homeLayout.setTop(menuBar);
        homeLayout.setCenter(content);
        //layout1.getChildren().addAll(title, searchLabel, searchField, submit, table, button1);
        GridPane.setConstraints(title, 0, 1);
        GridPane.setConstraints(searchLabel, 1, 1);
        GridPane.setConstraints(searchField, 2, 1);
        GridPane.setConstraints(submit, 3, 1);
        GridPane.setConstraints(table, 0, 5);
        content.getChildren().addAll(title, searchLabel, searchField, submit, table);
        scene1 = new Scene(homeLayout, 800, 600);

        primaryStage.setScene(scene1);
        primaryStage.show();
    }

    public Scene template(String title, MenuBar globalMenu, Label[] labels, TextField[] fields, Button button, DatabaseAccess db, ComboBox patients) throws SQLException {

        BorderPane outerPane = new BorderPane();
        outerPane.setTop(globalMenu);

        VBox innerPane = new VBox();
        outerPane.setCenter(innerPane);

        patients = new ComboBox();
        patients.getItems().addAll(db.getIndexList().get(0).getForename());
        innerPane.getChildren().add(patients);

        for (int i = 0; i < labels.length; i++) {
            innerPane.getChildren().add(labels[i]);
            innerPane.getChildren().add(fields[i]);
        }

        innerPane.getChildren().add(button);

        Scene sceneTemplate = new Scene(outerPane, 600, 400);
        return sceneTemplate;
    }

//    public Scene editTemplate(String title, MenuBar globalMenu, Button button, DatabaseAccess db, ComboBox patients, TableView tb) throws SQLException {
//        BorderPane outerPane = new BorderPane();
//        outerPane.setTop(globalMenu);
//
//        VBox innerPane = new VBox();
//        outerPane.setCenter(innerPane);
//
//        patients = new ComboBox();
//        patients.getItems().addAll(db.getIndexList().get(0).getForename());
//        //TableView tb = new TableView<>();
//        innerPane.getChildren().add(patients);
//
//
//        innerPane.getChildren().add(button);
//
//        Scene sceneTemplate = new Scene(outerPane, 600, 400);
//        return sceneTemplate;
//    }
    public void addPatient(Stage stage, MenuBar menuBar) {
        Label[] labels = new Label[]{new Label("Patient First Name"), new Label("Patient Surname"), new Label("Symptoms"), new Label("Illness"), new Label("In Queue"), new Label("Time Spent Waiting (minutes)"), new Label("Doctor ID"), new Label("Prescription ID")};
        TextField[] fields = new TextField[labels.length];
        System.out.println(labels.length);
        for (int i = 0; i < labels.length; i++) {
            //System.out.println(i);
            fields[i] = new TextField();
        }

        Button templateButton = new Button("Submit");
        templateButton.setOnAction(f -> {
            try {
                db.addPatient(fields[0].getText(), fields[1].getText(), fields[2].getText(), fields[3].getText(), Boolean.parseBoolean(fields[4].getText()), Integer.parseInt(fields[5].getText()), Integer.parseInt(fields[6].getText()), Integer.parseInt(fields[7].getText()));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });
        try {
            Scene addPatientPage = template("Add Patient", menuBar, labels, fields, templateButton, db, new ComboBox());
            stage.setScene(addPatientPage);
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public void addPrescription(Stage stage, MenuBar menuBar) {
        Label[] labels = new Label[]{new Label("Medication Name"), new Label("Frequency Per Day"), new Label("Number Of Days"), new Label("Time To Take Medecine"), new Label("Medecine Taken")};
        TextField[] fields = new TextField[labels.length];
        System.out.println(labels.length);
        for (int i = 0; i < labels.length; i++) {
            //System.out.println(i);
            fields[i] = new TextField();
        }

        Button templateButton = new Button("Submit");
        templateButton.setOnAction(f -> {
            try {
                String newTime = fields[3].getText();
                gregTimes.add(stringToDate(newTime) - getCurrentDate());
                Collections.sort(gregTimes);
                db.addPrescripton(fields[0].getText(), Integer.parseInt(fields[1].getText()), Integer.parseInt(fields[2].getText()), String.valueOf(fields[3].getText()), Boolean.parseBoolean(fields[4].getText()));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });
        try {
            Scene addPrescPage = template("Add Prescription", menuBar, labels, fields, templateButton, db, new ComboBox());
            stage.setScene(addPrescPage);
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public Scene editPrescription(String title, MenuBar menuBar) throws SQLException {
        //String[] columns = new String[] {"Medicine Name", "Frequency", "Number Of Days", "Time to Take Medicine", "Medicine Taken"};
        //ArrayList<Prescription> list = db.getPrescription();
        //System.out.println(labels.length);

        TableView tb = new TableView<Prescription>();
        //Problem - string not converting to int

        TableColumn<Prescription, Integer> id = new TableColumn("ID");
        createColumnInteger(id, "id");
        id.setOnEditCommit(e -> ((Prescription) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setFrequency(e.getNewValue()));

        TableColumn<Prescription, String> medName = new TableColumn("Medicine Name");
        createColumnString(medName, "medication");
        medName.setOnEditCommit(e -> ((Prescription) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setMedication(e.getNewValue()));

        TableColumn<Prescription, Integer> frequency = new TableColumn("Frequency");
        createColumnInteger(frequency, "frequency");
        frequency.setOnEditCommit(e -> ((Prescription) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setFrequency(e.getNewValue()));

        TableColumn<Prescription, Integer> numDays = new TableColumn("Number of Days");
        createColumnInteger(numDays, "number_of_days");
        numDays.setOnEditCommit(e -> ((Prescription) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setNumber_of_days(e.getNewValue()));

        TableColumn<Prescription, String> timeToTake = new TableColumn("Time to Take Medicine");
        createColumnString(timeToTake, "time_to_take_medicine");
        timeToTake.setOnEditCommit(e -> ((Prescription) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setTime_to_take_medicine(e.getNewValue()));

        TableColumn<Prescription, Boolean> medTaken = new TableColumn("Medicine Taken");
        createColumnBoolean(medTaken, "medicine_taken");
        medTaken.setOnEditCommit(e -> ((Prescription) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setMedicine_taken(e.getNewValue()));

        tb.setEditable(true);
        tb.setItems(db.getPrescription());
        tb.getColumns().addAll(id, medName, frequency, numDays, timeToTake);

        Button editButton = new Button("Save");
        editButton.setOnAction(f -> {
            ArrayList<Prescription> p = new ArrayList<>();
            for (int i = 0; i < tb.getItems().size(); i++) {
                p.add((Prescription) tb.getItems().get(i));
            }
            try {
                db.updatePrescription(p);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deletePrescription(tb));

        BorderPane outerPane = new BorderPane();
        outerPane.setTop(menuBar);
        VBox innerPane = new VBox();
        innerPane.getChildren().addAll(tb, editButton);
        outerPane.setCenter(innerPane);

        Scene scene = new Scene(outerPane, 800, 600);
        return scene;
    }

    public void createColumnInteger(TableColumn tc, String source) {
        tc.setCellValueFactory(new PropertyValueFactory<>(source));
        tc.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
    }

    public void createColumnString(TableColumn tc, String source) {
        tc.setCellValueFactory(new PropertyValueFactory<>(source));
        tc.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    public void createColumnBoolean(TableColumn tc, String source) {
        tc.setCellValueFactory(new PropertyValueFactory<>(source));
        tc.setCellFactory(TextFieldTableCell.forTableColumn(new BooleanStringConverter()));
    }

    public void deletePrescription(TableView tb) {
        ObservableList<Prescription> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deletePrescription(selected);
        selected.forEach(all::remove);
    }

    public long stringToDate(String date) {
        String[] hoursAndMins = date.split(":");

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int eventHour = Integer.parseInt(hoursAndMins[0]);
        int eventMinute = Integer.parseInt(hoursAndMins[1]);

        GregorianCalendar eventDate = new GregorianCalendar(currentYear, currentMonth, currentDay, eventHour, eventMinute);
        long timeMillis = eventDate.getTime().getTime();
        return timeMillis;
    }

    public long getCurrentDate() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);

        GregorianCalendar currentDate = new GregorianCalendar(currentYear, currentMonth, currentDay, currentHour, currentMinute);
        long currentMillis = currentDate.getTime().getTime();
        return currentMillis;
    }

    public void calculateDate() throws ParseException, SQLException {
        long difference = 0;
        ObservableList<Prescription> pres = db.getPrescription();
        for (int i = 0; i < pres.size(); i++) {
            difference = stringToDate(pres.get(i).getTime_to_take_medicine()) - getCurrentDate();
            if (difference < 0) {
                //Create a report saying when it was missed
            } else {
                gregTimes.add(difference);
            }
        }
        
        if (gregTimes.size() != 0) {
            Collections.sort(gregTimes);
            timer();
        }
        //Implement it so that each element of the list is the value take away the
        //previous value.
    }

    public void createReport() throws IOException {
        List<String> content = Arrays.asList("First line", "Second line");
        Path p = Paths.get("test.txt");
        Files.write(p, content, Charset.forName("UTF-8"));

    }
    
    public void changeInterval() {
        if (gregTimes.size() > 1) {
            long difference = gregTimes.get(1) - gregTimes.get(0);
            gregTimes.remove(0);
            ScheduledFuture<?> newTask;
            newTask = s.schedule(runner, difference, TimeUnit.MILLISECONDS);
        }
    }
    
    public void timer() {
        runner = () -> {
            //try {
                System.out.println("Hello");
                //createReport();
                if (gregTimes.size() > 1)
                    changeInterval();
            //} catch (IOException ex) {
                //System.out.println(ex);
            //}
        };
        //Make it so that this runs after each interval in the array
        s = Executors.newScheduledThreadPool(1);
        s.schedule(runner, gregTimes.get(0), TimeUnit.MILLISECONDS);
        
    }

    public static void main(String[] args) {
        launch(args);
    }
}
