package sample;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Rotate;

import java.io.File;

public class MapFromImage {
   Image image;
    int width;
    int height;
    PixelReader pr;

    public MapFromImage(String imagePath) {
    loadImage(imagePath);
    }

    public MapFromImage(Image image) {
        this.image = image;
    pr= image.getPixelReader();
        width = (int)image.getWidth();
        height = (int)image.getHeight();
    }
    public MapFromImage(File file) {
       loadImage(file);
    }

    void loadImage(String imagePath){
        File file = new File(imagePath);
         image = new Image(file.toURI().toString());
         pr =image.getPixelReader();
         width = (int)image.getWidth();
         height = (int)image.getHeight();
        System.out.println("Loaded image w "+width+" h "+height);

    }
    void loadImage(File file){
        image = new Image(file.toURI().toString());
        pr =image.getPixelReader();
        width = (int)image.getWidth();
        height = (int)image.getHeight();
        System.out.println("Loaded image w "+width+" h "+height);

    }


 void   drawImage(Canvas canvas, int xOffset, double rotation,double dx, double dy, Color color){
        GraphicsContext gc = canvas.getGraphicsContext2D();
     gc.save();
       rotate(gc,rotation,dx,dy);
       // gc.rotate(rotation);
   // gc.translate(dx, dy);
     gc.setFill(color);//(new Color(1,0,0,1));
     gc.setStroke(color);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

             int argb = pr.getArgb(i,j);
          //  if(((argb>>24) & 0x000000ff) > 1) {
                if(argb!=0) {
               // gc.strokeLine(i+xOffset, j, i+xOffset, j);
                gc.fillRect(i+xOffset, j, 1,1);

            }
            }
        }


gc.restore();

 }
    void   drawImageTranslated(Canvas canvas, int xOffset, double rotation,double dx, double dy, Color color){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();
        //rotate(gc,rotation,dx,dy);
        // gc.rotate(rotation);
         gc.translate(dx, dy);
        gc.setFill(color);//(new Color(1,0,0,1));
        gc.setStroke(color);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                int argb = pr.getArgb(i,j);
                //  if(((argb>>24) & 0x000000ff) > 1) {
                if(argb!=0) {
                    // gc.strokeLine(i+xOffset, j, i+xOffset, j);
                    gc.fillRect(i+xOffset, j, 1,1);

                }
            }
        }


        gc.restore();

    }
    private void rotate(GraphicsContext gc, double angle, double px, double py) {
        Rotate r = new Rotate(angle, px, py);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
    }
}
