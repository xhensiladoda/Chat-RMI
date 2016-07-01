import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.RMISecurityManager;


@SuppressWarnings("deprecation")
public class Server {

	static final String PATH_SECURITY = "file:///C://Users//xhensila//workspace_xf//ServerRMI_Final//src//security.policy";
	static final String DEST_IMAGE = "C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\";
	static final String service_LOGIN = "rmi://192.168.150.1:1099/myLoginRMI";
	static final String service_AMICI = "rmi://192.168.150.1:1099/myAmiciRMI";
	static final String service_NUOVI_AMICI = "rmi://192.168.150.1:1099/myNuoviAmiciRMI";
	static final String service_RICERCA = "rmi://192.168.150.1:1099/myRicercaRMI";
	static final String service_BACHECA = "rmi://192.168.150.1:1099/myBachecaRMI";
	static final String service_COMMENTI = "rmi://192.168.150.1:1099/myCommentiRMI";
	static final String TAB = "-->";
	
	public static void main(String[] args) {
		System.setProperty("java.security.policy",PATH_SECURITY);
		// Create and install the security manager
		System.setSecurityManager(new RMISecurityManager());
		try {
			// attivo servizi
			new LoginImpl(service_LOGIN);
			new AmiciImpl(service_AMICI);
			new NuoveAmicizieImpl(service_NUOVI_AMICI);
			new RicercaImpl(service_RICERCA);
			new BachecaImpl(service_BACHECA);
			new CommentiImpl(service_COMMENTI);
			System.out.println("Server ready.");
		} 
		catch (Exception e) { 
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static byte[] downloadFile(String fileName){
		try {
			File file = new File(fileName);
			byte buffer[] = new byte[(int)file.length()];
			BufferedInputStream input = new
				BufferedInputStream(new FileInputStream(fileName));
			input.read(buffer,0,buffer.length);
			input.close();
			return(buffer);
		} catch(Exception e){
			System.out.println("FileImpl: "+e.getMessage());
			e.printStackTrace();
			return(null);
		}
	}

}
