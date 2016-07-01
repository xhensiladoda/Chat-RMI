
	
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	
	public static final String PATH_SECURITY = "file:///C://Users//xhensila//workspace_xf//ClientRMI_Final//src//security.policy";
	public static final String PATH_DEFAULT_IMAGE = "C:/Users/xhensila/workspace_xf/ClientRMI_Final/src/img/default.jpg";
	static final String service_LOGIN = "rmi://192.168.150.1:1099/myLoginRMI";
	static final String service_AMICI = "rmi://192.168.150.1:1099/myAmiciRMI";
	static final String service_NUOVI_AMICI = "rmi://192.168.150.1:1099/myNuoviAmiciRMI";
	static final String service_RICERCA = "rmi://192.168.150.1:1099/myRicercaRMI";
	static final String service_BACHECA = "rmi://192.168.150.1:1099/myBachecaRMI";
	static final String service_COMMENTI = "rmi://192.168.150.1:1099/myCommentiRMI";
	
	@Override
	public void start(Stage primaryStage) {
		try {
			System.setProperty("java.security.policy",PATH_SECURITY);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/primaryScene.fxml"));     
			Parent node = (Parent)loader.load();
			/*
			 * inizializzo variabili di primarySceneController
			 */
			primarySceneController primaryContr = loader.<primarySceneController>getController();
			primaryContr.initData(primaryStage);
			Scene scene = new Scene(node,475,600);
			primaryStage.setTitle("Welcome to ChatRMI");
			primaryStage.getIcons().add(new Image("img/chatrmi.png"));
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public static byte[] downloadFile(String fileName){
	      try {
	         File file = new File(fileName);
	         byte buffer[] = new byte[(int)file.length()];
	         BufferedInputStream input = new
	        	BufferedInputStream(new FileInputStream(fileName));
	         input.read(buffer,0,buffer.length);
	         input.close();
	         return(buffer);
	      } catch(Exception e){
	         System.out.println("FileImpl: "+e.getMessage());
	         e.printStackTrace();
	         return(null);
	      }
		}

}
