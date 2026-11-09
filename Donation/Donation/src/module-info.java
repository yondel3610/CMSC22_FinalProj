module Donation {
	requires javafx.controls;
	
	opens application to javafx.graphics, javafx.fxml;
	
	opens manager to javafx.base;
}
