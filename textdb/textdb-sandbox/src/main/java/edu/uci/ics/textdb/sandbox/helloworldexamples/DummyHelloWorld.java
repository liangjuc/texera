package edu.uci.ics.textdb.sandbox.helloworldexamples;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.sysml.api.jmlc.Connection;
import org.apache.sysml.api.jmlc.PreparedScript;

/**
 * Hello world!
 *
 */
public class DummyHelloWorld {
    public static void main(String[] args) throws Exception {
        // Bad comments
        BasicConfigurator.configure();
        System.out.println("SystemML test!\n");
        
        Connection conn = new Connection();
        String dml = "print('hello world');";
        
 //       System.out.print(dml);
        PreparedScript script = conn.prepareScript(dml, new String[0], new String[0], false);
        script.executeScript();
        testSystemMl();
    }
    
    public  static void testSystemMl() throws Exception {
        // TOBE deleted
        // obtain connection to SystemML
        Connection conn = new Connection();
        
        // read in and precompile DML script, registering inputs and outputs
        String dml = conn.readScript("/tmp/scoring-example.dml");
        System.out.println(dml);
        PreparedScript script = conn.prepareScript(dml, new String[] { "W", "X" }, new String[] { "predicted_y" },
                false);
        
        double[][] mtx = matrix(4, 3, new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });
        double[][] result = null;
        
        // set inputs, execute script, and obtain output
        script.setMatrix("W", mtx);
        script.setMatrix("X", randomMatrix(3, 3, -1, 1, 0.7));
        result = script.executeScript().getMatrix("predicted_y");
        displayMatrix(result);
        
        script.setMatrix("W", mtx);
        script.setMatrix("X", randomMatrix(3, 3, -1, 1, 0.7));
        result = script.executeScript().getMatrix("predicted_y");
        displayMatrix(result);
        
        script.setMatrix("W", mtx);
        script.setMatrix("X", randomMatrix(3, 3, -1, 1, 0.7));
        result = script.executeScript().getMatrix("predicted_y");
        displayMatrix(result);
        
        // close connection
        conn.close();
    }
    
    public static double[][] matrix(int rows, int cols, double[] vals) {
        double[][] matrix = new double[rows][cols];
        if ((vals == null) || (vals.length == 0)) {
            return matrix;
        }
        for (int i = 0; i < vals.length; i++) {
            matrix[i / cols][i % cols] = vals[i];
        }
        return matrix;
    }
 
    public static double[][] randomMatrix(int rows, int cols, double min, double max, double sparsity) {
        double[][] matrix = new double[rows][cols];
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (random.nextDouble() > sparsity) {
                    continue;
                }
                matrix[i][j] = (random.nextDouble() * (max - min) + min);
            }
        }
        return matrix;
    }
 
    public static void displayMatrix(double[][] matrix) {
        System.out.println("Matrix size:" + matrix.length + "x" + matrix[0].length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (j > 0) {
                    System.out.print(", ");
                }
                System.out.print("[" + i + "," + j + "]:" + matrix[i][j]);
            }
            System.out.println();
        }
    }
}
