import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AmiciImpl extends UnicastRemoteObject implements Amici  {

	private static final long serialVersionUID = 1L;
	
	Utente[] amici = null;
	int num_amici = 0;
	
	String username;
    Connection_Db conn = new Connection_Db();
    
    /**
     * Default constructor.
     */
    public AmiciImpl(String name) throws RemoteException {
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
     * @see Amici#isOnline()
     */
    public boolean isOnline()  throws java.rmi.RemoteException,Exception{
    	return true;
    }
    
    /*
     * (non-Javadoc)
     * @see Amici#initData(java.lang.String)
     */
	public void initData(String username) throws RemoteException, Exception{
		this.username=username;
		setNumAmici();
		get_friends();
	}
  
    /*
     * Viene prelvato dal db il numero di amicizie già confermate
     */
	private void setNumAmici() throws SQLException{
		conn.open();
		num_amici=0;
		ResultSet a = conn.get_numAmici(username);
		if (a.next()){
			num_amici = a.getInt("num");
		}
		System.out.println("numero amici: "+ num_amici);
		conn.close();
	}
    
	/*
	 * Ottengo le informazioni di ogni amico
	 * Viene creato un array chiamato "amici" di tipo Utente
	 */
	private void get_friends() throws SQLException{
		amici = new Utente[num_amici];
		conn.open();
		ResultSet a = conn.get_friends(username);
		int cont=0;
		while (a.next() & cont<num_amici){
			amici[cont] = new Utente(); 
			System.out.println("amici username "+ a.getString("Username"));
			amici[cont].setUsername(a.getString("Username"));
			amici[cont].setEmail(a.getString("Email"));
			amici[cont].setImmagine(Server.downloadFile(a.getString("Immagine")));
			amici[cont].setIp(a.getString("Ip"));
			amici[cont].setPort(a.getInt("Port"));
			amici[cont].setData(a.getTimestamp("Data"));
			cont++;
		}
		conn.close();
	}
	
	public int getNumeroAmici() throws java.rmi.RemoteException,Exception {
		return num_amici;
	}
	
	/*
	 * Ritorna le informazioni dell utente
	 * Non l'immagine e la data 
	 * (non-Javadoc)
	 * @see Amici#getFriend(int)
	 */
	public String getFriend(int i) throws RemoteException, Exception{
		String friend = "";
		/*
		 * indici per lo split
		 * 0=Username, 1=Email, 2=Port, 3=Ip
		 */
		friend += amici[i].getUsername()+ Server.TAB;
		friend += amici[i].getEmail() + Server.TAB;
		friend += amici[i].getPort() + Server.TAB;
		friend += amici[i].getIp();
		return friend;
	}   

	public byte[] getImmagine(int i) throws RemoteException, Exception {
		return amici[i].getImmagine();
	}
	
	public Timestamp getData(int i) throws RemoteException, Exception {
		return amici[i].getData();
	}
	
	/*
	 * Rimozione amicizia dal db
	 * (non-Javadoc)
	 * @see Amici#deleteAmico(java.lang.String, java.lang.String)
	 */
	public int deleteAmico(String utente_login, String amico) throws RemoteException, Exception{
		conn.open();
		int rimosso = conn.remove_friend(utente_login, amico);
		conn.close();
		return rimosso;
	}
	
}