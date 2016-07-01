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

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;


public class tabCommentiController {

	/*
	 * Indici per split
	 * 0 numero, 1=id, 2=utente, 3=contenuto, 4=tipo
	 */
	
	private static final int NUMERO = 0;
	private static final int ID = 1;
	private static final int UTENTE = 2;
	private static final int CONTENUTO = 3;
	private static final int TIPO = 4;
	
	public static final String PATH_PRINC = "C:/progettoRMI";
	
	private String dir_utente;
	private String dir_immagini;
	private Stato item_selected;
	private String utente_login;
	private ObservableList<Stato> commenti;
	private Commenti myCommenti;
	private DbLocale db;
	
	@FXML public ListView<Stato> listCommenti;
	@FXML public ImageView immagine;
	@FXML public VBox text;
	@FXML public TextArea textComment;
	
	public tabCommentiController(){
		System.setProperty("java.security.policy",Main.PATH_SECURITY);
		System.setSecurityManager(new RMISecurityManager());
		activateService();
	}
	
	public void initData(Stato item_selected, String utente_login) throws Exception {
		db = new DbLocale(utente_login);
		this.item_selected=item_selected;
		this.utente_login=utente_login;
		dir_utente = PATH_PRINC + "/" + utente_login;
		dir_immagini = dir_utente + "/immagini";
		db.open();
		activateService();
		if (checkIfOnline()){
			caricaCommenti();
			myCommenti.initData(item_selected.getId());
			create_post();
			insert_comment();
		}
		else
			openPopup("Il server non è al momento raggiungibile. Riprova più tardi!", "img/about.png");
	}
	
