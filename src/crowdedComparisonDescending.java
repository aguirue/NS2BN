
import java.util.Comparator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author josueaguilera
 */
public class crowdedComparisonDescending implements Comparator<Chromosome> {
    public int compare(Chromosome i1, Chromosome i2) {

        if (i1.chromosome_rank <= i2.chromosome_rank) {
            return -1;
        } 
        if (i1.chromosome_rank >= i2.chromosome_rank) return 1;
        
        if (i1.chromosome_rank == i2.chromosome_rank) {
            if (i1.crowdingDistance<i2.crowdingDistance) return -1;
        } else {
            return 1;
        }
        return 0;
    }
}
