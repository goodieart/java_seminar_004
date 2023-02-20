import java.util.LinkedList;
import java.util.Queue;
import java.awt.Point;

public class LeeAlgorithm {
    private int[][] grid;
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private int rows;
    private int cols;
    private int[][] distances;
    private boolean[][] visited;
    

    public LeeAlgorithm(int[][] grid, int startX, int startY, int endX, int endY) {
        this.grid = grid;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.distances = new int[rows][cols];
        this.visited = new boolean[rows][cols];
    }

    public int[][] runLeeAlgorithm() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                distances[i][j] = -1;
            }
        }
        distances[startX][startY] = 0;

        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startX, startY});

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int x = curr[0];
            int y = curr[1];

            visited[x][y] = true;

            if (x == endX && y == endY) {
                break;
            }

            checkNeighbor(queue, x + 1, y, distances[x][y]);
            checkNeighbor(queue, x - 1, y, distances[x][y]);
            checkNeighbor(queue, x, y + 1, distances[x][y]);
            checkNeighbor(queue, x, y - 1, distances[x][y]);
        }
        return distances;
    }

    private void checkNeighbor(Queue<int[]> queue, int x, int y, int distance) {
        if (x >= 0 && x < rows && y >= 0 && y < cols && !visited[x][y]) {
            if (grid[x][y] == 0) {
                distances[x][y] = distance + 1;
                queue.offer(new int[]{x, y});
            }
        }
    }

    public Point getShortestDistance() {
        return new Point(endX, endY);
    }

    public LinkedList<Point> buildPath() {
        LinkedList<Point> path = new LinkedList<>();
        int x = endX, y = endY;
    
        path.add(new Point(x, y));
        
        while (!(x == startX && y == startY)) {
            if (distances[x + 1][y] == distances[x][y] - 1) {
                x++;
                path.add(new Point(x, y));
            } else if (distances[x][y + 1] == distances[x][y] - 1) {
                y++;
                path.add(new Point(x, y));
            } else if (distances[x - 1][y] == distances[x][y] - 1) {
                x--;
                path.add(new Point(x, y));
            } else if (distances[x][y - 1] == distances[x][y] - 1) {
                y--;
                path.add(new Point(x, y));
            }
        }
        return path;
    }
}