


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.search.local.Scoreable;
import weka.core.Instances;
import weka.core.SelectedTag;
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
public class FitnessGR {

    public Instances instances;
    private int nodes;
    public String red;
    public int folds;
    public double std;
    public double MDL;
    public double entropy;
    static BayesNet bAux;
    static BayesNet bn;
    static GeneticSearch g;
    static PrintStream out;
    static CSVLoader source = new CSVLoader();

    public FitnessGR(Instances instances, int nodes, String red, int folds) {
        this.instances = instances;
        this.nodes = nodes;
        this.red = red;
        this.folds = folds;
    }

    public double TwoFitness() throws IOException, Exception {
        //String r = null;
        int p = 0;
        double sum = 0;
        double precision[] = new double[folds];
        instances.setClassIndex(instances.numAttributes() - 1);
        bAux = new BayesNet();
        bAux.m_Instances = instances;
        bAux.initStructure();
        g = new GeneticSearch();        
        g.setScoreType(new SelectedTag(Scoreable.MDL, LocalScoreSearchAlgorithm.TAGS_SCORE_TYPE));
        g.m_BayesNet = bAux;
        int n = (int) Math.sqrt(red.length());
        char mat[][] = new char[n][n];
        int cont = 0;
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                mat[y][x] = red.charAt(cont);
                cont++;
            }
        }
        boolean[] gr = new boolean[nodes * nodes];
        int j = 0;
        for (char c : red.toCharArray()) {
            gr[j++] = c == '0' ? false : true;
        }
        GeneticSearch.BayesNetRepresentation bnR = g.new BayesNetRepresentation(bAux.getNrOfNodes());
        bnR.m_bits = gr;
        bnR.calcScore();
        bn = new BayesNet();
        bn = g.m_BayesNet;
        g.m_BayesNet.estimateCPTs();
        Classifier cls = (g.m_BayesNet);
        MDL = g.m_BayesNet.measureMDLScore();
        entropy = g.m_BayesNet.measureEntropyScore();
        Random rand = new Random(1);
        Instances randData = new Instances(instances);
        randData.randomize(rand);
        if (randData.classAttribute().isNominal()) {
            randData.stratify(folds);
        }
        Evaluation evalAll = new Evaluation(randData);
        for (int i = 0; i < folds; i++) {
            Evaluation eval = new Evaluation(randData);
            Instances test = randData.testCV(folds, i);
            eval.evaluateModel(cls, test);
            precision[i] = ((eval.incorrect() / (double) test.numInstances()));//instances.numInstances()));
            sum += precision[i];

        }
        double variance = 0, media = sum / (double) folds;
        for (int i = 0; i < precision.length; i++) {
            variance = +Math.pow((precision[i] - media), 2);
        }
        std = Math.sqrt(variance / (double) folds);
        return media;
    }

}
