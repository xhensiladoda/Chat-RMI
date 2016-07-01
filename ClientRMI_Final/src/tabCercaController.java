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


public class tabCercaController {

	/*
	 * Indici per split
	 * 0=username , 1=email, 2=ip, 3=port, 
	 */
	public static final int USERNAME =0;
	public static final int EMAIL = 1;
	public static final int IP = 2;
	public static final int PORT = 3;
	
	@FXML public Label lblSearch;
	@FXML public ListView<Utente> table;
	TabPane tabpane;
	String valore_ricerca;
	String utente_login;
	Ricerca myRicerca;
	ObservableList<Utente> amici_ricerca;
	
	public tabCercaController(){
		System.setProperty("java.security.policy",Main.PATH_SECURITY);
		System.setSecurityManager(new RMISecurityManager());
		activateService();
	}
	
	public void activateService(){
		/*
		 * Attivo il servizio per il prelievo dei dati degli amici
		 */
		try {
			myRicerca = (Ricerca)Naming.lookup(Main.service_RICERCA);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void initData(String valore, String utente_login, TabPane tabpane) throws Exception{
		this.valore_ricerca = valore;
		this.utente_login = utente_login;
		this.tabpane = tabpane;
		lblSearch.setText("Hai cercato: \""+ valore_ricerca + "\"");
        setItemsTable();
	}
	
	public void setValoreRicerca(String valore){
		lblSearch.setText("Hai cercato: \""+ valore+ "\"");
		valore_ricerca = valore;
	}
	
	public void setUtente(String utente_login){
		this.utente_login = utente_login;
	}
	
	public void setItemsTable() throws Exception{
		amici_ricerca = FXCollections.observableArrayList();
		activateService();
		if (checkIfOnline()){
			String stramici = myRicerca.search(valore_ricerca, utente_login);
			if (stramici.isEmpty()){
				table.setVisible(false);
				lblSearch.setText("Nessun risultato per \""+ valore_ricerca + "\"");
			}
			else {
				table.setVisible(true);
				for (int i=0; i<myRicerca.getNumeroNewAmici();i++){
					String[] split = myRicerca.getRicerca(i).split(secondSceneController.TAB);
					//0=username , 1=email, 2=ip, 3=port,
					int port = Integer.parseInt(split[PORT]);
					amici_ricerca.add(new Utente(split[USERNAME], split[EMAIL], 
							myRicerca.getImmagine(i),  split[IP], port , myRicerca.getData(i)));
				}
		        table.setItems(amici_ricerca);
		        table.setCellFactory(new Callback<ListView<Utente>, ListCell<Utente>>() {
		        	 @Override 
		                public ListCell<Utente> call(ListView<Utente> list) {
		                	return new ListUtenteCell();
		                }
		        }
				);
		        //MenuItem mnuProfilo = setMenuProfilo();
		        MenuItem mnuAggiungi = setMenuAggiungi();
		        //table.setContextMenu(new ContextMenu(mnuProfilo, mnuAggiungi));
		        table.setContextMenu(new ContextMenu(mnuAggiungi));
			}
		}
		else {
			lblSearch.setText("Server al momento non ragiungibile. Riprova più tardi!");
			table.setVisible(false);
		}
	}
	
	public MenuItem setMenuAggiungi(){
		MenuItem mnuAggiungi = new MenuItem("Aggiungi come amico", new ImageView(new Image("img/add.png")));
		mnuAggiungi.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	try {
		    		// ottengo il nome dell'utente selezionato nella listView
		    		Utente user_selected = table.getSelectionModel().getSelectedItem();
		    		String item_selected = user_selected.getUsername();
		    		activateService();
		    		if (checkIfOnline()){
		    			Boolean aggiunto = myRicerca.addFriend(item_selected);
						if (aggiunto){
					        amici_ricerca.remove(user_selected);
					        if (amici_ricerca.isEmpty()){
					        	table.setVisible(false);
								lblSearch.setText("Nessun altro risultato per \""+ valore_ricerca + "\"");
					        }
					        openPopup("La tua richiesta d'amicizia è andata a buon fine! Ora aspetta che l'utente accetti la richiesta!", "img/post.png");
						}
		    		}
		    		else openPopup("Server al momento non raggiungibile. Riprova più tardi!", "img/about.png");
				} catch (Exception e) {
					e.printStackTrace();
				} 
		    }
		});
		return mnuAggiungi;
	}
	
	/*
	 * Controlla se il server è online
	 */
	public boolean checkIfOnline() throws Exception
	{
	    boolean online = false;
	    try {
	    	online = myRicerca.isOnline();	
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
	
	public void update() throws Exception{
		amici_ricerca.removeAll(amici_ricerca);
		setItemsTable();
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
