package eod;

import eod.exceptions.MoveInvalidException;
import eod.snapshots.Snapshotted;
import eod.warObject.WarObject;
import eod.warObject.character.abstraction.Character;

import java.awt.*;
import java.util.ArrayList;

public class Gameboard implements Snapshotted<Gameboard.Snapshot>, GameObject {
    // Player A's base is (0,y) ~ (firstLine-1, y)
    // Player A's front = (1,0)
    public static final int boardSize = 8;
    public static final int firstLine = 2, secondLine = 6;
    private Game game;
    private WarObject[][] board = new Character[boardSize][boardSize];

    public Gameboard(Game game) {
        this.game = game;
    }

    public <T extends WarObject>T getObjectOn(int x, int y) throws IllegalArgumentException {
        if(!hasObjectOn(x, y)) {
            throw new IllegalArgumentException("There's no object on ("+x+", "+y+").");
        }
        try {
            return (T) board[x][y];
        } catch (Exception e) {
            throw new IllegalArgumentException("the object on ("+x+", "+y+") doesn't match the type you want.");
        }
    }

    public static boolean inBounds(Point p) {
        return inBounds(p.x, p.y);
    }

    public static boolean inBounds(int x, int y) {
        return inBounds(x) && inBounds(y);
    }

    public static boolean inBounds(int n) {
        return n >= 0 && n < boardSize;
    }

    public boolean hasObjectOn(int x, int y) throws ArrayIndexOutOfBoundsException {
        if(!inBounds(x, y)) {
            throw new ArrayIndexOutOfBoundsException("Trying to find an element at ("+x+", "+y+") on the board.");
        }
        return board[x][y]!=null;
    }

    public void moveObject(Point from, Point to) throws MoveInvalidException, ArrayIndexOutOfBoundsException {
        if(!inBounds(to)) {
            throw new ArrayIndexOutOfBoundsException("Trying to move a character to ("+to.x+", "+to.y+").");
        }

        if(board[to.x][to.y] != null) {
            throw new MoveInvalidException("There's already a character on ("+to.x+", "+to.y+").");
        }
        board[to.x][to.y] = board[from.x][from.y];
        board[from.x][from.y] = null;
    }

    public void removeObject(WarObject object) throws IllegalArgumentException {
        Point position = object.getPosition();
        int x = position.x;
        int y = position.y;
        removeObject(x, y);
    }

    public void removeObject(int x, int y) throws IllegalArgumentException {
        if(!inBounds(x, y)) {
            throw new IllegalArgumentException("Trying to remove a character at ("+x+", "+y+").");
        }
        if(board[x][y] == null) {
            throw new IllegalArgumentException("There's nothing at ("+x+", "+y+") on the board, cannot remove.");
        }
        board[x][y] = null;
    }

    public ArrayList<Point> allEmptySpaces() {
        ArrayList<Point> points = new ArrayList<>();
        for(int i = 0;i < boardSize; i++) {
            for(int j = 0;j < boardSize;j++) {
                if(!hasObjectOn(i, j)) {
                    points.add(new Point(i, j));
                }
            }
        }
        return points;
    }

    public ArrayList<Point> allEmptySpaces(Point at) {
        int iMin, iMax;
        if(at.x < firstLine) {
            iMin = 0;
            iMax = firstLine;
        } else if(at.x < secondLine) {
            iMin = firstLine;
            iMax = secondLine;
        } else {
            iMin = secondLine;
            iMax = boardSize;
        }

        ArrayList<Point> points = new ArrayList<>();
        for(int i = iMin;i < iMax;i++) {
            for(int j =0;j < boardSize;j++) {
                if(!hasObjectOn(i, j)) {
                    points.add(new Point(i, j));
                }
            }
        }
        return points;
    }

