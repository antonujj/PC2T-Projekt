module SemestralniProjekt {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.base;
	requires javafx.graphics;
	requires java.sql;
	
	opens me.antonujj to javafx.graphics, javafx.fxml;
    opens me.antonujj.Backend to javafx.base;
}
