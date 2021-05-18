
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
public class SortObjectiveTwo implements Comparator<Chromosome> {

    public int compare(Chromosome  i1, Chromosome i2) {

        if (i1.fxs[1] == i2.fxs[1]) {
            return 0;
        }
        if (i1.fxs[1] < i2.fxs[1]) {
            return -1;
        } else {
            return 1;
        }

    }

}
