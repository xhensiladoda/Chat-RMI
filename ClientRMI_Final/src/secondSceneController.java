import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.imageio.ImageIO;


@SuppressWarnings("deprecation")
public class secondSceneController {

	// in questa cartella verranno salvate tutte le info degli amici (nel db), le immagini e la cronologia della chat
	public static final String PATH_PRINC = "C:/progettoRMI"; // da modificare anche nel TabCommentiController
	public static final String TAB = "-->";
	/*
	 * indici per lo split
	 * 0=Username, 1=Email, 2=Port, 3=Ip
	 */
	public static final int USERNAME = 0;
	public static final int EMAIL = 1;
	public static final int PORT = 2;
	public static final int IP = 3;
		
	@FXML public ListView<Utente> viewamici;
	@FXML public TabPane tabpane;
	@FXML public AnchorPane anchorpane;
	@FXML public Label labelUser;
	@FXML public TextField search;
	@FXML public Button buttonAmicizia2;
	@FXML public MenuItem mioprofilo;
	@FXML public MenuItem apribacheca;
	@FXML public MenuItem esci;
	@FXML public MenuItem about;
	
	Amici myAmici;
	ObservableList<Utente> amici;
	private Utente utente;
	private String utente_login;
	Stage secondStage;
	DbLocale db;
	
	Tab tabBacheca;	
	tabBachecaController tabBachecaContr;
	
	Tab tabCerca;
	tabCercaController tabCercaContr;
	
	Tab tabAmicizie;
	
	String dir_utente = "";
	String dir_immagini = "";
	
	/*
	 * variabili tab chatta
	 */
	Tab tabChatta;
	@FXML private Button send;
	@FXML private Button exit;
	@FXML private TextArea textarea;
	@FXML private ListView<String> viewchat;

	public static int my_port; /* port of my server*/
	private static String my_host; /* my host to connect to */
	private String item_selected;
	private Socket my_server_socket;
	private static Hashtable<String, tabChattaController> contr_chatt_list;
	private static Hashtable<String, Socket> socket_contr_list;
	private static ArrayList<String> connected_friend;
	
	public secondSceneController(){
		System.setProperty("java.security.policy",Main.PATH_SECURITY);
		System.setSecurityManager(new RMISecurityManager());
		activateService();
	}
	
