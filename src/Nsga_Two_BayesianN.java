
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.core.converters.CSVLoader;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author josueaguilera
 */
public class Nsga_Two_BayesianN {

    public double lowerLimit = 0, upperLimit = 1;
    public static List<List<Chromosome>> fronts = (List<List<Chromosome>>) new ArrayList();
    public static List<Chromosome> F = new ArrayList();
    public static int nodes;
    public int front_counter = 0, gen, numGen;
    public static Random rnd = new Random();
    public int numVar, ni;
    public double pm;
    public double pc;
    private Queue<Integer> queue;
    private int pre[];
    public static String archivoBD;
    static Instances instances;
    static CSVLoader source = new CSVLoader();

    public Nsga_Two_BayesianN(int numGen, int numVar, int ni, double pc, double pm) {
        this.numGen = numGen;
        this.numVar = numVar;
        this.pm = pm;//1.0 / (double) numVar;
        this.ni = ni;
        this.pc = pc;
    }

    public static void main(String[] args) throws Exception {
        int numGen = 200;
        int gen = 0;
        nodes = 18;
        int numVar = nodes*nodes; //Num. variables
        int numInd = 100;
        double pc = 0.9;
        double pm = 0.3;
        List hiper = new ArrayList();
        
        archivoBD = "/Users/josueaguilera/MEGA/MEGAsync/Doctorado/Resultados/carDiagnosis2/carDiag_5000.csv";
        String name = "carDiag_5000";
        source.setSource(new File(archivoBD));
        instances = source.getDataSet();
        for (int i = 0; i < 5; i++) {
            Nsga_Two_BayesianN nsga2 = new Nsga_Two_BayesianN(numGen, numVar, numInd, pc, pm);
            nsga2.process();
            nsga2.hyperCont(nsga2.fronts.get(0));
            hiper.add(nsga2.hyper(nsga2.fronts.get(0)));
            nsga2.printFile(i, nsga2.getMisClas(nsga2.fronts.get(0)), name);
        }
        //Print list of hypervolume by each run
        for (int i = 0; i < hiper.size(); i++) {
            System.out.println(hiper.get(i));
        }
    }

    public void process() throws Exception {
        List<Chromosome> population = new ArrayList();
        List<Chromosome> offspring_population = new ArrayList();
        List<Chromosome> parents_population = new ArrayList();
        List<Chromosome> intermediate_population = new ArrayList();

        population = initProcess(population);
        population = evaluatePop(population);
        population = non_domination_sort(population);
        population = crowdingDistance(population);
       // this.printPop(population);
        while (gen < numGen) {
            parents_population = tournamentSelection(population);
            offspring_population = crossover(parents_population);
            offspring_population = mutation(offspring_population);
            offspring_population = evaluatePop(offspring_population);
            for (int i = 0; i < population.size(); i++) {
                intermediate_population.add(population.get(i).clone());
            }
            for (int i = 0; i < offspring_population.size(); i++) {
                intermediate_population.add(offspring_population.get(i).clone());
            }

            population.removeAll(population);
            offspring_population.removeAll(offspring_population);

            intermediate_population = non_domination_sort(intermediate_population);
            intermediate_population = crowdingDistance(intermediate_population);

            population = replacePopulation(intermediate_population);
            intermediate_population.removeAll(intermediate_population);

            System.out.println(gen);
            gen++;
        }
        System.out.println("Front 0");
        printPop(fronts.get(0));
    }

    public List<Chromosome> hyperCont(List<Chromosome> list) {
        double delta = 0.5;
        double xPunto = 0.0, yPunto = 0.0;
        Collections.sort(list);

        if (list.size() == 1) {
            list.get(0).area = ((((Math.abs(list.get(0).fxs[0]) / Math.abs(list.get(0).fxs[0])) - xPunto)) * (list.get(0).fxs[1] - yPunto));
        } else {
            list.get(0).area = (((Math.abs(list.get(0).fxs[0]) / Math.abs(list.get(0).fxs[0])) - xPunto) * (list.get(0).fxs[1] - yPunto));
            for (int i = 1; i < list.size(); i++) {
                list.get(i).area = ((Math.abs(list.get(i).fxs[0]) / Math.abs(list.get(0).fxs[0])) * (list.get(i).fxs[1] - list.get(i - 1).fxs[1]));
            }
        }
        return list;
    }

    public double hyper(List<Chromosome> list) {
        double hv = 0;
        for (int i = 0; i < list.size(); i++) {
            hv = hv + list.get(i).area;
        }
        return hv;
    }

