import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.imageio.ImageIO;


public class tabBachecaController {

	@FXML public AnchorPane scrollBacheca;
	@FXML public Button pubblica;
	@FXML public ListView<Stato> panel;
	@FXML public TextArea textStato;
	
	/*
	 * Indici per lo split
	 *  0 = numero_post; 1 = id(int); 2= utente, 3=contenuto, 4=tipo(int)
	 */
	public static final int NUMERO=0;
	public static final int ID=1;
	public static final int UTENTE=2;
	public static final int CONTENUTO=3;
	public static final int TIPO=4;
	
	private Bacheca myBacheca;
	private String utente_login;
	private DbLocale db ;
	public Timestamp lastDate;
	private ObservableList<Utente> amici;
	private secondSceneController secondScene;
	private ObservableList<Stato> stati;
	private ObservableList<Stato> stati_temp;
	private Tab tabCommenti;
	
	
	public tabBachecaController(){
		System.setProperty("java.security.policy",Main.PATH_SECURITY);
		System.setSecurityManager(new RMISecurityManager());
		activateService();
	}
	
	public void activateService(){
		/*
		 * Attivo il servizio per il prelievo dei dati degli amici
		 */
		try {
			myBacheca = (Bacheca)Naming.lookup(Main.service_BACHECA);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void initData(String utente_login, ObservableList<Utente> amici, secondSceneController secondScene) throws Exception{
		this.amici = amici;
		this.utente_login=utente_login;
		this.secondScene=secondScene;
		Date date= new Date();
		lastDate = new Timestamp(date.getTime());
		System.out.println(lastDate);
		db = new DbLocale(utente_login);
		db.open();
		activateService();
		caricaPost();
		insert_post();
		new MyBacheca(this,secondScene).start();
	}
	
	/*
	 * Prelevo i post dal db_locale e li carico nel db_server
	 */
	public void caricaPost() throws Exception, Exception{
		activateService();
		db.open();
		ResultSet post = db.getPost();
		while (post.next()){
			String contenuto = post.getString("Contenuto");
			if (!contenuto.isEmpty() || contenuto != null){
				myBacheca.setNewStatoData(utente_login, post.getString("Contenuto"), post.getTimestamp("Data"));
				db.removePost(utente_login,  post.getString("Contenuto"));
			}
		}
		db.close();
	}
	
	public void update_amici(ObservableList<Utente> amici){
		this.amici = amici;
	}
	
	private void insert_post() throws Exception{
		stati = FXCollections.observableArrayList();
		activateService();
		if (checkIfOnline()){
			//il server è online
			String stramici = "";
			for (int i=0; i<amici.size();i++){
				stramici += amici.get(i).getUsername() +",";
			}
			myBacheca.initData(utente_login, stramici);
			int numero_post = myBacheca.getNumero();
			if (numero_post>0){
				if (numero_post>7) numero_post=7;
				lastDate = myBacheca.getData(0);
				for (int i=0; i<numero_post;i++){
					String[] split = myBacheca.getStato(i).split(secondScene.TAB);
					// 0 = numero_post; 1 = id(int); 2= utente, 3=contenuto, 4=tipo(int)
					int id = Integer.parseInt(split[ID]);
					int tipo = Integer.parseInt(split[TIPO]);
					String img = secondScene.dir_immagini+ "/"+split[UTENTE]+".jpg";
					String image = checkImage(img, split[UTENTE]);
					stati.add(new Stato(id, split[UTENTE], split[CONTENUTO], tipo, myBacheca.getData(i), image));
					
				}    
				panel.setItems(stati);
				panel.setCellFactory(new Callback<ListView<Stato>, ListCell<Stato>>() {
		            @Override 
		            public ListCell<Stato> call(ListView<Stato> list) {
		            	return new ListStatoCell();
		            }
		        }
				);
				MenuItem mnuCommenti = setMenuCommenti();
				MenuItem mnuRimuoviPost = setMenuRimuoviPost();
				panel.setContextMenu(new ContextMenu(mnuCommenti, mnuRimuoviPost));
			}
		}
		else {
			openPopup("Il server non è al momento raggiungibile. Riprova più tardi!", "img/about.png");
		}
	}
	
	public String checkImage(String filename, String user) throws Exception{
		String img = filename;
		File f = new File(filename);
		if (!f.exists()){
			if (checkIfOnline()){
				byte[] image = myBacheca.getPhotoOf(user);
				if (image.length>0){
					InputStream in = new ByteArrayInputStream(image);
					BufferedImage bImageFromConvert = ImageIO.read(in);
					ImageIO.write(bImageFromConvert, "jpg", new File(filename));
				}
				else
					img = Main.PATH_DEFAULT_IMAGE;
			}
			else
				img = Main.PATH_DEFAULT_IMAGE;
		}
		return img;
	}
	/*
	 * Controlla se il server è online
	 */
	public boolean checkIfOnline() throws Exception
	{
	    boolean online = false;
	    try {
	    	online = myBacheca.isOnline();	
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
	 * Controlla se il server è online
	 */
	public boolean checkIfOnline(Commenti myCommenti) throws Exception
	{
	    boolean online = false;
	    try {
	    	online = myCommenti.isOnline();	
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
	
	private MenuItem setMenuRimuoviPost(){
		MenuItem mnuRimuoviPost = new MenuItem("Rimuovi post", new ImageView(new Image("img/remove.png")));
		mnuRimuoviPost.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	Stato item_selected = panel.getSelectionModel().getSelectedItem();
		    	try {
	    			if (item_selected.getUtente().equals(utente_login))
	    				deletePost(item_selected);
		    		
				} catch (Exception e) {
					e.printStackTrace();
				}
		    }
		});
		return mnuRimuoviPost;
	}
	
	public void deletePost(Stato item) throws RemoteException, Exception{
		activateService();
		if (checkIfOnline()){
			int rimosso = myBacheca.removePost(item.getId(), utente_login);
			if (rimosso > 0){
				stati.remove(item);
		        refresh();
		        openPopup("Post eliminato!", "img/post.png");
			}
		}
		else {
			openPopup("Il server non è al momento raggiungibile. Riprova più tardi!", "img/about.png");
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
	
	/*
	 * Aggiornamento Bacheca, con server online
	 */
	public void refresh() throws Exception{
		Platform.setImplicitExit(false);
		Platform.runLater(new Runnable() {
		     @Override public void run() {
		    	  try {
		    		  stati.removeAll(stati);
		    		  textStato.setText("");
		    		  db.open();
	    			  activateService();
	    			  caricaPost();
		    		  insert_post();
				} catch (Exception e) {
					e.printStackTrace();
				} 
		      }
		    });
	}

	/*
	 * Funzione utilizzata quando un utente vuole inserire un nuovo post
	 * 
	 */
	@FXML protected void add_post() throws Exception{
		activateService();
		// se il campo TextStato è vuoto, non inserisco nulla
		if (!textStato.getText().isEmpty()){
			if (checkIfOnline()){
				//il server è online quindi inserisco lo stato nel db_server
				myBacheca.setNewStato(utente_login, textStato.getText());
				refresh();
			}
			else {
				//il server non è online inserisco lo stato nel db_locale
				//appena il server sarà di nuovo disponibile lo salverò nel db_server
				db.open();
				Calendar calendar = Calendar.getInstance();
				java.util.Date now = calendar.getTime();
				java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
				String testo = textStato.getText();
				db.insertPost(utente_login, testo, 1,  currentTimestamp);
				openPopup("Post inserito! Non vedrai subito le modifiche perchè il server non è al momento raggiungibile!"
						, "img/post.png");
				textStato.setText("");
				//0 = numero_post; 1 = id(int); 2= utente, 3=contenuto, 4=tipo(int)
				String img = "C:/progettoRMI/"+utente_login+"/immagini/"+utente_login+".jpg";
				String image = checkImage(img, utente_login);
				stati_temp = FXCollections.observableArrayList();;
				stati_temp.add(new Stato(1, utente_login, testo, 1, currentTimestamp, image));
				stati_temp.addAll(stati);
				stati.removeAll(stati);
				stati.addAll(stati_temp);
				db.close();
			}
		}
		else 
			System.out.println("Stato vuoto: non inserisco nulla");
	}
	
	
	private MenuItem setMenuCommenti(){
		MenuItem mnuCommenti = new MenuItem("Visualizza commenti", new ImageView(new Image("img/post.png")));
		mnuCommenti.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	try {
					setTabCommenti();
				} catch (Exception e) {
					e.printStackTrace();
				}
		    }
		});
		return mnuCommenti;
	}
	
	public void setTabCommenti() throws Exception{
		Stato item_selected = panel.getSelectionModel().getSelectedItem();
		Commenti myCommenti = (Commenti)Naming.lookup(Main.service_COMMENTI);
		if (checkIfOnline(myCommenti)){
			FXMLLoader loader = new  FXMLLoader(getClass().getResource("fxml/tabCommenti.fxml"));
			Node pane = null;
			try {
				pane = loader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
			tabCommenti = new Tab();
			tabCommenti.setText("Post");
			tabCommenti.setStyle("-fx-background-color:#F7F7F7;");
			tabCommenti.setContent(pane);
	        secondScene.tabpane.getTabs().addAll(tabCommenti);
	        tabCommentiController tabCommentiContr = loader.<tabCommentiController>getController();
	        try {
	        	tabCommentiContr.initData(item_selected, utente_login);
			} catch (Exception e) {
				e.printStackTrace();
			}
	        SingleSelectionModel<Tab> selectionModel =  secondScene.tabpane.getSelectionModel();
	        selectionModel.select(tabCommenti);
		}
		else{
			openPopup("Il server non è al momento raggiungibile. Riprova più tardi!", "img/about.png");
		}
	}
	
	/*
	 * Prelevo i commenti dal db_locale e li carico nel db_server
	 */
	public void caricaCommenti() throws Exception, Exception{
		Commenti myCommenti = (Commenti)Naming.lookup(Main.service_COMMENTI);
		if (checkIfOnline(myCommenti)){
			db.open();
			ResultSet comment = db.getComment();
			while (comment.next()){
				String contenuto = comment.getString("Contenuto");
				if (!contenuto.isEmpty() || contenuto != null){
					myCommenti.setNewCommentoData(utente_login, comment.getString("Contenuto"), comment.getInt("idBacheca"),comment.getTimestamp("Data") );
					db.removeComment(utente_login,  comment.getString("Contenuto"), comment.getInt("idBacheca"));
				}
			}
			db.close();
		}
	}
	
}
