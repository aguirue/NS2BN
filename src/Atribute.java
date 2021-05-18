import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author josue
 */
public class Atribute {
    public String atributo; 
    public List <String>  valores= new ArrayList(); 
    public List <Double> Pval= new ArrayList(); 
    public List <Double> frecuencia= new ArrayList(); 
    public List <String>  parents;

    public Atribute(String atributo) {
        this.atributo = atributo;
        //this.parents  = new ArrayList(); 
    }
}
