//package bayesianNetworks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.IO;

/*
 * @author Yaneli
 */
public class Frequency2 {

    private String encabezado[];
    private String matrix[][];
    private List<String> clases = new ArrayList();
    private int numAtributos;
    private List<String> matrix2 = new ArrayList();
    private int numCasos;
    private int red[][];
    private List<Atribute> Atributes = new ArrayList();
    private int totalvalores;
    public double k;
    public double likelihood;
    public double complexity;
    private List<String> ListaRedes = new ArrayList();
    private IO io;
    private int nodos;
    private String archivoBD;
    public String archivoRB;
    private int arcos;
    public List<Double> numR = new ArrayList();
    public List<Integer> indi = new ArrayList();
    public List<String[]> matrixRep = new ArrayList();
    public int numExperimento;
    public double kAI2;

    public Frequency2(int nodos, int numFichero) {
        this.nodos = nodos;
        this.numExperimento = numFichero;
        //archivoBD = "bd/RB6N/data6N" + String.valueOf(this.numExperimento) + ".txt";
        archivoBD = "bd/alarm/Alarm_data" + String.valueOf(this.numExperimento) + ".txt";

//        archivoBD = "bd/Aleatorydata"+String.valueOf(this.numExperimento)+".txt";
        archivoRB = "redes/redHeckerman.txt";
        arcos = -1;
    }
    
    public Frequency2(int nodos,String BDFile){
        this.nodos = nodos;
        archivoBD = BDFile;
        arcos = -1;
    }

