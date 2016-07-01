import java.rmi.RemoteException;
import java.sql.Timestamp;
public interface Amici extends java.rmi.Remote{
	
	boolean isOnline()  throws java.rmi.RemoteException,Exception;
	void initData(String username) throws RemoteException, Exception;
	int getNumeroAmici() throws java.rmi.RemoteException,Exception;
	String getFriend(int i) throws RemoteException, Exception;
	byte[] getImmagine(int i) throws RemoteException, Exception;
	Timestamp getData(int i) throws RemoteException, Exception ;
	int deleteAmico(String utente_login, String amico) throws RemoteException, Exception;
}
