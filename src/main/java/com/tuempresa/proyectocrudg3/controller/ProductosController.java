package com.tuempresa.proyectocrudg3.controller;

import com.tuempresa.proyectocrudg3.database.ConexionBD;
import com.tuempresa.proyectocrudg3.model.Categorias;
import com.tuempresa.proyectocrudg3.model.Marcas;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.tuempresa.proyectocrudg3.model.Productos;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.math.BigDecimal;

public class ProductosController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private TextField txtDescripcion;

    @FXML private ComboBox<Categorias> cmbCategoria;
    @FXML private ComboBox<Marcas> cmbMarca;
    @FXML private DatePicker dpFecha;

    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;

    @FXML private TableView<Productos> tblProductos;

    @FXML private TableColumn<Productos, String> colCodigo;
    @FXML private TableColumn<Productos, String> colNombre;
    @FXML private TableColumn<Productos, String> colCategoria;
    @FXML private TableColumn<Productos, String> colMarca;
    @FXML private TableColumn<Productos, BigDecimal> colPrecio;
    @FXML private TableColumn<Productos, Integer> colStock;
    @FXML private TableColumn<Productos, LocalDate> colFechaCreacion;

    @FXML
    private void initialize() {
        cargarCategorias();
        cargarMarcas();

        configurarTabla();
        cargarProductos();

        btnNuevo.setOnAction(event -> nuevoProducto());

        nuevoProducto();
    }

    private void cargarCategorias() {
        String sql = """
                SELECT id_categoria, nombre, descripcion, estado
                FROM catalogos.categorias
                WHERE estado = 1
                ORDER BY nombre
                """;

        cmbCategoria.getItems().clear();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                Categorias categoria = new Categorias(
                        resultado.getInt("id_categoria"),
                        resultado.getString("nombre"),
                        resultado.getString("descripcion"),
                        resultado.getInt("estado")
                );

                cmbCategoria.getItems().add(categoria);
            }

        } catch (SQLException e) {
            mostrarError("No se pudieron cargar las categorías.", e);
        }
    }

    private void cargarMarcas() {
        String sql = """
                SELECT id_marca, nombre, pais_origen, estado
                FROM catalogos.marcas
                WHERE estado = 1
                ORDER BY nombre
                """;

        cmbMarca.getItems().clear();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                Marcas marca = new Marcas(
                        resultado.getInt("id_marca"),
                        resultado.getString("nombre"),
                        resultado.getString("pais_origen"),
                        resultado.getInt("estado")
                );

                cmbMarca.getItems().add(marca);
            }

        } catch (SQLException e) {
            mostrarError("No se pudieron cargar las marcas.", e);
        }
    }

    private void nuevoProducto() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.setText("0");
        txtDescripcion.clear();

        cmbCategoria.getSelectionModel().clearSelection();
        cmbMarca.getSelectionModel().clearSelection();

        dpFecha.setValue(LocalDate.now());

        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);

        txtCodigo.requestFocus();
    }

    private void mostrarError(String mensaje, SQLException e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(mensaje);
        alerta.setContentText(e.getMessage());
        alerta.showAndWait();
    }


    private void cargarOpcionesOrden() {


    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(
                celda -> new ReadOnlyStringWrapper(
                        celda.getValue().getCodigo()
                )
        );

        colNombre.setCellValueFactory(
                celda -> new ReadOnlyStringWrapper(
                        celda.getValue().getNombre()
                )
        );

        colPrecio.setCellValueFactory(
                celda -> new ReadOnlyObjectWrapper<>(
                        celda.getValue().getPrecio()
                )
        );

        colCategoria.setCellValueFactory(celda -> {
            int id = celda.getValue().getId_categoria();

            String nombre = cmbCategoria.getItems().stream()
                    .filter(categoria -> categoria.getId_categoria() == id)
                    .map(Categorias::getNombre)
                    .findFirst()
                    .orElse("ID: " + id);

            return new ReadOnlyStringWrapper(nombre);
        });

        colMarca.setCellValueFactory(celda -> {
            int id = celda.getValue().getId_marca();

            String nombre = cmbMarca.getItems().stream()
                    .filter(marca -> marca.getId_marca() == id)
                    .map(Marcas::getNombre)
                    .findFirst()
                    .orElse("ID: " + id);

            return new ReadOnlyStringWrapper(nombre);
        });

        colStock.setCellValueFactory(
                celda -> new ReadOnlyObjectWrapper<>(
                        celda.getValue().getStock()
                )
        );

        colFechaCreacion.setCellValueFactory(
                celda -> new ReadOnlyObjectWrapper<>(
                        celda.getValue().getFecha_creacion()
                )
        );
    }

    private void cargarProductos() {
        ObservableList<Productos> lista =
                FXCollections.observableArrayList();

        String sql = """
            SELECT id_producto, codigo, nombre, descripcion,
                   precio, stock, fecha_creacion,
                   id_categoria, id_marca, estado
            FROM catalogos.productos
            WHERE estado = 1
            ORDER BY nombre
            """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                Productos producto = new Productos(
                        resultado.getInt("id_producto"),
                        resultado.getString("codigo"),
                        resultado.getString("nombre"),
                        resultado.getString("descripcion"),
                        resultado.getBigDecimal("precio"),
                        resultado.getInt("stock"),
                        resultado.getDate("fecha_creacion").toLocalDate(),
                        resultado.getInt("id_categoria"),
                        resultado.getInt("id_marca"),
                        resultado.getInt("estado")
                );

                lista.add(producto);
            }

            tblProductos.setItems(lista);

        } catch (SQLException e) {
            mostrarError("No se pudieron cargar los productos.", e);
        }
    }
}