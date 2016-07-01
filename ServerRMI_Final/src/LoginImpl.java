// LoginRMIImpl.java, LoginRMI implementation

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;

public class LoginImpl extends UnicastRemoteObject implements Login{

	private static final long serialVersionUID = 1L;
	int index,retval;
	Connection_Db conn = new Connection_Db();
	Utente utente_login = null;
    
	public LoginImpl(String name) throws RemoteException {
	    super();
	    try{
	      Naming.rebind(name, this);
	    } 
	    catch (Exception e){ 
	    	System.out.println("Exception: " + e.getMessage());
	    	e.printStackTrace();
	    }
	}
	
	 /*
     * Utilizzata per verificare lato Client che il server sia raggiungibile
     * Se è raggiungibile questa funzione ritornerà 'true'
     * (non-Javadoc)
     * @see Login#isOnline()
     */
	public boolean isOnline()  throws java.rmi.RemoteException,Exception{
    	return true;
    }

	public int checklogin(String loginname) throws RemoteException,Exception {
	    conn.open();
	    // eseguo la query get_user che restituisce l'utente con username = loginname
	    ResultSet a = conn.get_user(loginname);
	    // se restituisce un risultato significa che l'utente è registrato
	    if (a.next()){
	    	retval = 1;
	    }
	    // altrimenti significa che non si è ancora registrato e l'username non è nel db
	    else retval = 0;
	    conn.close();
	    return retval;
	}
  
	public int checkpassword(String loginname,String password) throws RemoteException,Exception{ 
		conn.open();
		// eseguo la query get_user che restituisce l'utente con username = loginname
	    ResultSet a = conn.get_user(loginname);
	    // se restituisce un risultato significa che l'utente è registrato
	    if (a.next()){
	    	/* se la password memorizzata nel database è uguale a quella inserita nel form Login (campo password)
	    	* allora l'utente può accedere
	    	* Creo l'utente (istanza classe Utente) dell utente che ha effettuato il login
	    	*/
	    	if (a.getString("Password").equals(password)){
	    		utente_login = new Utente();
		    	utente_login.setUsername(a.getString("Username"));
		    	utente_login.setEmail(a.getString("Email"));
		    	utente_login.setImmagine(Server.downloadFile(a.getString("Immagine")));
		    	utente_login.setIp(a.getString("Ip"));
		    	utente_login.setPort(Integer.parseInt(a.getString("Port")));
		    	retval = 1;
	    	}
	    	else retval=0;
	    }
	    else retval=0;
	    conn.close();
	    return retval;
	}
	
	public String getEmail() throws java.rmi.RemoteException,Exception{
		return utente_login.getEmail();
	}
	
    public byte[] getImmagine() throws java.rmi.RemoteException,Exception{
    	return utente_login.getImmagine();
    }
	
	/*
	 * funzione utilizzata in fase di registrazione
	 * controlla se l'username inserito dall'utente che vuole registrarsi esiste gia
	 * se non esiste può usare quel username
	 * altrimenti dovrà modificarlo
	 * (non-Javadoc)
	 * @see LoginRMI#checkUser(java.lang.String)
	 */
	public int checkUser(String username) throws RemoteException,Exception{ 
		conn.open();
		// eseguo la query get_user che restituisce l'utente con Username = "username"
	    ResultSet a = conn.get_user(username);
	    // se restituisce un risultato significa che esiste già un utente con quel username
	    if (a.next()){
	    	retval = 0;
	    }
	    else retval = 1;
	    conn.close();
	    return retval;
	}
	
	public int registration(String username, String password, String email, String immagine,String ip, int port) throws RemoteException,Exception{ 
		conn.open();
		String complete_path="C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\"+immagine+"_profilo.jpg";
		int row_inserted = conn.set_User(username,password,email,complete_path,ip,port);
		conn.close();
		return row_inserted;
	}
	
	public int uploadFile(String immagine, String username){
		int caricato = 0;
		File source = new File(immagine);
		File dest = new File (Server.DEST_IMAGE+username+"_profilo.jpg");
		if (dest.exists()) {
			dest.delete();
		}
		InputStream input = null;
		OutputStream output = null;
        try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
	        byte[] buf = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = input.read(buf)) > 0) {
	            output.write(buf, 0, bytesRead);
	            caricato= 1;
	        }
		} catch (FileNotFoundException e) {
			caricato = 0;
			e.printStackTrace();
		} catch (IOException e) {
			caricato = 0;
			e.printStackTrace();
		}finally {
	        try {
				input.close();
		        output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
        return caricato;
	}
	
	public void changePass(String loginname,String password)throws RemoteException,Exception{
		conn.open();
		boolean row_inserted = conn.changePass(loginname, password);
		conn.close();
	}

	public byte[] getPhotoOf(String username)  throws java.rmi.RemoteException,Exception{
		conn.open();
		byte[] buffer = null;
		ResultSet rs = conn.getPhotoOf(username);
		if (rs.next()){
			try {
				File file = new File(rs.getString("Immagine"));
				buffer = new byte[(int)file.length()];
				BufferedInputStream input = new
		        	BufferedInputStream(new FileInputStream(rs.getString("Immagine")));
		        input.read(buffer,0,buffer.length);
		        input.close();
		        return(buffer);
			} catch(Exception e){
		         System.out.println("FileImpl: "+e.getMessage());
		         e.printStackTrace();
		         return(null);
		    }
		}
		conn.close();
		return buffer;
}

	public String getIp() throws RemoteException, Exception {
		return utente_login.getIp();
	}
	
	public int getPort()throws RemoteException, Exception {
		return utente_login.getPort();
	}
	

}
