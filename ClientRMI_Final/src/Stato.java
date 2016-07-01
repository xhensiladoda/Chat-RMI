
import java.sql.Timestamp;


public class Stato {

	int idStato;
	String utente;
	String contenuto;
	int tipo;
	Timestamp data;
	String url_immagine;
	
	public Stato() {
		super();
	}
	
	public Stato(int idStato,
				String utente,
				String contenuto,
				int tipo,
				Timestamp data,
				String url_immagine) {
		super();
		this.idStato=idStato;
		this.contenuto=contenuto;
		this.utente=utente;
		this.tipo=tipo;
		this.data=data;
		this.url_immagine=url_immagine;
	}
	
	public int getId() {
		return idStato;
	}

	public void setId(int idStato) {
		this.idStato = idStato;
	}
	
	public String getUrl_immagine() {
		return url_immagine;
	}

	public void setUrl_immagine(String url_immagine) {
		this.url_immagine = url_immagine;
	}

	
	
	public String getUtente() {
		return utente;
	}

	public void setUtente(String utente) {
		this.utente = utente;
	}

	public String getContenuto() {
		return contenuto;
	}

	public void setContenuto(String contenuto) {
		this.contenuto = contenuto;
	}

	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public Timestamp getData() {
		return data;
	}

	public void setData(Timestamp data) {
		this.data = data;
	}




}
