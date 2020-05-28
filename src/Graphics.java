import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is a class
 * Created 2020-03-25
 * Edited 2020-04-30
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
    private int ups = 70;
    private int SpriteSize = 32;

    //Character variables
    int movementSpeed = 2;
    private boolean playerColliding = false;
    private int playerTimer = 0;
    private int playerActiveSprite = 1;
    private boolean playerLookingRight = false;

    //Define Sprites
    private Sprite backdrop = new Sprite(width, height, 0xFFFFFF);
    private Sprite player = new Sprite("img/Player.png");
    private Sprite wall = new Sprite("img/Wall Texture.png");
    private Sprite floor = new Sprite("img/Floor Texture.png");
    private Sprite goal = new Sprite("img/Exit.png");
    private Sprite start = new Sprite("img/Exit.png");
    private Sprite maze = new Sprite("img/1Maze.png");

    //Sprite coordinates
    private int xBackDrop = 0;
    private int yBackDrop = 0;
    private int xGoal = 0;
    private int yGoal = 0;
    private int xStart = 9;
    private int yStart = 9;

    //Maze coordinates
    private ArrayList<Integer> xWall = new ArrayList<>();
    private ArrayList<Integer> yWall = new ArrayList<>();
    private ArrayList<Integer> xFloor = new ArrayList<>();
    private ArrayList<Integer> yFloor = new ArrayList<>();
    private boolean mazeDrawn = false;
    private int stage = 1;

    //Player coordinates
    private int xPlayer = 1;
    private int yPlayer = 1;
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
        Arrays.fill(pixels, 0);

        //Create Maze array
        if (!mazeDrawn) {
            int x = 0;
            int y = 0;
            for (int i = 0; i < maze.getHeight(); i++) {
                for (int j = 0; j < maze.getWidth(); j++) {
                        if (maze.getPixels()[i * maze.getWidth() + j] == 0) {
                            xWall.add(x);
                            yWall.add(y);
                        } else if (maze.getPixels()[i * maze.getWidth() + j] == 65280) {
                            xStart = x;
                            yStart = y;
                            xFloor.add(x);
                            yFloor.add(y);
                            xPlayer = xStart * SpriteSize;
                            yPlayer = yStart * SpriteSize;
                        } else if (maze.getPixels()[i * maze.getWidth() + j] == 255) {
                            xGoal = x;
                            yGoal = y;
                            xFloor.add(x);
                            yFloor.add(y);
                        } else {
                            xFloor.add(x);
                            yFloor.add(y);
                        }
                    if (x == maze.getWidth() - 1) {
                        x = 0;
                        y++;
                    } else {
                        x++;
                    }

                    //System.out.println("X =" + x + "    ,Y = " + y + "  ,Color = " + maze.getPixels()[i * maze.getWidth() + j]);
                }
            }
            mazeDrawn = true;
        }

        //Background sprite
        for (int i = 0; i < backdrop.getHeight(); i++) {
            for (int j = 0; j < backdrop.getWidth(); j++) {
                pixels[(yBackDrop + i) * width + xBackDrop + j] = backdrop.getPixels()[i * backdrop.getWidth() + j];
            }
        }

        //Floor Sprite
        for (int k = 0; k < xFloor.size(); k++) {
            for (int i = 0; i < floor.getHeight(); i++) {
                for (int j = 0; j < floor.getWidth(); j++) {
                    pixels[(yFloor.get(k) * SpriteSize + i) * width + xFloor.get(k) * SpriteSize + j] = floor.getPixels()[i * floor.getWidth() + j];
                }
            }
        }

        //Wall Sprite
        for (int k = 0; k < xWall.size(); k++) {
            for (int i = 0; i < wall.getHeight(); i++) {
                for (int j = 0; j < wall.getWidth(); j++) {
                        pixels[(yWall.get(k) * SpriteSize + i) * width + xWall.get(k) * SpriteSize + j] = wall.getPixels()[i * wall.getWidth() + j];
                }
            }
        }

        //Draw Goal
        for (int i = 0; i < goal.getHeight(); i++) {
            for (int j = 0; j < goal.getWidth(); j++) {
                if (!(goal.getPixels()[i * goal.getWidth() + j] / 240 == 0)) {
                    pixels[(yGoal * SpriteSize + i) * width + xGoal * SpriteSize + j] = goal.getPixels()[i * goal.getWidth() + j];
                }
            }
        }

        //Draw Start
        for (int i = 0; i < start.getHeight(); i++) {
            for (int j = 0; j < start.getWidth(); j++) {
                if (!(start.getPixels()[i * start.getWidth() + j] / 240 == 0)) {
                    pixels[(yStart * SpriteSize + i) * width + xStart * SpriteSize + j] = start.getPixels()[i * start.getWidth() + j];
                }
            }
        }

        //Keep player from going outside canvas
        if (xPlayer + vxPlayer < 0 || xPlayer + vxPlayer > width - player.getWidth())
            vxPlayer = 0;
        if (yPlayer + vyPlayer < 0 || yPlayer + vyPlayer > height - player.getHeight())
            vyPlayer = 0;


        //Check if player is colliding on the X axis
        playerColliding = false;
        for (int k = 0; k < xWall.size(); k++) {
            if ((xPlayer + vxPlayer < xWall.get(k) * SpriteSize + SpriteSize &&
                    xPlayer + vxPlayer + SpriteSize > xWall.get(k) * SpriteSize &&
                    yPlayer < yWall.get(k) * SpriteSize + SpriteSize &&
                    yPlayer + SpriteSize > yWall.get(k) * SpriteSize)) {
                playerColliding = true;
                break;
            }
        }
        if (!playerColliding) {
            xPlayer += vxPlayer;
        }

        //Check if player is colliding on the Y axis
        playerColliding = false;
        for (int k = 0; k < xWall.size(); k++) {
            if ((xPlayer < xWall.get(k) * SpriteSize + SpriteSize &&
                    xPlayer + SpriteSize > xWall.get(k) * SpriteSize &&
                    yPlayer + vyPlayer < yWall.get(k) * SpriteSize + SpriteSize &&
                    yPlayer + vyPlayer + SpriteSize > yWall.get(k) * SpriteSize)) {
                playerColliding = true;
                break;
            }
        }
        if (!playerColliding) {
            yPlayer += vyPlayer;
        }

        //Check if the player has reached the goal
        if ((xPlayer < xGoal * SpriteSize + SpriteSize && xPlayer + SpriteSize > xGoal * SpriteSize && yPlayer < yGoal * SpriteSize + SpriteSize && yPlayer + SpriteSize > yGoal * SpriteSize)) {
            stage++;
            System.out.println("img/" + stage + "Maze.png");
            maze = new Sprite("img/" + stage + "Maze.png");
            xWall.clear();
            yWall.clear();
            xFloor.clear();
            yFloor.clear();
            mazeDrawn = false;
        }

        /* Player Sprite */

        //Animate the player Sprite
        if (vxPlayer < 0) {playerLookingRight = false;}
        if (vxPlayer > 0) {playerLookingRight = true;}

        if (!(vxPlayer == 0 && vyPlayer == 0)) {
            if (playerTimer > 16) {
                switch (playerActiveSprite) {
                    case 1:
                        if (playerLookingRight) {
                            player = new Sprite("img/Player Squish.png");
                        } else { player = new Sprite("img/Player Squish L.png");}
                        playerActiveSprite = 2;
                        break;
                    case 2:
                        if (playerLookingRight) {
                            player = new Sprite("img/Player.png");
                        } else { player = new Sprite("img/Player L.png");}
                        playerActiveSprite = 1;
                        break;
                }
                playerTimer = 0;

            } else {
                playerTimer++;
            }
        } else if (!playerLookingRight) { player = new Sprite("img/Player L.png"); playerActiveSprite = 1; }
        else { player = new Sprite("img/Player.png"); playerActiveSprite = 1; }

        //Draw the player sprite
        for (int i = 0; i < player.getHeight(); i++) {
            for (int j = 0; j < player.getWidth(); j++) {
                if (!(player.getPixels()[i * player.getWidth() + j] == 16777215)) {
                    pixels[(yPlayer + i) * width + xPlayer + j] = player.getPixels()[i * player.getWidth() + j];
                }
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
