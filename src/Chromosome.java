
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import weka.core.Instances;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author josueaguilera
 */
public class Chromosome implements Comparable<Chromosome> {

    private Queue<Integer> queue;
    private int pre[];

    int num_chromosomes_dominate_me = 0;
    int chromosome_rank = 0;
    int numVar;
    int fr = -1;
    public double crowdingDistance;
    public double desvstd;
    public double entropy;
    public double[] fxs;
    public int[] x;
    public double ka;
    public double MDL;
    public double misc;
    public double complexity;
    public double likelihood;
    int nodes;
    public double area;

    public List<Chromosome> dominated_chromosome = new ArrayList();

    public Chromosome(int nVar) {
        this.numVar = nVar;
        this.x = new int[numVar];
        this.fxs = new double[2];
        nodes = (int) Math.sqrt(numVar);
        //this.archivoBD = "bd/Australian/AustralianDisc.csv";
    }

    public void GetAptitude(String BD, Instances instances) throws Exception {
        Frequency2 freq2 = new Frequency2(nodes, BD);
        MDL = freq2.MDLFit(x);
        fxs[1] = freq2.complexity;
        fxs[0] = freq2.likelihood; //both are minimization 
        ka = freq2.k;

        //System.out.println("MDL = "+MDL+" "+"Complexity = "+fxs[1]+" "+"Likelihood = "+fxs[0]+" "+"K = "+ka);
    }

    public void getMisc(Instances instances, int nodes) throws Exception {
        int kfold = 10;
        FitnessGR fitGR = new FitnessGR(instances, nodes, IntArrayToString(x), kfold);
        misc = fitGR.TwoFitness();
        desvstd = fitGR.std;
        entropy = -1.0 * fitGR.entropy;
    }


    public String IntArrayToString(int[] array) {
        String strRet = "";
        for (int i : array) {
            strRet += Integer.toString(i);
        }
        return strRet;
    }


    @Override
    public Chromosome clone() {
        Chromosome i = new Chromosome(numVar);
        i.crowdingDistance = (Double) this.crowdingDistance;
        i.chromosome_rank = this.chromosome_rank;
        i.numVar = this.numVar;
        i.nodes = this.nodes;
        i.area = this.area;
        i.fr = this.fr;
        i.ka = this.ka;
        i.num_chromosomes_dominate_me = this.num_chromosomes_dominate_me;
        i.desvstd = this.desvstd;
        i.entropy = this.entropy;
        i.misc = this.misc;
        System.arraycopy(this.fxs, 0, i.fxs, 0, i.fxs.length);
        System.arraycopy(this.x, 0, i.x, 0, i.x.length);
        for (int j = 0; j < this.dominated_chromosome.size(); j++) {
            i.dominated_chromosome.add(this.dominated_chromosome.get(j));
        }

        //System.arraycopy(this.dominated_chromosome,0, i.dominated_chromosome,0, i.num_chromosomes_dominate_me);
        return i;
    }

    @Override
    public String toString() {
        String cadena = "";
        for (int i = 0; i < x.length; i++) {
            cadena += x[i];
            //cadena += x[i];
        }
        cadena = cadena + " " + fxs[0] + " " + fxs[1] + " " + ka + " " + misc + " " + desvstd + " " + entropy;
        return cadena;
    }

    @Override
    public int compareTo(Chromosome c) {
        if (this.fxs[0] == c.fxs[0]) {
            return 0;
        }
        if (this.fxs[0] > c.fxs[0]) {
            return -1;
        } else {
            return 1;
        }
    }

    public static int[][] vecToMat(int vec[]) {
        int n = (int) Math.sqrt(vec.length);
        int mat[][] = new int[n][n];
        int cont = 0;

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                mat[y][x] = vec[cont];
                cont++;
            }
        }
        return mat;
    }

    public int numEdges() {
        int num = 0;
        for (int i = 0; i < x.length; i++) {
            if (x[i] == 1) {
                num++;
            }
        }
        return num;
    }

}
