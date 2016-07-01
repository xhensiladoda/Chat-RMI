import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class passwordController {

	Utente utente;
	Stage passwordStage;
	Login myLogin;
	String vecchia_password = "";
	String nuova_password = "";
	String conferma_password = "";
	@FXML protected PasswordField vecchiapass;
	@FXML protected PasswordField nuovapass;
	@FXML protected PasswordField confermapass;
	@FXML protected Label message;
	
	public passwordController(){
		
	}
	
	void initData(Utente utente, Stage passwordStage){
		this.utente=utente;
		this.passwordStage=passwordStage;
		System.setProperty("java.security.policy", Main.PATH_SECURITY);
    	System.setSecurityManager(new RMISecurityManager());
    	activateService();
	}
	
	@FXML protected void close(){
		passwordStage.close();
	}
	
	@FXML protected void conferma() throws Exception{
		vecchia_password =  vecchiapass.getText();
		nuova_password = nuovapass.getText();
		conferma_password = confermapass.getText();
    	activateService();
    	if (checkIfOnline()){
	    	if (!vecchia_password.isEmpty()) {
		    	int checkpassword = myLogin.checkpassword(utente.getUsername(), vecchia_password);
		    	System.out.println("ho controllato la vecchia password: "+ checkpassword);
		    	if (checkpassword > 0 ){
		    		System.out.println("la vecchia password coincide con quella nel db");
		    		if (!nuova_password.isEmpty()){
		    			if (nuova_password.equals(conferma_password)){
		    				myLogin.changePass(utente.getUsername(), nuova_password);
		    				message.setText("* Modifiche avvenute con successo!");
		    				vecchiapass.setText("");
		    				nuovapass.setText("");
		    				confermapass.setText("");		    				
		    			}
		    			else message.setText("* Nuove password non coincidenti");
		    		}
		    		else message.setText("* Inserisci nuova password");
		    	}
		    	else {
		    		message.setText("* La vecchia password non coincide con quella memorizzata");
		    	}
	    	}
	    	else message.setText("* Inserisci vecchia password");
    	}
    	else
    		openPopup("Il server non è al momento raggiungibile. Riprova più tardi!", "img/about.png");
    	
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
}
