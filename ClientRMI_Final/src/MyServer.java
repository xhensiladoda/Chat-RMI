
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Hashtable;
public class MyServer extends Thread{
	private static final int port = secondSceneController.my_port; /* porta dove ascolta il server */
	public MyServer() throws Exception{
	}
	@SuppressWarnings("resource")
	public void run (){
		ServerSocket server = null;
        try {
            server = new ServerSocket(port); /* il server inizia ad ascoltare */
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.err.println(e);
            System.exit(1);
        }
        
        Socket client = null;
        while(true) {
            try {
                client = server.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.err.println(e);
                System.exit(1);
            }
            /* inizio di un nuovo thread per gestire questo client */
            Thread t = new Thread(new ClientConn(client));
            t.start();
        }
    }
}

class ChatServerProtocol {
    private String nick;
    private ClientConn conn;
 
    /* hashtable necessaria per avere gli user e le rispettive connessioni */
    private static Hashtable<String, ClientConn> nicks = 
        new Hashtable<String, ClientConn>();
    private static final String msg_OK = "OK";
    private static final String msg_NICK_IN_USE = "Username già presente nel server!";
    private static final String msg_INVALID = "Il messaggio non è valido.";
    private static final String msg_SEND_FAILED = "Non è possibile inviare il messaggio.";
    private static final String file_SEND_FAILED = "Non è possibile inviare il file.";
    private static final String file_INVALID = "Il file non è valido.";
    private static final String connection_CLOSED = "La connessione è stata chiusa.";
    private static final String connection_PROBLEM = "Si è verificato un erore nel chiudere la connessione.";
 
    /**
     * Aggiunge un username al hashtable dei client.
     * @param nick username del client.
     * @param c thread del client.
     * @return vero se l'operazione va a buon fine, falso altrimenti.
     */
    private static boolean add_nick(String nick, String my_name, ClientConn c) {
        if (nicks.containsKey(nick)) {
            return false;
        } else {
        	//notifico a me stesso che ora l'amico è online se non siamo la stessa persona!
        	if(!nick.equals(my_name)){
        		ClientConn my_c=nicks.get(my_name);
            	my_c.notifyAliveFriend(nick);
        	}
        	nicks.put(nick, c);
            return true;
        }
    }
    /**
     * Associa un thread di tipo ClientConn.
     * @param c thread.
     */
    public ChatServerProtocol(ClientConn c) {
        nick = null;
        conn = c;
    }
    /**
     * Funzione per stampare un messaggio
     * @param msg messaggio
     */
    private void log(String msg) {
        System.err.println(msg);
    }
    /**
     * Funzione per verificare l'autenticazione di un client.
     * @return vero se l'operazione va a buon fine, falso altrimenti.
     */
    public boolean isAuthenticated() {
        return ! (nick == null);
    }
 
    /**
     * Funzione per l'autenticazione di un client.
     * @param nick username
     * @return vero se l'operazione va a buon fine, falso altrimenti.
     */
    private String authenticate(String nick, String my_name) {
        if(add_nick(nick,my_name, this.conn)) {
            log("Nick " + nick + " joined.");
            this.nick = nick;
            return msg_OK;
        } else {
        	return msg_NICK_IN_USE;
        }
    }
 
