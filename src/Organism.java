import java.awt.*;
import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

public class Organism extends Pixie {
    int OrgID;
    static int nextOrgID = 0;
    protected Queue<Point> movementQueue;
    protected Point currentTarget;
    protected Random random = new Random();
    protected int health;
    protected int hunger;
    protected double exactX;
    protected double exactY;
    protected int maxMove;
    protected double speed = 1.55;
    protected FoodPreferance foodPreferance;
    protected boolean isMoving;
    protected StateMachine brain;
    protected State currentState;
    protected long deathFrame;
    protected long birthFrame;
    boolean dead = false;
    protected int parent = -1;
    protected int child;

    public Organism (int startX, int startY, int sizeX, int sizeY, StateMachine brain) {
        super(startX, startY, sizeX, sizeY);
        OrgID = nextOrgID;
        nextOrgID++;
        this.brain = brain;
        this.currentState = State.WANDERING;

        this.exactX = startX;
        this.exactY = startY;
        this.health = 100;
        this.hunger = 100;
        this.isMoving = false;
        this.maxMove = 100;
        foodPreferance = FoodPreferance.FLORA;
        birthFrame = GameConstants.CurrentFrame;
        deathFrame = birthFrame+10000+random.nextInt(5000); //21600

        this.movementQueue = new LinkedList<>();
        this.currentTarget = null;
    }

    public Organism (int startX, int startY, int sizeX, int sizeY) {
        super(startX, startY, sizeX, sizeY);
        this.brain = new StateMachine();
        this.currentState = State.WANDERING;

        this.exactX = startX;
        this.exactY = startY;
        this.health = 100;
        this.hunger = 100;
        this.isMoving = false;
        this.maxMove = 100;
        foodPreferance = FoodPreferance.FLORA;
        birthFrame = GameConstants.CurrentFrame;
        deathFrame = birthFrame+10000;

        this.movementQueue = new LinkedList<>();
        this.currentTarget = null;
    }

    public void updateLogic(List<Organism> worldEntities, List<Organism> spawnQueue) {
        tryForChild(spawnQueue);
        tryToDie();
        currentState = brain.determineNextState(this, worldEntities);
        executeState(currentState);
        processMovement();
    }

    public void die () {
        dead = true;
    }

    public boolean isDead() {
        return dead;
    }

    private void processMovement() {
        if (currentTarget == null && !movementQueue.isEmpty()) {
            currentTarget = movementQueue.poll();
        }

        if (currentTarget == null) {
            return;
        }

        if (!isMoving) {
            this.isMoving = true;
        }

        double deltaX = currentTarget.x - exactX;
        double deltaY = currentTarget.y - exactY;
        double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

        if (distance <= speed) {
            exactX = currentTarget.x;
            exactY = currentTarget.y;
            currentTarget = null;
            this.isMoving = false;

        } else {
            double directionX = deltaX / distance;
            double directionY = deltaY / distance;

//            IO.println("Current Pos: " + exactX + ", " + exactY + "\nDegX: " + directionX + " DegY: " + directionY + " Target Pos: " + currentTarget.x + ", " +currentTarget.y);
            exactY += directionY*speed;
            exactX += directionX*speed;
        }
        this.x = (int) Math.round(exactX);
        this.y = (int) Math.round(exactY);
    }

    public void tryToDie() {
        if (GameConstants.CurrentFrame >= deathFrame) {
            die();
        }
    }

    public void tryForChild(List<Organism> spawnQueue) {
        if (hunger > 80) {
            if (random.nextInt(20) == 0) {
                Organism child = new Organism(x, y, 15, 15);
                child.parent = OrgID;
                this.child = child.OrgID;
                spawnQueue.add(child);
                hunger-=40;
            }
        }
    }

    public void eat() {
        if (random.nextInt(5) == 0) {
            hunger+=10;
            if (hunger > 100) {
                hunger = 100;
            }
        }
    }

    public void queueWaypoint(int targetX, int targetY) {
        movementQueue.add(new Point(targetX, targetY));
    }

    public void move(int x, int y) {
        if (this.x+x <= 0 || this.x+x >= GameConstants.AppWidth || this.y+y <= 0 || this.y+y >= GameConstants.AppHeight) {return;}
        movementQueue.add(new Point(this.x+x, this.y+y));
    }

    public void moveRandomly() {
        if (!isMoving) {
            move(random.nextInt(maxMove)-maxMove/2, random.nextInt(maxMove)-maxMove/2);
        }
    }

    public void flee() {
        move(1, 0);
    }

    protected void executeState(State state) {
        switch (state) {
            case WANDERING:
                moveRandomly();
                break;
            case FLEEING:
                flee();
                break;
            case HUNGRY:
                eat();
                break;
            case IDLE:
            default:
                break;
        }

    }


}
