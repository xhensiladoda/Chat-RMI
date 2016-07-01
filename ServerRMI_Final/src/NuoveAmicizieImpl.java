import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.Timestamp;


public class NuoveAmicizieImpl extends UnicastRemoteObject implements NuoveAmicizie{

	private static final long serialVersionUID = 1L;
	
	Utente[] nuovi_amici = null;
	int num_nuoviamici = 0;
	
	String username;
    Connection_Db conn = new Connection_Db();
    
    /**
     * Default constructor.
     */
    public NuoveAmicizieImpl(String name) throws RemoteException {
    	super();
	    try{
	      Naming.rebind(name, this);
	    } 
	    catch (Exception e){ System.out.println("Exception: " + e.getMessage());
	      e.printStackTrace();
	    }
    }
    
    public boolean isOnline()  throws java.rmi.RemoteException,Exception{
    	return true;
    }
    
	public void initData(String username) throws Exception{
		this.username=username;
		setNumNuoviAmici();
		get_newfriends();
	}
	
	public int getNumeroNewAmici() throws java.rmi.RemoteException,Exception {
		return num_nuoviamici;
	}
	
	private void setNumNuoviAmici() throws java.rmi.RemoteException,Exception{
		conn.open();
		ResultSet a = conn.get_NumNewFriend(username);
		if (a.next())
			num_nuoviamici =  a.getInt("num");
		conn.close();
	}
	
	
	private void get_newfriends() throws java.rmi.RemoteException,Exception{
		nuovi_amici = new Utente[num_nuoviamici];
		conn.open();
		ResultSet a = conn.get_NewFriend(username);
		int cont=0;
		while (a.next() & cont<num_nuoviamici){
			nuovi_amici[cont] = new Utente(); 
			nuovi_amici[cont].setUsername(a.getString("Username"));
			nuovi_amici[cont].setEmail(a.getString("Email"));
			nuovi_amici[cont].setImmagine(Server.downloadFile(a.getString("Immagine")));
			nuovi_amici[cont].setIp(a.getString("Ip"));
			nuovi_amici[cont].setPort(a.getInt("Port"));
			nuovi_amici[cont].setData(a.getTimestamp("Data"));
			cont++;
		}
		conn.close();
	}
	
	public String getNewFriend(int i) throws java.rmi.RemoteException,Exception{
		String friend = "";
		//0 numero, 1=username, 2=email, 3=ip, 4=port
		friend += num_nuoviamici + Server.TAB;
		friend += nuovi_amici[i].getUsername() + Server.TAB;
		friend += nuovi_amici[i].getEmail() + Server.TAB;
		friend += nuovi_amici[i].getIp() + Server.TAB;
		friend += nuovi_amici[i].getPort();
		return friend;
				
	}

	public Boolean accetta_amicizia(String user) throws java.rmi.RemoteException,Exception{
		conn.open();
		Boolean numero = conn.addNewFriend(username, user);
		conn.close();
		return numero;
	}
	
	public byte[] getImmagine(int i) throws RemoteException, Exception {
		return nuovi_amici[i].getImmagine();
	}

	public Timestamp getData(int i) throws RemoteException, Exception {
		return nuovi_amici[i].getData();
	}
}
