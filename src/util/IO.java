package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;


public class IO {
    private BufferedWriter bufferedWriter;
    
    public IO(String fileName){
        try{
            FileWriter results;
            results = new FileWriter("src/files/"+fileName+".txt");
            bufferedWriter = new BufferedWriter(results);
        }catch(Exception e){
            System.out.println("Error al intentar crear archivo "+fileName+".txt");
        }
    }
    /*Abre el archivo sin sobrescribirlo*/
    public IO(String fileName, boolean resume){
        try{
            FileWriter results;
            results = new FileWriter("src/files/"+fileName+".txt", resume);
            bufferedWriter = new BufferedWriter(results);
        }catch(Exception e){
            System.out.println("Error al intentar crear archivo "+fileName+".txt");
        }
    }
    /*cuenta lineas de un archivo*/
    public long getLines(String fileName){
        long count = 0;
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
               count ++;
            }
        }catch(Exception e){}
        return count;
    }
    
    public void storeGraph(int[][] m){
        try{
            String strG = "";
            for(int i = 0; i < m.length; i++){
                for(int j = 0; j< m.length; j++)
                    strG = strG + String.valueOf(m[i][j]);
            }
            bufferedWriter.append(strG);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch(Exception e){
        }
    }
    
    public void storeString(String line){
        try{
            bufferedWriter.append(line);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch(Exception e){
        }
    }
    
}
