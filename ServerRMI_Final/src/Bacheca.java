import java.sql.Timestamp;

public interface Bacheca extends java.rmi.Remote {
	
	public boolean isOnline()  throws java.rmi.RemoteException,Exception;
	public void initData(String utente, String amici) throws java.rmi.RemoteException,Exception ;
	int getNumero() throws java.rmi.RemoteException,Exception ;
	String getStato(int i) throws java.rmi.RemoteException,Exception;
	Timestamp getData(int i) throws java.rmi.RemoteException,Exception;	
	public byte[] getPhotoOf(String username)  throws java.rmi.RemoteException,Exception;
	Boolean setNewStato(String username, String contenuto) throws java.rmi.RemoteException,Exception;
	Boolean setNewStatoData(String username, String contenuto, Timestamp data) throws java.rmi.RemoteException,Exception;
	Boolean thereIsNewPost(Timestamp lastDate)  throws java.rmi.RemoteException,Exception;
	public int removePost(int id, String utente) throws java.rmi.RemoteException,Exception;
}
