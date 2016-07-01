import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


public class BachecaImpl  extends UnicastRemoteObject implements Bacheca {

	private static final long serialVersionUID = 1L;
	Stato[] stati;
	String utente_login;
	String amici;
	int numero;
	Connection_Db conn = new Connection_Db();
	
	protected BachecaImpl(String name) throws RemoteException {
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
     * @see Bacheca#isOnline()
     */
	public boolean isOnline()  throws java.rmi.RemoteException,Exception{
    	return true;
    }
	
    /*
     * (non-Javadoc)
     * @see Bacheca#initData(java.lang.String, java.lang.String)
     */
	public void initData(String utente_login, String amici){
		try {
			this.utente_login=utente_login;
			this.amici=amici;
			setBacheca();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
    /*
     * Viene prelvato dal db il numero di post e tutte le relative informazioni
     */
	public void setBacheca() throws SQLException{
		numero=0;
		conn.open();
		ResultSet a = conn.get_Post(utente_login, amici);
		// conto i post
		while(a.next()) numero++;
		System.out.println("numero post "+ numero);
		stati = new Stato[numero];
		a.beforeFirst();
		int cont=0;
		while (a.next() & cont<numero){
			stati[cont] = new Stato();
			stati[cont].setId(a.getInt("idBacheca"));
			stati[cont].setUtente(a.getString("Utente"));
			stati[cont].setContenuto(a.getString("Contenuto"));
			stati[cont].setTipo(a.getInt("Tipo"));
			stati[cont].setData(a.getTimestamp("Data"));
			cont++;
		}
		conn.close();
	}
	
	public int getNumero(){
		return numero;
	}
	
	public String getStato(int i) throws java.rmi.RemoteException,Exception{
		String stato = "";
		// 0 = numero_post; 1 = id(int); 2= utente, 3=contenuto, 4=tipo(int)
		stato += numero + Server.TAB;
		stato += stati[i].getId() + Server.TAB;
		stato += stati[i].getUtente()+Server.TAB;
		stato += stati[i].getContenuto()+ Server.TAB;
		stato += stati[i].getTipo();
		return stato;
	}
	
	public Timestamp getData(int i) {
		return stati[i].getData();
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

	public Boolean setNewStato(String username, String contenuto) throws RemoteException, Exception {
		conn.open();
		Boolean inserito = false;
		if (conn.insert_stato(username, contenuto)>0)
			inserito= true;
		conn.close();
		return inserito;
	}
	
	public Boolean setNewStatoData(String username, String contenuto, Timestamp data) throws RemoteException, Exception {
		conn.open();
		Boolean inserito = false;
		if (conn.insert_statoData(username, contenuto, data)>0)
			inserito= true;
		conn.close();
		return inserito;
	}
	
	

	public Boolean thereIsNewPost(Timestamp lastDate)  throws java.rmi.RemoteException,Exception{
		Boolean trovati = false;
		conn.open();
		int numero = conn.thereisNewPost(lastDate);
		System.out.println("numero nuovi post:"+ numero);
		if (numero>0)
			trovati=true;
		conn.close();
		return trovati;
	}
	
	public int removePost(int id, String utente) throws java.rmi.RemoteException,Exception{
		conn.open();
		String user = conn.getUserOfPost(id);
		int status = -1;
		if (user.equals(utente)){
			status = conn.removeComment(id);
			status = conn.removePost(id);
		}
		conn.close();
		return status;
	}



	

}
