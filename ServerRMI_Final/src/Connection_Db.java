import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Connection_Db {
	
	Connection connection= null;
	PreparedStatement p=null;
	ResultSet a=null;
	
	/*
	 * Costruttore
	 */
	public Connection_Db(){   
	}
	
	/*
	 * Stabilisce una connessione con il database "social_ssd"
	 */
	public void open(){
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/social_ssd", "root", "root");
			
		} 
		catch (Exception e) {
			System.out.println("Errore durante la connessione al db: "+e);
		}
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
 	
 	public boolean changePass(String utente_login, String password) throws SQLException {
		p = connection.prepareStatement("UPDATE social_ssd.utente SET Password = ? WHERE Username = ?;");
		p.setString(1,password);
		p.setString(2,utente_login);
		return p.execute();
	}
 	
 	/*
 	 * Restituisce l'utente che ha Username uguale a quello passato come parametro
 	 * Dato che Username è chiave primaria della tabella Utenti nel db, si presuppone 
 	 * che venga restituito un solo risultato o nessuno
 	 */
 	public ResultSet get_user(String username) throws SQLException{
		p=(PreparedStatement) connection.prepareStatement("select u.* from social_ssd.utente u where Username=?");
		p.setString(1, username);
		a=p.executeQuery();
		return a;
	}
 	
 	/*
 	 * Inserimento utente nel db
 	 */
 	public int set_User(String username, String password, String email, String immagine,String ip,int port) throws SQLException{
		p = connection.prepareStatement("INSERT into utente (Username, Password, Immagine, Email, Ip, Port) values (?,?,?,?,?,?)");
		p.setString(1,username);
		p.setString(2,password);
		p.setString(3,immagine);
		p.setString(4,email);
		p.setString(5,ip);
		p.setInt(6,port);
		return p.executeUpdate();
	}
 	
 	/*
 	 * Restituisce la lista degli amici di un utente
 	 */
 	public ResultSet get_friends(String username) throws SQLException{
		p=(PreparedStatement) connection.prepareStatement("(Select u.* from social_ssd.utente u where u.Username in ("
				+ "(SELECT a.Utente2 as Username FROM social_ssd.amico_di a Where a.Utente1 = ? "
						+ "and a.Accettata = 1))) UNION "
						+ "(Select u2.* from social_ssd.utente u2 where u2.Username in "
						+ "((SELECT a2.Utente1 as Username FROM social_ssd.amico_di a2 "
						+ "Where a2.Utente2 = ? and a2.Accettata = 1)));");
		p.setString(1, username);
		p.setString(2, username);
		a=p.executeQuery();
		return a;
	}
 	
 	public ResultSet getPhotoOf(String username) throws SQLException{
		p=(PreparedStatement) connection.prepareStatement("Select u.Immagine from utente u where Username =?;");
		p.setString(1, username);
		a=p.executeQuery();
		return a;
	}
 	
 	public ResultSet get_numAmici(String username) throws SQLException{
 		p=(PreparedStatement) connection.prepareStatement("SELECT count(*) as num FROM social_ssd.amico_di a Where (a.Utente1 = ? and a.Accettata = 1) OR (a.Utente2 = ? and a.Accettata = 1);");
		p.setString(1, username);
		p.setString(2, username);
		a=p.executeQuery();
		return a;
 	}
 	
 	/*
 	 * Restituisce i post della bacheca in ordine di data
 	 */
 	public ResultSet get_Post(String utente, String amici) throws SQLException{
 		String[] amici_split = amici.split(",");
 		String query = "SELECT * FROM social_ssd.bacheca WHERE Utente in (?";
 		for (int i=0; i< amici_split.length;i++){
 			if (!amici_split[i].isEmpty()) query += ",?";
 		}
 		query += ") ORDER by Data desc;";
 		p=(PreparedStatement) connection.prepareStatement(query);
 		p.setString(1, utente);
 		for (int i=0; i<amici_split.length;i++){
 			if (!amici_split[i].isEmpty()) p.setString(i+2, amici_split[i]);
 		}
 		a=p.executeQuery();
		return a;
 	}
 	
 	/*
 	 * Inserimento di un nuovo stato
 	 */
 	public int insert_stato(String username, String contenuto) throws SQLException {
 		p = connection.prepareStatement("INSERT INTO social_ssd.bacheca (Utente,Contenuto,Tipo)"+
 						"VALUES (?,?,1);");
		p.setString(1,username);
		p.setString(2,contenuto);
		return p.executeUpdate();
 	}
 	
 	/*
 	 * Inserimento di un nuovo stato CON DATA
 	 */
 	public int insert_statoData(String username, String contenuto, Timestamp data) throws SQLException {
 		p = connection.prepareStatement("INSERT INTO social_ssd.bacheca (Utente,Contenuto,Tipo, Data)"+
 						"VALUES (?,?,1,?);");
		p.setString(1,username);
		p.setString(2,contenuto);
		p.setTimestamp(3,data);
		return p.executeUpdate();
 	}
 	
 	public int thereisNewPost(Timestamp lastDate) throws SQLException {
 		int numero=0;
 		p = connection.prepareStatement("SELECT count(*) as num  FROM social_ssd.bacheca where Data>?;");
		p.setTimestamp(1,lastDate);
		a = p.executeQuery();
		if (a.next())
			numero = a.getInt("num");
		return numero;
 	}
 	
 	/*
 	 * Ricerca di nuovi amici
 	 */
 	public ResultSet searching(String valore, String username) throws SQLException{
 		p=(PreparedStatement) connection.prepareStatement("SELECT u.* from social_ssd.utente u where LOCATE(UCASE(?),UCASE(u.Username),1)>0 "
 				+ "and u.Username NOT in (SELECT a.Utente2 as Username FROM social_ssd.amico_di a Where a.Utente1 = ?) "
 						+ "and u.Username NOT IN (SELECT a2.Utente1 as Username FROM social_ssd.amico_di a2 Where a2.Utente2 = ? ) "
 								+ "and u.Username <> ?;");
 		p.setString(1,valore);
 		p.setString(2,username);
 		p.setString(3,username);
 		p.setString(4,username);
		a=p.executeQuery();
		return a;
 	}
 	
 	public ResultSet get_numSearch(String valore, String username ) throws SQLException{
 		p=(PreparedStatement) connection.prepareStatement("SELECT count(*) as num from social_ssd.utente u where LOCATE(UCASE(?),UCASE(u.Username),1)>0 "
 				+ "and u.Username NOT in (SELECT a.Utente2 as Username FROM social_ssd.amico_di a Where a.Utente1 = ?) "
 						+ "and u.Username NOT IN (SELECT a2.Utente1 as Username FROM social_ssd.amico_di a2 Where a2.Utente2 = ? ) "
 								+ "and u.Username <> ?;");
 		p.setString(1,valore);
 		p.setString(2,username);
 		p.setString(3,username);
 		p.setString(4,username);
		a=p.executeQuery();
		return a;
 	}

 	/*
 	 * Inserimento nuova amicizia
 	 */
 	public int set_NewFriend(String utente_login, String username) throws SQLException{
		p = connection.prepareStatement("INSERT into amico_di values (?,?,?)");
		p.setString(1,utente_login);
		p.setString(2,username);
		p.setInt(3, 0); //campo Accettata = 0
		return p.executeUpdate();
	}
 	
 	public ResultSet get_NumNewFriend(String utente) throws SQLException{
 		p = connection.prepareStatement("select count(*) as num from social_ssd.amico_di a where a.Utente2 = ? and a.Accettata=0;");
		p.setString(1,utente);
		a=p.executeQuery();
		return a;
 	}
 	
 	public ResultSet get_NewFriend(String utente) throws SQLException{
 		p = connection.prepareStatement("select u.* from amico_di a, utente u "
 				+ "where u.Username = a.Utente1 "
 				+ "and a.Utente2 = ? and a.Accettata=0;");
		p.setString(1,utente);
		a=p.executeQuery();
		return a;
 	}
 	
 	public Boolean addNewFriend(String utente_login, String username) throws SQLException {
		p = connection.prepareStatement("UPDATE social_ssd.amico_di SET Accettata = 1 WHERE (Utente1 = ? AND Utente2 = ?) or (Utente2=? and Utente1 =?);");
		p.setString(1,username);
		p.setString(2,utente_login);
		p.setString(3,username);
		p.setString(4,utente_login);
		return p.execute();
	}
 	
 	/*
 	 * Commenti
 	 */
 	public ResultSet get_Commenti(int idBacheca) throws SQLException{
 		p = connection.prepareStatement("select * from commenti where idBacheca = ? order by Data desc; ");
		p.setInt(1,idBacheca);
		a=p.executeQuery();
		return a;
 	}
 	
 	/*
 	 * Inserimento di un nuovo commento
 	 */
 	public int insert_commento(String username, String contenuto, int id) throws SQLException {
 		p = connection.prepareStatement("INSERT INTO social_ssd.commenti (Utente,Contenuto,idBacheca)"+
 						"VALUES (?,?,?);");
		p.setString(1,username);
		p.setString(2,contenuto);
		p.setInt(3,id);
		return p.executeUpdate();
 	}
 	
 	/*
 	 * Inserimento di un nuovo commento con data
 	 */
 	public int insert_commentoData(String username, String contenuto, int id, Timestamp data) throws SQLException {
 		p = connection.prepareStatement("INSERT INTO social_ssd.commenti (Utente,Contenuto,Data,idBacheca)"+
 						"VALUES (?,?,?,?);");
		p.setString(1,username);
		p.setString(2,contenuto);
		p.setTimestamp(3, data);
		p.setInt(4,id);
		return p.executeUpdate();
 	}
 	
 	/*
 	 * rimuovi amicizia
 	 */
 	public int remove_friend(String utente_login, String amico) throws SQLException {
 		p = connection.prepareStatement("DELETE FROM social_ssd.amico_di "
 				+ "where (Utente1=? and Utente2=?) or (Utente2=? and Utente1=?);");
		p.setString(1,utente_login);
		p.setString(2,amico);
		p.setString(3,utente_login);
		p.setString(4,amico);
		return p.executeUpdate();
 	}
 	
 	public int removeComment(int id) throws SQLException {
 		p = connection.prepareStatement("DELETE FROM social_ssd.commenti where idBacheca=?;");
		p.setInt(1,id);
		return p.executeUpdate();
 	}
 	
 	public int removePost(int id) throws SQLException {
 		p = connection.prepareStatement("DELETE FROM social_ssd.bacheca where idBacheca=?;");
		p.setInt(1,id);
		return p.executeUpdate();
 	}
 	
 	public String getUserOfPost(int id) throws SQLException{
 		p = connection.prepareStatement("SELECT * FROM social_ssd.bacheca where idBacheca=?;");
		p.setInt(1,id);
		a=p.executeQuery();
		a.next();
		return a.getString("Utente");
 	}
 	
 	public String getUserOfComment(int id) throws SQLException{
 		p = connection.prepareStatement("SELECT * FROM social_ssd.commenti where idCommento=?;");
		p.setInt(1,id);
		a=p.executeQuery();
		a.next();
		return a.getString("Utente");
 	}
 	
 	public int removeOnlyComment(int id) throws SQLException {
 		p = connection.prepareStatement("DELETE FROM social_ssd.commenti where idCommento=?;");
		p.setInt(1,id);
		return p.executeUpdate();
 	}
 	
}