    public ArrayList<Point> allSpaces(Point at) {
        int iMin, iMax;
        if(at.x < firstLine) {
            iMin = 0;
            iMax = firstLine;
        } else if(at.x < secondLine) {
            iMin = firstLine;
            iMax = secondLine;
        } else {
            iMin = secondLine;
            iMax = boardSize;
        }

        ArrayList<Point> points = new ArrayList<>();
        for(int i = iMin;i < iMax;i++) {
            for(int j =0;j < boardSize;j++) {
                points.add(new Point(i, j));
            }
        }
        return points;
    }

    public ArrayList<Point> getSurroundingEmpty(Point p, int range) {
        ArrayList<Point> points = new ArrayList<>();
        for(int x = p.x - range;x <= p.x + range;x++) {
            if(!inBounds(x)) {
                continue;
            }
            for(int y = p.y-range; y <= p.y+range;y++) {
                if(!inBounds(y)) {
                    continue;
                }
                if(!hasObjectOn(x, y)) {
                    points.add(new Point(x, y));
                }
            }
        }
        return points;
    }

    public ArrayList<Point> getSurrounding(Point p, int range) {
        // the return list DOESN'T contain p
        ArrayList<Point> points = new ArrayList<>();
        for(int x = p.x - range;x <= p.x + range;x++) {
            if(!inBounds(x)) {
                continue;
            }
            for(int y = p.y - range; y <= p.y + range; y++) {
                if(!inBounds(y) || (x == p.x && y == p.y)) {
                    continue;
                }
                points.add(new Point(x, y));
            }
        }
        return points;
    }

    public ArrayList<Point> get4Ways(Point p, int range) {
        // the return list DOESN'T contain p.
        ArrayList<Point> points = new ArrayList<>();
        int x = p.x - range, y = p.y;
        while(x <= p.x + range) {
            if(x < 0 || x == p.x) {
                continue;
            }
            if(x >= boardSize) {
                break;
            }
            points.add(new Point(x, y));
            x++;
        }
        x = p.x;
        y = p.y - range;
        while(y <= p.y + range) {
            if(y < 0 || y == p.y) {
                continue;
            }
            if(y >= boardSize) {
                break;
            }
            points.add(new Point(x, y));
            y++;
        }
        return points;
    }

    public ArrayList<Point> get8ways(Point p, int range) {
        // the return list DOESN'T contain p
        ArrayList<Point> points = get4Ways(p, range);
        int x = p.x - range, y = p.y - range, y2 = p.y + range;
        while(x <= p.x + range) {
            if(x <0 || x == p.x) {
                continue;
            }
            if(x >= boardSize) {
                break;
            }
            if(inBounds(y)) {
                points.add(new Point(x, y));
            }
            if(inBounds(y2)) {
                points.add(new Point(x, y2));
            }
            x++;
            y++;
            y2--;
        }
        return points;
    }

    public void summonObject(WarObject object, Point point) throws IllegalArgumentException {
        int x = point.x, y = point.y;
        if(hasObjectOn(x, y)) {
            throw new IllegalArgumentException("There's already an object on ("+x+", "+y+").");
        }
        board[x][y] = object;
        object.updatePosition(point);
    }

    @Override
    public void teardown() {
        game = null;

        for(GameObject[] line:board) {
            for(GameObject object:line) {
                object.teardown();
            }
        }
        board = null;
    }

    @Override
    public Snapshot takeSnapshot() {
        return new Snapshot();
    }

    public boolean inBase(Player sender, Point p) {
        if(game.isPlayerA(sender)) {
            return p.x < firstLine;
        } else {
            return p.x >= secondLine;
        }
    }

    public ArrayList<Point> getLine(Point p, int dx, int dy, int num) {
        ArrayList<Point> points = new ArrayList<>();
        int x = p.x + dx, y = p.y + dy;

        for(int i = 0;i < num; i++) {
            if(!inBounds(x, y)) {
                break;
            }
            points.add(new Point(x, y));
        }
        return points;
    }

    public class Snapshot implements eod.snapshots.Snapshot {
        private WarObject[][] allObjects = board;

        public WarObject[][] getAllObjects() {
            return allObjects;
        }
    }
}
