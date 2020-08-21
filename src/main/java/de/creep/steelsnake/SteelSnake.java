// SteelSnake: File was created on 19.08.2020 by Creep (Discord: Creep#4924)

package de.creep.steelsnake;

import com.tulskiy.keymaster.common.Provider;
import de.creep.steelsnake.hotkeys.DownListener;
import de.creep.steelsnake.hotkeys.LeftListener;
import de.creep.steelsnake.hotkeys.RightListener;
import de.creep.steelsnake.hotkeys.UpListener;
import de.creep.steelsnake.utils.Direction;
import de.creep.steelsnake.utils.KeyBoard;
import de.creep.steelsnake.utils.Position;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SteelSnake {

    private final static Provider provider = Provider.getCurrentProvider(false);
    private KeyBoard keyboard;

    private Position apple;
    private final ArrayList<Position> snake = new ArrayList<>();
    private Direction snakeDirection;
    private int score = 0;
    private Position tail = null;
    private boolean paused = false;

    private static SteelSnake instance;

    public static void main(String[] args) {
        instance = new SteelSnake();
        instance.onStart(args);
    }

    public void onStart(String[] args) {
        keyboard = new KeyBoard();

        registerKeyStrokes();
        setupSnake();
        spawnApple();
        setupScheduler();
    }

    public void setSnakeDirection(Direction direction) {
        snakeDirection = direction;
    }

    private void setupSnake() {
        snake.clear();
        snake.add(new Position(6, 2));
        snake.add(new Position(5, 2));
        snake.add(new Position(4, 2));
        snakeDirection = Direction.RIGHT;
    }

    private void setupScheduler() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(counter(), 0, 400);
    }

    private void registerKeyStrokes() {
        provider.register(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new UpListener());
        provider.register(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new DownListener());
        provider.register(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), new LeftListener());
        provider.register(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), new RightListener());
    }

    private void moveSnake() {
        Position head = snake.get(0);
        int predictedX = head.getX() + snakeDirection.getX();
        int predictedY = head.getY() + snakeDirection.getY();
        if (predictedX <= -1) {
            predictedX = KeyBoard.width -1;
        }

        if (predictedX >= KeyBoard.width) {
            predictedX = 0;
        }

        if (predictedY <= -1) {
            predictedY = KeyBoard.height -1;
        }

        if (predictedY >= KeyBoard.height) {
            predictedY = 0;
        }

        Position newHead = new Position(predictedX, predictedY);
        if (snake.contains(newHead)) {
            setupSnake();
            triggerFail();
            return;
        }

        appleCheck(newHead);
        snake.add(0, newHead);
        System.out.println(predictedX  + " : " + predictedY);
    }

    private void appleCheck(Position position) {
        if (!apple.equals(position)) {
            tail = snake.get(snake.size() -1);
            snake.remove(snake.size() -1);
        } else {
            tail = null;
            score += 1;
            spawnApple();
        }
    }

    private TimerTask counter() {
        return new TimerTask() {
            @Override
            public void run() {
                if (!paused) {
                    moveSnake();
                    keyboard.printConsoleField();
                    keyboard.printField();
                }
            }
        };
    }

    private void spawnApple() {
        Random random = new Random();
        Position currentPos;
        do {
            currentPos = new Position(random.nextInt(10), random.nextInt(4));
        } while (snake.contains(currentPos));
        apple = currentPos;
    }

    private void triggerFail() {
        System.out.println();
        System.out.println("FAIL!");
        System.out.println();
        keyboard.triggerFail();
    }

    public Position getApple() {
        return apple;
    }

    public void setApple(Position apple) {
        this.apple = apple;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Position getTail() {
        return tail;
    }

    public void setTail(Position tail) {
        this.tail = tail;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public ArrayList<Position> getSnake() {
        return snake;
    }

    public static SteelSnake getInstance() {
        return instance;
    }

    public KeyBoard getKeyboard() {
        return keyboard;
    }
}
