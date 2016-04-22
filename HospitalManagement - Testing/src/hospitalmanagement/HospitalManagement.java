/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hospitalmanagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 *
 * @author nathan ward
 */
public class HospitalManagement extends Application {

    Connection conn = null;
    DatabaseAccess db;
    ArrayList<Long> gregTimes = new ArrayList<>();
    ReportCreator rc;
    String errorText;
    String title;
    DesignWard dw = new DesignWard();

    /**
     *
     * @throws SQLException
     */
    public HospitalManagement() throws SQLException {
    }

    /**
     * This is the first function that is run once the program has started.
     * @param primaryStage
     * @throws Exception
     * @throws SQLException 
     */
    @Override
    public void start(Stage primaryStage) throws Exception, SQLException {

        //Initialises objects
        rc = new ReportCreator();
        db = new DatabaseAccess();
        //Automatically creates reports or adds data to the time list.
        rc.calculateDate();
        //Set the title of the window
        this.title = "Hospital Management";
        primaryStage.setTitle(title);
        //Displays the login screen first.
        primaryStage.setScene(loginPage(primaryStage, editPatient("View Patients", getMenu(primaryStage, getSearchBar()), getSearchBar(), primaryStage)));
        primaryStage.show();

        //Stops the timer once the program window has been exited.
        primaryStage.setOnCloseRequest(e -> {
            if (!gregTimes.isEmpty()) {
                rc.getScheduled().shutdown();
            }

            primaryStage.close();
            System.exit(0);
        });
    }

    /**
     * This function is used to insert the search bar in all the pages.
     * @return a search bar.
     */
    public TextField getSearchBar() {
        TextField searchField = new TextField();
        searchField.setPromptText("Search:");
        searchField.setText("");
        return searchField;
    }

    /**
     * This creates a menu and all of the links to different pages.
     * @param primaryStage
     * @param searchField
     * @return a menu
     */
    public MenuBar getMenu(Stage primaryStage, TextField searchField) {
        //Menu
        MenuItem viewPatient = new MenuItem("Patient");
        MenuItem viewPrescription = new MenuItem("Prescription");
        MenuItem viewBed = new MenuItem("Bed");
        MenuItem viewWard = new MenuItem("Ward");
        MenuItem viewDoctor = new MenuItem("Doctor");
        MenuItem viewMeal = new MenuItem("Meal");
        MenuItem viewReports = new MenuItem("Reports");

        MenuItem designWard = new MenuItem("Ward");

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> primaryStage.close());


        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(exit);

        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(viewPatient, viewPrescription, viewBed, viewWard, viewDoctor, viewMeal, viewReports);

