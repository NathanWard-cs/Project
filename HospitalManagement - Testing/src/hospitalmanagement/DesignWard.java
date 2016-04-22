
package hospitalmanagement;

import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 *
 * @author nathan ward
 */
public class DesignWard {
    
    /**
     *
     */
    public DesignWard() {}
    
    /**
     * If implemented correctly, would allow dragging and dropping
     * @return the drag and drop page
     */
    public Scene createToolbar() {
        //Loads image from file and puts it in a toolbar
        ToolBar tb = new ToolBar();
        tb.setOrientation(Orientation.VERTICAL);
        Image img = new Image("res/bed.jpg", 100, 100, false, false);
        
        ImageView iv = new ImageView();
        iv.setImage(img);
        
        iv.setOnDragDetected(e -> {
            System.out.println("Dragging detected");
        });
        
        tb.getItems().add(iv);
        GridPane gp = new GridPane();
        gp.add(tb, 0, 0);
        
        Scene s = new Scene(gp, 800, 600);
        s.setOnDragDone(e -> {
            System.out.println("Dropping detected");
        });
        return s;
    }
    
}
