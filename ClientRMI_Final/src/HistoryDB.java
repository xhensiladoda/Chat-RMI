import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class HistoryDB {

	Connection connection= null;
	public boolean esiste = false;
	String utente_login;
	String dir_utente ="";
	String dir_immagini ="";
	static final String NAME_DB= "history.db";
	
	public HistoryDB(String utente_login){
		this.utente_login=utente_login;
		// nella cartella progettoRMI viene creata una cartella con nome Utente
		dir_utente = secondSceneController.PATH_PRINC + "/" + utente_login;
		// viene creata anche una cartella immagini per contenere le immagini degli amici
		dir_immagini = "C:/progettoRMI/"+utente_login+"/immagini";
	}
	
	/*
	 * Apre connessione al db
	 */
	public void open(String username){
		File cartella = new File(secondSceneController.PATH_PRINC);
		if (!cartella.exists()) {
			cartella.mkdirs();
		}
		cartella = new File(dir_utente);
		if (!cartella.exists()) {
			cartella.mkdirs();
		}
		try {
			Class.forName("org.sqlite.JDBC");
		} 
		catch (Exception e) {
			System.out.println("Errore durante caricamento jdbc del db locale: "+e);
		}	
		try {
			//creo il database amiciDB.db per ogni utente
			// verranno memorizzate le informazioni degli amici
			File file = new File (dir_utente+"/"+ NAME_DB);
			esiste = file.exists();
			connection = DriverManager.getConnection("jdbc:sqlite:"+dir_utente+"/"+ NAME_DB);
			if (esiste){
				//controllo
				createTable(username);
			}
			else {
				System.out.println("il db locale non esiste --> lo creo!");
				// creo le tabelle
				createTable(username);
			}
		} catch (SQLException e) {
			System.out.println("PROBLEMA CONNESSIONE DB");
		}
	}
	
	/*
	 * Creazione della tabella CHATHISTORY
	 */
	public void createTable(String table) throws SQLException{
		Statement statement = connection.createStatement();
		String sql = "CREATE TABLE IF NOT EXISTS "+ table+
                  " (Username CHAR(45) NOT NULL," +
                  " Message  CHAR(1000), " + 
                  "Data DATETIME)"; 
		System.out.println(sql);
		statement.executeUpdate(sql);
	}
	
	/*
	 *  ------------- TABELLA CHATHISTORY ---------------
	 * Inserisci una riga nel db nella tabella CHATHISTORY
	 */
	public void insertMSG(String table, String username,String msg, String data) throws Exception{
		
		String sql = "insert into "+table+" values (?,?,?);";
		
		PreparedStatement st = connection.prepareStatement(sql);
		st.setString(1, username);
		st.setString(2, msg);
		st.setString(3, data);
		st.executeUpdate();
	}
	
	/*
	 * preleva dal db tutte i messaggi di quell'utente
	 */
	public ResultSet getMSG(String table) throws SQLException{
		PreparedStatement p = connection.prepareStatement("select * from "+table
				+ " ORDER by Data desc;");
		return p.executeQuery();
	}
 	/*
 	 * Chiude la connessione
 	 */
 	public void close(){
		try {
			connection.close();
		} catch (SQLException e) {
			System.out.println("Errore nella chiusura della connessione al db: "+e);
		}
	}
}