    public void printPop(List<Chromosome> pop) {
        for (int i = 0; i < pop.size(); i++) {
            System.out.println(pop.get(i).toString());
        }
    }

    public List<Chromosome> initProcess(List<Chromosome> pop) throws Exception {
        for (int i = 0; i < ni; i++) {
            pop.add(new Chromosome(numVar));
            for (int j = 0; j < numVar; j++) {
                int alea = rnd.nextInt(2);
                pop.get(i).x[j] = alea;
            }
        }
        return pop;
    }
    
    public List<Chromosome> evaluatePop(List<Chromosome> pop) throws Exception {
        pop = repairing(pop);
        for (int i = 0; i < pop.size(); i++) {
            pop.get(i).GetAptitude(archivoBD, instances);
        }
        return pop;
    }
    


    public List<Chromosome> non_domination_sort(List<Chromosome> pop) {
        fronts.removeAll(fronts);
        front_counter = 0;
        List<Chromosome> temp = new ArrayList();
        List<Chromosome> Q = new ArrayList();

        for (int i = 0; i < pop.size(); i++) {
            pop.get(i).num_chromosomes_dominate_me = 0;
            pop.get(i).dominated_chromosome.removeAll(pop.get(i).dominated_chromosome);

            for (int j = 0; j < pop.size(); j++) {
                int dom_less = 0;
                int dom_equal = 0;
                int dom_more = 0;
                for (int k = 0; k < 2; k++) {
                    if (pop.get(i).fxs[k] < pop.get(j).fxs[k]) {
                        dom_less = dom_less + 1;
                    } else if (pop.get(i).fxs[k] == pop.get(j).fxs[k]) {
                        dom_equal = dom_equal + 1;
                    } else {
                        dom_more = dom_more + 1;
                    }
                }
                if ((dom_less == 0) && (dom_equal != 2)) {
                    pop.get(i).num_chromosomes_dominate_me = pop.get(i).num_chromosomes_dominate_me + 1;
                } else if ((dom_more == 0) && (dom_equal != 2)) {
                    pop.get(i).dominated_chromosome.add(pop.get(j));
                }
            }
        }

        for (int i = 0; i < pop.size(); i++) {
            if (pop.get(i).num_chromosomes_dominate_me == 0) {
                pop.get(i).chromosome_rank = 0;
                temp.add(pop.get(i).clone());
            }
        }

        fronts.add(new ArrayList(temp));
        temp.removeAll(temp);
        //Find the subsequent fronts

        while (!fronts.get(front_counter).isEmpty()) {
            Q = new ArrayList();
            for (int i = 0; i < fronts.get(front_counter).size(); i++) {
                if (!fronts.get(front_counter).get(i).dominated_chromosome.isEmpty()) {
                    for (int j = 0; j < fronts.get(front_counter).get(i).dominated_chromosome.size(); j++) {
                        fronts.get(front_counter).get(i).dominated_chromosome.get(j).num_chromosomes_dominate_me = fronts.get(front_counter).get(i).dominated_chromosome.get(j).num_chromosomes_dominate_me - 1;
                        if (fronts.get(front_counter).get(i).dominated_chromosome.get(j).num_chromosomes_dominate_me == 0) {
                            fronts.get(front_counter).get(i).dominated_chromosome.get(j).chromosome_rank = front_counter + 1;
                            pop.get(getIndex(pop, fronts.get(front_counter).get(i).dominated_chromosome.get(j))).chromosome_rank = front_counter + 1; //para afectar tambien a la población
                            Q.add(fronts.get(front_counter).get(i).dominated_chromosome.get(j).clone());
                        }
                    }
                }
            }
//            System.out.println("Q size "+Q.size());
//            for (int i = 0; i < Q.size(); i++) {
//                if (unRepeated(i,Q)){
//                    Q.remove(i);
//                }
//            }
            front_counter = front_counter + 1;
            fronts.add(new ArrayList(Q));
        }
        for (int i = 0; i < fronts.get(0).size(); i++) {
            if (unRepeated(i,fronts.get(0))){
                fronts.get(0).remove(i);
            }
        }

        return pop;
    }

    public boolean unRepeated(int index, List<Chromosome> q) {
        for (int i = 0; i < q.size(); i++) {
            if (i!= index && q.get(index).fxs[0] == q.get(i).fxs[0] && q.get(index).fxs[1] == q.get(i).fxs[1]) {
                return true;
            }
        }
        return false;
    }

