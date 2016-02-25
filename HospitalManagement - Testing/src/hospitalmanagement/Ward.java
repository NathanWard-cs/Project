
package hospitalmanagement;

public class Ward {

    private int ward_id;
    private String ward_name;

    public Ward(int ward_id, String ward_name) {
        this.ward_id = ward_id;
        this.ward_name = ward_name;
    }

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
    
    
    
}
