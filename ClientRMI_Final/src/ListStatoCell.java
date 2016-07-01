
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import javax.imageio.ImageIO;


public class ListStatoCell extends ListCell<Stato> {
		Label label = new Label("(empty)");
	    Stato lastItem;
	    
	    public ListStatoCell(){
	    	super();
	    }
	    
	    @Override
	    protected void updateItem(Stato item, boolean empty) {
	        super.updateItem(item, empty);
	        setText(null);  // No text in label of super class
	        if (empty) {
	            lastItem = null;
	            setGraphic(null);
	        } else {
	            lastItem = item;
				HBox hbox = new HBox();
				hbox.setMaxSize(200.0, 600.0);
				ByteArrayInputStream bais = new ByteArrayInputStream(downloadFile(item.getUrl_immagine()));
                BufferedImage image = null;
    			try {
    				image = ImageIO.read(bais);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
                Image imag = SwingFXUtils.toFXImage(image, null);
        		ImageView immagine = new ImageView();
        		immagine.setId("immagine_bacheca");
        		immagine.setFitHeight(38.0);
        		immagine.setFitWidth(36.0);
        		immagine.setImage(imag);
				hbox.setMargin(immagine, new Insets(0,0,0,10));
				/*
				 * VBOX CON
				 * nome utente
				 * data e 
				 * post
				 */
					VBox vbox = new VBox();
					// Nome utente che ha inserito il post
					Label lblUser = new Label(item.getUtente());
					lblUser.setFont(Font.font("Tahoma", FontWeight.BOLD, 11.0));
					lblUser.setId(("lblUser"));
					vbox.setMargin(lblUser, new Insets(0,0,0,15));

					//Testo post
					Text t = new Text();
					t.setFont(new Font("Tahoma", 12.0));
					t.setWrappingWidth(300);
					t.setTextAlignment(TextAlignment.JUSTIFY);
					t.setText(item.getContenuto());
					vbox.setMargin(t, new Insets(0,0,0,15));
					
					//Data post (precisamente TimeStamp cioè data e orario creato in automatico dal db 
					//quando viene inserito un nuovo post)
					Timestamp data = item.getData();
					
					Label lblData = new Label("il " + data.getDate()+ "/"+data.getMonth()+" alle "
							+ "" +data.getHours()+":"+ data.getMinutes());
					lblData.setFont(new Font("Tahoma", 10.0));
					vbox.setMargin(lblData, new Insets(0,0,0,15));
					vbox.getChildren().addAll(lblUser, lblData, t);
				/*
				 * Fine VBOX
				 */
				hbox.getChildren().addAll(immagine, vbox);
				label.setText(item!=null ? item.getUtente() : "<null>");
				setGraphic(hbox);
	        }
	    }
	    
		private byte[] downloadFile(String fileName){
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
