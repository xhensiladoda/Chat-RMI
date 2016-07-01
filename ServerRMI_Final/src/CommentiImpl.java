import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


public class CommentiImpl extends UnicastRemoteObject implements Commenti {

	private static final long serialVersionUID = 1L;
	Stato[] commenti;
	String utente_login;
	int numero;
	int idBacheca;
	String img = "C:/progettoRMI/";
	Connection_Db conn = new Connection_Db();
	
	protected CommentiImpl(String name) throws RemoteException {
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
     * @see Commenti#isOnline()
     */
	public boolean isOnline()  throws java.rmi.RemoteException,Exception{
    	return true;
    }
	
    /*
     * Inizializzazione parametri
     * (non-Javadoc)
     * @see Bacheca#initData(java.lang.String, java.lang.String)
     */
	public void initData(int idBacheca){
		try {
			this.idBacheca=idBacheca;
			setCommenti();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	
	public void setCommenti() throws SQLException{
		numero=0;
		conn.open();
		ResultSet a = conn.get_Commenti(idBacheca);
		while(a.next()) numero++;
		System.out.println("numero post "+ numero);
		commenti = new Stato[numero];
		a.beforeFirst();
		int cont=0;
		while (a.next() & cont<numero){
			commenti[cont] = new Stato();
			commenti[cont].setId(a.getInt("idCommento"));
			commenti[cont].setUtente(a.getString("Utente"));
			commenti[cont].setContenuto(a.getString("Contenuto"));
			commenti[cont].setData(a.getTimestamp("Data"));
			cont++;
		}
		conn.close();
	}
	
	public Boolean setNewCommento(String username, String contenuto, int id) throws java.rmi.RemoteException,Exception{
		conn.open();
		Boolean inserito = false;
		if (conn.insert_commento(username, contenuto, id)>0)
			inserito= true;
		conn.close();
		return inserito;
	}
	
	public Boolean setNewCommentoData(String username, String contenuto, int id, Timestamp data) 
			throws java.rmi.RemoteException,Exception{
		conn.open();
		Boolean inserito = false;
		if (conn.insert_commentoData(username, contenuto, id, data)>0)
			inserito= true;
		conn.close();
		return inserito;
	}
	
	public int getNumero(){
		return numero;
	}
	
	public Timestamp getData(int i) {
		return commenti[i].getData();
	}
	
	public String getCommenti(int i) throws java.rmi.RemoteException,Exception{
		String comment = "";
		//0 numero, 1=id, 2=utente, 3=contenuto, 4=tipo
		comment += numero + Server.TAB;
		comment += commenti[i].getId() + Server.TAB;
		comment += commenti[i].getUtente() + Server.TAB;
		comment += commenti[i].getContenuto() + Server.TAB;
		comment += commenti[i].getTipo();
		return comment;
				
	}
	
	public int removeComment(int id, String utente) throws java.rmi.RemoteException,Exception{
		conn.open();
		String user = conn.getUserOfComment(id);
		int status = -1;
		if (user.equals(utente)){
			status = conn.removeOnlyComment(id);
		}
		conn.close();
		return status;
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

}
