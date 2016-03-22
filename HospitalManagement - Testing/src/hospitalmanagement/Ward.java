
package hospitalmanagement;

import javafx.collections.ObservableList;

public class Ward {

    private int ward_id;
    private String ward_name;
    private ObservableList<Bed> beds;

    public Ward(int ward_id, String ward_name, ObservableList<Bed> beds) {
        this.ward_id = ward_id;
        this.ward_name = ward_name;
        this.beds = beds;
    }
    
    public Ward() {}

    public int getWard_id() {
        return ward_id;
    }

    public void setWard_id(int ward_id) {
        this.ward_id = ward_id;
    }

    public String getWard_name() {
        return ward_name;
    }

    public void setWard_name(String ward_name) {
        this.ward_name = ward_name;
    }
    
    public ObservableList<Bed> getBeds() {
        return beds;
    }
    
    public void setBeds(ObservableList<Bed> beds) {
        this.beds = beds;
    }
    
    
    
}