    public static boolean getvacio(String aux[]) {
        for (int i = 0; i < aux.length; i++) {
            if (aux[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void leerArchivoBaseDatos(String archivo) {
        String aux[];
        try {
            Scanner scn = new Scanner(new FileInputStream(archivo));
            String cadena = scn.next();
            encabezado = cadena.split(",");
            numAtributos = encabezado.length; //vamos a llenar la matrix de datos t
            while (scn.hasNext()) {

                aux = scn.next().split(",");
                if (!getvacio(aux)) {
                    for (int j = 0; j < encabezado.length; j++) {
                        matrix2.add(aux[j]);
                    }
                }
            }
            numCasos = matrix2.size() / encabezado.length;
            matrix = new String[numCasos][encabezado.length];
            //se guardan en la matrix
            int longitud = encabezado.length;
            int indice = 0;
            for (int i = 0; i < numCasos; i++) {
                for (int j = 0; j < longitud; j++) {
                    indice = i * longitud + j;
                    matrix[i][j] = matrix2.get(indice);
                }
            }


        } catch (FileNotFoundException ex) {
            Logger.getLogger(Frequency2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void leerArchivoRedes(String archivo) {
        try {
            Scanner scn = new Scanner(new FileInputStream(archivo));
            while (scn.hasNext()) {
                this.ListaRedes.add(scn.next());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Frequency2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Frequency2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getAtributos() {
        totalvalores = 0;
        double res = 0;
        for (int j = 0; j < numAtributos; j++) {
            Atributes.add(new Atribute(encabezado[j]));
        }
        for (int j = 0; j < numAtributos; j++) {
            for (int i = 0; i < numCasos; i++) {
                if (!Atributes.get(j).valores.contains(matrix[i][j])) {
                    Atributes.get(j).valores.add(matrix[i][j]);
                    res = getProbMarginal(j, matrix[i][j]);
                    Atributes.get(j).frecuencia.add(res);
                    if (res > 0) {
                        res = res / (double) numCasos;
                    }
                    Atributes.get(j).Pval.add(res);
                    totalvalores++;
                }
            }

        }
    }

    public double getProbMarginal(int Indiceatributo, String val) {
        double res = 0;
        int j = Indiceatributo;
        for (int i = 0; i < numCasos; i++) {
            if (matrix[i][j].equals(val)) {
                res++;
            }
        }
        return res;
    }

    public void getPadres(int n) {
        for (int i = 0; i < this.Atributes.size(); i++) {
            this.Atributes.get(i).parents = new ArrayList();
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (red[j][i] == 1) {
                    this.Atributes.get(j).parents.add(Atributes.get(i).atributo);
                }
            }
        }
    }

    public double getq(int j) {
        double r = 1;
        int indice;
        for (int k = 0; k < Atributes.get(j).parents.size(); k++) {
            indice = getIndiceAtributo(Atributes.get(j).parents.get(k));
            r = r * (double) Atributes.get(indice).valores.size();
            //System.out.println("r ->"+r);
        }
        return r;
    }

    public void getk() {
        double q;
        k = 0;
        for (int i = 0; i < Atributes.size(); i++) {
            q = 1;
            if (Atributes.get(i).parents.size() > 0) {
                q = getq(i);

            }
            k = k + (q * ((double) Atributes.get(i).valores.size() - 1));
            //k= k+(q*getr(i));
        }

        double num = (double) (numCasos);
        complexity = (k / 2.0) * getlog2(num);

    }

    public void getkAIC2() {
        double q;
        this.kAI2 = 0;
        for (int i = 0; i < Atributes.size(); i++) {
            q = 1;
            if (Atributes.get(i).parents.size() > 0) {
                q = getq(i);

            }

            kAI2 = kAI2 + (q * ((double) Atributes.get(i).valores.size() - 1));
            //k= k+(q*getr(i));
        }

        double num = (double) (numCasos);
        complexity = (kAI2 / 2.0) * Math.log(num);

    }

    public double getlog2(double x) {
        if (x > 1e-6) {
            return (Math.log10(x) / Math.log10(2));
        } else {
            return 0.0;
        }
    }

    public int getIndiceAtributo(String atributo) {
        for (int i = 0; i < this.Atributes.size(); i++) {
            if (Atributes.get(i).atributo.equals(atributo)) {
                return i;
            }

        }
        return -1;
    }

    public double getProbMar(int i, String valor) {
        for (int j = 0; j < Atributes.get(i).valores.size(); j++) {
            if (Atributes.get(i).valores.get(j).equals(valor)) {
                return Atributes.get(i).Pval.get(j);
            }
        }
        return 1;
    }
//para cuando sólo se quiere la frecuencia de un sólo valor

    public double getFrecuenciaMar(int i, String valor) {
        for (int j = 0; j < Atributes.get(i).valores.size(); j++) {
            if (Atributes.get(i).valores.get(j).equals(valor)) {
                return Atributes.get(i).frecuencia.get(j);
            }
        }
        return -1;
    }

    public double getProbCon(int i, int j) {
        double P = 1;
        double d = 0; //el dividendo de la probabilidad conjunta
        int indice;
        String matrizC[] = new String[numAtributos];
        for (int h = 0; h < numAtributos; h++) {
            matrizC[h] = "";
        }
        matrizC[j] = matrix[i][j];
        for (int k = 0; k < Atributes.get(j).parents.size(); k++) {
            indice = this.getIndiceAtributo(Atributes.get(j).parents.get(k));
            matrizC[indice] = matrix[i][indice];
            //    d=d+getFrecuenciaMar(indice, matrizC[indice]);
        }
        P = getFrecuencia(matrizC);
        matrizC[j] = "";
        d = getFrecuencia(matrizC);
        P = P / d;
        return P;
    }

    public double getFrecuencia(String matrizC[]) {
        double f = 0;
        boolean bandera;
        for (int i = 0; i < this.matrixRep.size(); i++) {
            bandera = true;
            int j = 0;
            while (j < numAtributos && bandera) {
                if (!matrizC[j].equals("") && !matrizC[j].equals(matrixRep.get(i)[j])) {
                    bandera = false;
                }
                j++;
            }
            if (bandera) {
                f += this.numR.get(i);
            }
        }

        return f;

    }

    public double getProbCondicional(int i) {
        double P = 1;
        for (int j = 0; j < this.numAtributos; j++) {
            if (Atributes.get(j).parents.size() == 0) {
                P = P * getProbMar(j, matrix[i][j]);
            } else {
                P = P * getProbCon(i, j);
            }
        }
        //System.out.println("P->" +P+" Log "+ (-Math.log10(P)));
        return getlog2(P);
        //return -Math.log(P);
    }

    public double getProbCondicionalAIC2(int i) {
        double P = 1;
        for (int j = 0; j < this.numAtributos; j++) {
            if (Atributes.get(j).parents.size() == 0) {
                P = P * getProbMar(j, matrix[i][j]);
            } else {
                P = P * getProbCon(i, j);
            }
        }
        //System.out.println("P->" +P+" Log "+ (-Math.log10(P)));
        //return -getlog2(P);
        return Math.log(P);
    }

    public double getSumLogPro() {
        double res = 0;
        for (int i = 0; i < this.matrixRep.size(); i++) {
            res = res + (getProbCondicional(this.indi.get(i)) * this.numR.get(i));
        }
//    System.out.println("Res "+res);
        return -res;
    }

    public double getSumLogProA2() {
        double res = 0;
        for (int i = 0; i < this.matrixRep.size(); i++) {
            res = res + (getProbCondicionalAIC2(this.indi.get(i)) * this.numR.get(i));
        }
//    System.out.println("Res "+res);
        return -res;
    }

    public int[][] getMatrixRed(int nodo, String cad) {
        this.arcos = 0;
        int[][] mred = new int[nodo][nodo];
        int indice = 0;
        for (int i = 0; i < nodo; i++) {
            for (int j = 0; j < nodo; j++) {
                mred[i][j] = Integer.parseInt(String.valueOf(cad.charAt(indice)));
                arcos += mred[i][j];
                indice++;
            }
        }
//    imprimirRed(mred,nodo);
        return mred;
    }

    public void imprimirRed(int[][] r, int n) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(r[i][j] + ",");

            }
            System.out.println("");
        }
        System.out.println("");
    }

    public boolean esIgual(String a[], String b[]) {
        if (a.length == 0 || b.length == 0) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean SonIguales(String[][] m1, List<String[]> m2, int ind1, int ind2) {
        boolean ban = true;
        for (int j = 0; j < this.numAtributos; j++) {
            if (!m1[ind1][j].equals(m2.get(ind2)[j])) {
                return false;
            }
        }
        return true;
    }

    public boolean SonIguales2(String[][] m1, String[][] m2, int ind1, int ind2) {
        boolean ban = true;
        for (int j = 0; j < this.numAtributos; j++) {
            if (!m1[ind1][j].equals(m2[ind2][j])) {
                return false;
            }
        }
        return true;
    }

    public boolean estaenMatrix(int ind) {
        boolean ban = false;
        if (!matrixRep.isEmpty()) {
            for (int i = 0; i < this.matrixRep.size(); i++) {
                if (SonIguales(matrix, matrixRep, ind, i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setMatrixRep(int i) {
        int ind = matrixRep.size();
        String[] m = new String[numAtributos];
        for (int j = 0; j < this.numAtributos; j++) {
            m[j] = matrix[i][j];
        }
        matrixRep.add(m);

    }

    public void getRepetidos() {
        for (int i = 0; i < numCasos; i++) {
            if (!estaenMatrix(i)) {//No esta en matrix hacer el conteo
                setMatrixRep(i);//se añade a la matrix 
                double num = 1;
                for (int j = 1; j < numCasos; j++) {
                    if (i != j) {
                        if (SonIguales2(matrix, matrix, i, j)) {
                            num++;
                        }
                    }
                }
                this.numR.add(num);
                this.indi.add(i);
            }
        }
          }

    

      
    

    public boolean SonIguales(int redA[][], int redB[][]) {
        for (int i = 0; i < nodos; i++) {
            for (int j = 0; j < nodos; j++) {
                if (redA[i][j] != redB[i][j]) {
                    return false;
                }
            }
        }
        return true;

    }

    
    
    public int[][] getMatrixRed2(int nodo, int vec[]) {
        this.arcos = 0;
        int[][] mred = new int[nodo][nodo];
        int indice = 0;
        for (int i = 0; i < nodo; i++) {
            for (int j = 0; j < nodo; j++) {
                mred[i][j] = vec[indice];
                arcos += mred[i][j];
                indice++;
            }

        }
        return mred;
    }

    public double MDLFit(int vec[]) {
        //DecimalFormat df = new DecimalFormat("0.0000");
        double MDLa = Double.MAX_VALUE, MDL = 0;
        leerArchivoBaseDatos(archivoBD);
        getRepetidos();
        int redA[][] = new int[nodos][nodos];
        //IO aio = new IO("MDL" + String.valueOf(nodos));
        getAtributos();

        red = this.getMatrixRed2(nodos, vec);
        getPadres(nodos);
        getk();
        likelihood = getSumLogPro() ;
        MDL = likelihood + complexity;
        //System.out.println("K "+k);
        return MDL;
    }

}
