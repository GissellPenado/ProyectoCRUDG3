package com.tuempresa.proyectocrudg3.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.tuempresa.proyectocrudg3.model.Categorias;
import com.tuempresa.proyectocrudg3.model.Marcas;

public class ProductosController {

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtStock;

    @FXML
    private ComboBox<Categorias> cmbCategoria;

    @FXML
    private ComboBox<Marcas> cmbMarca;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private Button btnNuevo;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnEliminar;
}
