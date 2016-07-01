import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

@SuppressWarnings("deprecation")
public class registrationController {
	
	@FXML public StackPane stackPane;
	@FXML private TextField username;
	@FXML private PasswordField password;
	@FXML private PasswordField confermapassword;
	@FXML private TextField email;
	@FXML private ImageView immagineProfilo;
	
	@FXML private Label msgusername;
	@FXML private Label msgpassword;
	@FXML private Label msgconfermapassword;
	@FXML private Label msgregistr;
	
	Stage thirdStage;
	String path_immagine;
	
	String dir_principale = "C:/progettoRMI";
	String dir_utente ="";
	String dir_immagini ="";
	
	
	void initData(Stage thirdStage){
		this.thirdStage=thirdStage;
		File immagine_default = new File(Main.PATH_DEFAULT_IMAGE);
    	BufferedImage image;
		try {
			image = ImageIO.read(immagine_default);
			Image imag = SwingFXUtils.toFXImage(image, null);
	        immagineProfilo.setImage(imag);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}
	
	/*
	 * Bottone "Carica Immagine"
	 * Apre il fileChooser per poter permettere all'utente di scegliere un immagine
	 */
	@FXML protected void uploadImage() throws RemoteException, Exception{
		final FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Seleziona un'immagine");
	    fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
            );
        File immagine_file = fileChooser.showOpenDialog(stackPane.getScene().getWindow());
        if (immagine_file != null) {
            path_immagine = immagine_file.getAbsolutePath();
    	    BufferedImage image = ImageIO.read(immagine_file);
    	    Image imag = SwingFXUtils.toFXImage(image, null);
    	    immagineProfilo.setImage(imag);
           
        }
        
	}
	

	/*
	 * Bottone cancella: resetta tutti i campi
	 */
	@FXML protected void cancella(){
		username.setText("");
		password.setText("");
		confermapassword.setText("");
		email.setText("");
		immagineProfilo.setImage(null);
	}

	/*
	 * Chiudo questo stage e riapro lo stage relativo al login (primaryScene.fxml)
	 */
	@FXML protected void BacktoLogin(){
		try {
			thirdStage.close();
            Stage primaryStage = new Stage();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/primaryScene.fxml"));     
			Parent node = loader.load();
			Scene scene = new Scene(node,475,600);
			primarySceneController primaryContr = loader.<primarySceneController>getController();
			primaryContr.initData(primaryStage);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Funzione per il controllo della lunghezza del campo USERNAME
	 * se è nulla, allora visualizzo messaggio "Campo obbligatorio"
	 */
	@FXML protected void checkLength(){
		if (username.getText().isEmpty()){
			msgusername.setText("Campo Obbligatorio");
		}
		else msgusername.setText("");
	}
	
	/*
	 * Funzione per il controllo della lunghezza del campo PASSWORD
	 * se è nulla, allora visualizzo messaggio "Campo obbligatorio"
	 */
	@FXML protected void checkpasswordlength(){
		if (password.getText().isEmpty()){
			msgpassword.setText("Campo Obbligatorio");
		}
		else msgpassword.setText("");
	}
	
	/*
	 * Bottone Conferma
	 * controllo dei valori inseriti dall'utente e registrazione dell'utente nel db
	 */
	@FXML protected void conferma(){
		String user = username.getText();
		String pwd = password.getText();
		String confermapwd = confermapassword.getText();
		Login myLogin = null;
			/*
			 * Se il campo Username è vuoto --> Errore: visualizzo un messaggio nel campo error_username per informare l'utente
			 */
			if (user.isEmpty()){
				msgusername.setText("Campo obbligatorio");
			}
			/*
			 * Altrimenti chiamo il servizio remoto checkUser che mi controlla se l'username inserito esiste già o meno
			 */
			else {
				System.setProperty("java.security.policy", Main.PATH_SECURITY);
				System.setSecurityManager(new RMISecurityManager());
		    	
		    	int retval = 0;
				try {
					myLogin = (Login)Naming.lookup(Main.service_LOGIN);
					retval = myLogin.checkUser(user);  
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			    
			    if (retval==0){
			    	msgusername.setText("Username già esistente");
			    }
			    else {
			    	msgusername.setText("");
			    	/*
			    	 * ho controllato l'username e va bene, ora controllo che le password coincidano
			    	 * in primis controllo se il campo password è vuoto.
			    	 * Se è vuoto --> errore: visualizzo messaggio nel campo messagge per informare l'utente
			    	 */
			    	if (pwd.isEmpty()){
			    		msgpassword.setText("Campo obbligatorio: inserisci password");
			    	}
			    	else {
			    		msgpassword.setText("");
			    		/*
			    		 * La password non è vuota, quindi controllo che sia uguale al campo conferma_password
			    		 */
				    	if (!pwd.equals(confermapwd)){
				    		msgconfermapassword.setText("Password non coincidenti");
				    	}
				    	else {
				    		msgconfermapassword.setText("");
				    		/*
				    		 * Se coincidono posso registrarmi nel db, dato che il campo Email e Immagine non sono obbligatori
				    		 * Controllo però prima se è stata inserita un'immagine
				    		 * in tal caso la dovrò caricare e salvare nel server
				    		 * 
				    		 */
				    		String url_immagine = "";
					    	try
					        {
					    		if (path_immagine!=null){
						    		int caricato = myLogin.uploadFile(path_immagine, user);
						    		if (caricato==1){
						    			
						    			salvaImmagineProfilo(path_immagine);
						    		}
						    		else System.out.println("Immagine NON caricata");
					    		}    		
					        } catch(Exception e)
					        { 
					        	System.err.println("System Exception " + e);
					        }
				    		/*
				    		 * Chiamo il servizio remoto registration che esegue una query INSERT nel db
				    		 * le query INSERT ritornano il numero di righe inserite
				    		 * per controllare che la query sia stata eseguita basta controllare che il num di righe inserite
				    		 * sia maggiore di 0
				    		 */
				    		int row_inserted = 0;
							try {
								String ip=InetAddress.getLocalHost().getHostAddress();
								int port=6666;
								row_inserted = myLogin.registration(user, pwd, email.getText(), url_immagine,ip,port);
							} catch (Exception e) {
								e.printStackTrace();
							}
				    		if (row_inserted>0) {
				    			msgregistr.setText("Registrazione avvenuta con successo");
		                		cancella();
				    		}
				    		else System.out.println("errore registrazione");
				    	}
			    	}
			    }
			}
	}
	
	
	void salvaImmagineProfilo(String immagine){
		
		File cartella = new File(dir_principale);
		if (!cartella.exists()) cartella.mkdirs();
		
		dir_utente = dir_principale + "/" + username.getText();
		System.out.println("Cartella utente "+ dir_utente);
		cartella = new File(dir_utente);
		if (!cartella.exists()) {
			boolean success = cartella.mkdirs();
			if (success) System.out.println("cartella creata");
			else System.out.println("cartella non creata");
		}
		dir_immagini = dir_utente + "/immagini";
		System.out.println("Cartella immagini "+ dir_immagini);
		cartella = new File(dir_immagini);
		if (!cartella.exists()) {
			boolean success = cartella.mkdirs();
			if (success) System.out.println("cartella creata");
			else System.out.println("cartella non creata");
		}
		File source = new File(immagine);
		File dest = new File (dir_principale+ "\\"+username.getText()+"\\immagini\\"+username.getText()+".jpg");
		InputStream input = null;
		OutputStream output = null;
        try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
	        byte[] buf = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = input.read(buf)) > 0) {
	            output.write(buf, 0, bytesRead);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
	        try {
				input.close();
		        output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}

}
