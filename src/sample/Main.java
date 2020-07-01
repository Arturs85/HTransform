package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;

public class Main extends Application {
    static Canvas canvas = new Canvas(1000, 800);

    @Override
    public void start(Stage primaryStage) throws Exception {
        ScrollPane scrollPane = new ScrollPane(canvas);
        BorderPane root = new BorderPane(scrollPane);
        // Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("HTransform");
        primaryStage.setScene(new Scene(root, 1100, 800));
        primaryStage.show();
        MapFromImage map1 = new MapFromImage("/home/arturs/Downloads/lokalizacija_ASR/testMap1.png");
        MapFromImage map2 = new MapFromImage("/home/arturs/Downloads/lokalizacija_ASR/testMap2.png");

        map1.drawImage(canvas, 0, 0d, 0, 0, Color.GREEN);
        //  map2.drawImage(canvas, map1.width, 0d, 0, 0, Color.RED);
showPlotWindow("Pirmā karte",map1.width,map1.height,null,map1,0,Color.CRIMSON);
        showPlotWindow("Otrā karte",map2.width,map2.height,null,map2,0,Color.GREEN);

        HTransforFinder htf = new HTransforFinder(map1);
        htf.countHistogram(canvas);
//htf.drawLines(canvas,map1.width+200);
      //  htf.drawHistogram(htf.histogram, canvas, 300, 3, 200, Color.GREEN, 45,-89);
        drawPlotInWindow("1. kartes leņķiskais spektrs", htf.histogram, Color.GREEN,-90);

        HTransforFinder htf2 = new HTransforFinder(map2);
        htf2.countHistogram(canvas);
        //htf2.drawHistogram(htf2.histogram, canvas, 300, 3, 200, Color.CADETBLUE, 45,-89);
        drawPlotInWindow("2. kartes leņķiskais spektrs", htf2.histogram, Color.GREEN,-90);

        double[] cor = htf.calcCorelation(htf2);
        //htf.drawHistogram(cor, canvas, 500, 3, 200, Color.CRIMSON, 45,-90);
        drawPlotInWindow("1-2. kartes leņķisko spektru korelācija",cor, Color.GREEN,0);

        int rot = HTransforFinder.maxCorel(cor);
        System.out.println("rotation: " + rot);
//canvas.setRotate((rot));

        Canvas c2 = showPlotWindow("otrā karte pēc 98 grādu rotācijas",map1.width, map1.height * 2, map1, map2, rot,Color.CADETBLUE);
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        WritableImage wi = new WritableImage((int) c2.getWidth(), (int) c2.getHeight());

        c2.snapshot(sp, wi);
        File f = new File("savedMapim");
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(wi, null);
        ImageIO.write(renderedImage, "png", f);
        System.out.println("file :  saved");
        // wi.getPixelReader();
        //MapFromImage rotatedMap2 = new MapFromImage(f);
        //MapFromImage rotatedMap2 = new MapFromImage("/home/arturs/Documents/HTransform/savedMapim2.png");
        MapFromImage rotatedMap2 = new MapFromImage(wi);
        HTransforFinder htRot2 = new HTransforFinder(rotatedMap2);
        htRot2.countHistogram(canvas);
        htRot2.drawHistogram(htRot2.histogram, canvas, 600, 3, 200, Color.BLACK, 45,0);
        rotatedMap2.drawImage(canvas, map1.width, 0d, 0, 0, Color.CRIMSON);

        htf.countTranslaeHistogramX();
        htf.countTranslaeHistogramY();
        htRot2.countTranslaeHistogramX();
        htRot2.countTranslaeHistogramY();
        drawPlotInWindow("1. kartes x spektrs", htf.histX, Color.GREEN,0);
        drawPlotInWindow("1. kartes y spektrs", htf.histY, Color.CRIMSON,0);
        drawPlotInWindow("2. kartes x spektrs", htRot2.histX, Color.GREEN,0);
        drawPlotInWindow("2. kartes y spektrs", htRot2.histY, Color.CRIMSON,0);

        double[] corx = HTransforFinder.calcCorelation(htRot2.histX, htf.histX);
        double[] cory = HTransforFinder.calcCorelation(htRot2.histY, htf.histY);
        drawPlotInWindow("1-2. kartes x spektru korelācija", corx, Color.ORANGERED,0);
        drawPlotInWindow("1-2. kartes y spektru korelācija", cory, Color.ORANGERED,0);

        int maxx = HTransforFinder.maxCorel(corx);
        int maxy = HTransforFinder.maxCorel(cory);
        System.out.println("xcor = " + maxx);
        System.out.println("ycor = " + maxy);
        rotatedMap2.drawImageTranslated(canvas, 0, 0, -37, -52, Color.CADETBLUE);
        map1.drawImage(canvas, 0, 0d, 0, 0, Color.CRIMSON);

    }

    Canvas showPlotWindow(String name, int w, int h, MapFromImage m1, MapFromImage m2, int rot,Color color) {
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
        Stage stage = new Stage();
        stage.setX(100);
        stage.setY(100);

        StackPane root = new StackPane();

        Canvas canvas = new Canvas(w, h);

        root.getChildren().add(canvas);
        stage.setTitle(name);

        stage.setScene(new Scene(root));
        stage.show();
if(m1!=null)
        m2.drawImage(canvas, 0, -rot, m1.width / 2, m1.height / 2, color);
      else
    m2.drawImage(canvas, 0, -rot, 0, 0, color);

        // m1.drawImage(canvas, 0, 0, 0, 0, Color.BLACK);
        //m2.drawImage(canvas, 0, 0, 0, 0, Color.BLACK);
//
        //
//            }
//        });
        return canvas;
    }

    void drawPlotInWindow(String name, double[] hist, Color color,int labelOffset) {
        int xscale = 3;
        int yscale = 200;
//  Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
        Stage stage = new Stage();
        stage.setX(100);
        stage.setY(100);

        StackPane root = new StackPane();

        Canvas canvas = new Canvas(hist.length * xscale + 20, yscale + 30);

        root.getChildren().add(canvas);
        stage.setTitle(name);

        stage.setScene(new Scene(root));
        stage.show();
        HTransforFinder.drawHistogram(hist, canvas, 0, xscale, yscale, color, 20,labelOffset);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
