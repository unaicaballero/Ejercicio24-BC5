package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;


public class Main extends Application {

    private TableView<Empleado> tableView;
    private TextField campId;
    private TextField campNombre;
    private TextField campSalario;

    private static final String URL      = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER     = "RIBERA";
    private static final String PASSWORD = "ribera";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Ejercicio 24 - CRUD completo");

        tableView = new TableView<>();

        // Definir columnas
        TableColumn<Empleado, Integer> idCol = new TableColumn<>("ID");
        TableColumn<Empleado, String> nombreCol = new TableColumn<>("Nombre");
        TableColumn<Empleado, Integer> salarioCol = new TableColumn<>("Salario");

        // Asignar las propiedades del modelo a las columnas
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        salarioCol.setCellValueFactory(new PropertyValueFactory<>("salario"));

        tableView.getColumns().addAll(idCol, nombreCol, salarioCol);

        // Al seleccionar una fila se rellenan los campos del formulario
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        campId.setText(String.valueOf(seleccionado.getId()));
                        campNombre.setText(seleccionado.getNombre());
                        campSalario.setText(String.valueOf(seleccionado.getSalario()));
                    }
                }
        );

        // Formulario de entrada
        campId      = new TextField(); campId.setPromptText("ID");
        campNombre  = new TextField(); campNombre.setPromptText("Nombre");
        campSalario = new TextField(); campSalario.setPromptText("Salario");

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(8);
        formulario.add(new Label("ID:"),      0, 0); formulario.add(campId,      1, 0);
        formulario.add(new Label("Nombre:"),  0, 1); formulario.add(campNombre,  1, 1);
        formulario.add(new Label("Salario:"), 0, 2); formulario.add(campSalario, 1, 2);

        // Botones CRUD
        Button btnLeer       = new Button("Leer");
        Button btnCrear      = new Button("Crear");
        Button btnActualizar = new Button("Actualizar");
        Button btnEliminar   = new Button("Eliminar");

        btnLeer.setOnAction(       e -> leer());
        btnCrear.setOnAction(      e -> crear());
        btnActualizar.setOnAction( e -> actualizar());
        btnEliminar.setOnAction(   e -> eliminar());

        HBox botones = new HBox(10, btnLeer, btnCrear, btnActualizar, btnEliminar);

        VBox vbox = new VBox(12, tableView, formulario, botones);
        vbox.setStyle("-fx-padding: 20;");
        Scene scene = new Scene(vbox, 520, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Carga inicial al arrancar la aplicación
        leer();
    }

    // carga todos los empleados en el TableView
    private void leer() {
        tableView.getItems().clear();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Statement stmt = conn.createStatement();
            ResultSet rs   = stmt.executeQuery("SELECT id, nombre, salario FROM EJEMPLOCONEXION");

            while (rs.next()) {
                int    id      = rs.getInt("id");
                String nombre  = rs.getString("nombre");
                int    salario = rs.getInt("salario");
                tableView.getItems().add(new Empleado(id, nombre, salario));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // inserta un nuevo empleado en la base de datos
    private void crear() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO EJEMPLOCONEXION (id, nombre, salario) VALUES (?, ?, ?)");

            ps.setInt(1, Integer.parseInt(campId.getText().trim()));
            ps.setString(2, campNombre.getText().trim());
            ps.setInt(3, Integer.parseInt(campSalario.getText().trim()));
            ps.executeUpdate();

            System.out.println("Empleado insertado correctamente.");
            leer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // actualiza nombre y salario del empleado con el ID indicado
    private void actualizar() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE EJEMPLOCONEXION SET nombre = ?, salario = ? WHERE id = ?");

            ps.setString(1, campNombre.getText().trim());
            ps.setInt(2, Integer.parseInt(campSalario.getText().trim()));
            ps.setInt(3, Integer.parseInt(campId.getText().trim()));
            ps.executeUpdate();

            System.out.println("Empleado actualizado correctamente.");
            leer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // elimina el empleado con el ID indicado
    private void eliminar() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM EJEMPLOCONEXION WHERE id = ?");

            ps.setInt(1, Integer.parseInt(campId.getText().trim()));
            ps.executeUpdate();

            System.out.println("Empleado eliminado correctamente.");
            leer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Empleado {
        private final int    id;
        private final String nombre;
        private final int    salario;

        public Empleado(int id, String nombre, int salario) {
            this.id      = id;
            this.nombre  = nombre;
            this.salario = salario;
        }

        public int    getId()      { return id; }
        public String getNombre()  { return nombre; }
        public int    getSalario() { return salario; }
    }
}