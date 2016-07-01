import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import javafx.application.Platform;


@SuppressWarnings("deprecation")
public class MyBacheca extends Thread {
	tabBachecaController controller;
	secondSceneController secondContr;
	Bacheca myBacheca;
	NuoveAmicizie myNuoviAmici;
	
	public MyBacheca(tabBachecaController tabBachecaContr,
			secondSceneController secondContr) throws Exception{
		this.controller = tabBachecaContr;	
		this.secondContr = secondContr;
	}
	
	public void run (){
		
        while(true) {
        	try {
        		// se il server è online
				if (checkIfOnline(myBacheca)){
					System.out.println("IL server è online");
					// carico i post dal db_locale a quello del server
					controller.caricaPost();
					controller.caricaCommenti();
					secondContr.refresh();
					// controllo che ci siano nuovi post da visualizzare in Bacheca
					myBacheca = (Bacheca)Naming.lookup(Main.service_BACHECA);
					if (myBacheca.thereIsNewPost(controller.lastDate)){
						Platform.runLater(new Runnable() {
			                @Override
			                public void run() {
			                    try {
									controller.refresh();
								} catch (Exception e) {
									System.out.println("NON VA");
								}
			                }
			            });
					}
				}				// 10 minuti = 600000 millisec
				sleep(600000);
			} catch (Exception e1) {
				e1.printStackTrace();
			} 	
        	
        }
    }
	
	/*
	 * Controlla se il server è online
	 */
	public boolean checkIfOnline(Bacheca myBacheca) throws Exception
	{
		System.setProperty("java.security.policy",Main.PATH_SECURITY);
		System.setSecurityManager(new RMISecurityManager());
		myBacheca = (Bacheca)Naming.lookup(Main.service_BACHECA);
	    boolean online = false;
	    try {
	    	online = myBacheca.isOnline();	
	    } catch (RemoteException e) {
	        System.out.println(e.getMessage());
	        return false;
	    } catch (NotBoundException e) {
	        System.out.println(e.getMessage());
	        return false;
	    } catch (UnknownHostException e) {
	        e.printStackTrace();
	    }
	    return online;

	}
	
	/*
	 * Controlla se il server è online
	 */
	public boolean checkIfOnline(NuoveAmicizie myNuoviAmici) throws Exception
	{
		System.setProperty("java.security.policy",Main.PATH_SECURITY);
		System.setSecurityManager(new RMISecurityManager());
		myNuoviAmici = (NuoveAmicizie)Naming.lookup(Main.service_NUOVI_AMICI);
	    boolean online = false;
	    try {
	    	online = myNuoviAmici.isOnline();	
	    } catch (RemoteException e) {
	        System.out.println(e.getMessage());
	        return false;
	    } catch (NotBoundException e) {
	        System.out.println(e.getMessage());
	        return false;
	    } catch (UnknownHostException e) {
	        e.printStackTrace();
	    }
	    return online;

	}
}