    public int getIndex(List<Chromosome> pop, Chromosome ind) {
        boolean equal;
        for (int i = 0; i < pop.size(); i++) {
            equal = true;
            for (int j = 0; j < pop.get(i).x.length && equal == true; j++) {
                if (pop.get(i).x[j] != ind.x[j]) {
                    equal = false;
                }
            }
            if (equal) {
                return i;
            }
        }
        return -1;
    }

    //Get Crowding distance for each front
    public List<Chromosome> crowdingDistance(List<Chromosome> pop) {
        for (int i = 0; i < pop.size(); i++) {
            pop.get(i).crowdingDistance = 0.0;
        }

        for (int i = 0; i < fronts.size(); i++) {
            for (int j = 0; j < fronts.get(i).size(); j++) {
                fronts.get(i).get(j).crowdingDistance = 0.0;
            }
        }

        for (int i = 0; i < fronts.size(); i++) {
            for (int j = 1; j < fronts.get(i).size() - 1; j++) {
                for (int k = 0; k < fronts.get(i).get(j).fxs.length; k++) {
                    sortFronts(fronts.get(i), k);
                    pop.get(getIndex(pop, fronts.get(i).get(fronts.get(i).size() - 1))).crowdingDistance = Double.MAX_VALUE;
                    fronts.get(i).get(fronts.get(i).size() - 1).crowdingDistance = Double.MAX_VALUE;
                    pop.get(getIndex(pop, fronts.get(i).get(0))).crowdingDistance = Double.MAX_VALUE;
                    fronts.get(i).get(0).crowdingDistance = Double.MAX_VALUE;
                    pop.get(getIndex(pop, fronts.get(i).get(j))).crowdingDistance = pop.get(getIndex(pop, fronts.get(i).get(j))).crowdingDistance + Math.abs((fronts.get(i).get(j + 1).fxs[k] - fronts.get(i).get(j - 1).fxs[k])) / (fronts.get(i).get(fronts.get(i).size() - 1).fxs[k] - fronts.get(i).get(0).fxs[k]);
                    fronts.get(i).get(j).crowdingDistance = fronts.get(i).get(j).crowdingDistance + Math.abs(fronts.get(i).get(j + 1).fxs[k] - fronts.get(i).get(j - 1).fxs[k]) / (fronts.get(i).get(fronts.get(i).size() - 1).fxs[k] - fronts.get(i).get(0).fxs[k]);
                }
            }
        }
        return pop;
    }

    public void sortFronts(List<Chromosome> front, int obj) {
        if (obj == 0) {
            SortObjectiveOne sobjone = new SortObjectiveOne();
            Collections.sort(front, sobjone);
        }
        if (obj == 1) {
            SortObjectiveTwo sobjTwo = new SortObjectiveTwo();
            Collections.sort(front, sobjTwo);
        }
    }

    public List<Chromosome> tournamentSelection(List<Chromosome> pop) {
        List<Chromosome> parents_population = new ArrayList<Chromosome>();
        int tour_size = 2;
        Chromosome candidate1 = new Chromosome(numVar);
        Chromosome candidate2 = new Chromosome(numVar);
        for (int i = 0; i < pop.size(); i++) {
            int a = i;
            int b = rnd.nextInt(pop.size());
            while (a == b) {
                b = rnd.nextInt(pop.size());
            }
            candidate1 = pop.get(a);
            candidate2 = pop.get(b);

            //Crowdes-comparison operator
            if (candidate1.chromosome_rank < candidate2.chromosome_rank) {
                parents_population.add(candidate1.clone());
            } else if (candidate1.chromosome_rank > candidate2.chromosome_rank) {
                parents_population.add(candidate2.clone());
            } else if (candidate1.chromosome_rank == candidate2.chromosome_rank) {
                if (candidate1.crowdingDistance > candidate2.crowdingDistance) {
                    parents_population.add(candidate1.clone());
                } else {
                    parents_population.add(candidate2.clone());
                }
            }
        }

        return parents_population;
    }

