/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plagiarismdetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mussandi
 */
public class DetectBinaryThread extends Thread {
    private final Block paragraph;
    private String log;
    public DetectBinaryThread(Block s) {
        paragraph = s;
    }
    @Override
    public void run() {
        try {
            log = DetectBlocksBinary.metricsFiles(paragraph);
        } catch (IOException ex) {
            Logger.getLogger(DetectBinaryThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void printLog() {
        System.out.println(log);
    }
}