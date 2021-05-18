
import java.util.Comparator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author josue
 */
public class crowdedDescending implements Comparator<Chromosome> {

    @Override
    public int compare(Chromosome i1, Chromosome i2) {
        if (i1.crowdingDistance == i2.crowdingDistance) {
            return 0;
        }
        if (i1.crowdingDistance > i2.crowdingDistance) {
            return -1;
        } else {
            return 1;
        }
    }
    
}
