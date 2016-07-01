import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


public class RicercaImpl extends UnicastRemoteObject implements Ricerca{

	private static final long serialVersionUID = 1L;
	
	Utente[] ricerca = null;
	int num_nuoviamici =0;
	String username;
	String valore_ricerca;
    Connection_Db conn = new Connection_Db();
    
    /**
     * Default constructor.
     */
    public RicercaImpl(String name) throws RemoteException {
    	super();
	    try{
	      Naming.rebind(name, this);
	    } 
	    catch (Exception e){ System.out.println("Exception: " + e.getMessage());
	      e.printStackTrace();
	    }
    }
    
	 /*
     * Utilizzata per verificare lato Client che il server sia raggiungibile
     * Se è raggiungibile questa funzione ritornerà 'true'
     * (non-Javadoc)
     * @see Commenti#isOnline()
     */
	public boolean isOnline()  throws java.rmi.RemoteException,Exception{
    	return true;
    }
	

	/*
	 * Funzione per la ricerca
	 * L'utente inserisce una stringa e vengono cercati nel db gli utenti con nome uguale alla stringa
	 * o che contengono la stringa(non-Javadoc)
	 * @see Ricerca#search(java.lang.String, java.lang.String)
	 */
	public String search(String valore_ricerca, String username) throws java.rmi.RemoteException,Exception{
		this.valore_ricerca = valore_ricerca;
		this.username=username;
		setNumRicerca();
		get_ricerca();
		String ricerca_val="";
		for (int i=0; i<ricerca.length;i++)
			ricerca_val += ricerca[i].getUsername() + ",";
		return ricerca_val;
	}
	
	public int getNumeroNewAmici() throws java.rmi.RemoteException,Exception {
		return num_nuoviamici;
	}
	
	private void setNumRicerca() throws SQLException{
		conn.open();
		ResultSet a = conn.get_numSearch(valore_ricerca, username);
		if (a.next())
			num_nuoviamici =  a.getInt("num");
		conn.close();
	}
	
	private void get_ricerca() throws SQLException{
		ricerca = new Utente[num_nuoviamici];
		conn.open();
		ResultSet a = conn.searching(valore_ricerca, username);
		int cont=0;
		while (a.next() & cont<num_nuoviamici){
			ricerca[cont] = new Utente(); 
			ricerca[cont].setUsername(a.getString("Username"));
			ricerca[cont].setEmail(a.getString("Email"));
			ricerca[cont].setImmagine(Server.downloadFile(a.getString("Immagine")));
			ricerca[cont].setIp(a.getString("Ip"));
			ricerca[cont].setPort(a.getInt("Port"));
			ricerca[cont].setData(a.getTimestamp("Data"));
			cont++;
		}
		conn.close();
	}
	
	public String getRicerca(int i) throws java.rmi.RemoteException,Exception{
		String ricerc = "";
		//0=username , 1=email, 2=ip, 3=port, 
		ricerc += ricerca[i].getUsername() + Server.TAB;
		ricerc += ricerca[i].getEmail() + Server.TAB;
		ricerc += ricerca[i].getIp() + Server.TAB;
		ricerc += ricerca[i].getPort();
		return ricerc;
	}
	
	public Boolean addFriend(String new_user) throws java.rmi.RemoteException,Exception{
		conn.open();
		int nuova_amicizia = conn.set_NewFriend(username, new_user);
		conn.close();
		if (nuova_amicizia>0){
			return true;
		}
		else return false;
		
	}

	public byte[] getImmagine(int i) throws RemoteException, Exception {
		return ricerca[i].getImmagine();
	}
	
	public Timestamp getData(int i) throws RemoteException, Exception {
		return ricerca[i].getData();
	}
}
