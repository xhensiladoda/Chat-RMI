
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class tabChattaController {
	
	@FXML private Button send;
	@FXML private Button inviaFile;
	
	@FXML private TextArea textarea;
	@FXML private ListView<String> viewchat;
	
	String utente_login;
	String chat_with_user;
	private Socket friend_socket;
	private DataOutputStream out;
	private boolean dead=false;
	final FileChooser fileChooser = new FileChooser();
	HistoryDB h_db;
	public tabChattaController(){
		
	}
	void initData(String utente_login, String chat_with_user) throws Exception{
		this.utente_login = utente_login;
		this.chat_with_user = chat_with_user;
		h_db=new HistoryDB(utente_login);
		System.out.println(utente_login +" vuole chattare con "+chat_with_user);
		getFriendHistory(chat_with_user);
		send.setOnAction(new EventHandler<ActionEvent>() {
		    @Override 
		    public void handle(ActionEvent e) {
		    	try {
					sendButtonAction();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		    }
		});
		
		inviaFile.setOnAction(new EventHandler<ActionEvent>() {
		    @Override 
		    public void handle(ActionEvent e) {
		    	try {
		    		configureFileChooser(fileChooser);
                    File file = fileChooser.showOpenDialog(null);
                    if(file != null) {
                    	sendFile(file);
                    }
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		    }
		});
	}
	
	public void sendDeadStatus(){
		dead=true;
	}
	
	public void sendAliveStatus(){
		dead=false;
	}
	
	public void getFriendHistory(String username) throws SQLException{
		h_db.open(username);
		ResultSet rs=h_db.getMSG(username);
		int cont=0;
		ArrayList<String> history=new ArrayList<String>();
		while(rs.next()){
			history.add(rs.getString("Username")+ ":"+rs.getString("Message") +"   " +rs.getString("Data"));
			cont++;
			if(cont>8)
				break;
			
		}
		h_db.close();
		if(history.size()!=0){
			ObservableList<String> items = FXCollections.observableArrayList();
	        items = viewchat.getItems();
	        for (int i=history.size();i>0;i--){
	        	items.add(history.get(i-1));
	        }
	        viewchat.setItems(items);
		}
	}
	
	public String getDate(){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm:ss");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	public void insertMSG(String table,String username, String msg,String date) throws Exception{
		h_db.open(username);
		h_db.insertMSG(table,username,msg,date);
		h_db.close();
	}
	
	/*
	 * Invio al server i messaggi.
	 */
	public void sendButtonAction() throws Exception{
        if(!textarea.getText().equals("") && dead==false){
        	byte[] msg_buff=textarea.getText().getBytes();
    		byte[] type_buff="MESG".getBytes();
    		byte[] dest_buff=chat_with_user.getBytes();
    		int size=type_buff.length+ 4 + dest_buff.length + 4 + msg_buff.length;
            //invio i messaggi secondo il giusto formato
            ByteBuffer bbuf = ByteBuffer.allocate(size);
            bbuf.order(ByteOrder.BIG_ENDIAN);
            bbuf.put(type_buff);
            bbuf.putInt(dest_buff.length);
            bbuf.put(dest_buff);
            bbuf.putInt(msg_buff.length);
            bbuf.put(msg_buff);
            bbuf.rewind();
            byte[] complete_content=new byte[size];
            bbuf.get(complete_content);
            bbuf.clear();
            //si suppone che il messaggio non superi la lunghezza del buffer, altrimenti dovrei fare come nei file
            System.out.println("IL CLIENT "+utente_login +" INVIA IL MESG="+textarea.getText());
            try {
            	out.write(complete_content);
                out.flush();
                ObservableList<String> items = FXCollections.observableArrayList();
                
                items = viewchat.getItems();
                for (int i=0;i<items.size();i++){
                	if (items.get(i).isEmpty())
                		items.remove(i);
                }
                items.add(utente_login+": "+textarea.getText()+"   "+getDate());
                viewchat.setItems(items);
                insertMSG(chat_with_user,utente_login,textarea.getText(),getDate());
        		textarea.setText("");
			} catch( EOFException eof ) {
				dead=true;
				openPopup("L'amico "+chat_with_user+ " è più connesso! Riprovare più tardi!", "img/about.png");
	            System.out.println("CAN'T SEND MSG BECAUSE CLOSED SOCKED.");
			} catch(IOException e) {
				dead=true;
				openPopup("L'amico "+chat_with_user+ " non è più connesso! Riprovare più tardi!", "img/about.png");
				System.out.println("CAN'T SEND MSG BECAUSE CLOSED SOCKED.");
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

	public void assignSocket(Socket s){
		this.friend_socket=s;
		try {
			this.out=new DataOutputStream(friend_socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Socket getSocket(){
		return this.friend_socket;
	}
	
	/*
	 * Funzione che sarà chiamata ogni volta che devo visualizzare un messagio.
	 */
	public void showMSG(String msg){
		ObservableList<String> items = FXCollections.observableArrayList();
        
        items = viewchat.getItems();
        for (int i=0;i<items.size();i++){
        	if (items.get(i).isEmpty())
        		items.remove(i);
        }
        items.add(chat_with_user+": "+msg+"   "+getDate());
        viewchat.setItems(items);
        try {
			insertMSG(chat_with_user,chat_with_user,msg,getDate());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showFile(String msg){
		ObservableList<String> items = FXCollections.observableArrayList();
        
        items = viewchat.getItems();
        for (int i=0;i<items.size();i++){
        	if (items.get(i).isEmpty())
        		items.remove(i);
        }
        items.add(utente_login+": "+msg+"   "+getDate());
        viewchat.setItems(items);
	}
	
	void configureFileChooser(
	        final FileChooser fileChooser) {      
	            fileChooser.setTitle("View Pictures");
	            fileChooser.setInitialDirectory(
	                new File(System.getProperty("user.home"))
	            );                 
	            fileChooser.getExtensionFilters().addAll(
	                new FileChooser.ExtensionFilter("All Images", "*.*"),
	                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
	                new FileChooser.ExtensionFilter("PNG", "*.png")
	            );
	    }
	
	void sendFile(File filename) throws Exception{
		if(dead==false){
			try {
				byte[] name_buff=filename.getName().getBytes();
				byte[] type_buff="FILE".getBytes();
				byte[] dest_buff=chat_with_user.getBytes();
				int length=(int)filename.length();
				byte[] buffer = new byte[length];
				DataInputStream file = new DataInputStream(new FileInputStream(filename));
				file.readFully(buffer,0,length);
				file.close();
				int size=type_buff.length+ 4 + dest_buff.length+ 4 +name_buff.length + 4 + length;
	            ByteBuffer bbuf = ByteBuffer.allocate(size);
	            bbuf.order(ByteOrder.BIG_ENDIAN);
	            bbuf.put(type_buff);
	            bbuf.putInt(dest_buff.length);
	            bbuf.put(dest_buff);
	            bbuf.putInt(name_buff.length);
	            bbuf.put(name_buff);
	            bbuf.putInt(length);
	            bbuf.put(buffer);
	            bbuf.rewind();
	            byte[] complete_content=new byte[size];
	            bbuf.get(complete_content);
	            bbuf.clear();
				System.out.println(utente_login +" INVIA IL FILE "+filename.getName() +"  CON DIM. "+length);
				out.write(complete_content,0,complete_content.length);
	        	out.flush();
	        	showFile("TI HO INVIATO IL FILE "+filename.getName());
	        	try {
	    			insertMSG(chat_with_user,utente_login,"TI HO INVIATO IL FILE "+filename.getName(),getDate());
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
			} catch( EOFException eof ) {
				openPopup("L'amico non è più connesso! Riprovare più tardi!", "img/about.png");
	            System.out.println("CAN'T SEND FILE BECAUSE CLOSED SOCKED.");
			} catch(IOException e) {
				openPopup("L'amico non è più connesso! Riprovare più tardi!", "img/about.png");
				System.out.println("CAN'T SEND FILE BECAUSE CLOSED SOCKED.");
	        }
		}
	}
	
	public void saveFile(String filename,int length){
		try {
			FileOutputStream wr = new FileOutputStream(new File("C://temp/" + utente_login+"_"+filename));
            BufferedOutputStream bos = new BufferedOutputStream(wr);
			int buff_size = friend_socket.getReceiveBufferSize();
			byte[] buffer=new byte[buff_size];
			DataInputStream in=new DataInputStream(friend_socket.getInputStream());
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
	
}
