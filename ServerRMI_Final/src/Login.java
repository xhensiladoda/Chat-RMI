import java.rmi.RemoteException;

public interface Login extends java.rmi.Remote{
	boolean isOnline()  throws java.rmi.RemoteException,Exception;
	void changePass(String loginname,String password)throws RemoteException,Exception;
    int checklogin(String loginname) throws java.rmi.RemoteException,Exception;
    int checkpassword(String loginname,String password) throws java.rmi.RemoteException,Exception;
    String getEmail() throws java.rmi.RemoteException,Exception;
    String getIp() throws java.rmi.RemoteException,Exception;
    int getPort() throws java.rmi.RemoteException,Exception;
    byte[] getImmagine() throws java.rmi.RemoteException,Exception;
    int checkUser(String username) throws java.rmi.RemoteException,Exception;
    int registration(String username, String password, String email, String immagine,String ip, int port) throws java.rmi.RemoteException,Exception;
    int uploadFile(String immagine, String username) throws java.rmi.RemoteException,Exception;
    byte[] getPhotoOf(String username)  throws java.rmi.RemoteException,Exception;
}
