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
public class DetectTernaryThread extends Thread {
    private final Block paragraph;
    private String log;
    public DetectTernaryThread(Block s) {
        paragraph = s;
    }
    @Override
    public void run() {
        try {
            log = DetectBlocksTernary.metricsFiles(paragraph);
        } catch (IOException ex) {
            Logger.getLogger(DetectTernaryThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void printLog() {
        System.out.println(log);
    }
}