	/*
	 * Prelevo i commenti dal db_locale e li carico nel db_server
	 */
	public void caricaCommenti() throws Exception, Exception{
		activateService();
		if (checkIfOnline()){
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
	
	public void refresh() throws Exception{
		db.open();
		activateService();
		if (checkIfOnline()){
			caricaCommenti();
			myCommenti.initData(item_selected.getId());
			insert_comment();
		}
		else
			openPopup("Il server non è al momento raggiungibile. Riprova più tardi!", "img/about.png");
	}
	
	public void create_post() throws Exception{
		
		File f = new File(item_selected.getUrl_immagine());
		byte[] img = null;
		if (!f.exists()){
			if (checkIfOnline()){
				img = myCommenti.getPhotoOf(item_selected.getUtente());
				if (img.length>0){
					InputStream in = new ByteArrayInputStream(img);
					BufferedImage bImageFromConvert = ImageIO.read(in);
					ImageIO.write(bImageFromConvert, "jpg", new File(item_selected.getUrl_immagine()));
				}
				else
					img = Main.downloadFile(Main.PATH_DEFAULT_IMAGE);
			}
			else
				img = Main.downloadFile(Main.PATH_DEFAULT_IMAGE);
			
		}
		else img = Main.downloadFile(item_selected.getUrl_immagine());
		ByteArrayInputStream bais = new ByteArrayInputStream(img);
        BufferedImage image = null;
		try {
			image = ImageIO.read(bais);
		} catch (IOException e) {
			e.printStackTrace();
		}
        Image imag = SwingFXUtils.toFXImage(image, null);
		immagine.setFitHeight(45.0);
		immagine.setFitWidth(45.0);
		immagine.setImage(imag);
		// Nome utente che ha inserito il post
		Label lblUser = new Label(item_selected.getUtente());
		lblUser.setFont(Font.font("Tahoma", FontWeight.BOLD, 11.0));
		lblUser.setId(("lblUser"));
		text.setMargin(lblUser, new Insets(0,0,0,15));

		//Testo post
		Text t = new Text();
		t.setFont(new Font("Tahoma", 12.0));
		t.setWrappingWidth(300);
		t.setTextAlignment(TextAlignment.JUSTIFY);
		t.setText(item_selected.getContenuto());
		text.setMargin(t, new Insets(0,0,0,15));
		
		//Data post (precisamente TimeStamp cioè data e orario creato in automatico dal db 
		//quando viene inserito un nuovo post)
		Timestamp data = item_selected.getData();
		
		Label lblData = new Label("il " + data.getDate()+ "/"+data.getMonth()+" alle "
				+ "" +data.getHours()+":"+ data.getMinutes());
		lblData.setFont(new Font("Tahoma", 10.0));
		text.setMargin(lblData, new Insets(0,0,0,15));
		text.getChildren().addAll(lblUser, lblData, t);
	}
	
	public void activateService(){
		/*
		 * Attivo il servizio per il prelievo dei dati degli amici
		 */
		try {
			myCommenti = (Commenti)Naming.lookup(Main.service_COMMENTI);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Controlla se il server è online
	 */
	public boolean checkIfOnline() throws Exception
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
	
	private void insert_comment() throws Exception{
		commenti = FXCollections.observableArrayList();
		activateService();
		if (checkIfOnline()){
			int numero_post = myCommenti.getNumero();
			if (numero_post>0){
				for (int i=0; i<numero_post;i++){
					String[] split = myCommenti.getCommenti(i).split("-->");
					String img = "/"+split[UTENTE]+".jpg";
					String image = checkImage(img, utente_login);
					int id = Integer.parseInt(split[ID]);
					commenti.add(new Stato(id, split[UTENTE], split[CONTENUTO], 0, myCommenti.getData(i), image));
					
				}    
				listCommenti.setItems(commenti);
				listCommenti.setCellFactory(new Callback<ListView<Stato>, ListCell<Stato>>() {
		            @Override 
		            public ListCell<Stato> call(ListView<Stato> list) {
		            	return new ListStatoCell();
		            }
		        }
				);
				MenuItem mnuRimuoviPost = setMenuRimuoviComment();
				listCommenti.setContextMenu(new ContextMenu(mnuRimuoviPost));
			}
			else {
				
				listCommenti.setVisible(false);
			}
		}
		else 
			openPopup("Il server non è al momento raggiungibile. Riprova più tardi!", "img/about.png");
	}
	
	private MenuItem setMenuRimuoviComment(){
		MenuItem mnuRimuoviComment = null;
		mnuRimuoviComment = new MenuItem("Rimuovi post", new ImageView(new Image("img/remove.png")));
			mnuRimuoviComment.setOnAction(new EventHandler<ActionEvent>() {
			    public void handle(ActionEvent t) {
			    	try {
			    		Stato item_selected = listCommenti.getSelectionModel().getSelectedItem();
			    		activateService();
			    		if (checkIfOnline()){
			    			if (item_selected.getUtente().equals(utente_login))
			    				deleteComment(item_selected);
			    		}
			    		else {
			    			openPopup("Il server non è al momento raggiungibile. Riprova più tardi!", "img/about.png");
			    		}
					} catch (Exception e) {
						e.printStackTrace();
					}
			    }
			});
		return mnuRimuoviComment;
	}
	
	public void deleteComment(Stato item) throws RemoteException, Exception{
		activateService();
		int rimosso = myCommenti.removeComment(item.getId(), utente_login);
		if (rimosso > 0){
			commenti.remove(item);
			if (commenti.isEmpty())
				listCommenti.setVisible(false);
			openPopup("Commento eliminato!", "img/post.png");
		}
	}
	
	@FXML protected void add_comment() throws Exception{
		String testo = textComment.getText();
		textComment.setText("");
		activateService();
		if (!testo.isEmpty()){
			if (checkIfOnline()){
				//il server è online quindi inserisco lo stato nel db_server
				myCommenti.setNewCommento(utente_login, testo, item_selected.getId());
				listCommenti.setVisible(true);
				refresh();
			}
			else {
				//il server non è online inserisco lo stato nel db_locale
				//appena il server sarà di nuovo disponibile lo salverò nel db_server
				db.open();
				Calendar calendar = Calendar.getInstance();
				java.util.Date now = calendar.getTime();
				java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
				listCommenti.setVisible(true);
				db.insertComment(utente_login, testo, 1,  currentTimestamp,item_selected.getId());
				openPopup("Commento inserito! Non vedrai subito le modifiche perchè il server non è al momento raggiungibile!"
						, "img/post.png");
				String img = "C:/progettoRMI/"+utente_login+"/immagini/"+utente_login+".jpg";
				String image = checkImage(img, utente_login);
				ObservableList<Stato> stati_temp = FXCollections.observableArrayList();;
				stati_temp.add(new Stato(1, utente_login, testo, 1, currentTimestamp, image));
				stati_temp.addAll(commenti);
				commenti.removeAll(commenti);
				commenti.addAll(stati_temp);
				
				db.close();
			}			
		}
		else 
			System.out.println("Stato vuoto: non inserisco nulla");
	}

	public String checkImage(String filename, String user) throws Exception{
		String img = filename;
		File f = new File(filename);
		if (!f.exists()){
			System.out.println("Il file non esiste ");
			if (checkIfOnline()){
				System.out.println("Servizio online");
				byte[] image = myCommenti.getPhotoOf(user);
				if (image.length>0){
					System.out.println("Servizio online");
					InputStream in = new ByteArrayInputStream(image);
					BufferedImage bImageFromConvert = ImageIO.read(in);
					ImageIO.write(bImageFromConvert, "jpg", new File(filename));
				}
				else
					img =Main.PATH_DEFAULT_IMAGE;
			}
			else
				img =Main.PATH_DEFAULT_IMAGE;
		}
		return img;
	}


}
