

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


public class popupController {
	
	@FXML public Label lbltesto;
	@FXML public ImageView imgPopup;
	Stage popupStage;
	

	public popupController(){
		
	}
	
	public void initData(Stage popupStage, String testo){
		this.popupStage = popupStage;
		lbltesto.setText(testo);
		
	}
	
	@FXML protected void close() throws Exception{
		popupStage.close();
	}
}