        Menu designMenu = new Menu("Design");
        designMenu.getItems().addAll(designWard);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, viewMenu, designMenu);

        //Menu actions
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
        
        viewReports.setOnAction(e -> {
            try {
                primaryStage.setScene(viewFiles("View Reports", menuBar, primaryStage));
            } catch (IOException ex) {
                System.out.println(ex);
            }
        });

        designMenu.setOnAction(e -> {
            primaryStage.setScene(dw.createToolbar());
        });
        return menuBar;
    }

    /**
     *
     * @param labels
     * @param fields
     * @param button
     * @param select
     * @return A layout of the adding form for each entity (patient, doctor etc).
     */
    public VBox addTemplate(Label[] labels, TextField[] fields, Button button, ComboBox[] select) {
        VBox add = new VBox();
        //Adds a combo box if it is required.
        if (select != null) {
            add.getChildren().addAll(Arrays.asList(select));
        }
        //Adds labels and text boxes
        for (int i = 0; i < labels.length; i++) {
            add.getChildren().add(labels[i]);
            add.getChildren().add(fields[i]);
        }

        add.getChildren().add(button);
        return add;
    }

    /**
     *
     * @param primaryStage
     * @param home
     * @return the login screen page.
     */
    public Scene loginPage(Stage primaryStage, Scene home) {
        Label userLabel = new Label("Username:");
        TextField username = new TextField();
        Label passLabel = new Label("Password");
        PasswordField password = new PasswordField();

        Label error = new Label();

        //Checks to see if login details exist otherwise display error text
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

        //Set position of all nodes.
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

    /**
     *
     * @param title
     * @param menuBar
     * @param search
     * @param primaryStage
     * @return the edit bed page.
     * @throws SQLException
     */
    public Scene editBed(String title, MenuBar menuBar, TextField search, Stage primaryStage) throws SQLException {
        TableView tb = new TableView<>();
        
        //Create the table columns and make set functionality for when editing
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

        //Sets search bar functionality
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

        
        //Set table data to the filtered data
        SortedList<Bed> bedSorted = new SortedList(bedsFilt);
        bedSorted.comparatorProperty().bind(tb.comparatorProperty());

        //Set the table data to the normal data so functionality can be performed on it.
        tb.setItems(beds);

        tb.setEditable(true);
        tb.getColumns().addAll(id, cleaned);

        //Adds functionality for viewing a beds data.
        Button viewButton = new Button("View Data");
        viewButton.setOnAction(e -> {
            try {
                primaryStage.setScene(viewData(tb, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });
        
        //Adds functionality for updating data in table.
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

        //Functionality for deleting bed
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteBed(tb));

        BorderPane outerPane = new BorderPane();
        outerPane.setTop(menuBar);

        VBox innerPane = new VBox();
        innerPane.getChildren().addAll(search, tb, viewButton, editButton, deleteButton);
        innerPane.requestFocus();
        outerPane.setCenter(innerPane);

        //Data to be sent to add template
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

        //Setting add template button functionality
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

    /**
     *
     * @param title
     * @param menuBar
     * @param search
     * @param primaryStage
     * @return the edit doctor page.
     * @throws SQLException
     */
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
        viewButton.setOnAction(e -> {
            try {
                primaryStage.setScene(viewData(tb, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

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

    /**
     *
     * @param title
     * @param menuBar
     * @param search
     * @param primaryStage
     * @return the edit meal page.
     * @throws SQLException
     */
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
        viewButton.setOnAction(e -> {
            try {
                primaryStage.setScene(viewData(tb, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

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

    /**
     *
     * @param title
     * @param menuBar
     * @param search
     * @param primaryStage
     * @return the edit patient page
     * @throws SQLException
     */
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

        TableColumn<Patient, String> timeArrived = new TableColumn("Time Arrived");
        createColumnString(timeArrived, "timeArrived");
        timeArrived.setOnEditCommit(e -> ((Patient) e.getTableView().getItems().get(
                e.getTablePosition().getRow())).setTimeArrived(e.getNewValue()));
        
        
        
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
                } else if (String.valueOf(pat.getTimeArrived()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Patient> patSorted = new SortedList(patientsFilt);
        patSorted.comparatorProperty().bind(tb.comparatorProperty());

        if ("".equals(search.getText())) {
            tb.setItems(patients);
            System.out.println("Normal list");
        } else {
            tb.setItems(patSorted);
            System.out.println("Sorted list");
        }

        tb.setEditable(true);
        tb.getColumns().addAll(id, forename, surname, symptoms, illness, inQueue, timeArrived);
        //for (int i = 0; i < tb.getItems().size(); i++) {
            //Patient selected = timeArrived.getTableView().getItems().get(i);
            //if (rc.stringToDate(selected.getTimeArrived()) < 14400000) {
                
            //}
        //}

        Label[] labels = new Label[]{new Label("Patient Forename"), new Label("Patient Surname"), new Label("Symptoms"), new Label("Illness"), new Label("In Queue"), new Label("Time waiting (minutes)")};

        Button viewButton = new Button("View Data");
        viewButton.setTooltip(new Tooltip("View selected Patient"));
        viewButton.setOnAction(e -> {
            try {
                primaryStage.setScene(viewData(tb, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

        Button editButton = new Button("Save");
        editButton.setTooltip(new Tooltip("Edit selected Patient"));
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
        deleteButton.setTooltip(new Tooltip("Remove selected Patient"));
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
        button.setTooltip(new Tooltip("Create Patient"));
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

    /**
     *
     * @param title
     * @param menuBar
     * @param search
     * @param primaryStage
     * @return the edit prescription page
     * @throws SQLException
     */
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
        viewButton.setOnAction(e -> {
            try {
                primaryStage.setScene(viewData(tb, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

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

    /**
     *
     * @param title
     * @param menuBar
     * @param search
     * @param primaryStage
     * @return the edit ward page
     * @throws SQLException
     */
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
        viewButton.setOnAction(e -> {
            try {
                primaryStage.setScene(viewData(tb, primaryStage));
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });

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

    /**
     *
     * @param primaryStage
     * @param labels
     * @param data
     * @param ob
     * @param s
     * @return the page for viewing data about objects e.g. patients, doctors etc.
     * @throws SQLException
     */
    public Scene viewPage(Stage primaryStage, ArrayList<Label> labels, ArrayList<Label> data, Object ob, String s) throws SQLException {
        Button backButton = new Button("Back to " + s);
        //Sets the back button when viewing object for the selected object
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
        //Adds specific functionality to the view bed page.
        if (ob instanceof Bed) {
            ComboBox pats = new ComboBox();
            Button addPatient = new Button("Add Patient");
            ObservableList<Patient> patients = db.getPatients();
            ObservableList<Bed> beds = db.getBeds();
            ObservableList<String> patStrings = FXCollections.observableArrayList();
            boolean hasBed = false;
            for (Patient p : patients) {
                for (Bed b : beds) {
                    if (b.getPatient_id() == p) {
                        hasBed = true;
                    }
                }
                if (hasBed == false) {
                    patStrings.add(p.getForename());
                }
            }
            pats.getItems().addAll(patStrings);
            //Functionality for removing patient from a bed.
            Button removePatient = new Button("Remove Patient");
            removePatient.setOnAction(e -> {
                try {
                    db.removePatientFromBed((Bed) ob);
                    primaryStage.setScene(editBed("View Bed", getMenu(primaryStage, getSearchBar()), getSearchBar() , primaryStage));
                } catch (SQLException ex) {
                    System.out.println(ex);
                }
            });
            layout.add(pats, 0, labels.size()+1);
            layout.add(removePatient, 0, labels.size()+2);
        } else if (ob instanceof Patient) {
            //Functionality for removing a doctor from a patient.
            Button removeDoctor = new Button("Remove Doctor");
            removeDoctor.setOnAction(e -> {
                try {
                    ObservableList<Doctor> doctors = db.getDoctors();
                    for (Doctor doctor : doctors) {
                        for (int j = 0; j < doctor.getPatients().size(); j++) {
                            if (doctor.getPatients().get(j).getId() == ((Patient) ob).getId()) {
                                db.removePatientFromDoctor(doctor);
                                primaryStage.setScene(editPatient("View Patient", getMenu(primaryStage, getSearchBar()), getSearchBar() , primaryStage));
                            }
                        }
                    }
                } catch (SQLException ex) {
                    System.out.println(ex);
                }
            });
            layout.add(removeDoctor, 0, labels.size()+1);
        }
        layout.setPadding(new Insets(5, 5, 5, 5));
        Scene scene = new Scene(layout, 800, 600);
        return scene;
    }

    /**
     *
     * @param tb
     * @param primaryStage
     * @return the data that will be included on the view page function.
     * @throws SQLException
     */
    public Scene viewData(TableView tb, Stage primaryStage) throws SQLException {
        ArrayList<Label> labelList = new ArrayList<>();
        ArrayList<Label> data = new ArrayList<>();
        Object selected;
        selected = tb.getSelectionModel().getSelectedItem();
        if (selected instanceof Patient) {
            //Sets the layout for the view patient page
            Patient patient = (Patient) selected;

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
        //Sets the layout for the view prescription page    
        } else if (selected instanceof Prescription) {
            Prescription p =(Prescription) selected;
            data.add(new Label(""));
            data.addAll(getPrescriptionLabels((Prescription) selected));
            Label presc = new Label("Prescription:");
            presc.setStyle("-fx-underline: true");
            labelList.add(presc);
            labelList.addAll(getPrescriptionIdent());

            return viewPage(primaryStage, labelList, data, p, "Prescriptions");
        //Sets the layout for the view bed page
        } else if (selected instanceof Bed) {
            Bed b = (Bed) selected;
            data = getBedLabels((Bed) selected);
            data.add(new Label(""));
            Label bed = new Label("Bed:");
            bed.setStyle("-fx-underline: true");
            labelList.add(bed);
            labelList.addAll(getBedIdent());
            if (b.getPatient_id() != null) {
                Label pat = new Label("Patient:");
                pat.setStyle("-fx-underline: true");
                labelList.add(pat);

                labelList.addAll(getPatientIdent());
                data.add(new Label(""));
                data.addAll(getPatientLabels(((Bed) selected).getPatient_id()));
            } else {
                labelList.add(new Label("This bed is empty"));
                data.add(new Label());
            }
            return viewPage(primaryStage, labelList, data, b, "Beds");
        //Sets the layout for the view meal page.
        } else if (selected instanceof Meal) {
            Meal m = (Meal) selected;
            Label meal = new Label("Meal:");
            meal.setStyle("-fx-underline: true");
            data.add(new Label(""));
            data.addAll(getMealLabels((Meal) selected));
            labelList.add(meal);
            labelList.addAll(getMealIdent());
            return viewPage(primaryStage, labelList, data, m, "Meals");
        //Sets the layout for the view doctor page
        } else if (selected instanceof Doctor) {
            Doctor d = (Doctor) selected;
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
            //Sets the layout for the view ward page
            Ward w = (Ward) selected;
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
    
    /**
     *
     * @param title
     * @param menu
     * @param primaryStage
     * @return a page showing the reports created, in a list
     * @throws IOException
     */
    public Scene viewFiles(String title, MenuBar menu, Stage primaryStage) throws IOException {
        ObservableList<String> files = rc.getAllReports();
        ComboBox listOfFiles = new ComboBox(files);
        listOfFiles.setPromptText("Select a report:");
        Button view = new Button("View Data");
        
        final Label data = new Label();
        
        //Finds chosen report in the 'reports' folder
        view.setOnAction(e -> {
            String selectedFile = String.valueOf(listOfFiles.getSelectionModel().getSelectedItem());
            Path p = Paths.get("src/reports/"+selectedFile);
            List<String> dataFromFile = null;
            //Reads all text from the file and puts it together.
            try {
                dataFromFile = Files.readAllLines(p);
            } catch (IOException ex) {
                System.out.println(ex);
            }
            String allData = "";
            for (String s : dataFromFile) {
                allData = allData.concat(s);
            }
            data.setText(allData);
        });
        
        //Finds the chosen file and removes it from the reports folder
        Button remove = new Button("Remove Selected");
        remove.setOnAction(e -> {
            String selectedFile = String.valueOf(listOfFiles.getSelectionModel().getSelectedItem());
            Path p = Paths.get("src/reports/"+selectedFile);
            try {
                Files.delete(p);
                primaryStage.setScene(viewFiles("View Reports", getMenu(primaryStage, getSearchBar()), primaryStage));
            } catch (IOException ex) {
                System.out.println(ex);
            }
        });
        
        BorderPane page = new BorderPane();
        
        GridPane layout = new GridPane();
        
        layout.add(listOfFiles, 0, 0);
        layout.add(view, 0, 1);
        layout.add(remove, 0, 2);
        layout.add(data, 0, 3);
        
        page.setCenter(layout);
        page.setTop(menu);
        Scene scene = new Scene(page, 800, 600);
        
        return scene;
    }

    /**
     *
     * @return the list of bed labels
     */
    public ArrayList<Label> getBedIdent() {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label("Bed Number: "));
        data.add(new Label("Cleaned: "));
        return data;
    }

    /**
     *
     * @return the list of doctor labels
     */
    public ArrayList<Label> getDoctorIdent() {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label("Doctor Forename: "));
        data.add(new Label("Doctor Surname: "));
        data.add(new Label("Specialisation: "));
        return data;
    }

    /**
     *
     * @return the list of patient labels
     */
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

    /**
     *
     * @return the list of prescription labels
     */
    public ArrayList<Label> getPrescriptionIdent() {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label("Medication Name: "));
        data.add(new Label("Frequency Per Day: "));
        data.add(new Label("Number Of Days: "));
        data.add(new Label("Time To Take Medecine: "));
        data.add(new Label("Medecine Taken: "));
        return data;
    }

    /**
     *
     * @return the list of meal labels
     */
    public ArrayList<Label> getMealIdent() {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label("Meal Name: "));
        data.add(new Label("Meal Eaten: "));
        data.add(new Label("Meal Time: "));
        return data;
    }

    /**
     *
     * @return the list of ward labels
     */
    public ArrayList<Label> getWardIdent() {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label("Ward Name: "));
        return data;
    }

    /**
     *
     * @param selected
     * @return the list of patient data
     */
    public ArrayList<Label> getPatientLabels(Patient selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label((selected).getForename()));
        data.add(new Label((selected).getSurname()));
        data.add(new Label((selected).getSymptoms()));
        data.add(new Label((selected).getIllness()));
        data.add(new Label(String.valueOf((selected).isIn_queue())));
        data.add(new Label(String.valueOf((selected).getTimeArrived())));
        return data;
    }

    /**
     *
     * @param selected
     * @return the list of prescription data
     */
    public ArrayList<Label> getPrescriptionLabels(Prescription selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label(selected.getMedication()));
        data.add(new Label(String.valueOf(selected.getFrequency())));
        data.add(new Label(String.valueOf(selected.getNumber_of_days())));
        data.add(new Label(selected.getTime_to_take_medicine()));
        data.add(new Label(String.valueOf(selected.isMedicine_taken())));
        return data;
    }

    /**
     *
     * @param selected
     * @return the list of bed data
     */
    public ArrayList<Label> getBedLabels(Bed selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label(String.valueOf(selected.getBed_id())));
        data.add(new Label(String.valueOf(selected.isCleaned())));
        return data;
    }

    /**
     *
     * @param selected
     * @return the list of meal data
     */
    public ArrayList<Label> getMealLabels(Meal selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label(selected.getMeal_name()));
        data.add(new Label(String.valueOf(selected.isEaten())));
        data.add(new Label(selected.getMeal_time()));
        return data;
    }

    /**
     *
     * @param selected
     * @return the list of doctor data
     */
    public ArrayList<Label> getDoctorLabels(Doctor selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label(selected.getDoctor_forename()));
        data.add(new Label(selected.getDoctor_surname()));
        data.add(new Label(selected.getSpecialisation()));
        return data;
    }

    /**
     *
     * @param selected
     * @return the list of ward labels
     */
    public ArrayList<Label> getWardLabels(Ward selected) {
        ArrayList<Label> data = new ArrayList<>();
        data.add(new Label(selected.getWard_name()));
        return data;
    }

    /**
     *
     * @param value
     * @return the conversion of a string to boolean
     */
    public boolean validateBoolean(String value) {
        return ("Yes".equals(value));
    }

    /**
     *
     * @param input
     * @return whether the input is valid ('Yes' or 'No').
     */
    public boolean validateCorrectInputs(String input) {
        return input.equals("Yes") || input.equals("No");
    }

    /**
     *
     * @param time
     * @return whether the input matches the specified regular expression.
     */
    public boolean validateTimeInputs(String time) {
        return time.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]");
    }

    /**
     *
     * @param number
     * @return whether the input matches the specified regular expression.
     */
    public boolean validateIntegerInputs(String number) {
        return number.matches("[0-4][0-9]|[0-9]");
    }

    /**
     * This function takes the data from inserting a bed and adds it to the database and table.
     * @param tb
     * @param txtFields
     * @param combo
     * @throws SQLException
     */
    public void addBedToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        boolean cleaned = false;
        //Validates inputs from adding a bed
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

    /**
     * This function takes the data from adding a doctor and adds it to the database and the table.
     * @param tb
     * @param txtFields
     * @param combo
     * @throws SQLException
     */
    public void addDoctorToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        int newid = tb.getItems().size() + 1;
        Doctor d = new Doctor(newid, txtFields[0].getText(), txtFields[1].getText(), txtFields[2].getText(), null);
        tb.setItems(db.getDoctors());
        tb.getItems().add(d);
        db.addDoctor(txtFields[0].getText(), txtFields[1].getText(), txtFields[2].getText());
        for (TextField txtField : txtFields) {
            txtField.setText("");
        }
    }

    /**
     * This function adds a meal to the database and the table
     * @param tb
     * @param txtFields
     * @param combo
     * @throws SQLException
     */
    public void addMealToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        boolean eaten = false;
        //Validates inputs from add meal
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

    /**
     * This function adds a patient to the table and database
     * @param tb
     * @param txtFields
     * @param combo
     * @throws SQLException
     */
    public void addPatientToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        boolean inqueue = false;
        //Validates inputs from add patient
        if (validateCorrectInputs(txtFields[4].getText())) {
            inqueue = validateBoolean(txtFields[4].getText());
            if (validateTimeInputs(txtFields[5].getText())) {
                ObservableList<Patient> patients = db.getPatients();
                ObservableList<Bed> beds = db.getBeds();
                ObservableList<Doctor> doctors = db.getDoctors();
                int newid = tb.getItems().size() + 1;

                for (Bed bed : beds) {
                    if (bed.getBed_id() == combo[1].getSelectionModel().getSelectedIndex() + 1) {
                        db.updateBedPatientId(bed, newid);
                    } else if (combo[1] == null) {
                        inqueue = false;
                    }
                }
                tb.setItems(patients);
                Patient p = new Patient(newid, txtFields[0].getText(), txtFields[1].getText(), txtFields[2].getText(), txtFields[3].getText(), inqueue, txtFields[5].getText(), null, null);
                tb.getItems().add(p);
                for (Doctor doctor : doctors) {
                    if (doctor.getDoctor_id() == combo[1].getSelectionModel().getSelectedIndex() + 1) {
                        db.addPatient(txtFields[0].getText(), txtFields[1].getText(), txtFields[2].getText(), txtFields[3].getText(), Boolean.parseBoolean(txtFields[4].getText()), txtFields[5].getText(), doctor.getDoctor_id());
                    }
                }
            } else {
                errorText = "Please ener a number between 1 and 49 for the time waiting";
            }
        } else {
            errorText = "Please enter Yes or No as your input for 'In Queue'";
        }

    }

    /**
     * This function adds a prescription to the table and database
     * @param tb
     * @param txtFields
     * @param combo
     * @throws SQLException
     */
    public void addPrescToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        //Validates inputs from add prescription
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

    /**
     * This function adds a ward to a table and database.
     * @param tb
     * @param txtFields
     * @param combo
     * @throws SQLException
     */
    public void addWardToTable(TableView tb, TextField[] txtFields, ComboBox[] combo) throws SQLException {
        ObservableList<Ward> wards = db.getWard();
        int newid = tb.getItems().size() + 1;
        Ward ward = new Ward(newid, txtFields[0].getText(), null);
        tb.setItems(wards);
        tb.getItems().add(ward);
        db.addWard(txtFields[0].getText());
    }

    /**
     * This function sets the data type of a table column as integer and sets the data. 
     * @param tc
     * @param source
     */
    public void createColumnInteger(TableColumn tc, String source) {
        tc.setCellValueFactory(new PropertyValueFactory<>(source));
        tc.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
    }

    /**
     * This function sets the data type of a table column as string and sets the data.
     * @param tc
     * @param source
     */
    public void createColumnString(TableColumn tc, String source) {
        tc.setCellValueFactory(new PropertyValueFactory<>(source));
        tc.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    /**
     * This function sets the data type of a table column as boolean and sets the data.
     * @param tc
     * @param source
     */
    public void createColumnBoolean(TableColumn tc, String source) {
        tc.setCellValueFactory(new PropertyValueFactory<>(source));
        tc.setCellFactory(TextFieldTableCell.forTableColumn(new BooleanStringConverter()));
    }

    /**
     * This function removes a bed from the table and the database.
     * @param tb
     */
    public void deleteBed(TableView tb) {
        ObservableList<Bed> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deleteBed(selected);
        selected.forEach(all::remove);
    }

    /**
     * This function removes a doctor from the table and the database.
     * @param tb
     */
    public void deleteDoctor(TableView tb) {
        ObservableList<Doctor> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deleteDoctor(selected);
        selected.forEach(all::remove);
    }

    /**
     * This function removes a meal from the table and the database.
     * @param tb
     */
    public void deleteMeal(TableView tb) {
        ObservableList<Meal> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deleteMeal(selected);
        selected.forEach(all::remove);
    }

    /**
     * This function removes a patient from the table and the database.
     * @param tb
     */
    public void deletePatient(TableView tb) {
        ObservableList<Patient> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deletePatient(selected);
        selected.forEach(all::remove);
    }

    /**
     * This function removes a prescription from the table and the database.
     * @param tb
     */
    public void deletePrescription(TableView tb) {
        ObservableList<Prescription> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deletePrescription(selected);
        selected.forEach(all::remove);
    }

    /**
     * This function removes a ward from the table and the database.
     * @param tb
     */
    public void deleteWard(TableView tb) {
        ObservableList<Ward> selected, all;
        all = tb.getItems();
        selected = tb.getSelectionModel().getSelectedItems();
        db.deleteWard(selected);
        selected.forEach(all::remove);
    }
    
    /**
     *
     * @return the list of times
     */
    public ArrayList<Long> getGregTimes() {
        return gregTimes;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
