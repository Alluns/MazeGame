import javafx.scene.layout.Background;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * This is a class
 * Created 2020-03-25
 * Edited 2020-04-03
 *
 * @author Magnus Silverdal
 * @developer Allan Bäckman
 */
public class Graphics extends Canvas implements Runnable {
    private String title = "Graphics";
    private int width;
    private int height;

    private JFrame frame;
    private BufferedImage image;
    private int[] pixels;
    private int scale;

    // Window "Specs"
    private Thread thread;
    private boolean running = false;
    private int fps = 60;
    private int ups = 60;

    // Character "Specs"
    int movementSpeed = 1;

    // Define Sprites
    private Sprite backdrop;
    private Sprite player;
    private Sprite wall;

    // Sprite cords
    private int xBackDrop = 0;
    private int yBackDrop = 0;
    private int xWall = 100;
    private int yWall = 100;

    // Player cords
    private int xPlayer = 160;
    private int yPlayer = 160;
    private int vxPlayer = 0;
    private int vyPlayer = 0;

    //Draw the frame
    public Graphics(int w, int h, int scale) {
        this.width = w;
        this.height = h;
        this.scale = scale;
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        Dimension size = new Dimension(scale*width, scale*height);
        setPreferredSize(size);
        frame = new JFrame();
        frame.setTitle(title);
        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        this.addKeyListener(new MyKeyListener());
        this.requestFocus();

        //Add Sprites to be drawn
        backdrop = new Sprite(width,height,0xFFFFFF);
        player = new Sprite("img/Main.png");
        wall = new Sprite(8,8,0x678D58);
    }

    private void draw() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        java.awt.Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        g.dispose();
        bs.show();
    }

    private void update() {
        for (int i = 0 ; i < pixels.length ; i++) {
            pixels[i] = 0;
        }

        //Background sprite
        for (int i = 0 ; i < backdrop.getHeight() ; i++) {
            for (int j = 0 ; j < backdrop.getWidth() ; j++) {
                pixels[(yBackDrop+i)*width + xBackDrop+j] = backdrop.getPixels()[i*backdrop.getWidth()+j];
            }
        }

        //Draw Maze


        //Wall Sprite
        for (int i = 0 ; i < wall.getHeight() ; i++) {
            for (int j = 0 ; j < wall.getWidth() ; j++) {
                pixels[(yWall+i)*width + xWall+j] = wall.getPixels()[i*wall.getWidth()+j];
            }
        }

        //The Character Sprite
        if (xPlayer + vxPlayer < 0 || xPlayer + vxPlayer > width - player.getWidth())
            vxPlayer = 0;
        if (yPlayer + vyPlayer < 0 || yPlayer + vyPlayer > height - player.getHeight())
            vyPlayer = 0;

        //Player movement (Using AABB to calculate collisions)
        if (!(xPlayer + vxPlayer < xWall + 8 && xPlayer + vxPlayer + 8 > xWall && yPlayer + vyPlayer < yWall + 8 && yPlayer + vyPlayer + 8 > yWall)) {
            xPlayer += vxPlayer;
            yPlayer += vyPlayer;
        }



        for (int i = 0 ; i < player.getHeight() ; i++) {
            for (int j = 0 ; j < player.getWidth() ; j++) {
                pixels[(yPlayer+i)*width + xPlayer+j] = player.getPixels()[i*player.getWidth()+j];
            }
        }
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        double frameUpdateinteval = 1000000000.0 / fps;
        double stateUpdateinteval = 1000000000.0 / ups;
        double deltaFrame = 0;
        double deltaUpdate = 0;
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            deltaFrame += (now - lastTime) / frameUpdateinteval;
            deltaUpdate += (now - lastTime) / stateUpdateinteval;
            lastTime = now;

            while (deltaUpdate >= 1) {
                update();
                deltaUpdate--;
            }

            while (deltaFrame >= 1) {
                draw();
                deltaFrame--;
            }
        }
        stop();
    }

    private class MyKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent keyEvent) {

        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            if (keyEvent.getKeyChar()=='a') {
                vxPlayer = -movementSpeed;
            }
            if (keyEvent.getKeyChar()=='d') {
                vxPlayer = movementSpeed;
            }
            if (keyEvent.getKeyChar()=='w') {
                vyPlayer = -movementSpeed;
            }
            if (keyEvent.getKeyChar()=='s') {
                vyPlayer = movementSpeed;
            }
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            if (keyEvent.getKeyChar()=='a' || keyEvent.getKeyChar()=='d') {
                vxPlayer = 0;
            } else if (keyEvent.getKeyChar()=='w' || keyEvent.getKeyChar()=='s') {
                vyPlayer = 0;
            }
        }
    }
}

