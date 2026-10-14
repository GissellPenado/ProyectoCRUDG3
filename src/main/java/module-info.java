module com.tuempresa.proyectocrudg3 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.tuempresa.proyectocrudg3 to javafx.fxml;
    exports com.tuempresa.proyectocrudg3;

    requires java.sql;
    requires org.postgresql.jdbc;
}