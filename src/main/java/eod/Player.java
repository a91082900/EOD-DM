package eod;

import eod.IO.Input;
import eod.IO.Output;
import eod.card.abstraction.Card;
import eod.card.collection.Deck;
import eod.card.collection.Hand;
import eod.event.*;
import eod.event.Event;
import eod.event.listener.EventListener;
import eod.exceptions.GameLosingException;
import eod.param.PointParam;
import eod.snapshots.Snapshotted;
import eod.warObject.Damageable;
import eod.warObject.WarObject;
import eod.warObject.character.abstraction.Character;
import eod.warObject.leader.Leader;

import java.awt.*;
import java.util.*;

public class Player implements Snapshotted<Player.Snapshot>,
                                GameObject, EventListener {

    private Deck deck;
    private Game game;
    private Hand hand;
    private Leader leader;
    private Input input;
    private Output output;
    private String name;
    private boolean isPlayerA;

    public Player(Deck deck, String name) {
        this(deck, new Hand(), name);
    }

    public Player(Deck deck, Hand hand, String name) {
        this.deck = deck;
        this.hand = hand;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
    }

    public void attachToGame(Game game) {
        this.game = game;
        game.registerListener(this);
    }
    public void attachIO(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    public void setPlayerA(boolean is) {
        isPlayerA = is;
    }

    public boolean isPlayerA() {
        return isPlayerA;
    }

    public void handReceive(ArrayList<Card> h) {
        hand.receive(h);
        output.sendReceivedCards(this, h.toArray(new Card[0]));
    }

    public void drawFromDeck(int count) {
        output.sendDrawingCards(this);

        Card[] cards = deck.draw(count);
        handReceive(new ArrayList<>(Arrays.asList(cards)));

        output.sendReceivedCards(this, cards);
    }

    public boolean checkInHand(Class<? extends Card> c) {
        return hand.containsType(c);
    }

    public void dropCards() {
        hand.dropAllCards();
    }

    public void dropCards(int k) throws IllegalArgumentException {
        hand.randomlyDropCards(k);
    }

    public void startAutoAttackInOrder() {
        Gameboard board = game.getBoard();
        ArrayList<Character> characters;
        if(game.isPlayerA(this)) {
            characters = collectCharacterFromAView(board);
        } else {
            characters = collectCharacterFromBView(board);
        }
        //TODO: start auto attack
        output.sendRoundStartEffectActivate();
    }

    private ArrayList<Character> collectCharacterFromAView(Gameboard board) {
        ArrayList<Character> characters = new ArrayList<>();
        for(int y = Gameboard.boardSize-1; y >= 0; y--) {
            for(int x = 0; x < Gameboard.boardSize; x++) {
                Character character = board.getObjectOn(x, y);
                if(character.getPlayer().equals(this)) {
                    characters.add(character);
                }
            }
        }
        return characters;
    }

    private ArrayList<Character> collectCharacterFromBView(Gameboard board) {
        ArrayList<Character> characters = new ArrayList<>();
        for(int y = 0; y < Gameboard.boardSize; y++) {
            for(int x = Gameboard.boardSize-1; x >= 0; x--) {
                Character character = board.getObjectOn(x, y);
                if(character.getPlayer().equals(this)) {
                    characters.add(character);
                }
            }
        }
        return characters;
    }

    public void announceWon() {
        output.sendWinning(this);
    }
    
    public void announceLost() {
        output.sendLosing(this);
    }

    //TODO: implement validateDeck, just by checking the number of cards and other things.
    public boolean validateDeck() {
        return true;
    }

    public Leader getLeader() {
        return leader;
    }

    public Gameboard getBoard() {
        return game.getBoard();
    }

    public boolean isLeaderAlive() {
        return leader.isAlive();
    }

    public boolean inBase(Point p) {
        return game.getBoard().inBase(this, p);
    }

    public void sendPlayerOrder(boolean isFirst) {
        output.sendPlayerOrder(this, isFirst);
    }

    public ArrayList<Point> getBaseEmpty() {
        Gameboard gameboard = game.getBoard();
        int boardEdge = Gameboard.boardSize-1;
        PointParam param = new PointParam();
        param.emptySpace = true;
        if(game.isPlayerA(this)) {
            return gameboard.allSpaces(new Point(0,0), param);
        }
        else {
            return gameboard.allSpaces(new Point(boardEdge, boardEdge), param);
        }
    }

    public ArrayList<Point> getBase() {
        Gameboard gameboard = game.getBoard();
        int boardEdge = Gameboard.boardSize - 1;
        if(game.isPlayerA(this)) {
            return gameboard.allSpaces(new Point(0,0), new PointParam());
        } else {
            return gameboard.allSpaces(new Point(boardEdge, boardEdge), new PointParam());
        }
    }

    public ArrayList<Point> getConflictEmpty() {
        PointParam param = new PointParam();
        param.emptySpace = true;
        return game.getBoard().allSpaces(new Point(Gameboard.firstLine, Gameboard.firstLine), param);
    }

    public void summonObject(WarObject object, Point point) {
        getBoard().summonObject(object, point);
        output.sendWarObjectSummoned(object, point);
    }

    @Override
    public void teardown() {
        hand.teardown();
        hand = null;
        deck.teardown();
        deck = null;
        leader.teardown();
        leader = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Player player = (Player) o;
        return Objects.equals(deck, player.deck) &&
                Objects.equals(hand, player.hand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deck, hand);
    }

    @Override
    public Snapshot takeSnapshot() {
        return new Snapshot();
    }

    public Player rival() {
        return game.getRivalPlayer(this);
    }

    public WarObject selectObject(WarObject[] objects) {
        return input.waitForChooseWarObjectFrom(objects);
    }

    public WarObject[] selectMultipleObject(WarObject[] objects, int number) {
        return input.waitForChooseWarObjectFrom(objects, number);
    }

    public Point selectPosition(ArrayList<Point> points) {
        return input.waitForChoosePointFrom(points.toArray(new Point[0]));
    }

    public Card selectCard(Card[] cards) {
        return input.waitForChooseCardFrom(cards);
    }

    public void moveObject(WarObject object, Point point) {
        game.getBoard().moveObject(object.position, point);
        game.sendEvent(this, new ObjectMovingEvent(object, point));
        object.updatePosition(point);
    }

    public void loseObject(WarObject object) {
        getBoard().removeObject(object);
        if(object instanceof Damageable) {
            game.sendEvent(this, new ObjectDeadEvent((Damageable) object));
        }
    }

    public void loseLeader() throws GameLosingException {
        throw new GameLosingException("Player "+this+" loses.");
    }

    public ArrayList<Point> getFL(Point p, PointParam param) {
        int dx, dy;
        if(isPlayerA()) {
            dx = 1;
            dy = 1;
        } else {
            dx = -1;
            dy = -1;
        }
        return getBoard().getLine(p, dx, dy, param);
    }

    public ArrayList<Point> getFR(Point p, PointParam param) {
        int dx, dy;
        if(isPlayerA()) {
            dx = 1;
            dy = -1;
        } else {
            dx = -1;
            dy = 1;
        }
        return getBoard().getLine(p, dx, dy, param);
    }

    public ArrayList<Point> getFront(Point p, PointParam param) {
        int dx;
        if(isPlayerA()) {
            dx = 1;
        } else {
            dx = -1;
        }
        return getBoard().getLine(p, dx, 0, param);
    }

    @Override
    public ArrayList<Class<? extends Event>> supportedEventTypes() {
        //TODO: add RegionalAttackEvent support
        return new ArrayList<Class<? extends Event>>(){{
            add(RoundStartEvent.class);
            add(RoundEndEvent.class);
            add(DirectAttackEvent.class);
            add(ObjectDeadEvent.class);
            add(ObjectMovingEvent.class);
        }};
    }

    @Override
    public void onEventOccurred(GameObject sender, Event event) {
        if(event instanceof RoundStartEvent) {
            output.sendRoundStarted(((RoundStartEvent) event).getStartedRound());
            return;
        }
        if(event instanceof RoundEndEvent) {
            output.sendRoundEnded(((RoundEndEvent) event).getEndedRound());
            return;
        }
        if(event instanceof DirectAttackEvent) {
            DirectAttackEvent directAttack = (DirectAttackEvent) event;
            Character attacker = directAttack.getAttacker();
            for(Character victim: directAttack.getTargets()) {
                output.sendCharacterAttacked(attacker, victim);
            }
            return;
        }
        if(event instanceof ObjectDeadEvent) {
            ObjectDeadEvent objectDeadEvent = (ObjectDeadEvent) event;
            WarObject deadObject = (WarObject) objectDeadEvent.getDeadObject();
            output.sendWarObjectDied(deadObject);
            return;
        }
        if(event instanceof ObjectMovingEvent) {
            ObjectMovingEvent movingEvent = (ObjectMovingEvent) event;
            output.sendWarObjectMoved(movingEvent.getObject(), movingEvent.getNewPos());
        }
    }

    public class Snapshot implements eod.snapshots.Snapshot {
        private Deck.Snapshot deckSnapshot = deck.takeSnapshot();
        private Hand.Snapshot handSnapshot = hand.takeSnapshot();

        public Deck.Snapshot getDeck() {
            return deckSnapshot;
        }

        public Hand.Snapshot getHand() {
            return handSnapshot;
        }
    }
}
