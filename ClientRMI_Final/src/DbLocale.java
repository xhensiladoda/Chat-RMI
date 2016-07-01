import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import javax.imageio.ImageIO;


public class DbLocale {

	Connection connection= null;
	public boolean esiste = false;
	String utente_login;
	String dir_utente ="";
	String dir_immagini ="";
	static final String NAME_DB= "socialssd.db";
	
	public DbLocale(String utente_login){
		this.utente_login=utente_login;
		// nella cartella progettoRMI viene creata una cartella con nome Utente
		dir_utente = secondSceneController.PATH_PRINC + "/" + utente_login;
		// viene creata anche una cartella immagini per contenere le immagini degli amici
		dir_immagini = "C:/progettoRMI/"+utente_login+"/immagini";
	}
	
	/*
	 * Apre connessione al db
	 */
	public void open(){
		File cartella = new File(secondSceneController.PATH_PRINC);
		if (!cartella.exists()) {
			boolean success = cartella.mkdirs();
		}
		cartella = new File(dir_utente);
		if (!cartella.exists()) {
			boolean success = cartella.mkdirs();
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
				//non faccio nulla
			}
			else {
				System.out.println("il db locale non esiste --> lo creo!");
				// creo le tabelle
				createTable();
			}
		} catch (SQLException e) {
			System.out.println("PROBLEMA CONNESSIONE DB");
		}
	}
	
	/*
	 * Creazione della tabella utente
	 */
	public void createTable() throws SQLException{
		Statement statement = connection.createStatement();
		String sql = "CREATE TABLE utente " +
                  "(Username CHAR(45) PRIMARY KEY NOT NULL," +
                  " Password  CHAR(45), " + 
                  " Immagine CHAR(200), " + 
                  " Email CHAR(200), " + 
                  " Ip  CHAR(45),"
                  + "Port INT,"
                  + "Data DATETIME)"; 
		statement.executeUpdate(sql);
		sql = "CREATE TABLE bacheca " +
                "(Utente CHAR(45)," +
                " Contenuto  CHAR(10000), " + 
                " Tipo INT, " +
                " Data DATETIME default current_timestamp);"; 
		statement.executeUpdate(sql);
		sql = "CREATE TABLE commenti " +
                "(Utente CHAR(45)," +
                " Contenuto  CHAR(10000), " + 
                " Tipo INT, " +
                " Data DATETIME default current_timestamp,"
                + " idBacheca INT);"; 
		statement.executeUpdate(sql);
	}
	
	/*
	 *  ------------- TABELLA UTENTE ---------------
	 * Inserisci una riga nel db nella tabella utente
	 */
	public void insertAmici(String username, byte[] immagine, String email, String ip, 
			int port, Timestamp data) throws Exception{
		
		String filename = caricaImmagine(immagine, username);
		String sql = "insert into utente values (?,'password', ?,? , ?, ?,? );";
		
		PreparedStatement st = connection.prepareStatement(sql);
		st.setString(1, username);
		st.setString(2, filename);
		st.setString(3, email);
		st.setString(4, ip);
		st.setInt(5, port);
		st.setTimestamp(6, data);
		st.executeUpdate();
	}
	
	/*
	 * Funzione per il download delle immagini degli amici
	 * le inserisco nella cartella C:/progettoRmi/nome_utente/immagini
	 * Ad ogni foto assegno nome uguale a quello dell utente a cui appartiene
	 */
	public String caricaImmagine(byte[] immagine, String username) throws Exception{
		File cartella = new File(dir_immagini);
		if (!cartella.exists()) {
			boolean success = cartella.mkdirs();
		}
		String filename= dir_immagini+"/"+username+".jpg";
		InputStream in = new ByteArrayInputStream(immagine);
		BufferedImage bImageFromConvert = ImageIO.read(in);
		ImageIO.write(bImageFromConvert, "jpg", new File(filename));
		return filename;
	}
	
	/*
	 * preleva dal db tutte le info degli amici
	 */
	public ResultSet getAmici() throws SQLException{
		PreparedStatement p = connection.prepareStatement("select * from utente;");
		return p.executeQuery();
	}
	
	/*
	 * Aggiorna le informazioni di un amico
	 */
	public int aggiorna(String username, byte[] immagine, String email, 
			String ip, int port, Timestamp data) throws Exception {
		
		String filename = caricaImmagine(immagine, username);
		
		String sql = "update utente set Immagine = ?, Email = ?, Ip=?, Port=?,Data=? where Username = ?;";
		
		PreparedStatement st = connection.prepareStatement(sql);
		st.setString(1, filename);
		st.setString(2, email);
		st.setString(3, ip);
		st.setInt(4, port);
		st.setTimestamp(5, data);
		st.setString(6, username);
		
		return st.executeUpdate();
	}
	
	/*
	 * Rimuove amico
	 */
 	public int removeAmico(String amico) throws SQLException {
 		PreparedStatement p = connection.prepareStatement("DELETE FROM utente where Username = ?;");
		p.setString(1,amico);
		return p.executeUpdate();
 	}
 	
 	/*
 	 * Preleva port e ip di un amico
 	 */
 	public ResultSet getPortIp(String amico) throws SQLException{
 		PreparedStatement p = connection.prepareStatement("Select Ip, Port from utente where Username =?;");
		p.setString(1,amico);
		return p.executeQuery();
 	}
 	
	/*
	 *  ------------- TABELLA BACHECA ---------------
	 * Inserisci una riga nel db nella tabella bacheca
	 */
	public void insertPost(String username, String contenuto, int tipo, Timestamp data) throws Exception{
		
		String sql = "insert into bacheca values (?,?,?,?);";
		PreparedStatement st = connection.prepareStatement(sql);
		st.setString(1, username);
		st.setString(2, contenuto);
		st.setInt(3, tipo);
		st.setTimestamp(4, data);
		st.executeUpdate();
	}
	
	/*
	 * preleva dal db tutti post
	 */
	public ResultSet getPost() throws SQLException{
		Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery("select * from bacheca");
		return rs;
	}
	
	/*
	 * Rimuove post
	 */
 	public int removePost(String utente, String contenuto) throws SQLException {
 		PreparedStatement p = connection.prepareStatement("DELETE FROM bacheca where Utente = ? and Contenuto = ?;");
		p.setString(1,utente);
		p.setString(2,contenuto);
		return p.executeUpdate();
 	}
 	
 	
	/*
	 *  ------------- TABELLA COMMENTI ---------------
	 * Inserisci una riga nel db nella tabella bacheca
	 */
	public void insertComment(String username, String contenuto, int tipo, Timestamp data, int id) throws Exception{
		
		String sql = "insert into commenti values (?,?,?,?,?);";
		PreparedStatement st = connection.prepareStatement(sql);
		st.setString(1, username);
		st.setString(2, contenuto);
		st.setInt(3, tipo);
		st.setTimestamp(4, data);
		st.setInt(5, id);
		st.executeUpdate();
	}
	
	/*
	 * preleva dal db tutti i commenti
	 */
	public ResultSet getComment() throws SQLException{
		Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery("select * from commenti;");
		return rs;
	}
	
	/*
	 * Rimuove commento
	 */
 	public int removeComment(String utente, String contenuto, int id) throws SQLException {
 		PreparedStatement p = connection.prepareStatement("DELETE FROM commenti where Utente = ? and Contenuto = ?"
 				+ "and idBacheca = ?;");
		p.setString(1,utente);
		p.setString(2,contenuto);
		p.setInt(3,id);
		return p.executeUpdate();
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
