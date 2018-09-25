/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plagiarismdetect;

import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author mussandi
 */
public class Plot extends ApplicationFrame {

    private XYSeries currentSeries;
    private XYSeriesCollection data;
    private String title;

    public Plot(final String title) {
        super(title);
        this.title = title;
        data = new XYSeriesCollection();
    }

    public void addSeries(String name) {
        currentSeries = new XYSeries(name);
        data.addSeries(currentSeries);
    }

    public void addSeries(String name, ArrayList<Float> values) {
        currentSeries = new XYSeries(name);
        for(int i = 0; i < values.size(); i++) {
            addData(i, values.get(i));
        }
        data.addSeries(currentSeries);
    }

    public void addData(float x, float y) {
        currentSeries.add(x, y);
    }

    public void end() {

        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "X", "Y",
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
        
        pack();
        setVisible(true);
    }

}
