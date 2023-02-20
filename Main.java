import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

public class Main extends JPanel implements ActionListener {
    static final int TILE_WIDTH = 64;
    static final int TILE_HEIGHT = 64;
    static final BigInteger TILE_BLOCKED = new BigInteger("-1");
    static final BigInteger TILE_FREE = new BigInteger("0");
    static final BigInteger TILE_INIT = new BigInteger("1");
    static final Color COLOR_WHITE = new Color(255, 255, 255);
    static final Color COLOR_BLACK = new Color(0, 0, 0);
    static final Color COLOR_RED = new Color(255, 0, 0);
    static final int FONT_SIZE = 30;

    int toolbarHeight, offsetX, offsetY, miceX, miceY;
    int currentX, currentY, startX, startY, viewportW, viewportH;
    int viewportX = 1;
    int viewportY = 1;

    boolean showGrid = true;
    boolean isScrollBlocked = false;

    BigInteger[][] map = new BigInteger[0][0];

    Image img = new ImageIcon("gfx/test.png").getImage();
    Image img2 = new ImageIcon("gfx/test2.png").getImage();
    
    Font font = new Font("TimesRoman", Font.PLAIN, FONT_SIZE - 12);
    
    Timer timer = new Timer(20, this);
    JFrame frame;

    {
        MouseAdapter mAdapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 3) {
                    int mX = (e.getX() - offsetX) / TILE_WIDTH;
                    int mY = (e.getY() - offsetY) / TILE_HEIGHT;
                    map[mX + viewportX][mY + viewportY] = map[mX + viewportX][mY + viewportY] != TILE_BLOCKED
                            ? TILE_BLOCKED
                            : TILE_FREE;
                    if (startX != 0 || startY != 0) {
                        refreshMap(startX, startY);
                        System.out.printf("Количество возможных маршрутов до правой нижней клетки: %s\n",
                                map[map.length - 2][map[0].length - 2].toString());
                    }
                } else if (e.getButton() == 1)
                    if (map[currentX][currentY] != TILE_BLOCKED) {
                        startX = currentX;
                        startY = currentY;
                        refreshMap(startX, startY);
                        System.out.printf("Количество возможных маршрутов до правой нижней клетки: %s\n",
                                map[map.length - 2][map[0].length - 2].toString());
                    }
            }

            public void mouseMoved(MouseEvent e) {
                miceX = e.getX();
                miceY = e.getY();
                currentX = (miceX - offsetX) / TILE_WIDTH + viewportX;
                if (currentX > map.length - 2) currentX = map.length - 2;
                currentY = (miceY - offsetY) / TILE_HEIGHT + viewportY;
                if (currentY > map[0].length - 2) currentY = map[0].length - 2;
            }
        };
        addMouseListener(mAdapter);
        addMouseMotionListener(mAdapter);
    };

    /**
     * Конструктор. Создание нового объекта с переданными значениями
     * @param frame Объект JFrame
     * @param width Ширина карты в клетках
     * @param height Высота карты в клетках
     * @param randomize Заполнять карту случайными препятствиями
     * @param toolbarHeight Высота панели интерфейса
     */
    public Main(JFrame frame, int width, int height, boolean randomize, int toolbarHeight) {
        this.frame = frame;
        this.map = new BigInteger[width][height];
        this.miceX = frame.getWidth() / 2;
        this.miceY = frame.getHeight() / 2;
        
        if ((map.length - 2) * TILE_WIDTH < frame.getWidth()) {
            this.viewportW = map.length - 3;
            isScrollBlocked = true;
        } else {
            this.viewportW = Math.round(frame.getWidth() / TILE_WIDTH);
        }
       
        if ((map[0].length - 2) * TILE_HEIGHT < frame.getHeight()) {
            this.viewportH = map[0].length - 3;
            isScrollBlocked = true;
        } else {
            this.viewportH = Math.round(frame.getHeight() / TILE_HEIGHT);
        }
        
        this.toolbarHeight = toolbarHeight;
        
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                 if (e.getKeyCode() == 27) {
                    System.exit(0);
                 }
            }
        });
        
        initMap(randomize);
        this.timer.start();
    }

    /**
     * Метод полностью заполняет карту нулевыми значениями
     */
    private void zeroMap() {
        for (int i = 0; i < map[0].length; i++) {
            for (int j = 0; j < map.length; j++) {
                map[j][i] = TILE_FREE;
            }
        }
    }

    /**
     * Метод заполняет карту нулевыми значениями, кроме заблокированных клеток
     */
    private void zeroMapPath() {
        for (int i = 0; i < map[0].length; i++) {
            for (int j = 0; j < map.length; j++) {
                if (map[j][i] != TILE_BLOCKED)
                    map[j][i] = TILE_FREE;
            }
        }
    }

    /**
     * Метод случайным образом добавляет на карту заблокированные клетки
     */
    private void randMap() {
        for (int i = 1; i < map[0].length - 1; i++) {
            for (int j = 1; j < map.length - 1; j++) {
                map[j][i] = ThreadLocalRandom.current().nextInt(-1, 2) == -1 ? TILE_BLOCKED : TILE_FREE;
            }
        }
    }

    /**
     * Метод инициализирует карту
     * @param randomize Добавить на карту случайные препятствия
     */
    private void initMap(boolean randomize) {
        zeroMap();
        if (randomize) randMap();
    }

    /**
     * Метод обновляет карту и рассчитывает маршруты
     * @param x Координата клетки X, с которой начинается расчет маршрута
     * @param y Координата клетки Y, с которой начинается расчет маршрута
     */
    private void refreshMap(int x, int y) {
        BigInteger tempX;
        BigInteger tempY;

        zeroMapPath();

        map[x][y] = TILE_INIT;
        for (int i = y; i < map[0].length - 1; i++) {
            for (int j = x; j < map.length - 1; j++) {
                if (i > y || j > x) {
                    if (map[j][i] != TILE_BLOCKED) {
                        tempX = map[j - 1][i] == TILE_BLOCKED ? TILE_FREE : map[j - 1][i];
                        tempY = map[j][i - 1] == TILE_BLOCKED ? TILE_FREE : map[j][i - 1];
                        map[j][i] = tempX.add(tempY);
                    }
                }
            }
        }
    }

    public void paint(Graphics g) {
        if (!isScrollBlocked) {
            // Проверка положения курсора по оси X (для плавного скроллинга)
            if (miceX < 100) {
                if (viewportX > 1)
                    offsetX += 5;
            } else if (miceX > frame.getWidth() - 100) {
                if (viewportX + viewportW < map.length - 1)
                    offsetX -= 5;
            }
            // Проверка положения курсора по оси Y (для плавного скроллинга)
            if (miceY < 100) {
                if (viewportY > 1)
                    offsetY += 5;
            } else if (miceY > frame.getHeight() - 100) {
                if (viewportY + viewportH < map[0].length - 1)
                    offsetY -= 5;
            }
            // Проверка смещения карты по оси X (для плавного скроллинга)
            if (offsetX <= -TILE_WIDTH) {
                offsetX = 0;
                if (viewportX + viewportW < map.length - 1) {
                    viewportX++;
                }
            } else if (offsetX >= TILE_WIDTH) {
                offsetX = 0;
                if (viewportX > 1)
                    viewportX--;
            }
            // Проверка смещения карты по оси Y (для плавного скроллинга)
            if (offsetY <= -TILE_HEIGHT) {
                if (viewportY + viewportH < map.length - 1)
                    offsetY = 0;
                viewportY++;
            } else if (offsetY >= TILE_HEIGHT) {
                offsetY = 0;
                if (viewportY > 1)
                    viewportY--;
            }
        }

        g.setFont(font);
        g.setColor(COLOR_WHITE);

        // Отрисовка карты
        for (int i = -1; i < viewportW + 1; i++) {
            for (int j = -1; j < viewportH + 1; j++) {
                if (map[i + viewportX][j + viewportY] == TILE_BLOCKED) {
                    g.drawImage(img2, i * TILE_WIDTH + offsetX, j * TILE_HEIGHT + offsetY, TILE_WIDTH, TILE_HEIGHT,
                            null);
                } else {
                    g.drawImage(img, i * TILE_WIDTH + offsetX, j * TILE_HEIGHT + offsetY, TILE_WIDTH, TILE_HEIGHT,
                            null);
                }
                // Отрисовка сетки
                if (showGrid) {
                    g.drawRect(i * TILE_WIDTH + offsetX, j * TILE_HEIGHT + offsetY, TILE_WIDTH, TILE_HEIGHT);
                    g.drawString(map[i + viewportX][j + viewportY].toString(), i * TILE_WIDTH + 20 + offsetX,
                            j * TILE_HEIGHT + 30 + offsetY);
                }
                // Отрисовка клетки старта
                if (i + viewportX == startX && j + viewportY == startY) {
                    g.setColor(COLOR_RED);
                    g.drawRect(i * TILE_WIDTH + offsetX + 1, j * TILE_HEIGHT + offsetY + 1, TILE_WIDTH - 2, TILE_HEIGHT - 2);
                    g.setColor(COLOR_WHITE);
                }
            }
        }
        
        // Отрисовка интерфейса
        g.fillRect(0, frame.getHeight() - toolbarHeight, frame.getWidth(), toolbarHeight);
        g.setColor(COLOR_BLACK);
        g.drawString("X:" + Integer.toString(currentX), 10, frame.getHeight() - toolbarHeight + FONT_SIZE);
        g.drawString("Y:" + Integer.toString(currentY), 80, frame.getHeight() - toolbarHeight + FONT_SIZE);
        g.drawString(map[currentX][currentY].toString(), frame.getWidth() / 2,
                frame.getHeight() - toolbarHeight + FONT_SIZE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

}
