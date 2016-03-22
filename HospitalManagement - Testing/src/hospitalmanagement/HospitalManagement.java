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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class HospitalManagement extends Application {

    Connection conn = null;
    DatabaseAccess db;
    ArrayList<Long> gregTimes = new ArrayList<>();
    ScheduledExecutorService s;
    Runnable runner;
    String errorText;
    String title;

    public HospitalManagement() throws SQLException {
    }

    @Override
    public void start(Stage primaryStage) throws Exception, SQLException {

        db = new DatabaseAccess();
        calculateDate();
        this.title = "Hospital Management";
        primaryStage.setTitle(title);

        primaryStage.setScene(loginPage(primaryStage, homePage("Hospital Management", getMenu(primaryStage, getSearchBar()), getSearchBar())));
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            if (!gregTimes.isEmpty()) {
                s.shutdown();
            }

            primaryStage.close();
            System.exit(0);
        });
    }

    public TextField getSearchBar() {
        TextField searchField = new TextField();
        searchField.setPromptText("Search:");
        searchField.setText("");
        return searchField;
    }

    public MenuBar getMenu(Stage primaryStage, TextField searchField) {
        //Menu
        MenuItem viewPatient = new MenuItem("Patient");

        //MenuItem editPatient = new MenuItem("Patient");
        MenuItem viewPrescription = new MenuItem("Prescription");
        MenuItem viewBed = new MenuItem("Bed");
        MenuItem viewWard = new MenuItem("Ward");
        MenuItem viewDoctor = new MenuItem("Doctor");
        MenuItem viewMeal = new MenuItem("Meal");

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> primaryStage.close());

        MenuItem home = new MenuItem("Home");

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(home, exit);

        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(viewPatient, viewPrescription, viewBed, viewWard, viewDoctor, viewMeal);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, viewMenu);

        home.setOnAction(e -> {
            try {
                primaryStage.setScene(homePage("Hospital Management", menuBar, searchField));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });
        //Creates the add patient form
        viewPatient.setOnAction(e -> {
            try {
                primaryStage.setScene(editPatient("Patient", menuBar, searchField, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        viewPrescription.setOnAction(e -> {
            try {
                primaryStage.setScene(editPrescription("Edit Prescription", menuBar, searchField, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        viewDoctor.setOnAction(e -> {
            try {
                primaryStage.setScene(editDoctor("Edit Doctor", menuBar, searchField, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        viewBed.setOnAction(e -> {
            try {
                primaryStage.setScene(editBed("Edit Bed", menuBar, searchField, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        viewMeal.setOnAction(e -> {
            try {
                primaryStage.setScene(editMeal("Edit Meal", menuBar, searchField, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        viewWard.setOnAction(e -> {
            try {
                primaryStage.setScene(editWard("Edit Ward", menuBar, searchField, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });
        return menuBar;
    }

    public VBox addTemplate(Label[] labels, TextField[] fields, Button button, ComboBox[] select) {
        VBox add = new VBox();
        if (select != null) {
            add.getChildren().addAll(Arrays.asList(select));
        }
        for (int i = 0; i < labels.length; i++) {
            add.getChildren().add(labels[i]);
            add.getChildren().add(fields[i]);
        }

        add.getChildren().add(button);
        return add;
    }

    public Scene homePage(String title, MenuBar menuBar, TextField search) throws SQLException {
        TableColumn<Patient, String> forenameCol = new TableColumn<>("Forename");
        forenameCol.setCellValueFactory(new PropertyValueFactory<>("forename"));
        forenameCol.setMinWidth(20);

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

        TableView table = new TableView<>();
        table.setItems(db.getPatients());
        table.getColumns().addAll(forenameCol, surnameCol, symptomsCol, illnessCol, inQueueCol, doctorForenameCol);

        //Home screen layout
        BorderPane homeLayout = new BorderPane();
        GridPane content = new GridPane();
        homeLayout.setTop(menuBar);
        homeLayout.setCenter(content);

        GridPane.setConstraints(search, 2, 1);
        GridPane.setConstraints(table, 0, 5);
        content.getChildren().addAll(search, table);
        Scene scene1 = new Scene(homeLayout, 800, 600);
        return scene1;
    }

    public Scene loginPage(Stage primaryStage, Scene home) {
        Label userLabel = new Label("Username:");
        TextField username = new TextField();
        Label passLabel = new Label("Password");
        PasswordField password = new PasswordField();

        Label error = new Label();

        Button submit = new Button("Log in");
        submit.setOnAction(e -> {
            try {
                if (db.login(username.getText(), password.getText()) == true) {
                    primaryStage.setScene(home);
                } else {
                    error.setText("Your login details are incorrect, please try again");
                    error.setStyle("-fx-text-fill: red;");
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        GridPane layout = new GridPane();
        GridPane.setConstraints(userLabel, 0, 0);
        GridPane.setConstraints(username, 1, 0);
        GridPane.setConstraints(passLabel, 0, 1);
        GridPane.setConstraints(password, 1, 1);
        GridPane.setConstraints(submit, 0, 3);
        GridPane.setConstraints(error, 1, 4);
        layout.getChildren().addAll(userLabel, username, passLabel, password, submit, error);
        Scene scene = new Scene(layout, 800, 600);
        return scene;
    }

    public Scene editBed(String title, MenuBar menuBar, TextField search, Stage primaryStage) throws SQLException {
        TableView tb = new TableView<>();

        TableColumn<Bed, Integer> id = new TableColumn("ID");
        createColumnInteger(id, "bed_id");
        id.setOnEditCommit(e -> ((Bed) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setBed_id(e.getNewValue()));

        TableColumn<Bed, Boolean> cleaned = new TableColumn("Cleaned");
        createColumnBoolean(cleaned, "cleaned");
        cleaned.setOnEditCommit(e -> ((Bed) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setCleaned(e.getNewValue()));

        ObservableList<Bed> beds = FXCollections.observableArrayList(db.getBeds());
        FilteredList<Bed> bedsFilt = new FilteredList<>(beds, bed -> true);

        search.textProperty().addListener((observable, oldValue, newValue) -> {
            bedsFilt.setPredicate(bed -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(bed.getBed_id()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(bed.isCleaned()).toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches first name.
                }
                return false;
            });
        });

        SortedList<Bed> bedSorted = new SortedList(bedsFilt);
        bedSorted.comparatorProperty().bind(tb.comparatorProperty());

        tb.setItems(beds);

        tb.setEditable(true);
        tb.getColumns().addAll(id, cleaned);

        Button viewButton = new Button("View Data");
        viewButton.setOnAction(e -> primaryStage.setScene(viewData(tb, primaryStage)));

        Button editButton = new Button("Save");
        editButton.setOnAction(f -> {
            ArrayList<Bed> b = new ArrayList<>();
            for (int i = 0; i < tb.getItems().size(); i++) {
                b.add((Bed) tb.getItems().get(i));
            }
            try {
                db.updateBed(b);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteBed(tb));

        BorderPane outerPane = new BorderPane();
        outerPane.setTop(menuBar);

        VBox innerPane = new VBox();
        innerPane.getChildren().addAll(search, tb, viewButton, editButton, deleteButton);
        innerPane.requestFocus();
        outerPane.setCenter(innerPane);

        Label[] labels = new Label[]{new Label("Cleaned")};
        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new TextField();
        }

        ObservableList<Ward> wards = db.getWard();
        ObservableList<String> wardList = FXCollections.observableArrayList();
        for (Ward w : wards) {
            wardList.add(w.getWard_name());
        }

        ComboBox combo = new ComboBox(wardList);
        combo.setPromptText("Select Ward:");
        ComboBox[] select = new ComboBox[]{combo};

        Button button = new Button("Submit");
        button.setOnAction(e -> {
            try {
                addBedToTable(tb, fields, select);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        outerPane.setBottom(addTemplate(labels, fields, button, select));

        Scene scene = new Scene(outerPane, 800, 600);
        return scene;
    }

    public Scene editDoctor(String title, MenuBar menuBar, TextField search, Stage primaryStage) throws SQLException {
        TableView tb = new TableView<>();

        TableColumn<Doctor, Integer> id = new TableColumn("ID");
        createColumnInteger(id, "doctor_id");
        id.setOnEditCommit(e -> ((Doctor) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setDoctor_id(e.getNewValue()));

        TableColumn<Doctor, String> forename = new TableColumn("Forename");
        createColumnString(forename, "doctor_forename");
        forename.setOnEditCommit(e -> ((Doctor) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setDoctor_forename(e.getNewValue()));

        TableColumn<Doctor, String> surname = new TableColumn("Surname");
        createColumnString(surname, "doctor_surname");
        surname.setOnEditCommit(e -> ((Doctor) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setDoctor_surname(e.getNewValue()));

        TableColumn<Doctor, String> spec = new TableColumn("Specialisation");
        createColumnString(spec, "specialisation");
        spec.setOnEditCommit(e -> ((Doctor) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setSpecialisation(e.getNewValue()));

        ObservableList<Doctor> doctors = FXCollections.observableArrayList(db.getDoctors());
        FilteredList<Doctor> doctorsFilt = new FilteredList<>(doctors, doctor -> true);

        search.textProperty().addListener((observable, oldValue, newValue) -> {
            doctorsFilt.setPredicate(doc -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(doc.getDoctor_id()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                } else if (doc.getDoctor_forename().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches first name.
                } else if (doc.getDoctor_surname().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches last name.
                } else if (doc.getSpecialisation().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Doctor> docSorted = new SortedList(doctorsFilt);
        docSorted.comparatorProperty().bind(tb.comparatorProperty());

        tb.setEditable(true);
        if ("".equals(search.getText())) {
            System.out.println("Normal Objects");
            tb.setItems(doctors);
        } else {
            System.out.println("Filtered Objects");
            tb.setItems(docSorted);
        }

        tb.getColumns().addAll(id, forename, surname, spec);

        Button editButton = new Button("Save");
        editButton.setOnAction(f -> {
            ArrayList<Doctor> d = new ArrayList<>();
            for (int i = 0; i < tb.getItems().size(); i++) {
                d.add((Doctor) tb.getItems().get(i));
            }
            try {
                db.updateDoctor(d);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteDoctor(tb));

        BorderPane outerPane = new BorderPane();
        outerPane.setTop(menuBar);

        Label[] labels = new Label[]{new Label("Doctor Forename"), new Label("Doctor Surname"), new Label("Specialisations")};
        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new TextField();
        }

        Button viewButton = new Button("View Data");
        viewButton.setOnAction(e -> primaryStage.setScene(viewData(tb, primaryStage)));

        Button button = new Button("Submit");
        button.setOnAction(e -> {
            try {
                addDoctorToTable(tb, fields, null);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        VBox innerPane = new VBox();
        innerPane.getChildren().addAll(search, tb, viewButton, editButton, deleteButton);
        innerPane.requestFocus();
        outerPane.setCenter(innerPane);

        outerPane.setBottom(addTemplate(labels, fields, button, null));

        Scene scene = new Scene(outerPane, 800, 600);
        return scene;
    }

    public Scene editMeal(String title, MenuBar menuBar, TextField search, Stage primaryStage) throws SQLException {
        TableView tb = new TableView<>();

        TableColumn<Meal, Integer> id = new TableColumn("ID");
        createColumnInteger(id, "meal_id");
        id.setOnEditCommit(e -> ((Meal) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setMeal_id(e.getNewValue()));

        TableColumn<Meal, String> mealName = new TableColumn("Meal Name");
        createColumnString(mealName, "meal_name");
        mealName.setOnEditCommit(e -> ((Meal) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setMeal_name(e.getNewValue()));

        TableColumn<Meal, Boolean> eaten = new TableColumn("Eaten");
        createColumnBoolean(eaten, "eaten");
        eaten.setOnEditCommit(e -> ((Meal) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setEaten(e.getNewValue()));

        TableColumn<Meal, String> mealTime = new TableColumn("Meal Time");
        createColumnString(mealTime, "meal_time");
        mealTime.setOnEditCommit(e -> ((Meal) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setMeal_time(e.getNewValue()));

        ObservableList<Meal> meals = FXCollections.observableArrayList();
        meals = db.getMeals();
        FilteredList<Meal> mealsFilt = new FilteredList<>(meals, meal -> true);

        search.textProperty().addListener((observable, oldValue, newValue) -> {
            mealsFilt.setPredicate(meal -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(meal.getMeal_id()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches first name.
                } else if (meal.getMeal_name().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches last name.
                } else if (meal.getMeal_time().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                } else if (String.valueOf(meal.isEaten()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Meal> mealSorted = new SortedList(mealsFilt);
        mealSorted.comparatorProperty().bind(tb.comparatorProperty());

        tb.setEditable(true);
        tb.setItems(mealSorted);
        tb.getColumns().addAll(id, mealName, eaten, mealTime);

        Button viewButton = new Button("View Data");
        viewButton.setOnAction(e -> primaryStage.setScene(viewData(tb, primaryStage)));

        Button editButton = new Button("Save");
        editButton.setOnAction(f -> {
            ArrayList<Meal> m = new ArrayList<>();
            for (int i = 0; i < tb.getItems().size(); i++) {
                m.add((Meal) tb.getItems().get(i));
            }
            try {
                db.updateMeal(m);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteMeal(tb));

        BorderPane outerPane = new BorderPane();
        outerPane.setTop(menuBar);

        VBox innerPane = new VBox();
        innerPane.getChildren().addAll(search, tb, viewButton, editButton, deleteButton);
        outerPane.setCenter(innerPane);

        Label[] labels = new Label[]{new Label("Meal Name"), new Label("Meal Eaten"), new Label("Meal Time")};
        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new TextField();
        }

        ObservableList<String> patientStrings;
        patientStrings = FXCollections.observableArrayList();
        for (int i = 0; i < db.getPatients().size(); i++) {
            patientStrings.add(db.getPatients().get(i).getForename());
        }

        ComboBox patientStringsList = new ComboBox(patientStrings);
        patientStringsList.setPromptText("Patient:");
        ComboBox[] selects = new ComboBox[]{patientStringsList};

        Button button = new Button("Submit");
        button.setOnAction(e -> {
            try {
                addMealToTable(tb, fields, selects);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        outerPane.setBottom(addTemplate(labels, fields, button, selects));

        Scene scene = new Scene(outerPane, 800, 600);
        return scene;
    }

    public Scene editPatient(String title, MenuBar menuBar, TextField search, Stage primaryStage) throws SQLException {
        TableView tb = new TableView<>();

        TableColumn<Patient, Integer> id = new TableColumn("ID");
        createColumnInteger(id, "id");
        id.setOnEditCommit(e -> ((Patient) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setId(e.getNewValue()));

        TableColumn<Patient, String> forename = new TableColumn("Forename");
        createColumnString(forename, "forename");
        forename.setOnEditCommit(e -> ((Patient) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setForename(e.getNewValue()));

        TableColumn<Patient, String> surname = new TableColumn("Surname");
        createColumnString(surname, "surname");
        surname.setOnEditCommit(e -> ((Patient) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setForename(e.getNewValue()));

        TableColumn<Patient, String> symptoms = new TableColumn("Symptoms");
        createColumnString(symptoms, "symptoms");
        symptoms.setOnEditCommit(e -> ((Patient) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setSymptoms(e.getNewValue()));

        TableColumn<Patient, String> illness = new TableColumn("Illness");
        createColumnString(illness, "illness");
        illness.setOnEditCommit(e -> ((Patient) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setIllness(e.getNewValue()));

        TableColumn<Patient, Boolean> inQueue = new TableColumn("In Queue");
        createColumnBoolean(inQueue, "in_queue");
        inQueue.setOnEditCommit(e -> ((Patient) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setIn_queue(e.getNewValue()));

        TableColumn<Patient, Integer> timeWaiting = new TableColumn("Time Waiting (minutes)");
        createColumnInteger(timeWaiting, "time_waiting");
        timeWaiting.setOnEditCommit(e -> ((Patient) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setTime_waiting(e.getNewValue()));

        ObservableList<Patient> patients = FXCollections.observableArrayList();
        patients = db.getPatients();
        FilteredList<Patient> patientsFilt = new FilteredList<>(patients, patient -> true);

        search.textProperty().addListener((observable, oldValue, newValue) -> {
            patientsFilt.setPredicate(pat -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (pat.getForename().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches first name.
                } else if (pat.getSurname().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches last name.
                } else if (pat.getSymptoms().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                } else if (pat.getIllness().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                } else if (String.valueOf(pat.isIn_queue()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                } else if (String.valueOf(pat.getTime_waiting()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Patient> patSorted = new SortedList(patientsFilt);
        patSorted.comparatorProperty().bind(tb.comparatorProperty());

        if ("".equals(search.getText())) {
            tb.setItems(patients);
        } else {
            tb.setItems(patSorted);
        }

        tb.setEditable(true);
        tb.getColumns().addAll(id, forename, surname, symptoms, illness, inQueue, timeWaiting);

        Label[] labels = new Label[]{new Label("Patient Forename"), new Label("Patient Surname"), new Label("Symptoms"), new Label("Illness"), new Label("In Queue"), new Label("Time waiting (minutes)")};

        Button viewButton = new Button("View Data");
        viewButton.setOnAction(e -> primaryStage.setScene(viewData(tb, primaryStage)));

        Button editButton = new Button("Save");
        editButton.setOnAction(f -> {
            ArrayList<Patient> p = new ArrayList<>();
            for (int i = 0; i < tb.getItems().size(); i++) {
                p.add((Patient) tb.getItems().get(i));
            }
            try {
                db.updatePatient(p);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deletePatient(tb));

        BorderPane outerPane = new BorderPane();
        outerPane.setTop(menuBar);

        VBox innerPane = new VBox();
        innerPane.getChildren().addAll(search, tb, viewButton, editButton, deleteButton);
        innerPane.requestFocus();
        outerPane.setCenter(innerPane);

        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new TextField();
        }
        ObservableList<String> doctorStrings;
        doctorStrings = FXCollections.observableArrayList();
        for (int i = 0; i < db.getDoctors().size(); i++) {
            doctorStrings.add(db.getDoctors().get(i).getDoctor_forename());
        }

        ObservableList<String> bedStrings;
        bedStrings = FXCollections.observableArrayList();
        for (int i = 0; i < db.getBeds().size(); i++) {
            bedStrings.add(String.valueOf(db.getBeds().get(i).getBed_id()));
        }

        ComboBox doctorStringsList = new ComboBox(doctorStrings);
        doctorStringsList.setPromptText("Select Doctor:");
        ComboBox bedStringsList = new ComboBox(bedStrings);
        bedStringsList.setPromptText("Select Bed Number:");
        ComboBox[] selects = new ComboBox[]{doctorStringsList, bedStringsList};

        Button button = new Button("Submit");
        button.setOnAction(e -> {
            try {
                addPatientToTable(tb, fields, selects);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        outerPane.setBottom(addTemplate(labels, fields, button, selects));

        Scene scene = new Scene(outerPane, 800, 600);
        return scene;
    }

    public Scene editPrescription(String title, MenuBar menuBar, TextField search, Stage primaryStage) throws SQLException {

        TableView tb = new TableView<>();

        TableColumn<Prescription, Integer> id = new TableColumn("ID");
        createColumnInteger(id, "id");
        id.setOnEditCommit(e -> ((Prescription) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setId(e.getNewValue()));

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

        ObservableList<Prescription> prescriptions = db.getPrescription();
        FilteredList<Prescription> presFilt = new FilteredList<>(prescriptions, pres -> true);

        search.textProperty().addListener((observable, oldValue, newValue) -> {
            presFilt.setPredicate(pres -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(pres.getId()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches first name.
                } else if (pres.getMedication().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches last name.
                } else if (String.valueOf(pres.getFrequency()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                } else if (pres.getTime_to_take_medicine().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                } else if (String.valueOf(pres.isMedicine_taken()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                } else if (String.valueOf(pres.getNumber_of_days()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Prescription> presSorted = new SortedList(presFilt);
        presSorted.comparatorProperty().bind(tb.comparatorProperty());

        tb.setEditable(true);
        if ("".equals(search.getText())) {
            tb.setItems(prescriptions);
        } else {
            tb.setItems(presSorted);
        }

        tb.setEditable(true);
        tb.getColumns().addAll(id, medName, frequency, numDays, timeToTake);

        Button viewButton = new Button("View Data");
        viewButton.setOnAction(e -> primaryStage.setScene(viewData(tb, primaryStage)));

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
        innerPane.getChildren().addAll(search, tb, viewButton, editButton, deleteButton);
        outerPane.setCenter(innerPane);

        Label[] labels = new Label[]{new Label("Medication Name"), new Label("Frequency Per Day"), new Label("Number Of Days"), new Label("Time To Take Medecine"), new Label("Medecine Taken")};
        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new TextField();
        }
        ObservableList<String> patientStrings;
        patientStrings = FXCollections.observableArrayList();
        for (int i = 0; i < db.getPatients().size(); i++) {
            patientStrings.add(db.getPatients().get(i).getForename());
        }

        ComboBox patientStringsList = new ComboBox(patientStrings);
        patientStringsList.setPromptText("Patient:");
        ComboBox[] selects = new ComboBox[]{patientStringsList};

        Button button = new Button("Submit");
        button.setOnAction(e -> {
            try {
                addPrescToTable(tb, fields, selects);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        outerPane.setBottom(addTemplate(labels, fields, button, selects));

        Scene scene = new Scene(outerPane, 800, 600);
        return scene;
    }

    public Scene editWard(String title, MenuBar menuBar, TextField search, Stage primaryStage) throws SQLException {
        TableView tb = new TableView<>();

        TableColumn<Ward, Integer> id = new TableColumn("ID");
        createColumnInteger(id, "ward_id");
        id.setOnEditCommit(e -> ((Ward) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setWard_id(e.getNewValue()));

        TableColumn<Ward, String> wardName = new TableColumn("Ward Name");
        createColumnString(wardName, "ward_name");
        wardName.setOnEditCommit(e -> ((Ward) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setWard_name(e.getNewValue()));

        ObservableList<Ward> wards = db.getWard();
        FilteredList<Ward> wardFilt = new FilteredList<>(wards, ward -> true);

        search.textProperty().addListener((observable, oldValue, newValue) -> {
            wardFilt.setPredicate(ward -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(ward.getWard_id()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches first name.
                } else if (ward.getWard_name().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches last name.
                }
                return false;
            });
        });

        SortedList<Prescription> wardSorted = new SortedList(wardFilt);
        wardSorted.comparatorProperty().bind(tb.comparatorProperty());

        tb.setEditable(true);
        if ("".equals(search.getText())) {
            tb.setItems(wards);
        } else {
            tb.setItems(wardSorted);
        }

        tb.setEditable(true);
        tb.getColumns().addAll(id, wardName);

        Button viewButton = new Button("View Data");
        viewButton.setOnAction(e -> primaryStage.setScene(viewData(tb, primaryStage)));

        Button editButton = new Button("Save");
        editButton.setOnAction(f -> {
            ArrayList<Ward> w = new ArrayList<>();
            for (int i = 0; i < tb.getItems().size(); i++) {
                w.add((Ward) tb.getItems().get(i));
            }
            try {
                db.updateWard(w);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        Label errorLabel = new Label(errorText);

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteWard(tb));

        BorderPane outerPane = new BorderPane();
        outerPane.setTop(menuBar);

        VBox innerPane = new VBox();
        innerPane.getChildren().addAll(search, tb, viewButton, editButton, deleteButton, errorLabel);
        outerPane.setCenter(innerPane);

        Label[] labels = new Label[]{new Label("Ward Name")};
        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new TextField();
        }

        Button button = new Button("Submit");
        button.setOnAction(e -> {
            try {
                addWardToTable(tb, fields, null);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        outerPane.setBottom(addTemplate(labels, fields, button, null));

        Scene scene = new Scene(outerPane, 800, 600);
        return scene;
    }

    public Scene viewPage(Stage primaryStage, ArrayList<Label> labels, ArrayList<Label> data, Object ob, String s) {
        Button backButton = new Button("Back to " + s);
        backButton.setOnAction(e -> {
            try {
                if (ob instanceof Patient) {
                    primaryStage.setScene(editPatient("View Patient", getMenu(primaryStage, getSearchBar()), getSearchBar(), primaryStage));
                } else if (ob instanceof Prescription) {
                    primaryStage.setScene(editPrescription("View Prescription", getMenu(primaryStage, getSearchBar()), getSearchBar(), primaryStage));
                } else if (ob instanceof Bed) {
                    primaryStage.setScene(editBed("View Bed", getMenu(primaryStage, getSearchBar()), getSearchBar(), primaryStage));
                } else if (ob instanceof Doctor) {
                    primaryStage.setScene(editDoctor("View Doctor", getMenu(primaryStage, getSearchBar()), getSearchBar(), primaryStage));
                } else if (ob instanceof Meal) {
                    primaryStage.setScene(editMeal("View Meal", getMenu(primaryStage, getSearchBar()), getSearchBar(), primaryStage));
                } else {
                    primaryStage.setScene(editWard("View Ward", getMenu(primaryStage, getSearchBar()), getSearchBar(), primaryStage));
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });
        GridPane layout = new GridPane();
        layout.add(backButton, 0, 0);
        for (int i = 0; i < labels.size(); i++) {
            layout.add(labels.get(i), 0, (i + 1));
            layout.add(data.get(i), 1, (i + 1));
        }
        layout.setPadding(new Insets(5, 5, 5, 5));
        Scene scene = new Scene(layout, 800, 600);
        return scene;
    }

    public Scene viewData(TableView tb, Stage primaryStage) {
        ArrayList<Label> labelList = new ArrayList<>();
        ArrayList<Label> data = new ArrayList<>();
        Object selected;
        selected = tb.getSelectionModel().getSelectedItem();
        if (selected instanceof Patient) {
            Patient patient = new Patient();

            Label patTitle = new Label("Patient:");
            patTitle.setStyle("-fx-underline: true");

            labelList.add(patTitle); //1
            labelList.addAll(getPatientIdent());//6

            data.add(new Label(""));//1
            data.addAll(getPatientLabels((Patient) selected));//6

            for (int i = 0; i < ((Patient) selected).getPresc().size(); i++) { //x2
                labelList.add(new Label(""));

                Label prestitle = new Label("Prescription:");
                prestitle.setStyle("-fx-underline: true");
                labelList.add(prestitle);

                data.add(new Label(""));
                data.add(new Label(""));

                labelList.addAll(getPrescriptionIdent());//5

                ArrayList<Label> presLab = getPrescriptionLabels(((Patient) selected).getPresc().get(i));
                for (Label presLab1 : presLab) {
                    data.add(presLab1);//5
                }

            }
            for (int i = 0; i < ((Patient) selected).getMeals().size(); i++) { //x2
                labelList.add(new Label(""));

                Label mealtitle = new Label("Meal:");
                mealtitle.setStyle("-fx-underline: true");
                labelList.add(mealtitle);

                data.add(new Label(""));
                data.add(new Label(""));

                labelList.addAll(getMealIdent());//3
                ArrayList<Label> mealLab = getMealLabels(((Patient) selected).getMeals().get(i));
                for (Label mealLab1 : mealLab) {
                    data.add(mealLab1);//3
                }

            }
            return viewPage(primaryStage, labelList, data, patient, "Students");
        } else if (selected instanceof Prescription) {
            Prescription p = new Prescription();
            data.add(new Label(""));
            data.addAll(getPrescriptionLabels((Prescription) selected));
            Label presc = new Label("Prescription:");
            presc.setStyle("-fx-underline: true");
            labelList.add(presc);
            labelList.addAll(getPrescriptionIdent());

            return viewPage(primaryStage, labelList, data, p, "Prescriptions");
        } else if (selected instanceof Bed) {
            Bed b = new Bed();
            data = getBedLabels((Bed) selected);
            data.add(new Label(""));
            Label bed = new Label("Bed:");
            bed.setStyle("-fx-underline: true");
            labelList.add(bed);
            labelList.addAll(getBedIdent());

            Label pat = new Label("Patient:");
            pat.setStyle("-fx-underline: true");
            labelList.add(pat);

            labelList.addAll(getPatientIdent());
            data.add(new Label(""));
            data.addAll(getPatientLabels(((Bed) selected).getPatient_id()));
            return viewPage(primaryStage, labelList, data, b, "Beds");
        } else if (selected instanceof Meal) {
            Meal m = new Meal();
            Label meal = new Label("Meal:");
            meal.setStyle("-fx-underline: true");
            data.add(new Label(""));
            data.addAll(getMealLabels((Meal) selected));
            labelList.add(meal);
            labelList.addAll(getMealIdent());
            return viewPage(primaryStage, labelList, data, m, "Meals");
        } else if (selected instanceof Doctor) {
            Doctor d = new Doctor();
            data.add(new Label(""));
            data.addAll(getDoctorLabels((Doctor) selected));
            Label doc = new Label("Doctor:");
            doc.setStyle("-fx-underline: true");
            labelList.add(doc);
            labelList.addAll(getDoctorIdent());
            for (int i = 0; i < ((Doctor) selected).getPatients().size(); i++) {
                Label pat = new Label("Patient:");
                pat.setStyle("-fx-underline: true");
                labelList.add(pat);
                labelList.addAll(getPatientIdent());
                data.add(new Label(""));
                ArrayList<Label> patLab = getPatientLabels(((Doctor) selected).getPatients().get(i));
                for (Label patLab1 : patLab) {
                    data.add(patLab1);
                }
            }
            return viewPage(primaryStage, labelList, data, d, "Doctors");
        } else {
            Ward w = new Ward();
            Label ward = new Label("Ward");
            ward.setStyle("-fx-underline: true");
            data.add(new Label(""));
            data.addAll(getWardLabels((Ward) selected));
            labelList.add(ward);
            labelList.addAll(getWardIdent());
            for (int i = 0; i < ((Ward) selected).getBeds().size(); i++) {
                Label bed = new Label("Bed:");
                bed.setStyle("-fx-underline: true");
                labelList.add(bed);
                labelList.addAll(getBedIdent());
                data.add(new Label(""));
                ArrayList<Label> bedLab = getBedLabels(((Ward) selected).getBeds().get(i));
                for (Label bedLab1 : bedLab) {
                    data.add(bedLab1);
                }
            }
            return viewPage(primaryStage, labelList, data, w, "Wards");
        }
    }

    public ArrayList<Label> getBedIdent() {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label("Bed Number: "));
        data.add(new Label("Cleaned: "));
        return data;
    }

    public ArrayList<Label> getDoctorIdent() {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label("Doctor Forename: "));
        data.add(new Label("Doctor Surname: "));
        data.add(new Label("Specialisation: "));
        return data;
    }

    public ArrayList<Label> getPatientIdent() {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label("Patient Forename: "));
        data.add(new Label("Patient Surname: "));
        data.add(new Label("Symptoms: "));
        data.add(new Label("Illness: "));
        data.add(new Label("In Queue: "));
        data.add(new Label("Time waiting (minutes): "));
        return data;
    }

    public ArrayList<Label> getPrescriptionIdent() {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label("Medication Name: "));
        data.add(new Label("Frequency Per Day: "));
        data.add(new Label("Number Of Days: "));
        data.add(new Label("Time To Take Medecine: "));
        data.add(new Label("Medecine Taken: "));
        return data;
    }

    public ArrayList<Label> getMealIdent() {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label("Meal Name: "));
        data.add(new Label("Meal Eaten: "));
        data.add(new Label("Meal Time: "));
        return data;
    }

    public ArrayList<Label> getWardIdent() {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label("Ward Name: "));
        return data;
    }

    public ArrayList<Label> getPatientLabels(Patient selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label((selected).getForename()));
        data.add(new Label((selected).getSurname()));
        data.add(new Label((selected).getSymptoms()));
        data.add(new Label((selected).getIllness()));
        data.add(new Label(String.valueOf((selected).isIn_queue())));
        data.add(new Label(String.valueOf((selected).getTime_waiting())));
        return data;
    }

    public ArrayList<Label> getPrescriptionLabels(Prescription selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label(selected.getMedication()));
        data.add(new Label(String.valueOf(selected.getFrequency())));
        data.add(new Label(String.valueOf(selected.getNumber_of_days())));
        data.add(new Label(selected.getTime_to_take_medicine()));
        data.add(new Label(String.valueOf(selected.isMedicine_taken())));
        return data;
    }

    public ArrayList<Label> getBedLabels(Bed selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label(String.valueOf(selected.getBed_id())));
        data.add(new Label(String.valueOf(selected.isCleaned())));
        return data;
    }

    public ArrayList<Label> getMealLabels(Meal selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label(selected.getMeal_name()));
        data.add(new Label(String.valueOf(selected.isEaten())));
        data.add(new Label(selected.getMeal_time()));
        return data;
    }

    public ArrayList<Label> getDoctorLabels(Doctor selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label(selected.getDoctor_forename()));
        data.add(new Label(selected.getDoctor_surname()));
        data.add(new Label(selected.getSpecialisation()));
        return data;
    }

    public ArrayList<Label> getWardLabels(Ward selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label(selected.getWard_name()));
        return data;
    }

    public boolean validateBoolean(String value) {
        return ("Yes".equals(value));
    }

    public boolean validateCorrectInputs(String input) {
        return input.equals("Yes") || input.equals("No");
    }

    public boolean validateTimeInputs(String time) {
        return time.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]");
    }

    public boolean validateIntegerInputs(String number) {
        return number.matches("[0-4][0-9]|[0-9]");
    }

    public void addBedToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        boolean cleaned = false;
        if (validateCorrectInputs(txtFields[0].getText())) {
            cleaned = validateBoolean(txtFields[0].getText());
            ObservableList<Ward> wards = db.getWard();
            int newid = tb.getItems().size() + 1;
            Bed b = new Bed(newid, cleaned, null);
            tb.setItems(db.getBeds());
            tb.getItems().add(b);
            for (Ward w : wards) {
                if (w.getWard_id() == combo[0].getSelectionModel().getSelectedIndex() + 1) {
                    int ward_id = w.getWard_id();
                    db.addBed(cleaned, ward_id);
                }
            }
            for (TextField txtField : txtFields) {
                txtField.setText("");
            }
        } else {
            errorText = "Please enter Yes or No as your input for 'cleaned'";
        }

    }

    public void addDoctorToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        int newid = tb.getItems().size() + 1;
        Doctor d = new Doctor(newid, txtFields[0].getText(), txtFields[1].getText(), txtFields[2].getText(), null);
        //System.out.println("Hello");
        tb.setItems(db.getDoctors());
        tb.getItems().add(d);
        //System.out.println("From the other side");
        db.addDoctor(txtFields[0].getText(), txtFields[1].getText(), txtFields[2].getText());
        for (int i = 0; i < txtFields.length; i++) {
            txtFields[i].setText("");
        }
    }

    public void addMealToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        boolean eaten = false;
        if (validateCorrectInputs(txtFields[1].getText())) {
            eaten = validateBoolean(txtFields[1].getText());
            String time = null;
            if (validateTimeInputs(txtFields[2].getText())) {
                time = txtFields[2].getText();
                ObservableList<Meal> meals = db.getMeals();
                ObservableList<Patient> patients = db.getPatients();
                int newid = tb.getItems().size() + 1;
                Meal m = new Meal(newid, txtFields[0].getText(), eaten, time);
                tb.setItems(meals);
                tb.getItems().add(m);
                for (Patient p : patients) {
                    if (p.getId() == combo[0].getSelectionModel().getSelectedIndex() + 1) {
                        db.addMeal(txtFields[0].getText(), eaten, time, p.getId());
                    }
                }
            } else {
                errorText = "Please enter a time in the 24 hour format e.g. 23:00, 4:35";
            }

        } else {
            errorText = "Please enter Yes or No as your input for 'eaten'";
        }

    }

    public void addPatientToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        boolean inqueue = false;
        if (validateCorrectInputs(txtFields[4].getText())) {
            inqueue = validateBoolean(txtFields[4].getText());
            if (validateIntegerInputs(txtFields[5].getText())) {
                ObservableList<Patient> patients = db.getPatients();
                ObservableList<Bed> beds = db.getBeds();
                ObservableList<Doctor> doctors = db.getDoctors();
                int newid = tb.getItems().size() + 1;

                for (Bed bed : beds) {
                    if (bed.getBed_id() == combo[1].getSelectionModel().getSelectedIndex() + 1) {
                        db.updateBedPatientId(bed, newid);
                    }
                }
                tb.setItems(patients);
                Patient p = new Patient(newid, txtFields[0].getText(), txtFields[1].getText(), txtFields[2].getText(), txtFields[3].getText(), inqueue, Integer.parseInt(txtFields[5].getText()), null, null);
                tb.getItems().add(p);
                for (Doctor doctor : doctors) {
                    if (doctor.getDoctor_id() == combo[1].getSelectionModel().getSelectedIndex() + 1) {
                        db.addPatient(txtFields[0].getText(), txtFields[1].getText(), txtFields[2].getText(), txtFields[3].getText(), Boolean.parseBoolean(txtFields[4].getText()), Integer.parseInt(txtFields[5].getText()), doctor.getDoctor_id());
                    }
                }
            } else {
                errorText = "Please ener a number between 1 and 49 for the time waiting";
            }
        } else {
            errorText = "Please enter Yes or No as your input for 'In Queue'";
        }

    }

    public void addPrescToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        if (validateIntegerInputs(txtFields[1].getText())) {
            if (validateIntegerInputs(txtFields[2].getText())) {
                if (validateTimeInputs(txtFields[3].getText())) {
                    boolean medTaken = false;
                    if (validateCorrectInputs(txtFields[4].getText())) {
                        medTaken = validateBoolean(txtFields[4].getText());
                        ObservableList<Prescription> prescriptions = db.getPrescription();
                        int newid = tb.getItems().size() + 1;
                        Prescription pres = new Prescription(newid, Integer.parseInt(txtFields[1].getText()), Integer.parseInt(txtFields[2].getText()), txtFields[0].getText(), medTaken, String.valueOf(txtFields[3].getText()));
                        tb.setItems(prescriptions);
                        tb.getItems().add(pres);

                        ObservableList<Patient> patients = db.getPatients();

                        for (Patient patient : patients) {
                            if (patient.getId() == combo[0].getSelectionModel().getSelectedIndex() + 1) {
                                db.addPrescripton(txtFields[0].getText(), Integer.parseInt(txtFields[1].getText()), Integer.parseInt(txtFields[2].getText()), String.valueOf(txtFields[3].getText()), medTaken, patient.getId());
                            }
                        }
                        for (TextField txtField : txtFields) {
                            txtField.setText("");
                        }
                    } else {
                        errorText = "Please enter Yes or No in the medicine taken textbox";
                    }
                } else {
                    errorText = "Please enter a time in 24 hour format e.g. 12:20, 21:12 in the time to take medicine textbox";
                }
            } else {
                errorText = "Please enter a number between 1 and 49 in the number of days textbox";
            }
        } else {
            errorText = "Please enter a number between 1 and 49 in the frequency textbox";
        }
    }

    public void addWardToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        ObservableList<Ward> wards = db.getWard();
        int newid = tb.getItems().size() + 1;
        Ward ward = new Ward(newid, txtFields[0].getText(), null);
        tb.setItems(wards);
        tb.getItems().add(ward);
        db.addWard(txtFields[0].getText());
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

    public void createColumnDoctor(TableColumn tc) throws SQLException {
        ObservableList<Doctor> doctors = db.getDoctors();
        ObservableList<String> doctorStrings = FXCollections.observableArrayList();
        for (Doctor doctor : doctors) {
            doctorStrings.add(doctor.getDoctor_forename());
        }
        //ComboBox list = new ComboBox(doctorStrings);
        tc.setCellValueFactory(new PropertyValueFactory<>("doctor_forename"));
        tc.setCellFactory(ComboBoxTableCell.<Doctor, String>forTableColumn(doctorStrings));

    }

    public void deleteBed(TableView tb) {
        ObservableList<Bed> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deleteBed(selected);
        selected.forEach(all::remove);
    }

    public void deleteDoctor(TableView tb) {
        ObservableList<Doctor> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deleteDoctor(selected);
        selected.forEach(all::remove);
    }

    public void deleteMeal(TableView tb) {
        ObservableList<Meal> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deleteMeal(selected);
        selected.forEach(all::remove);
    }

    public void deletePatient(TableView tb) {
        ObservableList<Patient> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deletePatient(selected);
        selected.forEach(all::remove);
    }

    public void deletePrescription(TableView tb) {
        ObservableList<Prescription> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deletePrescription(selected);
        selected.forEach(all::remove);
    }

    public void deleteWard(TableView tb) {
        ObservableList<Ward> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deleteWard(selected);
        selected.forEach(all::remove);
    }

    public GregorianCalendar stringToGreg(String date) {
        String[] hoursAndMins = date.split(":");

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int eventHour = Integer.parseInt(hoursAndMins[0]);
        int eventMinute = Integer.parseInt(hoursAndMins[1]);

        GregorianCalendar eventDate = new GregorianCalendar(currentYear, currentMonth, currentDay, eventHour, eventMinute);
        return eventDate;
    }

    public long stringToDate(String date) {
        long timeMillis = stringToGreg(date).getTime().getTime();
        return timeMillis;
    }

    public GregorianCalendar currentDateGreg() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);

        GregorianCalendar currentDate = new GregorianCalendar(currentYear, currentMonth, currentDay, currentHour, currentMinute);
        return currentDate;
    }

    public long getCurrentDate() {
        long currentMillis = currentDateGreg().getTime().getTime();
        return currentMillis;
    }

    public void calculateDate() throws ParseException, SQLException, IOException {
        long difference = 0;
        ObservableList<Patient> pats = db.getPatients();
        for (Patient p : pats) {
            for (Prescription pre : p.getPresc()) {
                if (pre.isMedicine_taken() == false) {
                    difference = stringToDate(pre.getTime_to_take_medicine()) - getCurrentDate();
                    if (difference < 0) {
                        writeReport(p, pre);
                    } else {
                        gregTimes.add(difference);
                    }
                }
            }
            for (Meal m : p.getMeals()) {
                difference = stringToDate(m.getMeal_time()) - getCurrentDate();
                if (difference < 0) {
                    writeReport(p, m);
                } else {
                    gregTimes.add(difference);
                }
            }
        }

        if (!gregTimes.isEmpty()) {
            Collections.sort(gregTimes);
            timer();
        }
        //Implement it so that each element of the list is the value take away the
        //previous value.
    }

    public void createReport() throws IOException, SQLException {
        ObservableList<Patient> patients = db.getPatients();
        ObservableList<Patient> patients2 = FXCollections.observableArrayList();
        ObservableList<Prescription> pres = FXCollections.observableArrayList();
        ObservableList<Meal> meals = FXCollections.observableArrayList();
        for (Patient p : patients) {
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

    public void writeReport(Patient pat, Object ob2) throws IOException {
        List<String> content = new ArrayList<>();
        Path p;
        if (ob2 instanceof Prescription) {
            content = Arrays.asList(
                    "Patient: " + pat.getForename() + " " + pat.getSurname() + " has missed their scheduled "
                    + "prescription of " + ((Prescription) ob2).getMedication() + " at " + ((Prescription) ob2).getTime_to_take_medicine(),
                    "");
            p = Paths.get(pat.getForename() + "-" + ((Prescription) ob2).getMedication() + "-" + gregorianToString(currentDateGreg()) + ".txt");
        } else {
            content = Arrays.asList(
                    "Patient: " + pat.getForename() + " " + pat.getSurname() + " has missed their scheduled"
                    + "meal of " + ((Meal) ob2).getMeal_name() + " at " + ((Meal) ob2).getMeal_time(),
                    "");
            p = Paths.get(pat.getForename() + "-" + ((Meal) ob2).getMeal_name() + "-" + gregorianToString(currentDateGreg()) + ".txt");
        }

        Files.write(p, content, Charset.forName("UTF-8"));
    }

    public String gregorianToString(GregorianCalendar gc) {
        SimpleDateFormat sd = new SimpleDateFormat("HH-mm, dd-MM-YYYY");
        sd.setCalendar(gc);
        String date = sd.format(gc.getTime());
        return date;
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
            try {
                createReport();
                if (gregTimes.size() > 1) {
                    changeInterval();
                }
            } catch (IOException | SQLException ex) {
                System.out.println(ex);
            }
        };
        //Make it so that this runs after each interval in the array
        s = Executors.newScheduledThreadPool(1);
        s.schedule(runner, gregTimes.get(0), TimeUnit.MILLISECONDS);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
