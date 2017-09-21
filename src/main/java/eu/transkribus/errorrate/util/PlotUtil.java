/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.util;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.ImageTerminal;
import com.panayotis.gnuplot.terminal.TextFileTerminal;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.math3.util.Pair;

/**
 * Describtion of Gnuplot:
 *
 *
 * @author Tobias Strauß <tobias.strauss@uni-rostock.de>
 * since 05.01.2014
 */
public class PlotUtil {

    public static boolean withGrid = false;

    public static void plotCurve(List<double[]> val, String[] result_names, String title, double ymin, double ymax, String outName) {
        plotCurve(val, result_names, title, ymin, ymax, outName, JavaPlot.Key.TOP_RIGHT);
    }

    public static void plotCurve(List<double[]> val, String[] result_names, String title, double ymin, double ymax, String outName, JavaPlot.Key setKey) {
        ImageTerminal png = new ImageTerminal();
        File file = new File(outName);
        try {
            file.createNewFile();
            png.processOutput(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            System.err.print(ex);
        } catch (IOException ex) {
            System.err.print(ex);
        }

        JavaPlot p = new JavaPlot();
        p.setTerminal(png);

        //p.set("xrange", "[-1.1:1.1]");
        //p.getAxis("x").setBoundaries(-1.1, 1.1);
        if (!Double.isNaN(ymin) && !Double.isNaN(ymax)) {
            p.set("yrange", "[" + ymin + ":" + ymax + "]");
            //p.getAxis("y").setBoundaries(-1.1, 1.1);
        }

        p.setTitle(title);
        p.setKey(setKey);

        for (int h = 1; h < val.get(0).length; h++) {
            double[][] points = new double[val.size()][2];
            for (int i = 0; i < val.size(); i++) {
                points[i][0] = val.get(i)[0];
                points[i][1] = val.get(i)[h];
            }
            DataSetPlot dsp = new DataSetPlot(points);
            if (result_names != null && result_names.length > h) {
                dsp.setTitle(result_names[h]);
            } else {
                dsp.setTitle("Result " + h);
            }
            p.addPlot(dsp);
            PlotStyle stl = ((AbstractPlot) p.getPlots().get(h - 1)).getPlotStyle();
            stl.setStyle(Style.LINES);
            //stl.setLineType(NamedPlotColor.BLUE);
//        stl.setStyle(Style.POINTS);
//        stl.setPointType(3); // nice stars
//        stl.setPointSize(2);
        }

        if (withGrid) {
            p.set("grid", "back ls 12");
        }

        p.plot();
        try {
            ImageIO.write(png.getImage(), "png", file);
        } catch (IOException ex) {
            Logger.getLogger(PlotUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void plot(double[] yAxis) {
        List<double[]> yAxes = new ArrayList<>();
        yAxes.add(yAxis);
        plot(yAxes);
    }

    public static void plot(List<double[]> yAxis) {

        String title = "Dummy";
        String[] names = new String[yAxis.size()];
        double[] xAxis = null;
        int cnt = -1;
        double minValue = Double.MAX_VALUE;
        double maxValue = -Double.MAX_VALUE;
        for (double[] yAxi : yAxis) {
            cnt++;
            String aName = String.valueOf(cnt);
            names[cnt] = aName;
            if (xAxis == null) {
                xAxis = new double[yAxi.length];
                for (int i = 0; i < xAxis.length; i++) {
                    xAxis[i] = i;
                }
            }
            double[] minMax = minmax(yAxi);
            minValue = Math.min(minValue, minMax[0]);
            maxValue = Math.max(maxValue, minMax[1]);
        }

        plot(xAxis, yAxis, title, names, minValue, maxValue, JavaPlot.Key.TOP_RIGHT);
    }

    private static double[] minmax(double[] d) {
        if (d == null || d.length == 0) {
            return null;
        }
        double min = d[0];
        double max = d[0];
        for (int i = 1; i < d.length; i++) {
            min = Math.min(d[i], min);
            max = Math.max(d[i], max);
        }
        return new double[]{min, max};
    }

    public static void plot(double[] xAxis, List<double[]> yAxis, String title, String[] result_names, double ymin, double ymax) {
        plot(xAxis, yAxis, title, result_names, ymin, ymax, JavaPlot.Key.TOP_RIGHT);
    }

    public static void plot(double[] xAxis, List<double[]> yAxis, String title, String[] result_names, double ymin, double ymax, JavaPlot.Key key) {
        Pair<String, String> p = null;
        if (!Double.isNaN(ymin) && !Double.isNaN(ymax)) {
            p = new Pair<>("yrange", "[" + ymin + ":" + ymax + "]");
        }
        if (p != null) {
            plot(xAxis, yAxis, title, result_names, key, p);
        } else {
            plot(xAxis, yAxis, title, result_names, key);
        }
    }

    public static void plot(double[] xAxis, List<double[]> yAxis, String title, String[] result_names, JavaPlot.Key key, Pair<String, String>... options) {
        JavaPlot p = getJavaPlot(xAxis, yAxis, title, result_names, key, options);
        p.plot();
    }

    public static void plot(double[] xAxis, List<double[]> yAxis, String title, String[] result_names, double ymin, double ymax, String fileName) {
        plot(xAxis, yAxis, title, result_names, ymin, ymax, fileName, JavaPlot.Key.TOP_RIGHT);
    }

    public static void plot(double[] xAxis, List<double[]> yAxis, String title, String[] result_names, double ymin, double ymax, String fileName, JavaPlot.Key key) {
        Pair<String, String> p = null;
        if (!Double.isNaN(ymin) && !Double.isNaN(ymax)) {
            p = new Pair<>("yrange", "[" + ymin + ":" + ymax + "]");
        }
        if (p != null) {
            plot(xAxis, yAxis, title, result_names, fileName, key, p);
        } else {
            plot(xAxis, yAxis, title, result_names, fileName, key);
        }
    }

    public static void plot(double[] xAxis, List<double[]> yAxis, String title, String[] result_names, String fileName, JavaPlot.Key key, Pair<String, String>... options) {
        ImageTerminal png = new ImageTerminal();
        File file = new File(fileName);
        try {
            file.createNewFile();
            png.processOutput(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            System.err.print(ex);
        } catch (IOException ex) {
            System.err.print(ex);
        }
        png.set("size", "1024,768");
        JavaPlot p = getJavaPlot(xAxis, yAxis, title, result_names, key, options);
        p.setTerminal(png);
        p.plot();
        try {
            ImageIO.write(png.getImage(), "png", file);
        } catch (IOException ex) {
            System.err.print(ex);
        }
    }

    public static void genAndPlotHists(List<List<Double>> lists) {
        double xmax = -Double.MAX_VALUE, xmin = Double.MAX_VALUE;

        for (List<Double> arrayList : lists) {
            Collections.sort(arrayList);
            xmax = Math.max(xmax, arrayList.get(arrayList.size() - 1));
            xmin = Math.min(xmin, arrayList.get(0));
        }
        int num = 100;
        double ymax = 0;
        double h = (xmax - xmin) / (num - 1);
        List<double[]> yVals = new ArrayList<double[]>();
        String[] names = new String[lists.size()];
        int size = 0;
        for (List<Double> arrayList : lists) {
            size += arrayList.size();
        }
        int nameIdx = 0;

        for (List<Double> arrayList : lists) {
            double[] vals = new double[num];
            int idx = 0;
            for (Double val : arrayList) {
                while (val > xmin + (idx + 0.5) * h) {
                    idx++;
                }
                vals[idx]++;
            }
            for (int i = 0; i < vals.length; i++) {
                vals[i] /= size;
                if (vals[i] > ymax) {
                    ymax = vals[i];
                }
            }
            yVals.add(vals);
            names[nameIdx] = "" + nameIdx + " (" + arrayList.size() + " values)";
            nameIdx++;
        }
        double[] xVals = new double[num];
        for (int i = 0; i < xVals.length; i++) {
            xVals[i] = xmin + i * h;

        }
        plot(xVals, yVals, "Histogramm", names, 0, ymax);
    }

    public static void plotLaTeX(double[] xAxis, List<double[]> yAxis, String title, String[] result_names, String fileName, JavaPlot.Key key, Pair<String, String>... options) {
        TextFileTerminal tex = new TextFileTerminal("tikz color standalone", fileName);
        tex.set("output", fileName);
        File file = new File(fileName);
        try {
            file.createNewFile();
            tex.processOutput(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            System.err.print(ex);
        } catch (IOException ex) {
            System.err.print(ex);
        }
        JavaPlot p = getJavaPlot(xAxis, yAxis, title, result_names, key, options);
        p.setTerminal(tex);
        p.plot();
    }

    private static JavaPlot getJavaPlot(double[] xAxis, List<double[]> yAxis, String title, String[] result_names, JavaPlot.Key key, Pair<String, String>... options) {
        int plotcnt = -1;
        JavaPlot p = new JavaPlot();
        for (Pair<String, String> option : options) {
            if (option.getFirst().equals("xlabel")) {
                p.getAxis("x").setLabel(option.getSecond());
            } else if (option.getFirst().equals("ylabel")) {
                p.getAxis("y").setLabel(option.getSecond());
            } else {
                p.set(option.getFirst(), option.getSecond());
            }
        }

        if (xAxis == null) {
            int size = 0;
            for (int i = 0; i < yAxis.size(); i++) {
                size = Math.max(size, yAxis.get(i).length);
            }
            xAxis = new double[size];
            for (int i = 0; i < xAxis.length; i++) {
                xAxis[i] = i;
            }
        }
        p.setTitle(title);
        p.setKey(key);
        for (int h = 0; h < yAxis.size(); h++) {
            double[] yY = yAxis.get(h);
            double[][] points = new double[yY.length][];
            for (int i = 0; i < yY.length; i++) {
                points[i] = new double[]{xAxis[i], yY[i]};
            }
            DataSetPlot dsp = new DataSetPlot(points);
            if (result_names != null && result_names.length > h) {
                dsp.setTitle(result_names[h]);
            } else {
                dsp.setTitle("Result " + h);
            }

            p.addPlot(dsp);
            plotcnt++;
            PlotStyle stl = ((AbstractPlot) p.getPlots().get(plotcnt)).getPlotStyle();
            stl.setStyle(Style.LINES);
        }

        return p;
    }

}
