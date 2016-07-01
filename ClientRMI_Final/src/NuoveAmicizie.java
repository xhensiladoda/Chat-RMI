import java.rmi.RemoteException;
import java.sql.Timestamp;


public interface NuoveAmicizie extends java.rmi.Remote{	
	public boolean isOnline()  throws java.rmi.RemoteException,Exception;
	public void initData(String username) throws Exception;
	String getNewFriend(int i) throws java.rmi.RemoteException,Exception;
	public int getNumeroNewAmici() throws java.rmi.RemoteException,Exception;
	public Boolean accetta_amicizia(String user) throws java.rmi.RemoteException,Exception;
	public byte[] getImmagine(int i) throws RemoteException, Exception;
	public Timestamp getData(int i) throws RemoteException, Exception ;
}
