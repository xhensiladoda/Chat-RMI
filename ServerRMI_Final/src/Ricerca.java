import java.rmi.RemoteException;
import java.sql.Timestamp;

public interface Ricerca extends java.rmi.Remote {
	boolean isOnline()  throws java.rmi.RemoteException,Exception;
	public int getNumeroNewAmici() throws java.rmi.RemoteException,Exception;
	public String search(String valore_ricerca, String username) throws java.rmi.RemoteException,Exception;
	public Boolean addFriend(String new_user) throws java.rmi.RemoteException,Exception;
	String getRicerca(int i) throws java.rmi.RemoteException,Exception;
	public byte[] getImmagine(int i) throws java.rmi.RemoteException, Exception;
	public Timestamp getData(int i) throws RemoteException, Exception ;
}
