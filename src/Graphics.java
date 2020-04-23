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
 * Edited 2020-04-23
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

    //Window variables
    private Thread thread;
    private boolean running = false;
    private int fps = 60;
    private int ups = 60;

    //Character variables
    int movementSpeed = 1;
    boolean playerColliding = false;

    //Define Sprites
    private Sprite backdrop;
    private Sprite player;
    private Sprite wall;
    private Sprite goal;
    private Sprite start;

    //Sprite coordinates
    private int xBackDrop = 0;
    private int yBackDrop = 0;
    private int xGoal = 5;
    private int yGoal = 5;
    private int xStart = 3;
    private int yStart = 4;

    //Maze coordinates
    private int[] xWall = {0, 1, 2, 3, 4, 5, 6, 0, 3, 6, 0, 3};
    private int[] yWall = {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 3};

    //Player coordinates
    private int xPlayer = xStart * 8;
    private int yPlayer = yStart * 8;
    private int vxPlayer = 0;
    private int vyPlayer = 0;

    //Draw the frame
    public Graphics(int w, int h, int scale) {
        this.width = w;
        this.height = h;
        this.scale = scale;
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        Dimension size = new Dimension(scale * width, scale * height);
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
        backdrop = new Sprite(width, height, 0xFFFFFF);
        player = new Sprite("img/Main.png");
        wall = new Sprite("img/Wall.png");
        goal = new Sprite("img/Goal.png");
        start = new Sprite("img/Start.png");
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
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0;
        }

        //Background sprite
        for (int i = 0; i < backdrop.getHeight(); i++) {
            for (int j = 0; j < backdrop.getWidth(); j++) {
                pixels[(yBackDrop + i) * width + xBackDrop + j] = backdrop.getPixels()[i * backdrop.getWidth() + j];
            }
        }

        //Wall Sprite
        for (int k = 0; k < xWall.length; k++) {
            for (int i = 0; i < wall.getHeight(); i++) {
                for (int j = 0; j < wall.getWidth(); j++) {
                    pixels[(yWall[k] * 8 + i) * width + xWall[k] * 8 + j] = wall.getPixels()[i * wall.getWidth() + j];
                }
            }
        }

        //Draw Goal
        for (int i = 0; i < goal.getHeight(); i++) {
            for (int j = 0; j < goal.getWidth(); j++) {
                pixels[(yGoal * 8 + i) * width + xGoal * 8 + j] = goal.getPixels()[i * goal.getWidth() + j];
            }
        }

        //Draw Start
        for (int i = 0; i < start.getHeight(); i++) {
            for (int j = 0; j < start.getWidth(); j++) {
                pixels[(yStart * 8 + i) * width + xStart * 8 + j] = start.getPixels()[i * start.getWidth() + j];
            }
        }

        //Keep player from going outside canvas
        if (xPlayer + vxPlayer < 0 || xPlayer + vxPlayer > width - player.getWidth())
            vxPlayer = 0;
        if (yPlayer + vyPlayer < 0 || yPlayer + vyPlayer > height - player.getHeight())
            vyPlayer = 0;

        //Player movement (Using AABB collision to calculate... well... collisions)

        //Check if player is colliding on the X axis
        playerColliding = false;
        for (int k = 0; k < xWall.length; k++) {
            if ((xPlayer + vxPlayer < xWall[k] * 8 + 8 &&
                    xPlayer + vxPlayer + 8 > xWall[k] * 8 &&
                    yPlayer < yWall[k] * 8 + 8 &&
                    yPlayer + 8 > yWall[k] * 8)) {
                playerColliding = true;
            }
        }
        if (!playerColliding) {
            xPlayer += vxPlayer;
        }

        //Check if player is colliding on the Y axis
        playerColliding = false;
        for (int k = 0; k < xWall.length; k++) {
            if ((xPlayer < xWall[k] * 8 + 8 &&
                    xPlayer + 8 > xWall[k] * 8 &&
                    yPlayer + vyPlayer < yWall[k] * 8 + 8 &&
                    yPlayer + vyPlayer + 8 > yWall[k] * 8)) {
                playerColliding = true;
            }
        }
        if (!playerColliding) {
            yPlayer += vyPlayer;
        }

        //Check if the player has reached the goal
        if ((xPlayer < xGoal * 8 + 8 && xPlayer + 8 > xGoal * 8 && yPlayer < yGoal * 8 + 8 && yPlayer + 8 > yGoal * 8)) {
            JOptionPane.showMessageDialog(null, "You win!");
            System.exit(0); //Closes the game
        }

        //Draw the player sprite
        for (int i = 0; i < player.getHeight(); i++) {
            for (int j = 0; j < player.getWidth(); j++) {
                pixels[(yPlayer + i) * width + xPlayer + j] = player.getPixels()[i * player.getWidth() + j];
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
            if (keyEvent.getKeyChar() == 'a') {
                vxPlayer = -movementSpeed;
            }
            if (keyEvent.getKeyChar() == 'd') {
                vxPlayer = movementSpeed;
            }
            if (keyEvent.getKeyChar() == 'w') {
                vyPlayer = -movementSpeed;
            }
            if (keyEvent.getKeyChar() == 's') {
                vyPlayer = movementSpeed;
            }
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            if (keyEvent.getKeyChar() == 'a' || keyEvent.getKeyChar() == 'd') {
                vxPlayer = 0;
            } else if (keyEvent.getKeyChar() == 'w' || keyEvent.getKeyChar() == 's') {
                vyPlayer = 0;
            }
        }
    }
}

