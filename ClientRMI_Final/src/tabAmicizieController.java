import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;


public class tabAmicizieController {

	/*
	 * Indici per split
	 * 0 numero, 1=username, 2=email, 3=ip, 4=port
	 */
	public static final int NUMERO = 0;
	public static final int USERNAME = 1;
	public static final int EMAIL = 2;
	public static final int IP = 3;
	public static final int PORT = 4;
	
	@FXML public Label lblSearch;
	@FXML public ListView<Utente> viewamicizie;
	
	TabPane tabpane;
	
	String valore_ricerca;
	String utente_login;
	NuoveAmicizie myNuoviAmici;
	secondSceneController secondContr;
	ObservableList<Utente> nuoviamici;
	
	public tabAmicizieController(){
		System.setProperty("java.security.policy",Main.PATH_SECURITY);
		System.setSecurityManager(new RMISecurityManager());
		activateService();
	}
	
	public void activateService(){
		/*
		 * Attivo il servizio per il prelievo dei dati degli amici
		 */
		try {
			myNuoviAmici = (NuoveAmicizie)Naming.lookup(Main.service_NUOVI_AMICI);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void initData(String utente_login, secondSceneController secondSceneController) {
		this.utente_login=utente_login;
		this.secondContr= secondSceneController;
		try {
			setItemsTable();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void setItemsTable() throws Exception{
		nuoviamici = FXCollections.observableArrayList();
		activateService();
		//se il server è online
		if (checkIfOnline()){
			myNuoviAmici.initData(utente_login);
			int numero = myNuoviAmici.getNumeroNewAmici();
			if (numero<=0){
				viewamicizie.setVisible(false);
				lblSearch.setText("Nessuna nuova amicizia");
				secondContr.buttonAmicizia2.setText("0");
			}
			else {
				viewamicizie.setVisible(true);
				for (int i=0; i<numero;i++){
					String[] split = myNuoviAmici.getNewFriend(i).split(secondSceneController.TAB);
					//0 numero, 1=username, 2=email, 3=ip, 4=port
					int port = Integer.parseInt(split[PORT]);
					nuoviamici.add(new Utente(split[USERNAME], split[EMAIL], myNuoviAmici.getImmagine(i), 
							split[IP], port, myNuoviAmici.getData(i)));
				}    
				viewamicizie.setItems(nuoviamici);
				viewamicizie.setCellFactory(new Callback<ListView<Utente>, ListCell<Utente>>() {
	                @Override 
	                public ListCell<Utente> call(ListView<Utente> list) {
	                	return new ListUtenteCell();
	                }
		        }
				);
				MenuItem mnuAggiungi = setMenuAggiungi();
				viewamicizie.setContextMenu(new ContextMenu(mnuAggiungi));
			}
		}
		else  {
			secondContr.buttonAmicizia2.setText("0");
			lblSearch.setText("Server al momento non raggiungibile. Riprova più tardi!");
			viewamicizie.setVisible(false);
		}
	}
	
	/*
	 * Controlla se il server è online
	 */
	public boolean checkIfOnline() throws Exception
	{
	    boolean online = false;
	    try {
	    	online = myNuoviAmici.isOnline();	
	    } catch (RemoteException e) {
	        System.out.println(e.getMessage());
	        return false;
	    } catch (NotBoundException e) {
	        System.out.println(e.getMessage());
	        return false;
	    } catch (UnknownHostException e) {
	        e.printStackTrace();
	    }
	    return online;

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
	
	public MenuItem setMenuAggiungi(){
		MenuItem mnuAggiungi = new MenuItem("Accetta richiesta d'amicizia", new ImageView(new Image("img/add.png")));
		mnuAggiungi.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	try {
		    		Utente user_selected = viewamicizie.getSelectionModel().getSelectedItem();
		    		String item_selected = user_selected.getUsername();
		    		activateService();
		    		if (checkIfOnline()){
		    			myNuoviAmici.accetta_amicizia(item_selected);
		    			nuoviamici.remove(user_selected);
				        if (nuoviamici.isEmpty()){
				        	viewamicizie.setVisible(false);
							lblSearch.setText("Nessuna nuova amicizia");
							secondContr.buttonAmicizia2.setText("0");				        
						}
				        secondContr.buttonAmicizia2.setText(""+nuoviamici.size());
				        secondContr.updateAmicizie(user_selected);
				        openPopup("Richiesta d'amicizia accettata!", "img/amicizia.png");
		    		}
		    		else
		    			 openPopup("Server al momento non raggiungibile.Riprova più tardi!", "img/amicizia.png");
			        
				} catch (Exception e) {
					e.printStackTrace();
				} 
		    }
		});
		return mnuAggiungi;
	}
}
