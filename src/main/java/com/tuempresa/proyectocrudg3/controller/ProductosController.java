package com.tuempresa.proyectocrudg3.controller;

import com.tuempresa.proyectocrudg3.database.ConexionBD;
import com.tuempresa.proyectocrudg3.model.Categorias;
import com.tuempresa.proyectocrudg3.model.Marcas;
import com.tuempresa.proyectocrudg3.model.Productos;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductosController {

    // Formulario
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

    // Filtros
    @FXML private TextField txtFiltro;
    @FXML private ComboBox<Categorias> cmbFiltroCategoria;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TextField txtPrecioMinimo;
    @FXML private TextField txtPrecioMaximo;
    @FXML private CheckBox chkConStock;
    @FXML private ComboBox<String> cmbOrdenar;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarFiltros;

    // Tabla
    @FXML private TableView<Productos> tblProductos;
    @FXML private TableColumn<Productos, String> colCodigo;
    @FXML private TableColumn<Productos, String> colNombre;
    @FXML private TableColumn<Productos, String> colCategoria;
    @FXML private TableColumn<Productos, String> colMarca;
    @FXML private TableColumn<Productos, BigDecimal> colPrecio;
    @FXML private TableColumn<Productos, Integer> colStock;
    @FXML private TableColumn<Productos, LocalDate> colFechaCreacion;

    private final ObservableList<Productos> listaProductos =
            FXCollections.observableArrayList();

    private final ObservableList<Categorias> listaCategorias =
            FXCollections.observableArrayList();

    private final ObservableList<Marcas> listaMarcas =
            FXCollections.observableArrayList();

    private final Map<Integer, String> nombresCategorias = new HashMap<>();
    private final Map<Integer, String> nombresMarcas = new HashMap<>();

    @FXML
    private void initialize() {
        configurarTableView();
        configurarSeleccionTableView();

        cargarCategorias();
        cargarMarcas();
        cargarOpcionesOrden();

        btnNuevo.setOnAction(event -> nuevoProducto());
        btnGuardar.setOnAction(event -> guardarProducto());
        btnActualizar.setOnAction(event -> actualizarProducto());
        btnEliminar.setOnAction(event -> eliminarProducto());
        btnBuscar.setOnAction(event -> buscarProductos());
        btnLimpiarFiltros.setOnAction(event -> limpiarFiltros());
        txtFiltro.setOnAction(event -> buscarProductos());

        nuevoProducto();
        cargarProductos();
    }

    private void configurarTableView() {
        colCodigo.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getCodigo()));

        colNombre.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getNombre()));

        colCategoria.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        nombresCategorias.getOrDefault(
                                data.getValue().getId_categoria(), "")));

        colMarca.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        nombresMarcas.getOrDefault(
                                data.getValue().getId_marca(), "")));

        colPrecio.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getPrecio()));

        colStock.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getStock()));

        colFechaCreacion.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getFecha_creacion()));

        tblProductos.setItems(listaProductos);
    }

    private void configurarSeleccionTableView() {
        tblProductos.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, actual) -> {
                    if (actual != null) {
                        cargarProductoEnFormulario(actual);
                    }
                });
    }

    private void cargarProductoEnFormulario(Productos p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(p.getPrecio().toPlainString());
        txtStock.setText(String.valueOf(p.getStock()));
        dpFecha.setValue(p.getFecha_creacion());

        cmbCategoria.setValue(null);
        cmbMarca.setValue(null);

        for (Categorias categoria : listaCategorias) {
            if (categoria.getId_categoria() == p.getId_categoria()) {
                cmbCategoria.setValue(categoria);
                break;
            }
        }

        for (Marcas marca : listaMarcas) {
            if (marca.getId_marca() == p.getId_marca()) {
                cmbMarca.setValue(marca);
                break;
            }
        }

        btnGuardar.setDisable(true);
        btnActualizar.setDisable(false);
        btnEliminar.setDisable(false);
    }

    private void cargarCategorias() {
        String sql = """
                SELECT id_categoria, nombre, descripcion, estado
                FROM catalogos.categorias
                WHERE estado = 1
                ORDER BY nombre
                """;

        listaCategorias.clear();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaCategorias.add(new Categorias(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("estado")
                ));
            }

            cmbCategoria.setItems(
                    FXCollections.observableArrayList(listaCategorias));

            cmbFiltroCategoria.setItems(
                    FXCollections.observableArrayList(listaCategorias));

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

        listaMarcas.clear();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaMarcas.add(new Marcas(
                        rs.getInt("id_marca"),
                        rs.getString("nombre"),
                        rs.getString("pais_origen"),
                        rs.getInt("estado")
                ));
            }

            cmbMarca.setItems(
                    FXCollections.observableArrayList(listaMarcas));

        } catch (SQLException e) {
            mostrarError("No se pudieron cargar las marcas.", e);
        }
    }

    private void cargarOpcionesOrden() {
        cmbOrdenar.setItems(FXCollections.observableArrayList(
                "Nombre",
                "Precio menor a mayor",
                "Precio mayor a menor",
                "Fecha más reciente",
                "Fecha más antigua",
                "Stock mayor a menor",
                "Stock menor a mayor"
        ));

        cmbOrdenar.setValue("Nombre");
    }

    private Productos crearProductoDesdeResultSet(ResultSet rs)
            throws SQLException {

        return new Productos(
                rs.getInt("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBigDecimal("precio"),
                rs.getInt("stock"),
                rs.getDate("fecha_creacion").toLocalDate(),
                rs.getInt("id_categoria"),
                rs.getInt("id_marca"),
                rs.getInt("estado")
        );
    }

    private void cargarProductos() {
        ejecutarBusqueda(false);
    }

    @FXML
    private void buscarProductos() {
        ejecutarBusqueda(true);
    }

    private void ejecutarBusqueda(boolean avisarSinResultados) {
        BigDecimal minimo;
        BigDecimal maximo;

        try {
            minimo = leerPrecioFiltro(txtPrecioMinimo);
            maximo = leerPrecioFiltro(txtPrecioMaximo);

            if (minimo != null && maximo != null
                    && minimo.compareTo(maximo) > 0) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Precios incorrectos",
                        "El precio mínimo no puede superar al máximo.");
                return;
            }

            if (dpDesde.getValue() != null && dpHasta.getValue() != null
                    && dpDesde.getValue().isAfter(dpHasta.getValue())) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Fechas incorrectas",
                        "La fecha Desde no puede ser posterior a Hasta.");
                return;
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Precio incorrecto",
                    "Los precios de los filtros deben ser números no negativos.");
            return;
        }

        StringBuilder sql = new StringBuilder("""
                SELECT p.id_producto, p.codigo, p.nombre,
                       p.descripcion, p.precio, p.stock,
                       p.fecha_creacion, p.id_categoria,
                       p.id_marca, p.estado,
                       c.nombre AS categoria,
                       m.nombre AS marca
                FROM catalogos.productos p
                INNER JOIN catalogos.categorias c
                    ON p.id_categoria = c.id_categoria
                INNER JOIN catalogos.marcas m
                    ON p.id_marca = m.id_marca
                WHERE p.estado = 1
                """);

        List<Object> parametros = new ArrayList<>();

        String texto = txtFiltro.getText().trim();

        if (!texto.isBlank()) {
            sql.append(" AND (p.codigo ILIKE ? OR p.nombre ILIKE ?)");
            parametros.add("%" + texto + "%");
            parametros.add("%" + texto + "%");
        }

        Categorias categoria = cmbFiltroCategoria.getValue();

        if (categoria != null) {
            sql.append(" AND p.id_categoria = ?");
            parametros.add(categoria.getId_categoria());
        }

        if (dpDesde.getValue() != null) {
            sql.append(" AND p.fecha_creacion >= ?");
            parametros.add(dpDesde.getValue());
        }

        if (dpHasta.getValue() != null) {
            sql.append(" AND p.fecha_creacion <= ?");
            parametros.add(dpHasta.getValue());
        }

        if (minimo != null) {
            sql.append(" AND p.precio >= ?");
            parametros.add(minimo);
        }

        if (maximo != null) {
            sql.append(" AND p.precio <= ?");
            parametros.add(maximo);
        }

        if (chkConStock.isSelected()) {
            sql.append(" AND p.stock > 0");
        }

        String orden = cmbOrdenar.getValue() == null
                ? "Nombre" : cmbOrdenar.getValue();

        // Solo se permiten estas opciones conocidas.
        switch (orden) {
            case "Precio menor a mayor" ->
                    sql.append(" ORDER BY p.precio ASC, p.nombre ASC");
            case "Precio mayor a menor" ->
                    sql.append(" ORDER BY p.precio DESC, p.nombre ASC");
            case "Fecha más reciente" ->
                    sql.append(" ORDER BY p.fecha_creacion DESC, p.nombre ASC");
            case "Fecha más antigua" ->
                    sql.append(" ORDER BY p.fecha_creacion ASC, p.nombre ASC");
            case "Stock mayor a menor" ->
                    sql.append(" ORDER BY p.stock DESC, p.nombre ASC");
            case "Stock menor a mayor" ->
                    sql.append(" ORDER BY p.stock ASC, p.nombre ASC");
            default ->
                    sql.append(" ORDER BY p.nombre ASC");
        }

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                Object valor = parametros.get(i);

                if (valor instanceof LocalDate fecha) {
                    ps.setDate(i + 1, Date.valueOf(fecha));
                } else {
                    ps.setObject(i + 1, valor);
                }
            }

            ObservableList<Productos> resultados =
                    FXCollections.observableArrayList();

            Map<Integer, String> categorias = new HashMap<>();
            Map<Integer, String> marcas = new HashMap<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Productos producto = crearProductoDesdeResultSet(rs);
                    resultados.add(producto);

                    categorias.put(producto.getId_categoria(),
                            rs.getString("categoria"));

                    marcas.put(producto.getId_marca(),
                            rs.getString("marca"));
                }
            }

            nombresCategorias.clear();
            nombresCategorias.putAll(categorias);
            nombresMarcas.clear();
            nombresMarcas.putAll(marcas);

            nuevoProducto();
            listaProductos.setAll(resultados);
            tblProductos.refresh();

            if (avisarSinResultados && resultados.isEmpty()) {
                mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Sin resultados",
                        "No existen productos que coincidan con los filtros.");
            }

        } catch (SQLException e) {
            mostrarError("No se pudieron consultar los productos.", e);
        }
    }

    private BigDecimal leerPrecioFiltro(TextField campo) {
        String texto = campo.getText().trim();

        if (texto.isEmpty()) {
            return null;
        }

        BigDecimal numero = new BigDecimal(texto);

        if (numero.signum() < 0) {
            throw new NumberFormatException();
        }

        return numero;
    }

    @FXML
    private void limpiarFiltros() {
        txtFiltro.clear();
        cmbFiltroCategoria.setValue(null);
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        txtPrecioMinimo.clear();
        txtPrecioMaximo.clear();
        chkConStock.setSelected(false);
        cmbOrdenar.setValue("Nombre");

        cargarProductos();
    }

    @FXML
    private void nuevoProducto() {
        tblProductos.getSelectionModel().clearSelection();

        txtCodigo.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStock.setText("0");

        cmbCategoria.setValue(null);
        cmbMarca.setValue(null);
        dpFecha.setValue(LocalDate.now());

        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);

        txtCodigo.requestFocus();
    }

    private boolean validarFormulario() {
        String mensaje = null;

        if (txtCodigo.getText().trim().isEmpty()
                || txtNombre.getText().trim().isEmpty()) {
            mensaje = "Ingresá el código y el nombre.";

        } else if (txtCodigo.getText().trim().length() > 20) {
            mensaje = "El código admite un máximo de 20 caracteres.";

        } else if (txtNombre.getText().trim().length() > 120) {
            mensaje = "El nombre admite un máximo de 120 caracteres.";

        } else if (txtDescripcion.getText().trim().length() > 200) {
            mensaje = "La descripción admite un máximo de 200 caracteres.";

        } else if (cmbCategoria.getValue() == null
                || cmbMarca.getValue() == null) {
            mensaje = "Seleccioná una categoría y una marca activas.";

        } else if (dpFecha.getValue() == null) {
            mensaje = "Seleccioná una fecha.";
        }

        if (mensaje != null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Revisá el formulario", mensaje);
            return false;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());

            // NUMERIC(10,2): ocho cifras enteras y dos decimales.
            BigDecimal precioBD = precio.setScale(2, RoundingMode.UNNECESSARY);

            if (precioBD.signum() <= 0
                    || precioBD.compareTo(
                    new BigDecimal("99999999.99")) > 0
                    || stock < 0) {
                throw new IllegalArgumentException();
            }

        } catch (IllegalArgumentException | ArithmeticException e) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Precio o stock incorrectos",
                    "Precio: mayor que 0, máximo 99999999.99 y hasta "
                            + "dos decimales.\nStock: entero mayor o igual a 0."
                            + "\nUsá punto para los decimales.");
            return false;
        }

        return true;
    }

    private boolean existeCodigo(String codigo, Integer idExcluir)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM catalogos.productos
                WHERE codigo = ?
                """;

        if (idExcluir != null) {
            sql += " AND id_producto <> ?";
        }

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, codigo);

            if (idExcluir != null) {
                ps.setInt(2, idExcluir);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void asignarParametrosFormulario(PreparedStatement ps)
            throws SQLException {

        ps.setString(1, txtCodigo.getText().trim());
        ps.setString(2, txtNombre.getText().trim());
        ps.setString(3, txtDescripcion.getText().trim());
        ps.setBigDecimal(4, new BigDecimal(txtPrecio.getText().trim()));
        ps.setInt(5, Integer.parseInt(txtStock.getText().trim()));
        ps.setDate(6, Date.valueOf(dpFecha.getValue()));
        ps.setInt(7, cmbCategoria.getValue().getId_categoria());
        ps.setInt(8, cmbMarca.getValue().getId_marca());
    }

    @FXML
    private void guardarProducto() {
        if (!validarFormulario()) {
            return;
        }

        String sql = """
                INSERT INTO catalogos.productos
                    (codigo, nombre, descripcion, precio, stock,
                     fecha_creacion, id_categoria, id_marca, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)
                """;

        try {
            if (existeCodigo(txtCodigo.getText().trim(), null)) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Código duplicado",
                        "Ese código ya pertenece a un producto, "
                                + "incluso si está desactivado.");
                return;
            }

            try (Connection conexion = ConexionBD.conectar();
                 PreparedStatement ps = conexion.prepareStatement(sql)) {

                asignarParametrosFormulario(ps);

                if (ps.executeUpdate() > 0) {
                    mostrarAlerta(Alert.AlertType.INFORMATION,
                            "Registro guardado",
                            "El producto fue registrado correctamente.");

                    nuevoProducto();
                    cargarProductos();
                }
            }

        } catch (SQLException e) {
            manejarErrorEscritura("No se pudo guardar el producto.", e);
        }
    }

    @FXML
    private void actualizarProducto() {
        Productos seleccionado =
                tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Producto no seleccionado",
                    "Seleccioná un producto de la tabla.");
            return;
        }

        if (!validarFormulario()) {
            return;
        }

        String sql = """
                UPDATE catalogos.productos
                SET codigo = ?, nombre = ?, descripcion = ?,
                    precio = ?, stock = ?, fecha_creacion = ?,
                    id_categoria = ?, id_marca = ?
                WHERE id_producto = ? AND estado = 1
                """;

        try {
            if (existeCodigo(txtCodigo.getText().trim(),
                    seleccionado.getId_producto())) {

                mostrarAlerta(Alert.AlertType.WARNING,
                        "Código duplicado",
                        "El código pertenece a otro producto.");
                return;
            }

            try (Connection conexion = ConexionBD.conectar();
                 PreparedStatement ps = conexion.prepareStatement(sql)) {

                asignarParametrosFormulario(ps);
                ps.setInt(9, seleccionado.getId_producto());

                if (ps.executeUpdate() > 0) {
                    mostrarAlerta(Alert.AlertType.INFORMATION,
                            "Registro actualizado",
                            "El producto fue actualizado correctamente.");
                } else {
                    mostrarAlerta(Alert.AlertType.WARNING,
                            "Sin cambios",
                            "El producto ya no existe o está desactivado.");
                }

                nuevoProducto();
                cargarProductos();
            }

        } catch (SQLException e) {
            manejarErrorEscritura("No se pudo actualizar el producto.", e);
        }
    }

    @FXML
    private void eliminarProducto() {
        Productos seleccionado =
                tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Producto no seleccionado",
                    "Seleccioná un producto de la tabla.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("Desactivar producto");
        confirmacion.setContentText(
                "¿Deseás desactivar " + seleccionado.getNombre() + "?");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        String sql = """
                UPDATE catalogos.productos
                SET estado = 0
                WHERE id_producto = ? AND estado = 1
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, seleccionado.getId_producto());

            if (ps.executeUpdate() > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Producto eliminado",
                        "El producto fue desactivado correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Sin cambios",
                        "El producto ya no existe o está desactivado.");
            }

            nuevoProducto();
            cargarProductos();

        } catch (SQLException e) {
            mostrarError("No se pudo eliminar el producto.", e);
        }
    }

    private void manejarErrorEscritura(String mensaje, SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Registro duplicado",
                    "Ya existe un producto con ese código o nombre.");
        } else {
            mostrarError(mensaje, e);
        }
    }

    private void mostrarAlerta(
            Alert.AlertType tipo, String titulo, String mensaje) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje, SQLException e) {
        mostrarAlerta(Alert.AlertType.ERROR,
                "Error de base de datos",
                mensaje + "\n" + e.getMessage());
    }
}