    /**
     * Funzione per inviare un messaggio.
     * @param recipient destinatario.
     * @param msg messaggio.
     * @return vero se l'operazione va a buon fine, falso altrimenti.
     */
    private boolean sendMsg(String recipient, String msg) {
        if (nicks.containsKey(recipient) && !msg.equals("")) {
            ClientConn c = nicks.get(recipient);
            c.sendMsg("MESG:"+nick+":"+ msg);
            return true;
        } else {
            return false;
        }
    }
    /**
     * Funzione per inviare un file.
     * @param recipient destinatario.
     * @param filename nome del file.
     * @param in inputstream da dove leggere il bytestream.
     * @param l lunghezza del file.
     * @return vero se l'operazione va a buon fine, falso altrimenti.
     */
    private boolean sendFile(String recipient,String filename, DataInputStream in,int l){
    	if (nicks.containsKey(recipient) && l>0) {
            ClientConn c = nicks.get(recipient);
        	c.sendFile(nick,filename,in, l);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Elimina un amico.
     * @param friend amico da eliminare.
     * @return vero se l'operazione va a buon fine, falso altrienti.
     */
    private boolean deleteFriend(String friend,String myname){
    	if(nicks.containsKey(myname)){
    		ClientConn myc = nicks.get(myname);
    		myc.notifyDeadFriend(friend);
    		System.out.println("NOTIFICO "+myname +" CHE "+friend +" NON C'E' PIU' ");
    		if(nicks.contains(friend))
    			nicks.remove(friend);
//    		System.out.println("HO RIMOSSO DAL MIO SERVER L'AMICO "+ friend);
    		return true;
    	}
    	else return false;
    }
    
    /**
     * Funzione per elaborare il messaggio che arriva dal client.
     * @param content contenuto del TIPO del messaggio.
     * @param in inputstream da dove leggere il bytestream.
     * @param buff_size lunghezza dello stream di byte da leggere.
     * @return un messaggio a seconda di com'è andata l'operazione.
     * @throws Exception nel caso in cui si verificassero degli errori.
     */
    public String process(byte[] content, DataInputStream in, int buff_size){
    	String type=new String(content);
    	try {
    		if (!isAuthenticated()) {
        		if(type.equals("NICK")){
            		int f_nl=in.readInt();
            		if(f_nl>0){
            			byte[] f_name_buff=new byte[f_nl];
            			in.read(f_name_buff);
            			String f_name=new String(f_name_buff);
            			int my_nl=in.readInt();
            			if(my_nl>0){
            				byte[] my_name_buff=new byte[my_nl];
            				in.read(my_name_buff);
            				String my_name=new String (my_name_buff);
                			return authenticate(f_name,my_name);
            			}
            		}
            	}
        	}
        	int dest_nl;
        	byte[] dest_name_buff;
        	String dest_name;
    		dest_nl=in.readInt();
    		if(dest_nl>0){
    			dest_name_buff=new byte[dest_nl];
    			in.read(dest_name_buff);
    			dest_name=new String (dest_name_buff);
    			if(type.equals("MESG")){
    				int msg_l=in.readInt();
    				if(msg_l>0){
    					byte[] msg_buff=new byte[msg_l];
    					in.read(msg_buff);
    					String msg=new String(msg_buff);
    					if(sendMsg(dest_name, msg)) return msg_OK;
    		            else return msg_SEND_FAILED;
    				}
    				else return msg_INVALID;
    			}
    			if(type.equals("FILE")){
    				int file_nl=in.readInt();
    				if(file_nl>0){
    					byte[] file_name_buff=new byte[file_nl];
    					in.read(file_name_buff);
    					String filename=new String(file_name_buff);
    					if(!filename.equals("")){
    						int file_l=in.readInt();
//    						System.out.println("TYPE: "+type+" DEST: "+dest_name+" FNAME: "+filename+" L: "+file_l);
    						if(file_l>0){
    							if(sendFile(dest_name,filename, in, file_l))
    								return msg_OK;
    							else return file_SEND_FAILED;
    						}
    						else return file_INVALID;
    					}
    					else return file_INVALID;
    				}
    				else return file_INVALID;
    			}
    			if(type.equals("DEAD")){
    				int l_myname=in.readInt();
    				byte[] myname_buff=new byte[l_myname];
    				in.read(myname_buff);
    				String myname=new String(myname_buff);
    				//questo amico non è più online
    				if(deleteFriend(dest_name,myname))
    					return connection_CLOSED;
    				else return connection_PROBLEM;
    			}
    			else return msg_INVALID;
    		}else return msg_INVALID;
		} catch (Exception e) {
			return msg_INVALID;
		}
    }
}

class ClientConn implements Runnable {
    private Socket client;
    private DataOutputStream out;
    private DataInputStream in;
    private int buff_size;
    ClientConn(Socket client) {
    	this.client=client;
        try {
            /* inputstream per questo client */
        	in=new DataInputStream(client.getInputStream());
            /* outputstream per lo stesso client */
        	out=new DataOutputStream(client.getOutputStream());
        	buff_size=client.getReceiveBufferSize();
        } catch (IOException e) {
        	System.out.println(e);
            return;
        }
    }

    public void run() {
    	String response;
        byte[] type=new byte[4];
        ChatServerProtocol protocol = new ChatServerProtocol(this);
        try {
            /* ciclo che legge messaggi che arrivano dal client
             * e poi manda il messaggio alla funzione process per elaborarli
             * il messaggio della funzione process è inviato allo stesso client        
             */
        	 while (true) {
             	in.read(type);
             	//invio anche IN per leggere il resto del messaggio
             	String type_msg=new String(type);
             	System.out.println("IL SERVER HA RICEVUTO IL MESSAGGIO: "+type_msg);
             	response = protocol.process(type, in,buff_size);
//             	System.out.println(response); 
             	out.writeUTF("SERVER:"+response);
             	out.flush();
//             	if(type_msg.equals("DEAD")){
//             		closeConnection();
//             		break;
//             	}
//             	if(!response.equals("OK")) break;
             }
            //se sono qui devo chiudere la connessione
        } catch( EOFException eof ) {
            System.out.println("SERVER CAN'T READ BECAUSE CLOSED CONNECTION.");
		} catch (Exception e) {
//			e.printStackTrace();
			System.out.println("SERVER CAN'T READ BECAUSE CLOSED CONNECTION.");
		}
    }
 
    public void sendMsg(String msg) {
        try {
			out.writeUTF(msg);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} 
    }
    

    public void sendFile(String dest, String filename,DataInputStream in, int l) {
    	//scrivo in out che è un file
    	try {
			out.writeUTF("FILE:"+dest+":"+filename+":"+l);
			out.flush();
			int size_buff=client.getSendBufferSize();
			byte[] buff=new byte[size_buff];
			int bytesRead = 0;
			int i=0;
			while(i<l){
				bytesRead=in.read(buff, 0, size_buff);
				i+=bytesRead;
				out.write(buff,0,bytesRead);
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void closeConnection(){
    	try {
			client.shutdownOutput();
			client.shutdownInput();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void notifyDeadFriend(String friend){
    	try {
			out.writeUTF("DEAD:"+friend);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void notifyAliveFriend(String friend){
    	try {
			out.writeUTF("ALIVE:"+friend);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
 }