    public List<Chromosome> tournamentSelectionObsolet(List<Chromosome> pop) {
        List<Chromosome> parents_population_aux = new ArrayList<Chromosome>();
        List<Chromosome> parents_population = new ArrayList<Chromosome>();
        int tour_size = 2;
        int pool_size = pop.size() / 2;
        Chromosome candidate1 = new Chromosome(numVar);
        Chromosome candidate2 = new Chromosome(numVar);
        //System.out.println("POOL SIZE " + pool_size);
        for (int i = 0; i < pool_size; i++) {
            int a = rnd.nextInt(pop.size());
            int b = rnd.nextInt(pop.size());
            while (a == b) {
                a = rnd.nextInt(pop.size());
                b = rnd.nextInt(pop.size());
            }
            candidate1 = pop.get(a);
            candidate2 = pop.get(b);

            while (parents_population_aux.contains(candidate1) || parents_population_aux.contains(candidate2)) {
                if (parents_population_aux.contains(candidate1)) {
                    a = rnd.nextInt(pop.size());
                    parents_population_aux.remove(candidate1);
                    candidate1 = pop.get(a);
                    i--;
                }
                if (parents_population_aux.contains(candidate2)) {
                    b = rnd.nextInt(pop.size());
                    parents_population_aux.remove(candidate2);
                    candidate1 = pop.get(b);
                    i--;
                }
            }


            if (candidate1.chromosome_rank < candidate2.chromosome_rank) {
                parents_population_aux.add(candidate1);
            } else if (candidate1.chromosome_rank > candidate2.chromosome_rank) {
                parents_population_aux.add(candidate2);
            } else if (candidate1.chromosome_rank == candidate2.chromosome_rank) {
                if (candidate1.crowdingDistance > candidate2.crowdingDistance) {
                    parents_population_aux.add(candidate1);
                } else {
                    parents_population_aux.add(candidate2);
                }
            }

        }

        for (int i = 0; i < parents_population_aux.size(); i++) {
            parents_population.add(parents_population_aux.get(i).clone());
        }
        return parents_population;
    }

