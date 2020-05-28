/**
 * This is a class
 * Created 2020-03-25
 * Edited 2020-04-03
 *
 * @author Magnus Silverdal
 * @developer Allan Bäckman
 */
public class Main {
    public static void main(String[] args) {
        Sprite maze = new Sprite("img/1Maze.png");
        Graphics graphics = new Graphics( maze.getWidth()*32,maze.getHeight()*32,1);
        graphics.start();
    }
}
