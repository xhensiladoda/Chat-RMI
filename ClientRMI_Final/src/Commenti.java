import java.sql.Timestamp;
public interface Commenti extends java.rmi.Remote {
	void initData(int idBacheca) throws java.rmi.RemoteException,Exception ;
	String getCommenti(int i) throws java.rmi.RemoteException,Exception;
	int getNumero() throws java.rmi.RemoteException,Exception ;
	boolean isOnline()  throws java.rmi.RemoteException,Exception;
	Timestamp getData(int i) throws java.rmi.RemoteException,Exception;
	Boolean setNewCommento(String username, String contenuto, int id) throws java.rmi.RemoteException,Exception;
	Boolean setNewCommentoData(String username, String contenuto, int id, Timestamp data) throws java.rmi.RemoteException,Exception;
	int removeComment(int id, String utente) throws java.rmi.RemoteException,Exception;
	byte[] getPhotoOf(String username)  throws java.rmi.RemoteException,Exception;
}
