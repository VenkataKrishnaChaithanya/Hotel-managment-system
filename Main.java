import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.beans.property.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class Main extends Application {

    // ===================== DATA MODELS =====================

    public static class Room {
        private final IntegerProperty roomNumber = new SimpleIntegerProperty();
        private final StringProperty type = new SimpleStringProperty();
        private final DoubleProperty price = new SimpleDoubleProperty();
        private final BooleanProperty available = new SimpleBooleanProperty(true);

        public Room(int roomNumber, String type, double price, boolean available) {
            this.roomNumber.set(roomNumber);
            this.type.set(type);
            this.price.set(price);
            this.available.set(available);
        }

        public int getRoomNumber() { return roomNumber.get(); }
        public IntegerProperty roomNumberProperty() { return roomNumber; }
        public String getType() { return type.get(); }
        public StringProperty typeProperty() { return type; }
        public double getPrice() { return price.get(); }
        public DoubleProperty priceProperty() { return price; }
        public boolean isAvailable() { return available.get(); }
        public BooleanProperty availableProperty() { return available; }
        public void setAvailable(boolean v) { available.set(v); }
        public String getStatus() { return available.get() ? "[OK] Available" : "[X] Booked"; }
        public StringProperty statusProperty() { return new SimpleStringProperty(getStatus()); }
    }

    public static class Customer {
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty contact = new SimpleStringProperty();
        private final IntegerProperty roomNumber = new SimpleIntegerProperty();
        private final StringProperty checkIn = new SimpleStringProperty();
        private final DoubleProperty totalBill = new SimpleDoubleProperty();

        public Customer(String name, String contact, int roomNumber, String checkIn, double totalBill) {
            this.name.set(name);
            this.contact.set(contact);
            this.roomNumber.set(roomNumber);
            this.checkIn.set(checkIn);
            this.totalBill.set(totalBill);
        }

        public String getName() { return name.get(); }
        public StringProperty nameProperty() { return name; }
        public String getContact() { return contact.get(); }
        public StringProperty contactProperty() { return contact; }
        public int getRoomNumber() { return roomNumber.get(); }
        public IntegerProperty roomNumberProperty() { return roomNumber; }
        public String getCheckIn() { return checkIn.get(); }
        public StringProperty checkInProperty() { return checkIn; }
        public double getTotalBill() { return totalBill.get(); }
        public DoubleProperty totalBillProperty() { return totalBill; }
    }

    // ===================== STATE =====================

    ObservableList<Room> rooms = FXCollections.observableArrayList();
    ObservableList<Customer> customers = FXCollections.observableArrayList();
    HashMap<Integer, Room> roomMap = new HashMap<>();

    static final String ROOMS_FILE     = "hotel_rooms.csv";
    static final String CUSTOMERS_FILE = "hotel_customers.csv";
    static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ===================== STYLESHEET =====================

    String cssContent() {
        return
            ".root {\n" +
            "    -fx-background-color: #14392F;\n" +
            "    -fx-font-family: 'Georgia';\n" +
            "}\n" +
            ".tab-pane {\n" +
            "    -fx-background-color: #14392F;\n" +
            "}\n" +
            ".tab {\n" +
            "    -fx-background-color: #24593D;\n" +
            "    -fx-padding: 8 20 8 20;\n" +
            "}\n" +
            ".tab:selected {\n" +
            "    -fx-background-color: #F6F6E3;\n" +
            "}\n" +
            ".tab .tab-label {\n" +
            "    -fx-text-fill: #F6F6E3;\n" +
            "}\n" +
            ".tab:selected .tab-label {\n" +
            "    -fx-text-fill: #14392F;\n" +
            "    -fx-font-weight: bold;\n" +
            "}\n" +
            ".card {\n" +
            "    -fx-background-color: #24593D;\n" +
            "    -fx-background-radius: 10;\n" +
            "    -fx-padding: 20;\n" +
            "}\n" +
            ".section-title {\n" +
            "    -fx-text-fill: #F6F6E3;\n" +
            "    -fx-font-size: 16px;\n" +
            "    -fx-font-weight: bold;\n" +
            "}\n" +
            ".field-label {\n" +
            "    -fx-text-fill: #F8F6E0;\n" +
            "}\n" +
            ".text-field {\n" +
            "    -fx-background-color: #14392F;\n" +
            "    -fx-text-fill: #FFFFFF;\n" +
            "    -fx-prompt-text-fill: #7a9e6e;\n" +
            "    -fx-border-color: #3a6e52;\n" +
            "    -fx-border-radius: 4;\n" +
            "    -fx-background-radius: 4;\n" +
            "}\n" +
            ".combo-box {\n" +
            "    -fx-background-color: #14392F;\n" +
            "    -fx-border-color: #3a6e52;\n" +
            "    -fx-border-radius: 4;\n" +
            "}\n" +
            ".combo-box .list-cell {\n" +
            "    -fx-text-fill: #FFFFFF;\n" +
            "    -fx-background-color: #14392F;\n" +
            "}\n" +
            ".combo-box-popup .list-view {\n" +
            "    -fx-background-color: #14392F;\n" +
            "    -fx-border-color: #3a6e52;\n" +
            "}\n" +
            ".combo-box-popup .list-view .list-cell {\n" +
            "    -fx-text-fill: #F6F6E3;\n" +
            "    -fx-background-color: #14392F;\n" +
            "}\n" +
            ".combo-box-popup .list-view .list-cell:hover {\n" +
            "    -fx-background-color: #24593D;\n" +
            "}\n" +
            ".btn-primary {\n" +
            "    -fx-background-color: #F6F6E3;\n" +
            "    -fx-text-fill: #14392F;\n" +
            "    -fx-font-weight: bold;\n" +
            "    -fx-background-radius: 5;\n" +
            "    -fx-padding: 7 16 7 16;\n" +
            "    -fx-cursor: hand;\n" +
            "}\n" +
            ".btn-primary:hover {\n" +
            "    -fx-background-color: #dde8c0;\n" +
            "}\n" +
            ".btn-secondary {\n" +
            "    -fx-background-color: #24593D;\n" +
            "    -fx-text-fill: #F6F6E3;\n" +
            "    -fx-border-color: #F6F6E3;\n" +
            "    -fx-border-radius: 5;\n" +
            "    -fx-background-radius: 5;\n" +
            "    -fx-padding: 7 16 7 16;\n" +
            "    -fx-cursor: hand;\n" +
            "}\n" +
            ".btn-secondary:hover {\n" +
            "    -fx-background-color: #2e7050;\n" +
            "}\n" +
            ".btn-danger {\n" +
            "    -fx-background-color: #8B0000;\n" +
            "    -fx-text-fill: white;\n" +
            "    -fx-background-radius: 5;\n" +
            "    -fx-padding: 7 16 7 16;\n" +
            "    -fx-cursor: hand;\n" +
            "}\n" +
            ".btn-danger:hover {\n" +
            "    -fx-background-color: #a50000;\n" +
            "}\n" +
            ".table-view {\n" +
            "    -fx-background-color: #14392F;\n" +
            "    -fx-border-color: #24593D;\n" +
            "}\n" +
            ".table-view .column-header {\n" +
            "    -fx-background-color: #24593D;\n" +
            "    -fx-text-fill: #F6F6E3;\n" +
            "    -fx-border-color: #3a6e52;\n" +
            "}\n" +
            ".table-view .column-header-background {\n" +
            "    -fx-background-color: #24593D;\n" +
            "}\n" +
            ".table-view .column-header .label {\n" +
            "    -fx-text-fill: #F6F6E3;\n" +
            "    -fx-font-weight: bold;\n" +
            "}\n" +
            ".table-view .table-row-cell {\n" +
            "    -fx-background-color: #14392F;\n" +
            "    -fx-border-color: #1e4a38;\n" +
            "}\n" +
            ".table-view .table-row-cell:odd {\n" +
            "    -fx-background-color: #1a4535;\n" +
            "}\n" +
            ".table-view .table-row-cell:selected {\n" +
            "    -fx-background-color: #2e7050;\n" +
            "}\n" +
            ".table-view .table-cell {\n" +
            "    -fx-text-fill: #F6F6E3;\n" +
            "    -fx-border-color: transparent;\n" +
            "}\n" +
            ".scroll-bar {\n" +
            "    -fx-background-color: #14392F;\n" +
            "}\n" +
            ".scroll-bar .thumb {\n" +
            "    -fx-background-color: #24593D;\n" +
            "    -fx-background-radius: 4;\n" +
            "}\n" +
            ".scroll-pane {\n" +
            "    -fx-background-color: #14392F;\n" +
            "    -fx-border-color: transparent;\n" +
            "}\n" +
            ".scroll-pane > .viewport {\n" +
            "    -fx-background-color: #14392F;\n" +
            "}\n" +
            ".header-box {\n" +
            "    -fx-background-color: linear-gradient(to right, #14392F, #24593D);\n" +
            "    -fx-padding: 18 24 18 24;\n" +
            "}\n" +
            ".header-title {\n" +
            "    -fx-text-fill: #F6F6E3;\n" +
            "    -fx-font-size: 22px;\n" +
            "    -fx-font-weight: bold;\n" +
            "}\n" +
            ".header-sub {\n" +
            "    -fx-text-fill: #7a9e6e;\n" +
            "    -fx-font-size: 12px;\n" +
            "}\n" +
            ".stat-card {\n" +
            "    -fx-background-color: #24593D;\n" +
            "    -fx-padding: 15;\n" +
            "    -fx-background-radius: 8;\n" +
            "}\n" +
            ".separator {\n" +
            "    -fx-background-color: #3a6e52;\n" +
            "}\n" +
            ".alert .dialog-pane {\n" +
            "    -fx-background-color: #14392F;\n" +
            "}\n" +
            ".alert .dialog-pane .content.label {\n" +
            "    -fx-text-fill: #F6F6E3;\n" +
            "}\n";
    }

    // Writes CSS to a temp file and returns its URI string
    String loadStylesheet() {
        try {
            Path cssFile = Files.createTempFile("hotel_style", ".css");
            Files.writeString(cssFile, cssContent());
            cssFile.toFile().deleteOnExit();
            return cssFile.toUri().toString();
        } catch (IOException e) {
            System.err.println("Could not write CSS file: " + e.getMessage());
            return null;
        }
    }

    // ===================== MAIN UI =====================

    @Override
    public void start(Stage stage) {
        loadRooms();
        loadCustomers();

        Label title = new Label("[ GRAND HOTEL MANAGER ]");
        title.getStyleClass().add("header-title");

        Label sub = new Label("Property Management System - All data persisted automatically");
        sub.getStyleClass().add("header-sub");

        VBox header = new VBox(4, title, sub);
        header.getStyleClass().add("header-box");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab dashTab   = new Tab("  Dashboard  ",  buildDashboard(tabPane));
        Tab roomTab   = new Tab("  Rooms  ",       buildRoomsTab());
        Tab bookTab   = new Tab("  Bookings  ",    buildBookingsTab());
        Tab reportTab = new Tab("  Reports  ",     buildReportsTab());

        tabPane.getTabs().addAll(dashTab, roomTab, bookTab, reportTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        VBox root = new VBox(header, tabPane);

        Scene scene = new Scene(root, 900, 680);

        
        String stylesheet = loadStylesheet();
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet);
        }

        Thread autoSave = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try { Thread.sleep(15000); } catch (InterruptedException e) { break; }
                saveRooms();
                saveCustomers();
                System.out.println("[AutoSave] " + LocalDateTime.now().format(DATE_FMT));
            }
        });
        autoSave.setDaemon(true);
        autoSave.start();

        stage.setScene(scene);
        stage.setTitle("Grand Hotel Manager");
        stage.setOnCloseRequest(e -> { saveRooms(); saveCustomers(); });
        stage.show();
    }

    // ===================== DASHBOARD =====================

    VBox buildDashboard(TabPane tabPane) {
        Label welcome = new Label("Welcome back.");
        welcome.setStyle("-fx-text-fill: #dde8c0; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label totalRoomsNum  = new Label();
        Label availRoomsNum  = new Label();
        Label bookedRoomsNum = new Label();
        Label totalGuestsNum = new Label();

        Runnable refresh = () -> {
            totalRoomsNum.setText(String.valueOf(rooms.size()));
            long avail = rooms.stream().filter(Room::isAvailable).count();
            availRoomsNum.setText(String.valueOf(avail));
            bookedRoomsNum.setText(String.valueOf(rooms.size() - avail));
            totalGuestsNum.setText(String.valueOf(customers.size()));
        };
        refresh.run();
        rooms.addListener((ListChangeListener<Room>) c -> refresh.run());
        customers.addListener((ListChangeListener<Customer>) c -> refresh.run());

        HBox stats = new HBox(14,
            statCard("Total Rooms",  totalRoomsNum,  "#a8b83e"),
            statCard("Available",    availRoomsNum,  "#6aaf5a"),
            statCard("Occupied",     bookedRoomsNum, "#c2622a"),
            statCard("Guests",       totalGuestsNum, "#7a9e6e")
        );
        stats.setAlignment(Pos.CENTER_LEFT);

        Button goRooms = new Button("+ Add New Room");
        goRooms.getStyleClass().add("btn-primary");
        goRooms.setOnAction(e -> tabPane.getSelectionModel().select(1));

        Button goBook = new Button(">> Book a Room");
        goBook.getStyleClass().add("btn-secondary");
        goBook.setOnAction(e -> tabPane.getSelectionModel().select(2));

        Button goReport = new Button("# View Reports");
        goReport.getStyleClass().add("btn-secondary");
        goReport.setOnAction(e -> tabPane.getSelectionModel().select(3));

        HBox actions = new HBox(12, goRooms, goBook, goReport);

        Label actionsLabel = new Label("Quick Actions");
        actionsLabel.setStyle("-fx-text-fill: #7a8c6a; -fx-font-size: 12px;");

        VBox card = new VBox(18, welcome, stats, new Separator(), actionsLabel, actions);
        card.getStyleClass().add("card");

        VBox dash = new VBox(card);
        dash.setPadding(new Insets(20));
        return dash;
    }

    VBox statCard(String label, Label numLabel, String color) {
        numLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #7a8c6a; -fx-font-size: 12px;");
        VBox card = new VBox(4, numLabel, lbl);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(160);
        return card;
    }

    // ===================== ROOMS TAB =====================

    VBox buildRoomsTab() {
        TextField roomNoF = styledField("e.g. 101");
        ComboBox<String> typeF = new ComboBox<>();
        typeF.getItems().addAll("Single", "Double", "Deluxe", "Suite");
        typeF.setPromptText("Select Type");
        typeF.setPrefWidth(200);
        TextField priceF = styledField("e.g. 2500.00");

        Button addBtn       = new Button("Add Room");      addBtn.getStyleClass().add("btn-primary");
        Button showAllBtn   = new Button("Show All");      showAllBtn.getStyleClass().add("btn-secondary");
        Button showAvailBtn = new Button("Available Only");showAvailBtn.getStyleClass().add("btn-secondary");
        Button deleteBtn    = new Button("Delete Selected");deleteBtn.getStyleClass().add("btn-danger");

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(10);
        form.addRow(0, fieldLabel("Room Number"),         roomNoF);
        form.addRow(1, fieldLabel("Room Type"),           typeF);
        form.addRow(2, fieldLabel("Price / Night (Rs.)"), priceF);

        VBox formCard = new VBox(12, sectionTitle("Add New Room"), form, new HBox(10, addBtn));
        formCard.getStyleClass().add("card");

       
        TableView<Room> table = new TableView<>(rooms);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(340);

        TableColumn<Room, Integer> c1 = new TableColumn<>("Room #");
        c1.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Room, String> c2 = new TableColumn<>("Type");
        c2.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Room, Double> c3 = new TableColumn<>("Price / Night (Rs.)");
        c3.setCellValueFactory(new PropertyValueFactory<>("price"));
        c3.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("Rs. %,.2f", v));
            }
        });

        TableColumn<Room, String> c4 = new TableColumn<>("Status");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        c4.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle(v.startsWith("[OK]")
                    ? "-fx-text-fill: #6aaf5a; -fx-font-weight: bold;"
                    : "-fx-text-fill: #c2622a; -fx-font-weight: bold;");
            }
        });

        table.getColumns().addAll(c1, c2, c3, c4);

        HBox tableBtns = new HBox(10, showAllBtn, showAvailBtn, deleteBtn);
        VBox tableCard = new VBox(10, sectionTitle("Room Inventory"), tableBtns, table);
        tableCard.getStyleClass().add("card");

        addBtn.setOnAction(e -> {
            try {
                int rn = Integer.parseInt(roomNoF.getText().trim());
                if (roomMap.containsKey(rn)) { alert("Room " + rn + " already exists!"); return; }
                String t = typeF.getValue();
                if (t == null) { alert("Please select a room type!"); return; }
                double p = Double.parseDouble(priceF.getText().trim());
                Room r = new Room(rn, t, p, true);
                rooms.add(r);
                roomMap.put(rn, r);
                saveRooms();
                clearFields(roomNoF, priceF);
                typeF.setValue(null);
                alert("Room " + rn + " added successfully!");
            } catch (NumberFormatException ex) {
                alert("Please enter valid room number and price.");
            }
        });

        showAllBtn.setOnAction(e -> table.setItems(rooms));
        showAvailBtn.setOnAction(e -> table.setItems(rooms.filtered(Room::isAvailable)));

        deleteBtn.setOnAction(e -> {
            Room sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alert("Select a room to delete."); return; }
            if (!sel.isAvailable()) { alert("Cannot delete a booked room!"); return; }
            rooms.remove(sel);
            roomMap.remove(sel.getRoomNumber());
            saveRooms();
            alert("Room " + sel.getRoomNumber() + " deleted.");
        });

        VBox tab = new VBox(14, formCard, tableCard);
        tab.setPadding(new Insets(16));
        ScrollPane sp = new ScrollPane(tab);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane");
        VBox wrapper = new VBox(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
        return wrapper;
    }

    // ===================== BOOKINGS TAB =====================

    VBox buildBookingsTab() {
        TextField nameF    = styledField("Guest full name");
        TextField contactF = styledField("Phone / Email");
        TextField roomNoF  = styledField("Room number");

        Button bookBtn     = new Button("Book Room"); bookBtn.getStyleClass().add("btn-primary");
        Button checkoutBtn = new Button("Checkout");  checkoutBtn.getStyleClass().add("btn-danger");

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(10);
        form.addRow(0, fieldLabel("Guest Name"),  nameF);
        form.addRow(1, fieldLabel("Contact"),     contactF);
        form.addRow(2, fieldLabel("Room Number"), roomNoF);

        VBox formCard = new VBox(12, sectionTitle("Manage Booking"), form, new HBox(10, bookBtn, checkoutBtn));
        formCard.getStyleClass().add("card");

        TableView<Customer> table = new TableView<>(customers);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(320);

        TableColumn<Customer, String>  c1 = new TableColumn<>("Guest Name");
        c1.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Customer, String>  c2 = new TableColumn<>("Contact");
        c2.setCellValueFactory(new PropertyValueFactory<>("contact"));

        TableColumn<Customer, Integer> c3 = new TableColumn<>("Room #");
        c3.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Customer, String>  c4 = new TableColumn<>("Check-In");
        c4.setCellValueFactory(new PropertyValueFactory<>("checkIn"));

        TableColumn<Customer, Double>  c5 = new TableColumn<>("Bill (Rs.)");
        c5.setCellValueFactory(new PropertyValueFactory<>("totalBill"));
        c5.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("Rs. %,.2f", v));
            }
        });

        table.getColumns().addAll(c1, c2, c3, c4, c5);

        VBox tableCard = new VBox(10, sectionTitle("Current Guests"), table);
        tableCard.getStyleClass().add("card");

        bookBtn.setOnAction(e -> {
            try {
                String n   = nameF.getText().trim();
                String con = contactF.getText().trim();
                int rn     = Integer.parseInt(roomNoF.getText().trim());
                if (n.isEmpty() || con.isEmpty()) { alert("Please fill all fields."); return; }
                Room r = roomMap.get(rn);
                if (r == null)        { alert("Room " + rn + " does not exist!"); return; }
                if (!r.isAvailable()) { alert("Room " + rn + " is already booked!"); return; }
                r.setAvailable(false);
                String checkIn = LocalDateTime.now().format(DATE_FMT);
                Customer c = new Customer(n, con, rn, checkIn, r.getPrice());
                customers.add(c);
                saveRooms();
                saveCustomers();
                clearFields(nameF, contactF, roomNoF);
                rooms.set(rooms.indexOf(r), r);
                alert("Room " + rn + " booked for " + n + "!\nCheck-in: " + checkIn);
            } catch (NumberFormatException ex) {
                alert("Enter a valid room number.");
            }
        });

        checkoutBtn.setOnAction(e -> {
            try {
                int rn = Integer.parseInt(roomNoF.getText().trim());
                Room r = roomMap.get(rn);
                Customer guest = customers.stream()
                        .filter(c -> c.getRoomNumber() == rn)
                        .findFirst().orElse(null);
                if (guest == null) { alert("No guest found in room " + rn); return; }
                String bill = "Rs. " + String.format("%,.2f", guest.getTotalBill());
                customers.removeIf(c -> c.getRoomNumber() == rn);
                if (r != null) { r.setAvailable(true); rooms.set(rooms.indexOf(r), r); }
                saveRooms();
                saveCustomers();
                clearFields(roomNoF);
                alert("Checkout complete for Room " + rn
                    + "\nGuest: " + guest.getName()
                    + "\nBill: "  + bill);
            } catch (NumberFormatException ex) {
                alert("Enter a valid room number.");
            }
        });

        VBox tab = new VBox(14, formCard, tableCard);
        tab.setPadding(new Insets(16));
        ScrollPane sp = new ScrollPane(tab);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane");
        VBox wrapper = new VBox(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
        return wrapper;
    }

    // ===================== REPORTS TAB =====================

    VBox buildReportsTab() {
        TextArea reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setWrapText(true);
        reportArea.setStyle(
            "-fx-control-inner-background: #1a1e0f;" +
            "-fx-text-fill: #a8b83e;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: #3a4520;" +
            "-fx-border-radius: 6;"
        );
        reportArea.setPrefHeight(420);

        Button genBtn    = new Button("Generate Full Report"); genBtn.getStyleClass().add("btn-primary");
        Button exportBtn = new Button("Export to File");       exportBtn.getStyleClass().add("btn-secondary");

        genBtn.setOnAction(e -> reportArea.setText(generateReport()));

        exportBtn.setOnAction(e -> {
            String report = generateReport();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("hotel_report_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".txt"))) {
                bw.write(report);
                alert("Report exported successfully!");
            } catch (Exception ex) {
                alert("Export failed: " + ex.getMessage());
            }
        });

        VBox card = new VBox(12, sectionTitle("Reports & Analytics"), new HBox(10, genBtn, exportBtn), reportArea);
        card.getStyleClass().add("card");

        VBox tab = new VBox(card);
        tab.setPadding(new Insets(16));
        return tab;
    }

    String generateReport() {
        StringBuilder sb = new StringBuilder();
        String line  = "=".repeat(52);
        String dline = "-".repeat(52);

        sb.append("  GRAND HOTEL - MANAGEMENT REPORT\n");
        sb.append("  Generated: ").append(LocalDateTime.now().format(DATE_FMT)).append("\n");
        sb.append(line).append("\n\n");

        long avail   = rooms.stream().filter(Room::isAvailable).count();
        long booked  = rooms.size() - avail;
        double revenue = customers.stream().mapToDouble(Customer::getTotalBill).sum();

        sb.append("  SUMMARY\n").append(dline).append("\n");
        sb.append(String.format("  %-25s %d%n",        "Total Rooms:",      rooms.size()));
        sb.append(String.format("  %-25s %d%n",        "Available Rooms:",  avail));
        sb.append(String.format("  %-25s %d%n",        "Occupied Rooms:",   booked));
        sb.append(String.format("  %-25s %d%n",        "Current Guests:",   customers.size()));
        sb.append(String.format("  %-25s Rs. %,.2f%n", "Expected Revenue:", revenue));
        sb.append("\n");

        sb.append("  ROOMS BY TYPE\n").append(dline).append("\n");
        Map<String, Long> byType = new LinkedHashMap<>();
        for (Room r : rooms) byType.merge(r.getType(), 1L, Long::sum);
        byType.forEach((t, cnt) ->
            sb.append(String.format("  %-20s %d room(s)%n", t + ":", cnt)));
        sb.append("\n");

        sb.append("  ALL ROOMS\n").append(dline).append("\n");
        sb.append(String.format("  %-8s %-10s %-14s %s%n", "Room#", "Type", "Price/Night", "Status"));
        for (Room r : rooms)
            sb.append(String.format("  %-8d %-10s Rs. %-10.2f %s%n",
                r.getRoomNumber(), r.getType(), r.getPrice(),
                r.isAvailable() ? "Available" : "Booked"));
        sb.append("\n");

        sb.append("  CURRENT GUESTS\n").append(dline).append("\n");
        if (customers.isEmpty()) {
            sb.append("  No guests currently checked in.\n");
        } else {
            sb.append(String.format("  %-18s %-12s %-8s %-18s %-10s%n",
                "Name", "Contact", "Room#", "Check-In", "Bill(Rs.)"));
            for (Customer c : customers)
                sb.append(String.format("  %-18s %-12s %-8d %-18s %-10.2f%n",
                    c.getName(), c.getContact(), c.getRoomNumber(),
                    c.getCheckIn(), c.getTotalBill()));
        }
        sb.append("\n").append(line).append("\n  END OF REPORT\n");
        return sb.toString();
    }

    // ===================== FILE I/O =====================

    void saveRooms() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ROOMS_FILE))) {
            bw.write("roomNumber,type,price,available"); bw.newLine();
            for (Room r : rooms) {
                bw.write(r.getRoomNumber() + "," + r.getType() + "," + r.getPrice() + "," + r.isAvailable());
                bw.newLine();
            }
        } catch (Exception e) { System.err.println("Error saving rooms: " + e.getMessage()); }
    }

    void loadRooms() {
        File f = new File(ROOMS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] d = line.split(",");
                if (d.length < 4) continue;
                Room r = new Room(Integer.parseInt(d[0].trim()), d[1].trim(),
                    Double.parseDouble(d[2].trim()), Boolean.parseBoolean(d[3].trim()));
                rooms.add(r);
                roomMap.put(r.getRoomNumber(), r);
            }
        } catch (Exception e) { System.err.println("Error loading rooms: " + e.getMessage()); }
    }

    void saveCustomers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CUSTOMERS_FILE))) {
            bw.write("name,contact,roomNumber,checkIn,totalBill"); bw.newLine();
            for (Customer c : customers) {
                bw.write(String.format("\"%s\",\"%s\",%d,\"%s\",%.2f",
                    c.getName(), c.getContact(), c.getRoomNumber(), c.getCheckIn(), c.getTotalBill()));
                bw.newLine();
            }
        } catch (Exception e) { System.err.println("Error saving customers: " + e.getMessage()); }
    }

    void loadCustomers() {
        File f = new File(CUSTOMERS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] d = parseCSVLine(line);
                if (d.length < 5) continue;
                customers.add(new Customer(d[0], d[1],
                    Integer.parseInt(d[2].trim()), d[3],
                    Double.parseDouble(d[4].trim())));
            }
        } catch (Exception e) { System.err.println("Error loading customers: " + e.getMessage()); }
    }

    String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') { inQuotes = !inQuotes; }
            else if (ch == ',' && !inQuotes) { result.add(cur.toString().trim()); cur.setLength(0); }
            else { cur.append(ch); }
        }
        result.add(cur.toString().trim());
        return result.toArray(new String[0]);
    }

    // ===================== HELPERS =====================

    TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(230);
        return tf;
    }

    Label fieldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        l.setMinWidth(140);
        return l;
    }

    Label sectionTitle(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-title");
        return l;
    }

    void clearFields(TextField... fields) { for (TextField tf : fields) tf.clear(); }

    void alert(String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            a.setHeaderText(null);
            a.setTitle("Hotel Manager");
            a.show();
        });
    }

    public static void main(String[] args) { launch(args); }
}