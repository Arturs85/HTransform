package sample;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.*;
import java.lang.management.GarbageCollectorMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.TreeSet;

import static java.lang.Math.*;

public class HTransforFinder {
    int x1, x2, y1, y2;
    ArrayList<Point> curLine = new ArrayList<>(2);
    MapFromImage map;
    double[] histogram = new double[180];//[degrees]- val = px^2 summ
   double [] histX;
   double [] histY;
    // double[] corel;
    public HTransforFinder(MapFromImage map) {
        this.map = map;

    }

   double[] calcCorelation(HTransforFinder other) {
    return calcCorelation(other.histogram,histogram);
    }

  static double[] calcCorelation(double[] otherHist,double[] histogram){
        double[] corel = new double[histogram.length];
        for (int i = 0; i < histogram.length; i++) {//shift
            for (int j = 0; j < histogram.length; j++) {//curInd
               int otherIndex = i+j;
               if(otherIndex>=histogram.length)
                   otherIndex-=histogram.length;
              corel[i]+=  histogram[j]*otherHist[otherIndex];

            }
        }
       return corel;
    }

static int maxCorel(double[] hist){
    double maxSoFar = 0;
    int degrSoFar = 0;
        for (int i = 0; i < hist.length; i++) {
        if(hist[i]>maxSoFar){
            maxSoFar=hist[i];
        degrSoFar = i;
        }
    }
    return degrSoFar;
    }

    boolean calcLineStartNEnd(int degrees, int l, int w, int h) {
        //sākumunkti uz ass x = 0, vai y = h, normalāle no 0 - 90 grādi
        double x1, x2, y1, y2;
        if (degrees == 90) {
            x1 = 0;
            y1 = l;
            x2 = w;
            y2 = l;
        } else if (degrees == 0) {
            x1 = l;
            y1 = 0;
            x2 = l;
            y2 = h;
        } else {
            double x = l / Math.cos(Math.toRadians(degrees));

            if (x > w) {// punkts ir x=w ass
                x2 = w;
                y2 = (x - w) * Math.tan(Math.toRadians(90 - degrees));
            } else {
                x2 = x;
                y2 = 0;
            }


            double y = l / Math.sin(Math.toRadians(degrees));

            if (y > h) {
                x1 = (y - h) * Math.tan(Math.toRadians(degrees));
                y1 = h;
            } else {
                x1 = 0;
                y1 = y;
            }
        }
        this.x1 = (int) x1;
        this.x2 = (int) x2;
        this.y1 = (int) y1;
        this.y2 = (int) y2;
//if(degrees>90){
//    this.x1+=map.width;
//    this.x2+=map.width;
//}

        if (x1 > w || x2 > w || y1 > h || y2 > h)
            return false;// line is not valid
        else
            return true;
    }


    boolean calcLineStartNEnd2(int degrees, int l, int w, int h) {
        curLine.clear();
        if (degrees == 90) {
            curLine.add(new Point(0, l));
            curLine.add(new Point(0, l));
        }
        if (degrees == 0) {
            curLine.add(new Point(l, 0));
            curLine.add(new Point(l, h));
        } else {


            double rad = toRadians(degrees);
            double ya = l / sin(rad);
            if (ya > 0 && ya < h) {// hip x = 0
                curLine.add(new Point(0, (int) ya));
            }
            ya = (l - w * cos(rad)) / sin(rad);
            if (ya > 0 && ya < h) {
                curLine.add(new Point(w, (int) ya));
            }

            double xa = (l / cos(rad));
            if (xa > 0 && xa < w) {
                curLine.add(new Point((int) xa, 0));
            }
            xa = (l - h * sin(rad)) / cos(rad);
            if (xa > 0 && xa < w) {
                curLine.add(new Point((int) xa, h));
            }
        }
        if (curLine.size() == 2) return true;
        else return false;

    }


    void countHistogram(Canvas canvas) {
        int maxL = (int) Math.sqrt(map.height * map.height + map.width * map.width);
        for (int i = 0; i < 179; i += 1) {
            for (int j = 0; j < maxL; j = j + 1) {
                if (calcLineStartNEnd2(i-89, j, map.width, map.height)) {
                    //int pxOnLine = countFilledPoints(x1,y1,x2,y2,map.pr,canvas);
                    int pxOnLine = countFilledPoints(curLine.get(0).x, curLine.get(0).y, curLine.get(1).x, curLine.get(1).y, map.pr, canvas);

                    histogram[i] += (pxOnLine * pxOnLine);
                }
            }
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
        for (int i = 0; i < histogram.length; i++) {
            System.out.println("deg " + i + " val " + histogram[i]);
        }
    }

    //bresenham
    int countFilledPoints(int x1, int y1, int x2, int y2, PixelReader pr, Canvas canvas) {
        //GraphicsContext gc = canvas.getGraphicsContext2D();
       // gc.setStroke(Color.RED);

        ArrayList<Point> brezLne = generateLine(new Point(x1, y1), new Point(x2, y2));
        int count = 0;

        for (Point p :
                brezLne) {

            int argb = 0;
            try {
                argb = pr.getArgb(p.x, p.y);

            } catch (IndexOutOfBoundsException e) {
                //  System.out.println("bad index "+x+" or y "+y);
                continue;
            }
           // if (((argb >> 0) & 0x000000ff) > 10) {
                if(argb!=0) {
              //  gc.strokeLine(p.x, p.y, p.x, p.y);

                count++;
            }
        }


        return count;
    }

    void drawLines(Canvas canvas, int xOffset) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        int maxL = (int) Math.sqrt(map.height * map.height + map.width * map.width);
        for (int i = -89; i <= 90; i += 30) {
            for (int j = 0; j < maxL; j = j + 10) {
                if (calcLineStartNEnd2(i, j, map.width, map.height)) {
                    gc.setStroke(Color.GREEN);

                    gc.strokeLine(curLine.get(0).x + xOffset, map.height - curLine.get(0).y, curLine.get(1).x + xOffset, map.height - curLine.get(1).y);

                    // gc.strokeLine(x1 + xOffset, map.height - y1, x2 + xOffset, map.height - y2);
                }
            }
        }
    }