	public void activateService(){
		/*
		 * Attivo il servizio per il prelievo dei dati degli amici
		 */
		try {
			myAmici = (Amici)Naming.lookup(Main.service_AMICI);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createDirectory(){
		// nella cartella progettoRMI viene creata una cartella con nome Utente
		dir_utente = PATH_PRINC + "/" + utente_login;
		// viene creata anche una cartella immagini per contenere le immagini degli amici
		dir_immagini = "C:/progettoRMI/"+utente_login+"/immagini";
		File cartella = new File(PATH_PRINC);
		if (!cartella.exists()) {
			cartella.mkdirs();
		}
		cartella = new File(dir_utente);
		if (!cartella.exists()) {
			cartella.mkdirs();
		}
		cartella = new File(dir_immagini);
		if (!cartella.exists()) {
			cartella.mkdirs();
		}
	}
	
	/*
	 * Inizializzazione parametri
	 */
	public void initData(Utente utente, Stage secondStage)throws Exception {
		activateService();
		this.utente=utente;
		this.utente_login=utente.getUsername();
		this.secondStage=secondStage;
		// creo le directory necessarie per salvare le informazioni in locale
		createDirectory();	
		final Text text = new Text("Ciao, "+utente_login);
		final double width = text.getLayoutBounds().getWidth();
		labelUser.setMinWidth(width);
		labelUser.setText("Ciao, "+utente_login);
		/*
		 * NUOVA FUNZIONE PER IL DB LOCALE
		 */
		db = new DbLocale(utente_login);
		checkAmici();
		initListView();
		
		// Imposto icone dei menu
		mioprofilo.setGraphic(new ImageView(new Image("img/amicizia.png")));
		apribacheca.setGraphic(new ImageView(new Image("img/post.png")));
		esci.setGraphic(new ImageView(new Image("img/exit.png")));
		about.setGraphic(new ImageView(new Image("img/about.png")));
		
		/*
		 * Bacheca
		 */
		setTabBacheca();
		setNumNuoveAmicizie();
		
		/*
		 * Chat
		 */
		my_host=utente.getIp();
		my_port=utente.getPort();
		System.out.println("IP: "+my_host +"   PORT: "+my_port);
		contr_chatt_list=new Hashtable<String, tabChattaController>();
		socket_contr_list=new Hashtable<String, Socket>();
		connected_friend=new ArrayList<String>();
		boolean server_online=hostAvailabilityCheck(my_host,my_port);
    	/* verifico se il mio server è già online */
    	if(server_online==false)
    		new MyServer().start();
    	/* questa è la socket del mio server */
    	Socket my_server=null; 
        try { /* mi collego al mio server */
            my_server =new Socket(my_host,my_port);
        } catch (UnknownHostException e) {
            System.err.println(e);
            System.exit(1);
        }
        this.my_server_socket=my_server;
        new BufferedReader(new InputStreamReader(System.in));
        /* output stream del server */
        DataOutputStream out_my_server = new DataOutputStream(my_server.getOutputStream());
        /* input stream al server */
        DataInputStream in_my_server = new DataInputStream(my_server.getInputStream());
        /* Mi registro nel mio sever.
        * Non controllo se il nome si ripete siccome
        * mi registro con lo user che è unico. */
        addNick(utente_login,utente_login,in_my_server,out_my_server);
        socket_contr_list.put(utente_login, my_server_socket);
        /*thread che legge i messaggi in modo asincrono dal mio server */
        ServerConn sc = new ServerConn(my_server);
        Thread thread = new Thread(sc);
        thread.start();
        this.secondStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
            	terminateThreads();
            }
        });

	}
	
	public void setNumNuoveAmicizie() throws Exception{
		NuoveAmicizie myNuoviAmici = (NuoveAmicizie)Naming.lookup(Main.service_NUOVI_AMICI);
		myNuoviAmici.initData(utente_login);
		int numero_amicizie = myNuoviAmici.getNumeroNewAmici();
		buttonAmicizia2.setText(""+numero_amicizie);
	}
	
	/*
	 * Controlla se il server è online
	 */
	public boolean checkIfOnline() throws Exception
	{
	    boolean online = false;
	    try {
	    	online = myAmici.isOnline();	
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
	public boolean checkIfOnline(Login myLogin) throws Exception
	{
	    boolean online = false;
	    try {
	    	online = myLogin.isOnline();	
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
	 * Funzione per la creazione e l'aggiornamento del db_locale
	 */
	public void checkAmici() throws Exception{
		//controllo che il server centrale sia online
		activateService();
		if (checkIfOnline()){
			//System.out.println("il server è online");
			db.open();
			if (db.esiste){
				//il db locale esiste già, quindi devo aggiornarlo
				aggiornaDb();
			}
			else {
				//il db locale non esiste lo devo creare e riempire di dati
				// inizializzo i valori di myAmici sul server
				myAmici.initData(utente_login);
				// ora li prelevo
				for (int i=0; i<myAmici.getNumeroAmici();i++){
					/*
					 * indici per lo split
					 * 0=Username, 1=Email, 2=Port, 3=Ip
					 */
					String[] split = myAmici.getFriend(i).split(TAB);
				    int port = Integer.parseInt(split[2]);
					db.insertAmici(split[USERNAME],myAmici.getImmagine(i), split[EMAIL], split[IP], port, myAmici.getData(i));
				}
			}
			db.close();
			
		}
	}
	
	/*
	 * Funzione per l'aggiornamento del database locale
	 */
	public void aggiornaDb() throws Exception{
		activateService();
		// recupero la lista degli amici salvati nel db remoto (nel server)
		myAmici.initData(utente_login);
		
		/*
		 * Per ogni amico trovato nel db_server controllo se l'amico è già salvato nel db_locale.
		 * Se NON è salvato, allora inserisco le informazioni
		 * Altrimenti se è già memorizzato, controllo che la data (cioè la data delle ultime modifiche al profilo utente)
		 * del db_server sia > della data nel db_locale.
		 * Se è minore non faccio nulla. 
		 * Se è maggiore (quindi significa che ci sono stati aggiornamenti non salvati nel db_locale), cancello le
		 * vecchie informazioni dell'amico nel db_locale e inserisco quelle nuove
		 */
		for (int i=0; i<myAmici.getNumeroAmici(); i++){
			// recupero la lista degli amici salvati nel db_locale
			ResultSet rs_local = db.getAmici();
			boolean trovato = false;
			/*
			 * indici per lo split
			 * 0=Username, 1=Email, 2=Port, 3=Ip
			 */
			String[] split = myAmici.getFriend(i).split(TAB);
		    int port = Integer.parseInt(split[PORT]);
			// se l'utente è già inserito nel db_locale allora controllo che sia aggiornato
			while (rs_local.next()){
				if (split[USERNAME].equals(rs_local.getString("Username"))){
					// L'UTENTE è gia inserito nel dbLocale, devo controllare se ci sono state delle modifiche
					trovato = true;
					if (myAmici.getData(i).after(rs_local.getTimestamp("Data"))){
						//devo aggiornare
						//System.out.println(" ---- Aggiornamento db locale per l'utente "+user+" ---- ");
						//elimino la foto dalla cartella C:/progettoRM/nomeutente/immagini/nomeutente.jpg
						File f = new File(dir_immagini+"/"+split[USERNAME]+".jpg");
						if (f.delete()) System.out.println("Immagine dell utente "+split[USERNAME]+" cancellata");
						int aggiornato = db.aggiorna(split[USERNAME], myAmici.getImmagine(i), split[EMAIL], 
								split[IP], port, myAmici.getData(i));
						if (aggiornato>0)
							System.out.println("Utente "+ split[USERNAME] + " aggiornato con successo");
					}
				}
			}
			if (!trovato) {
				//se l'utente è tra gli amici, ma non ancora salvato nel db_locale, lo salvo
				db.insertAmici(split[USERNAME], myAmici.getImmagine(i),split[EMAIL], 
						split[IP], port, myAmici.getData(i));
			}
		}
	}
	
	public void initListView() throws Exception{
		// amici = Collection<Utente>
		amici = FXCollections.observableArrayList();
		// gli attribuisco tutti gli elementi della ListView viewamici
		// e li elimino (nel caso ci fossero elementi non desiderati)
		amici = viewamici.getItems();
		amici.removeAll(amici);
		// connessione al db_locale
		db.open();
		// prelevo gli amici dal db_locale
		ResultSet rs = db.getAmici();
		// per ogni amico prelevato, inserisco una nuova istanza di Utente alla Collection amici
		while (rs.next()){
			//controllo che ci sia l'immagine dell utente nella cartella immagini
			// se non esiste la riscarico dal server centrale
			File f = new File(rs.getString("Immagine"));
			byte[] img = null;
			if (!f.exists()){
				Login myLogin = null;
				try {
					myLogin = (Login)Naming.lookup(Main.service_LOGIN);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (checkIfOnline(myLogin)){
					img = myLogin.getPhotoOf(rs.getString("Username"));
					if (img.length>0){
						InputStream in = new ByteArrayInputStream(img);
						BufferedImage bImageFromConvert = ImageIO.read(in);
						ImageIO.write(bImageFromConvert, "jpg", new File(rs.getString("Immagine")));
					}
					else
						img = Main.downloadFile(Main.PATH_DEFAULT_IMAGE);
				}
				else
					img = Main.downloadFile(Main.PATH_DEFAULT_IMAGE);
				
			}
			else img = Main.downloadFile(rs.getString("Immagine"));
			String username=rs.getString("Username");
			if(!username.equals(utente_login)){
				amici.add(new Utente(rs.getString("Username"),rs.getString("Email"),img,
						rs.getString("Ip"), rs.getInt("Port"), rs.getTimestamp("Data") ));
			}
		}
		// aggiungo gli elementi alla ListView "viewamici" (visualizzata nella barra laterale)
		db.close();
		viewamici.setItems(amici);
		viewamici.setCellFactory(new Callback<ListView<Utente>, ListCell<Utente>>() {                
			    @Override 
                public ListCell<Utente> call(ListView<Utente> list) {
                	return new ListUtenteCell();
                }
        	}
	    );
		/*
		 *  creo i due ContextMenu relativi alla listView.
		 *  quando verrà premuto il tasto destro del mouse su un elemento della listview
		 *  comparirà un menu con 3 opzioni: "Visualizza profilo", "Chatta", "Rimuovi amico"
		 *  ogni opzione aprirà il tab Profilo e il tab Chatta rispettivamente
		 */
		
		// se la listView è vuota non visualizzo i Context Menu relativi agli utenti, ma solo il context menu AGGIORNA
		if (!amici.isEmpty()) {
			//MenuItem mnuProfilo = setMenuProfilo();
			MenuItem mnuChatta = setMenuChatta();
			MenuItem mnuRimuovi = setMenuRimuovi();
			MenuItem mnuAggiorna = setMenuAggiorna();
			viewamici.setContextMenu(new ContextMenu(mnuChatta, mnuRimuovi, mnuAggiorna));
		}
		else {
			MenuItem mnuAggiorna = setMenuAggiorna();
			viewamici.setContextMenu(new ContextMenu(mnuAggiorna));
		}
	}
	
	/*
	 * Creazione TabCHatta
	 */
	public MenuItem setMenuChatta(){
		MenuItem mnuChatta = new MenuItem("Chatta",  new ImageView(new Image("img/chat.png")));
				
				/* Se premo tasto destro e poi chatta significa che sono io a collegarmi
				 * alla porta del mio amico.
				 */
				mnuChatta.setOnAction(new EventHandler<ActionEvent>(){
		
					public void handle(ActionEvent t){
						item_selected = viewamici.getSelectionModel().getSelectedItem().getUsername();
				    	Socket server = null;
						try {
							/*
					    	 * Controllo prima se l'amico a cui voglio connettermi
					    	 * non sia già connesso al mio server.
					    	 * Se non è già connesso allora apro un nuovo tab.
					    	 */
					    	if(!contr_chatt_list.containsKey(item_selected)){
					    		try {
					    			Address address=getPortIp(item_selected);
					    			server = new Socket(address.IP,address.PORT);
					    			System.out.println(item_selected+" --> IP= "+address.IP+" PORT="+address.PORT);
									/* obtain an output stream to the server... */
							        DataOutputStream out = new DataOutputStream(server.getOutputStream());
							        /* ... and an input stream */
							        DataInputStream in = new DataInputStream(server.getInputStream());
							        //mi registro nel server dell'amico
							        addNick(utente_login,item_selected,in,out);
							        socket_contr_list.put(item_selected, server);
							        ServerConn sc = new ServerConn(server);
							        Thread thread = new Thread(sc);
							        thread.start();
							        createTab(utente_login, item_selected);
								} catch (Exception e) {
									Platform.setImplicitExit(false);
				                    Platform.runLater(new Runnable() {
				                        public void run() {
				                        	try {
												openPopup("L'amico "+ item_selected+" non è connesso! Riprovare più tardi!", "img/about.png");
											} catch (Exception e) {
												e.printStackTrace();
											}
				                        }
				                    });
								}
							} else {
								int tab=checkTab("Chatta "+item_selected);
					    		System.out.println(tab);
					    		if(tab==-1){
					    			//creo una nuova tab ma senza bisogno di creare una nuova socket
					    			createTab(utente_login, item_selected);
					    		}
					    		else {
					    			//riporto in primo piano il tab già aperto
					    			SingleSelectionModel<Tab> selectionModel = tabpane.getSelectionModel();
							        selectionModel.select(tab);
					    		}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
				}});
				return mnuChatta;
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
	
	@FXML protected void refresh(){
		Platform.setImplicitExit(false);
		Platform.runLater(new Runnable() {
		     @Override public void run() {
		    	  try {
		    		checkAmici();
		      		initListView();
		      		setNumNuoveAmicizie();
				} catch (Exception e) {
					e.printStackTrace();
				} 
		      }
		    });
	}
	
	/*
	 * Aggiorna lista Amici
	 */
	public MenuItem setMenuAggiorna(){
		MenuItem mnuAggiorna = new MenuItem("Aggiorna", new ImageView(new Image("img/refresh.png")));
		mnuAggiorna.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	try {
		    		refresh();
				} catch (Exception e) {
					e.printStackTrace();
				}
		    	
		    }
		});
		return mnuAggiorna;
	}
	
	/*
	 * Rimuovi amico
	 */
	public MenuItem setMenuRimuovi(){
		MenuItem mnuRimuovi = new MenuItem("Rimuovi amico", new ImageView(new Image("img/remove.png")));
		mnuRimuovi.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		    	Utente item_selected = viewamici.getSelectionModel().getSelectedItem();
		    	try {
					deleteamico(item_selected);
				} catch (Exception e) {
					e.printStackTrace();
				}
		    	
		    }
		});
		return mnuRimuovi;
	}
	
	/*
	 * Ritorna indirizzo ip e porta di un utente
	 * ------ prelevato da db _locale ------
	 */
	public Address getPortIp(String username) throws SQLException{
		db.open();
		ResultSet rs = db.getPortIp(username);
		String ip;
		Address address=new Address("",0);
		int port;
		if (rs.next()){
			ip = rs.getString("Ip");
			port = rs.getInt("Port");
			address.IP=ip;
			address.PORT=port;
		}
		else
			System.out.println("Utente "+ username+ "non presente nel db");
		db.close();
		return address;
	}
	
	/*
	 * Rimuovi un amico.
	 */
	
	public void deleteamico(Utente item) throws RemoteException, Exception{
		db.open();
		activateService();
		if (checkIfOnline()){
			//se il server è online allora rimuovo l'amico sia dal db locale, sia nel db del server
			int rimosso = myAmici.deleteAmico(utente_login, item.getUsername());
			// rimuovo la foto 
			File f = new File (dir_immagini+"/"+item.getUsername()+".jpg");
			if (f.delete()) System.out.println("Ho rimosso anche la foto");
			rimosso += db.removeAmico(item.getUsername());
			if (rimosso > 0){
				amici.remove(item);
				tabBachecaContr.update_amici(amici);
		        tabBachecaContr.refresh();
		        openPopup("Amicizia rimossa!", "img/amicizia.png");
			}
		}
		else {
			openPopup("Il server non è al momento raggiungibile. Riprova più tardi!", "img/about.png");
		}
        db.close();
	}
	
	/*
	 * Creazione TabBacheca
	 */
	@FXML protected void openBacheca(){
		if (checkTab("Bacheca")==-1) setTabBacheca();
		else {
			SingleSelectionModel<Tab> selectionModel = tabpane.getSelectionModel();
	        selectionModel.select(tabBacheca);
		}
	}
	
	public void setTabBacheca(){
    	FXMLLoader loader = new  FXMLLoader(getClass().getResource("fxml/tabBacheca.fxml"));
        Node pane = null;
		try {
			pane = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tabBacheca = new Tab();
		tabBacheca.setText("Bacheca");
		tabBacheca.setStyle("-fx-background-color:#F7F7F7;");
		tabBacheca.setContent(pane);
		tabBacheca.setGraphic(new ImageView(new Image("img/post.png")));
        tabpane.getTabs().addAll(tabBacheca);
        tabBachecaContr = loader.<tabBachecaController>getController();
        try {
        	tabBachecaContr.initData(utente_login, amici, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
        SingleSelectionModel<Tab> selectionModel = tabpane.getSelectionModel();
        selectionModel.select(tabBacheca);
	}
	
	/*
	 * Funzione per verificare che un Tab sia già presente.
	 * Se un tab è aperto allora ritorno il numero del tab.
	 */
	public int checkTab(String nameTab){
		int trovato =-1;
		ObservableList<Tab> tab = tabpane.getTabs();
		for (int i=0; i<tab.size();i++){
			if (tab.get(i).getText().equals(nameTab)){
				trovato = i;
				break;
			}
		}
		return trovato;
	}
	
	/*
	 * Ricerca
	 */
	@FXML protected void ricerca() throws Exception{
		if (checkTab("Ricerca")==-1) setTabCerca();
		else {
			tabCercaContr.setValoreRicerca(search.getText());
			tabCercaContr.update();
			SingleSelectionModel<Tab> selectionModel = tabpane.getSelectionModel();
	        selectionModel.select(tabCerca);
		}
	}
	
	public void setTabCerca(){
    	FXMLLoader loader = new  FXMLLoader(getClass().getResource("fxml/tabCerca.fxml"));
        Node pane = null;
		try {
			pane = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tabCerca = new Tab();
		tabCerca.setText("Ricerca");
		tabCerca.setStyle("-fx-background-color:#F7F7F7;");
		tabBacheca.setGraphic(new ImageView(new Image("img/search.png")));
		tabCerca.setContent(pane);
        tabpane.getTabs().addAll(tabCerca);
        tabCercaContr = loader.<tabCercaController>getController();
        try {
        	tabCercaContr.initData(search.getText(), this.utente_login, tabpane);
		} catch (Exception e) {
			e.printStackTrace();
		}
        SingleSelectionModel<Tab> selectionModel = tabpane.getSelectionModel();
        selectionModel.select(tabCerca);
	}
	
	
	
	@FXML protected void logout(){
		terminateThreads();
	}
	
	public void terminateThreads(){
		// devo chiudere la connessione per ogni amico
		Enumeration<Socket> listaS=socket_contr_list.elements();
		Enumeration<String> keys=socket_contr_list.keys();
		while(keys.hasMoreElements()){
			String key=keys.nextElement();
			Socket s=listaS.nextElement();
			if(!key.equals(utente_login)){
				try {
					DataOutputStream out=new DataOutputStream(s.getOutputStream());
					byte[] dead="DEAD".getBytes();
					byte[] me=utente_login.getBytes();
					byte[] friend=key.getBytes();
					int size=dead.length + 4 + me.length+4+friend.length;
					ByteBuffer bbuf = ByteBuffer.allocate(size);
					bbuf.order(ByteOrder.BIG_ENDIAN);
					bbuf.put(dead);
					bbuf.putInt(me.length);
					bbuf.put(me);
					bbuf.putInt(friend.length);
					bbuf.put(friend);
					bbuf.rewind();
			        byte[] complete_content=new byte[size];
			        bbuf.get(complete_content);
			        out.write(complete_content);
			        if(!connected_friend.contains(key)){
			        	System.out.println(utente_login+": HO CHIUSO LA SOCKET DELL'UTENTE "+ key);
			        	s.shutdownOutput();
						s.shutdownInput();
						s.close();
			        }
				}catch (Exception e) {
//					e.printStackTrace();
				}
			}
		}
		Platform.exit();
        System.exit(0);
	}
	
	@FXML protected void openMioProfilo(){
		Stage profiloStage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/modificaProfilo.fxml"));     
		Parent node = null;
		try {
			node = loader.load();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Scene scene = new Scene(node);
		profiloStage.setTitle("Profilo di "+ utente_login);
		profiloStage.getIcons().add(new Image("img/amicizia.png"));
		scene.getStylesheets().add("application.css");
		modificaController tabContr = loader.<modificaController>getController();
        try {
        	tabContr.initData(utente, profiloStage);
		} catch (Exception e) {
			e.printStackTrace();
		}
        profiloStage.setScene(scene);
		profiloStage.show();
	}
	
	@FXML protected void openAbout() throws Exception{
        Stage aboutStage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/aboutScene.fxml"));
		aboutStage.setTitle("About ChatRMI");
		aboutStage.getIcons().add(new Image("img/about.png"));
		Parent node = loader.load();
		Scene scene = new Scene(node,255,170);
		aboutStage.setScene(scene);
		aboutStage.show();
	}
	
	@FXML protected void tabNuoveAmicizie(){
		if (checkTab("Richiesta d'amicizia")==-1) setTabAmicizie();
		else {
			SingleSelectionModel<Tab> selectionModel = tabpane.getSelectionModel();
	        selectionModel.select(tabAmicizie);
		}
	}
	
	public void setTabAmicizie(){
		FXMLLoader loader = new  FXMLLoader(getClass().getResource("fxml/tabAmicizie.fxml"));
        Node pane = null;
		try {
			pane = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tabAmicizie = new Tab();
		tabAmicizie.setText("Richiesta d'amicizia");
		tabAmicizie.setStyle("-fx-background-color:#F7F7F7;");
		tabBacheca.setGraphic(new ImageView(new Image("img/add.png")));
		tabAmicizie.setContent(pane);
        tabpane.getTabs().addAll(tabAmicizie);
        tabAmicizieController tabAmiciContr = loader.<tabAmicizieController>getController();
        try {
        	tabAmiciContr.initData(utente_login, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
        SingleSelectionModel<Tab> selectionModel = tabpane.getSelectionModel();
        selectionModel.select(tabAmicizie);	
	}
	
	public void updateAmicizie(Utente user) throws Exception{
		activateService();
		if (checkIfOnline()){
			refresh();
		}
	}
	
	private static void addNick(String username, String friend,DataInputStream in_my_server, 
	        DataOutputStream out_my_server) throws IOException {
			byte[] type="NICK".getBytes();
			byte[] u_buff=username.getBytes();
			byte[] f_buff=friend.getBytes();
			int size=type.length + 4 + u_buff.length + 4 + f_buff.length; // 4 per rappresentare un INT
			ByteBuffer bbuf = ByteBuffer.allocate(size);
			bbuf.order(ByteOrder.BIG_ENDIAN);
			bbuf.put(type);
			bbuf.putInt(u_buff.length);
			bbuf.put(u_buff);
			bbuf.putInt(f_buff.length);
			bbuf.put(f_buff);
			bbuf.rewind();
	        byte[] complete_content=new byte[size];
	        bbuf.get(complete_content);
	        out_my_server.write(complete_content);
	        out_my_server.flush();
	        String serverResponse = in_my_server.readUTF();
	        if ("SERVER:OK".equals(serverResponse))
	        	System.out.println(username+" è ora conesso al server!");
	        else System.out.println(username+" non si è conesso al server!");
		}
	
	public static boolean hostAvailabilityCheck(String host,int port) { 
		try (Socket s = new Socket(host, port)) {
		return true;
		} catch (IOException ex) {
		/* ignore */
		}
		return false;
	}
		
	public void createTab(String user, String friend){
		FXMLLoader loader = new  FXMLLoader(getClass().getResource("fxml/tabChatta.fxml"));
        Node pane = null;
		try {
			pane = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tabChatta = new Tab();
		tabChatta.setText("Chatta "+ item_selected);
		tabChatta.setStyle("-fx-background-color:#F7F7F7;");
		tabChatta.setGraphic(new ImageView(new Image("img/chat.png")));
		tabChatta.setContent(pane);
        tabpane.getTabs().addAll(tabChatta);
        SingleSelectionModel<Tab> selectionModel = tabpane.getSelectionModel();
        selectionModel.select(tabChatta);
        contr_chatt_list.put(item_selected,loader.<tabChattaController>getController());
        try {
        	contr_chatt_list.get(item_selected).initData(utente_login, item_selected);
		} catch (Exception e) {
			e.printStackTrace();
		}
        contr_chatt_list.get(item_selected).assignSocket(socket_contr_list.get(item_selected));
    }
	
	//classe per la connessione al server
		class ServerConn implements Runnable {
		    private DataInputStream in = null;
		    private String msg_to_view="";
		    private boolean stop=false;
		    public ServerConn(Socket server) throws IOException {
		        /* obtain an input stream from the server */
		        in = new DataInputStream(server.getInputStream());
		    }
		    public void run() {
	    		String msg;
		        try {
		            /* ciclo che legge i messaggi dal server e li visualizza */
		            while (true) {
		            	if(stop==true) break;
		            	msg = in.readUTF();
		            	System.out.println("MSG IN ARRIVO: "+msg);
		            	String[] m=msg.split(":",2);
		            	String msg_type=m[0];
		            	String message=m[1];
		            	String textTab;
		            	int length=0;
		            	String filename="";
		            	if(msg_type.equals("SERVER")){
		            		System.out.println("IL SERVER RISPONDE CON IL MESSAGGIO: "+message);
		            	}
		            	else if(msg_type.equals("DEAD")){
		            		item_selected=message;
		            		if(contr_chatt_list.containsKey(item_selected)){
		            			contr_chatt_list.get(item_selected).sendDeadStatus();
		            			Platform.setImplicitExit(false);
			                    Platform.runLater(new Runnable() {
			                        public void run() {
			                        	try {
											openPopup("L'amico "+item_selected+" non è più connesso! Riprovare più tardi!", "img/about.png");
										} catch (Exception e) {
											e.printStackTrace();
										}	
				            			//elimino il seguente amico dai tab aperti
			                        	ObservableList<Tab> tab = tabpane.getTabs();
				            			for (int i=0; i<tab.size();i++){
				            				if (tab.get(i).getText().equals("Chatta "+item_selected)){
				            					tabpane.getTabs().remove(tab.get(i));
				            					break;
				            				}
				            			}
				            			if(!connected_friend.contains(item_selected)){
//				            				try {
//												socket_contr_list.get(item_selected).shutdownInput();
//												socket_contr_list.get(item_selected).shutdownOutput();
//												socket_contr_list.get(item_selected).close();
//												stop=true;
//											} catch (IOException e) {
//												e.printStackTrace();
//											}
				            				stop=true;
				            			}
				            			socket_contr_list.remove(item_selected);
				            			contr_chatt_list.remove(item_selected);
			                        }
			                    });
		            		}
		            	}
		            	else if(msg_type.equals("ALIVE")){
		            		item_selected=message;
		            		if(contr_chatt_list.containsKey(item_selected))
		            			contr_chatt_list.get(item_selected).sendAliveStatus();
		            	}
	            		else {
			        		if(msg_type.equals("FILE")){
			            		String[] content=message.split(":",3);
			            		item_selected=content[0];
			            		filename=content[1];
			            		length=Integer.parseInt(content[2]);
			            		msg_to_view="TI HO INVIATO IL FILE "+filename;
			            		System.out.println(utente_login +" DEVO SALVARE: "+filename+"\t"+length);
			            		//qui salvo il file
				            	try {
				        			FileOutputStream wr = new FileOutputStream(new File("C://temp/" + utente_login+"_"+filename));
				                    BufferedOutputStream bos = new BufferedOutputStream(wr);
				        			int buff_size =65536;//buffer di una socket
				        			byte[] buffer=new byte[buff_size];
				        			int bytesRead = 0;
				        			System.out.println("IL CLIENT " +utente_login +" INIZIA A SALVARE IL FILE "+filename);
				        			int i=0;
				        			while(i<length){
				        				bytesRead=in.read(buffer, 0, buff_size);
				        				i+=bytesRead;
				        				bos.write(buffer,0,bytesRead);
				        			}
				            		bos.close();
				            		System.out.println("IL CLIENT "+utente_login +" HA SALVATO IL FILE "+filename );
				        		} catch (Exception e) {
				        			e.printStackTrace();
				        		}
			            	}
			            	else if(msg_type.equals("MESG")){
			            		String[] content=message.split(":",2);
			            		item_selected=content[0]; 
			            		msg_to_view=content[1];
			            		System.out.println("MITT: "+item_selected +"\t MSG: "+msg_to_view);
				            }
			            	textTab="Chatta "+item_selected;
			            	int n_tab=checkTab(textTab);
				            	if(n_tab==-1) 
				            	{    
				            	   /* devo vedere prima se il controller esiste o no
				            		* se il controller non esiste allora significa
				            		* che un mio amico mi ha inviato dei messaggi per la PRIMA volta 
				            		*/
				            		
				            		if(!contr_chatt_list.containsKey(item_selected)){
				            			Platform.setImplicitExit(false);
					                    Platform.runLater(new Runnable() {
					                        public void run() {
					                        	socket_contr_list.put(item_selected, my_server_socket);
					                        	connected_friend.add(item_selected);
						                        createTab(utente_login, item_selected);
							            		contr_chatt_list.get(item_selected).showMSG(msg_to_view);
					                        }
					                    });
				            		}
				            		//altrimenti apro il controller
				            		else {
				            			Platform.setImplicitExit(false);
					                    Platform.runLater(new Runnable() {
					                        public void run() {
						            			createTab(utente_login, item_selected);
						            			contr_chatt_list.get(item_selected).showMSG(msg_to_view);
					                        }
					                    });  
				            		}
				            	}
				            	else
				            	{
				            		//esiste già il controller e devo solo visualizzare il tab
				            		Platform.setImplicitExit(false);
				                    Platform.runLater(new Runnable() {
				                        public void run() {
				                        	contr_chatt_list.get(item_selected).showMSG(msg_to_view);
				                        }
				                    });      		
				            	}
			            	}
		            }
		        }
		        catch (IOException e) {
//						e.printStackTrace();
		        }
		    }
		}
		
		class Address{
			String IP;
			int PORT;
			
			public Address(String ip, int port){
				this.IP=ip;
				this.PORT=port;
			}
		}

}
