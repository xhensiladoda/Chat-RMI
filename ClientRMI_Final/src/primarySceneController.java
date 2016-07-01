import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class primarySceneController {

	@FXML private Text actiontarget;
	@FXML private TextField username;
	@FXML private PasswordField password;
	@FXML private AnchorPane content;
	@FXML private Label messagelbl;
	@FXML private Button signin;
	
	Stage primaryStage;
	Utente utente_login;
	Login myLogin;
	
	public primarySceneController(){
		
	}
	
	/*
	 * Funzione utilizzata per inizializzare delle variabili
	 */
	public void initData(Stage primaryStage){
		this.primaryStage=primaryStage;	
		System.setProperty("java.security.policy", Main.PATH_SECURITY);
    	System.setSecurityManager(new RMISecurityManager());
		/*
		 * Inserisco un EventHandler per rilevare il fatto che l'utente ha premuto un tasto
		 * Se il tasto è ENTER allora eseguo la funzione "handleSubmitButtonAction" che equivale a "sign in!" (vedi sotto)
		 */
		content.setOnKeyReleased(new EventHandler<KeyEvent>() {
	        @Override
	        public void handle(KeyEvent t) {
	            KeyCode key = t.getCode();
	            if (key == KeyCode.ENTER){
	            	try {
						handleSubmitButtonAction();
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }
	        }
	    });
		
		/*
		 * Inserisco un eventHandler per il bottone Sign in.
		 * Quando viene premuto si esegue la funzione "handleSubmitButtonAction"
		 */
		signin.setOnAction(new EventHandler<ActionEvent>() {
		    @Override 
		    public void handle(ActionEvent e) {
		    	try {
					handleSubmitButtonAction();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		    }
		});
	}
	
	/*
	 * Funzione per il controllo del login
	 */
    @FXML protected void handleSubmitButtonAction() {
		try {
			myLogin = (Login)Naming.lookup(Main.service_LOGIN);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
    	String user = username.getText();
		String pw = password.getText();
		int status = checkLogin(user, pw);
		// Se il login è andato a buon fine visualizzo la finestra principale chiudo quella del login
		if(status==1){
			utente_login = new Utente();
			try {
				// prelevo le informazioni dell'Utente che ha effettuato il login
				utente_login.setEmail(myLogin.getEmail());
				utente_login.setImmagine(myLogin.getImmagine());
				utente_login.setUsername(user);
				utente_login.setIp(myLogin.getIp());
				utente_login.setPort(myLogin.getPort());
			} catch (Exception e1) {
				messagelbl.setText("Server al momento non disponibile: riprova più tardi!");
				e1.printStackTrace();
				return;
			}
			primaryStage.close();
            Stage secondStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/secondScene.fxml"));     
			Parent node;
			try {
				node = loader.load();
				Scene scene = new Scene(node,700,600);
				scene.getStylesheets().add("application.css");
				secondStage.setScene(scene);
				secondStage.setTitle("ChatRMI");
				secondStage.getIcons().add(new Image("img/chatrmi.png"));
				secondStage.show();
				secondSceneController secondContr = loader.<secondSceneController>getController();
				try {
					secondContr.initData(utente_login, secondStage);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else if (status!=-1){
			System.out.println("Credenziali Sbagliate!");
			messagelbl.setText("Credenziali errate!");
		}

    }
    
    public int checkLogin(String user, String password){
    	int status = -1;
    	try {
			myLogin = (Login)Naming.lookup(Main.service_LOGIN);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
        try
        { 	
        	// controllo username e password inseriti dall'utente
    	    status = myLogin.checklogin(user);  
    	    status = myLogin.checkpassword(user,password);
        } catch(Exception e)
        { 
        	messagelbl.setText("Server al momento non disponibile!");
        	System.err.println("System Exception " + e);
        }
    	 return status; 
     }
    
    /*
	 * Funzione utilizzata per aprire lo Stage riferito alla registrazione di un utente
	 */
    @FXML protected void registration(){
    	try {
    		primaryStage.close();
            Stage thirdStage = new Stage();
            
			FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/registration.fxml"));     
			Parent node = loader.load();
			Scene scene = new Scene(node,475,600);
			thirdStage.setTitle("Registrazione");
			thirdStage.getIcons().add(new Image("img/chatrmi.png"));
			scene.getStylesheets().add("application.css");
			registrationController thirdContr = loader.<registrationController>getController();
			thirdContr.initData(thirdStage);
			thirdStage.setScene(scene);
			thirdStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
