import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/*
  This is a class
  Created 2020-03-25
  Edited 2020-04-23

  @author Magnus Silverdal
  @developer Allan Bäckman
 */

/**
 * Color scheme used
 * https://coolors.co/app/002626-0e4749-95c623-e55812-efe7da
 *
 * Brick colors
 * https://coolors.co/ac69a0-e9b794-779fa1-c0c999-feadc0
 */
public class Sprite {
    private int width;
    private int height;
    private int[] pixels;

    public Sprite(int w, int h, int col) {
        this.width = w;
        this.height = h;
        pixels = new int[w*h];
        Arrays.fill(pixels, col);
    }

    public Sprite(String path) {
        BufferedImage image = null;
        try {
            BufferedImage rawImage = ImageIO.read(new File(path));
            // Since the type of image is unknown it must be copied into an INT_RGB
            image = new BufferedImage(rawImage.getWidth(), rawImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            image.getGraphics().drawImage(rawImage, 0, 0, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.width = image.getWidth();
        this.height = image.getHeight();
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public int[] getPixels() { return pixels; }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setColor(int color) {
        Arrays.fill(pixels, color);
    }
}