    public List<Chromosome> crossover(List<Chromosome> list) throws Exception {
        int was_crossover = 0, was_mutation = 0;
        int p = 0;
        List<Chromosome> offspring = new ArrayList();
        for (int i = 0; i < list.size(); i += 2) {
            int index_parent1, index_parent2;
            Chromosome child1 = new Chromosome(numVar);
            Chromosome child2 = new Chromosome(numVar);

            index_parent1 = i;
            index_parent2 = i + 1;

            double rndC = rnd.nextDouble();

            if (rndC < pc) {

                int cout = rnd.nextInt(numVar);
                while ((cout == 0) || (cout == list.get(i).x.length - 1)) {
                    cout = rnd.nextInt(numVar);
                }
                //Child onde with que firts parent's genotipe and second parent's genotipe
                for (int j = 0; j < list.get(index_parent1).x[cout]; j++) {
                    child1.x[j] = list.get(index_parent1).x[j];
                }
                for (int j = list.get(index_parent2).x[cout]; j < list.get(index_parent2).x.length; j++) {
                    child1.x[j] = list.get(index_parent2).x[j];
                }
                //Second child with que firts parent's genotipe and second parent's genotipe
                for (int j = 0; j < list.get(index_parent2).x[cout]; j++) {
                    child2.x[j] = list.get(index_parent2).x[j];
                }
                for (int j = list.get(index_parent1).x[cout]; j < list.get(index_parent1).x.length; j++) {
                    child2.x[j] = list.get(index_parent1).x[j];
                }
                was_crossover = 1;
            }
            if (was_crossover == 0) {
                offspring.add(list.get(index_parent1).clone());
                offspring.add(list.get(index_parent2).clone());
            } else {
                offspring.add(child1);
                offspring.add(child2);
                was_crossover = 0;
            }
        }
        return offspring;
    }

   
    public List<Chromosome> mutation(List<Chromosome> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < numVar; j++) {
                double r = rnd.nextDouble();
                if (r < pm) {
                    list.get(i).x[j] = 1 - list.get(i).x[j];
                }
            }
        }
        return list;
    }


    public double verifyLimit(double d) {
        double xi = 0;
        if (d < lowerLimit) {
            xi = (lowerLimit * 2) - d;
        } else if (d > upperLimit) {
            xi = (upperLimit * 2) - d;
        }
        return xi;
    }

    public int crowded_comparison_operator(List<Chromosome> pop) {
        int a, b, res;
        a = rnd.nextInt(pop.size());
        b = rnd.nextInt(pop.size());

        if (pop.get(a).chromosome_rank < pop.get(b).chromosome_rank) {
            res = a;
        } else if (pop.get(a).chromosome_rank > pop.get(b).chromosome_rank) {
            res = b;
        } else if (pop.get(a).crowdingDistance > pop.get(b).crowdingDistance) {
            res = a;
        } else {
            res = b;
        }
        return res;
    }

    public List<Chromosome> replacePopulation(List<Chromosome> intermediate_population) {
        List<Chromosome> current_population = new ArrayList<Chromosome>();
        int i = 0;
        while (current_population.size() + fronts.get(i).size() <= ni) {
            for (int j = 0; j < fronts.get(i).size(); j++) {
                current_population.add(fronts.get(i).get(j).clone());
            }
            i++;
        }
        crowdedDescending ccd = new crowdedDescending();
        Collections.sort(fronts.get(i), ccd);
        int j = 0;
        while (current_population.size() < ni) {
            current_population.add(fronts.get(i).get(j).clone());
            j++;
        }

        return current_population;
    }

    public List<Chromosome> repairing(List<Chromosome> pop) {
        for (Chromosome c : pop) {
            c.x = repairEdges(c.x);
            c.x = repairNet(c.x);
        }
        return pop;
    }

    public int[] repairEdges(int vec[]) {
        int edges = (nodes * (nodes - 1)) / 2;
        Random rnd = new Random();
        int i;
        if (countEdges(vec) > edges) {
            while (countEdges(vec) > edges) {
                i = rnd.nextInt(vec.length);
                if (vec[i] == 1) {
                    vec[i] = 0;
                }
            }
            return vec;
        } else {
            return vec;
        }
    }

    public int[] repairNet(int vec[]) {
        pre = new int[vec.length];
        int r[][];
        r = vecToMat(vec);
        Random rnd = new Random();
        int i, j;
        repair(r);
        while (containCycle(r)) {
            i = rnd.nextInt(r.length);
            j = rnd.nextInt(r.length);
            //System.out.println("i "+i+" j "+j);
            if (r[i][j] == 1) {
                r[i][j] = 0;

            }
        }
        return (matToVec(r));
    }

    public int countEdges(int vec[]) {
        int numEdges = 0;
        for (int i = 0; i < vec.length; i++) {
            if (vec[i] == 1) {
                numEdges++;
            }
        }
        return numEdges;
    }

    public int[][] vecToMat(int vec[]) {
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

    public void repair(int r[][]) {
        Random rnd = new Random();
        for (int i = 0; i < r.length; i++) {  //row number
            for (int j = 0; j < r[i].length; j++) { //columns in row            
                if (j == i) {
                    if (r[i][j] == 1) {
                        if (rnd.nextDouble() <= 0.5) {
                            r[i][j] = 0;
                        } else {
                            r[j][i] = 0;
                        }
                    }
                }
                if ((r[i][j] == 1) && (r[j][i] == 1)) {
                    //System.out.println("rij "+r[i][j]+" rji "+r[j][i]);
                    if (rnd.nextDouble() <= 0.5) {
                        r[i][j] = 0;
                    } else {
                        r[j][i] = 0;
                    }
                }
            }
        }
    }

    private boolean containCycle(int[][] m) {
        this.queue = new LinkedList();
        int p = 0;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++) {
                if (m[i][j] == 1) {
                    p++;
                }
            }
            pre[i] = p;
            p = 0;
        }
        for (int i = 0; i < m.length; i++) {
            if (pre[i] == 0) {
                queue.add(i);
            }
        }
        while (!queue.isEmpty()) {
            int elemento;
            elemento = queue.poll();

            for (int i = 0; i < m.length; i++) {
                if (m[i][elemento] == 1) {
                    pre[i]--;
                    if (pre[i] == 0) {
                        queue.add(i);
                    }
                }
            }

        }
        if (verifyPreZeros() && queue.isEmpty()) {
            return true;
        } else {
            return false;
        }

    }

    public int[] matToVec(int mat[][]) {
        int k = 0;
        int vec[] = new int[mat.length * mat.length];
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[i].length; j++) {
                vec[k] = mat[i][j];
                k++;
            }

        }

        return vec;
    }

    private boolean verifyPreZeros() {
        int cont = 0;
        for (int i = 0; i < pre.length; i++) {
            if (pre[i] != 0) {
                cont++;
            }
        }
        if (cont == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void printFile(int it, List<Chromosome> list, String name) {
        String route = name+"_" + it + ".txt";
        File f;
        f = new File(route);
        try {
            FileWriter fw = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            for (int i = 0; i < list.size(); i++) {
                pw.append(list.get(i).toString() + "\n");
                //pw.append(list.get(i).numEdges() + " " + list.get(i).toString() + " " + list.get(i).desvstd + "\n");

            }
            pw.close();
            bw.close();

        } catch (IOException ex) {
            Logger.getLogger(Nsga_Two_BayesianN.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
    public List<Chromosome> getMisClas(List<Chromosome> list) throws Exception{
        for (int i = 0; i < list.size(); i++) {
            list.get(i).getMisc(instances, nodes);
        }
        return list;
    }
    
}
