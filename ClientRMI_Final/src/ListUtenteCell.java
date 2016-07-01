import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import javax.imageio.ImageIO;

public class ListUtenteCell extends ListCell<Utente> {
        
        
        Label label = new Label("(empty)");
        Pane pane = new Pane();
        Utente lastItem;

        public ListUtenteCell() {
            super();

        }

        @Override
        protected void updateItem(Utente item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);  // No text in label of super class
            if (empty) {
                lastItem = null;
                setGraphic(null);
            } else {
                lastItem = item;
                ByteArrayInputStream bais = new ByteArrayInputStream(item.getImmagine());
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
        		imag.cancel();
        		VBox vbox = new VBox();
        		Label email = new Label(item.getEmail());
        		email.setMaxWidth(85);
        		email.setFont(new Font("Tahoma", 10.0));
        		vbox.getChildren().addAll(label, email);
        		HBox hbox = new HBox();
        		hbox.setMargin(immagine, new Insets(0,10,0,0));
                HBox.setHgrow(pane, Priority.ALWAYS);
                label.setFont(Font.font("Tahoma", 11.0));
                label.setId(("lblUser"));
                label.setText(item!=null ? item.getUsername() : "<null>");
                hbox.getChildren().addAll(immagine, vbox, pane);
                setGraphic(hbox);
            }
        }
    }