   static void drawHistogram(double[] histogram,Canvas canvas, int yOffset, int xScale, int yScale, Color color, int labelInt, int labelOffset) {
       yOffset+=10;
        normalizeHist( histogram);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.DARKGRAY);
        gc.strokeLine(0,yScale+yOffset,histogram.length*xScale,yScale+yOffset);
gc.str
        gc.setStroke(color);
        int labelCounter=0;
        for (int i = 1; i < histogram.length; i++) {
            gc.strokeLine((i - 1) * xScale, yScale - histogram[i - 1] * yScale + yOffset, (i) * xScale, yScale - histogram[i] * yScale + yOffset);
        //x vals
            if(i%labelInt==0){
            gc.strokeText((i+labelOffset)+"",i*xScale,yScale+yOffset+15);
        }


        }


    }

   static void normalizeHist(double[] histogram) {
        double max = Arrays.stream(histogram).max().getAsDouble();
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = histogram[i] / max;
        }

    }

    double[] countTranslaeHistogramX(){
        PixelReader pr = map.pr;
        int w = map.width;
        int h = map.height;
        double[] histx = new double[w];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int argb = 0;
                try {
                    argb = pr.getArgb(i, j);

                } catch (IndexOutOfBoundsException e) {
                    //  System.out.println("bad index "+x+" or y "+y);
                    continue;
                }
                // if (((argb >> 0) & 0x000000ff) > 10) {
                if(argb!=0) {
                    //  gc.strokeLine(p.x, p.y, p.x, p.y);

                    histx[i]++;
                }
            }
        }
        histX = histx;
      return histx;
    }
    double[] countTranslaeHistogramY(){
        PixelReader pr = map.pr;
        int w = map.width;
        int h = map.height;
        double[] histy = new double[h];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int argb = 0;
                try {
                    argb = pr.getArgb(j, i);

                } catch (IndexOutOfBoundsException e) {
                    //  System.out.println("bad index "+x+" or y "+y);
                    continue;
                }
                // if (((argb >> 0) & 0x000000ff) > 10) {
                if(argb!=0) {
                    //  gc.strokeLine(p.x, p.y, p.x, p.y);

                    histy[i]++;
                }
            }
        }
        histY = histy;
        return histy;
    }

    //Brezenheima alg
    ArrayList<Point> generateLine(Point start, Point end) {
        ArrayList<Point> possibleLine = new ArrayList<>(500);

        int xn, yn, dx, dy, pn, xi, yi, i;
        int x1 = end.x;
        int y1 = end.y;
        int x2 = start.x;
        int y2 = start.y;

        i = -1;
        xn = x1;
        yn = y1;
        dx = abs(x2 - x1);
        dy = abs(y2 - y1);
        if (x1 < x2)
            xi = 1;
        else
            xi = -1;
        if (y1 < y2)
            yi = 1;
        else
            yi = -1;
        i++;
        if (dx >= dy) { // Lezen�s l�nijas
            pn = 2 * dy - dx;
            //tekstaLauks.append(i + ".)x: " + xn + " y: " + yn + " pn: " + pn + "\n");

            while (xn != x2) {
                if (pn > 0) {
                    xn = xn + xi;
                    yn = yn + yi;
                    pn = pn + 2 * dy - 2 * dx;
                } else {
                    xn = xn + xi;
                    pn = pn + 2 * dy;
                }
                i++;
                //	g.drawLine(xn, yn, xn, yn);
                //tekstaLauks.append(i + ".)x: " + xn + " y: " + yn + " pn: " + pn + "\n");

                possibleLine.add(new Point(xn, yn));

            }

        } // St�v�s l�nijas
        else {
            pn = 2 * dx - dy;
            //	tekstaLauks.append(i + ".)x: " + xn + " y: " + yn + " pn: " + pn + "\n");

            while (yn != y2) {
                if (pn > 0) {
                    xn = xn + xi;
                    yn = yn + yi;
                    pn = pn + 2 * dx - 2 * dy;

                } else {
                    yn = yn + yi;
                    pn = pn + 2 * dx;
                }
                i++;
                //	g.drawLine(xn, yn, xn, yn);
                //tekstaLauks.append(i + ".)x: " + xn + " y: " + yn + " pn: " + pn + "\n");
                possibleLine.add(new Point(xn, yn));

            }
        }
        return possibleLine;
    }


}
