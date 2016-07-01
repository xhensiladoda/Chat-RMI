import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;


public class modificaController {

	@FXML public TextField username;
	@FXML public PasswordField password;
	@FXML public TextField email;
	@FXML public ImageView immagineProfilo;
	@FXML public Button ok;
	@FXML public Button conferma;
	@FXML public Button annulla;
	@FXML public Button changeImm;
	
	Stage profiloStage;
	Utente utente;
	String path_immagine;
	String dir_principale = "C:/progettoRMI";
	String dir_utente ="";
	String dir_immagini ="";
	Login myLogin;
		
	public modificaController(){
		System.setProperty("java.security.policy", Main.PATH_SECURITY);
		System.setSecurityManager(new RMISecurityManager());
		activateService();
	}
	
	/*
	 * Controlla se il server è online
	 */
	public boolean checkIfOnline() throws Exception
	{
	    boolean online = false;
	    activateService();
	    try {
	    	online = myLogin.isOnline();	
	    } catch (RemoteException e) {
	        System.out.println(e.getMessage());
	        return false;
	    } catch (NotBoundException e) {
	        System.out.println(e.getMessage());
	        return false;
	    }
	    return online;
	}
	
	public void activateService(){
		/*
		 * Attivo il servizio per il prelievo dei dati degli amici
		 */
		try {
			myLogin = (Login)Naming.lookup(Main.service_LOGIN);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void initData(Utente utente, Stage profiloStage) throws Exception{
		this.profiloStage = profiloStage;
		this.utente = utente;
		username.setText(utente.getUsername());
		password.setText("password");
		email.setText(utente.getEmail());
		conferma.setVisible(false);
		annulla.setVisible(false);
		activateService();
		ByteArrayInputStream bais = new ByteArrayInputStream(utente.getImmagine());
        BufferedImage image = null;
		try {
			image = ImageIO.read(bais);
		} catch (IOException e) {
			e.printStackTrace();
		}
        Image imag = SwingFXUtils.toFXImage(image, null);
        immagineProfilo.setImage(imag);
	}
	
	@FXML protected void changeUser() throws Exception{
		
	}
	
	@FXML protected void changeEmail() throws Exception{
		
	}
	
	@FXML protected void changePassword() throws Exception{
		Stage passStage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/modificaPassword.fxml"));     
		Parent node = null;
		try {
			node = loader.load();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Scene scene = new Scene(node);
		passStage.setTitle("Modifica password");
		passStage.getIcons().add(new Image("img/refresh.png"));
		scene.getStylesheets().add("application.css");
		passwordController tabContr = loader.<passwordController>getController();
        try {
        	tabContr.initData(utente, passStage);
		} catch (Exception e) {
			e.printStackTrace();
		}
        passStage.setScene(scene);
        passStage.show();
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
        File immagine_file = fileChooser.showOpenDialog(profiloStage.getScene().getWindow());
        if (immagine_file != null) {
            path_immagine = immagine_file.getAbsolutePath();
    	    BufferedImage image = ImageIO.read(immagine_file);
    	    Image imag = SwingFXUtils.toFXImage(image, null);
    	    immagineProfilo.setImage(imag);
    		conferma.setVisible(true);
    		annulla.setVisible(true);
        }
	}
	
	@FXML protected void conferma() throws Exception{
		activateService();
		if (checkIfOnline()){
			if (path_immagine!=null){
	    		int caricato = myLogin.uploadFile(path_immagine, utente.getUsername());
	    		if (caricato==1){
	    			File f = new File(dir_principale+ "\\"+utente.getUsername()+"\\immagini\\"+utente.getUsername()+".jpg");
	    			if (f.delete()) System.out.println("Immagine vecchia cancellata ");
	    			salvaImmagineProfilo(path_immagine);
	    			conferma.setVisible(false);
	        		annulla.setVisible(false);
	                if (f.exists()) {
	            	    BufferedImage image = ImageIO.read(f);
	            	    Image imag = SwingFXUtils.toFXImage(image, null);
	            	    immagineProfilo.setImage(imag);
	                }
	    		}
	    		else System.out.println("Immagine NON caricata");
			}   
		}
		else 
			openPopup("Il server non è al momento raggiungibile. Riprova più tardi!", "img/about.png");
	}
	
	void salvaImmagineProfilo(String immagine){
		
		File cartella = new File(dir_principale);
		if (!cartella.exists()) cartella.mkdirs();
		
		dir_utente = dir_principale + "/" + utente.getUsername();
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
		File dest = new File (dir_principale+ "\\"+utente.getUsername()+"\\immagini\\"+utente.getUsername()+".jpg");
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
	
	/*
	 * Funzione per l'apertura di una nuova finestra per visualizzare un messaggio all'utente
	 */
	public void openPopup(String testo, String imm) throws Exception{
		Stage popupStage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/popup.fxml"));     
		Parent node = loader.load();
		Scene scene = new Scene(node,300,112);
		popupStage.setTitle("Informazione");
		popupStage.getIcons().add(new Image(imm));
		popupController popupContr = loader.<popupController>getController();
		try {
        	popupContr.initData(popupStage, testo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		popupStage.setScene(scene);
		popupStage.show();
	}
	
	@FXML protected void annulla() throws IOException{
		conferma.setVisible(false);
		annulla.setVisible(false);
		File immagine_file = new File (dir_principale+ "\\"+utente.getUsername()+"\\immagini\\"+utente.getUsername()+".jpg");
        if (immagine_file.exists()) {
    	    BufferedImage image = ImageIO.read(immagine_file);
    	    Image imag = SwingFXUtils.toFXImage(image, null);
    	    immagineProfilo.setImage(imag);
        }
	}
	
	@FXML protected void close() throws Exception{
		profiloStage.close();
	}
}
