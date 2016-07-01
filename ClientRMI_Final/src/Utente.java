import java.sql.Timestamp;


public class Utente {
	
	private String username;
    private String email;
    private byte[] immagine;
    private String ip;
    private int port;
	private Timestamp data;
    
   	public Utente(){
    	
    }
   	
   	public Utente(String username, String email, byte[] immagine, 
   			String ip, int port, Timestamp data){
    	this.username=username;
    	this.email=email;
    	this.immagine=immagine;
    	this.ip=ip;
    	this.port=port;
    	this.data=data;
    }
   	
   	public Utente(String username, String email, byte[] immagine){
    	this.username=username;
    	this.email=email;
    	this.immagine=immagine;
    }


	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public byte[] getImmagine() {
		return immagine;
	}

	public void setImmagine(byte[] immagine) {
		this.immagine = immagine;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Timestamp getData() {
		return data;
	}

	public void setData(Timestamp data) {
		this.data = data;
	}
	
	 public String getIp() {
			return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
    
}
