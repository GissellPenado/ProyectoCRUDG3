package com.tuempresa.proyectocrudg3.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/bdventas";

    private static final String USUARIO = "postgres";

    private static final String PASSWORD = "TU_CONTRASEÑA";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }

    // Prueba de conexión
    public static void main(String[] args) {
        try (Connection conexion = conectar()) {
            System.out.println(
                    "Conexión exitosa a: " + conexion.getCatalog()
            );
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }
}