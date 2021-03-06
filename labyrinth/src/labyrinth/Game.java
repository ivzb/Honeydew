package labyrinth;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import labyrinth.gfx.Render;
import labyrinth.input.Input;
import labyrinth.level.LoadLevel;
import labyrinth.player.Player;
import labyrinth.status.Status;

public class Game extends Canvas implements Runnable {

    private static final long serialVersionUID = 1L;

    private final int WIDTH = 512 * 2;
    private final int HEIGHT = 512;
    private final int SCALE = 1;
    private final int TIME = 50;
    private final Dimension SIZE = new Dimension((WIDTH * SCALE), (HEIGHT * SCALE));
    public final String TITLE = "Maze Game Demo | Res: " + (WIDTH * SCALE) + "x" + (HEIGHT * SCALE) + " | ";

    private JFrame frame;
    private Thread thread;
    private boolean looping;

    private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

    private Render render;
    private Input input;
    private LoadLevel loadLevel;
    private Player player;
    private Status status;

    private int time = TIME;

    public Game() {
        setPreferredSize(SIZE);
        setMinimumSize(SIZE);
        setMaximumSize(SIZE);

        frame = new JFrame();
        render = new Render(WIDTH, HEIGHT);
        input = new Input();
        addKeyListener(input);
        loadLevel = new LoadLevel("/map/level.png");
        player = new Player(loadLevel, (1 * 32), (14 * 32), input);
        status = new Status("ingame");
    }

    public void start() {
        looping = true;
        thread = new Thread(this, "GameLoop");
        thread.start();
    }

    public void stop() {
        looping = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        long lastTime = System.nanoTime();
        final double ns = 1000000000D / 60D;
        double delta = 0D;

        long timer = System.currentTimeMillis();
        int frames = 0;
        int updates = 0;

        requestFocus();
        while (looping) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1D) {
                update();
                updates++;
                delta--;
            }

            render();
            frames++;

            if (System.currentTimeMillis() - timer >= 1000) {
                timer += 1000;
                frame.setTitle(TITLE + updates + "ups, " + frames + "fps | ");
                frames = 0;
                updates = 0;
            }
        }
        stop();
    }

    long now = System.currentTimeMillis();

    private void update() {
        if (time > 0 && Player.won == 0) {
            if (System.currentTimeMillis() - now >= 1000) {
                time--;
                now += 1000;
            }
        }
        
        String currentStatus = status.getStatus();

        if (time == 0) {
            Player.won = 1;
        }

        if (Player.won == 0) {
            input.update();
            player.update();
        } else {
            input.update();
            player.update2();
            if (input.R) {
                time = TIME;
            }
            now = System.currentTimeMillis();
        }
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        loadLevel.render(render);
        player.render(render);

        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = render.pixels[i];
        }

        Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(255, 255, 255));
        g.drawString("Time: " + time, 10, 20);
        //if(gameStatus == "escape") {
        	 //Graphics2D g2d = (Graphics2D) g;
             /* g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
             g2d.setFont(new Font("SansSerif", Font.BOLD, 48));
             g2d.setColor(new Color(0, 255, 255));
             g2d.drawString("escape!", WIDTH * SCALE / 4, HEIGHT * SCALE / 2);
             g2d.setFont(new Font("SansSerif", Font.BOLD, 22));*/
        	 //g2d.drawRect (150, 150, 200, 200);    //can use either of the two//
        	// g2d.fillRect (150, 150, 200, 200);
        	 //g2d.setColor(new Color(1, 0, 0));
             //g2d.drawString("Press 'R' to reset!", WIDTH * SCALE / 4, HEIGHT * SCALE / 3);
        //}
        if (Player.won == 1) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 48));
            g2d.setColor(new Color(0, 255, 255));
            g2d.drawString("YOU LOSE!", WIDTH * SCALE / 4, HEIGHT * SCALE / 2);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
            g2d.drawString("Press 'R' to reset!", WIDTH * SCALE / 4, HEIGHT * SCALE / 3);
        }
        if (Player.won == 2) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 48));
            g2d.setColor(new Color(255, 255, 0));
            g2d.drawString("YOU WON!", WIDTH * SCALE / 4, HEIGHT * SCALE / 2);
        }
        g.dispose();
        bs.show();
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.frame.setTitle(game.TITLE);
        game.frame.setResizable(false);
        game.frame.add(game);
        game.frame.pack();
        game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.frame.setLocationRelativeTo(null);
        game.frame.setVisible(true);
        game.start();
    }
}
