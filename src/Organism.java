import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Organism extends Pixie {
    int OrgID;
    static int nextOrgID = 0;
    static int preyEaten = 0;
    static int preyDiedByOld = 0;
    static int preyDiedBySomething = 0;
    protected Queue<Point> movementQueue;
    protected Point currentTarget;
    protected Random random = new Random();
    protected int health;
    protected double hunger;
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
    protected int childCooldown = 850;
    boolean dead = false;
    protected int parent = -1;
    protected int child;
    protected int digesting = 0;

    public Organism (int startX, int startY, int sizeX, int sizeY, StateMachine brain) {
        super(startX, startY, sizeX, sizeY);
        this.OrgID = nextOrgID++;
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
        deathFrame = birthFrame+4500+random.nextInt(6000); //21600

        this.movementQueue = new LinkedList<>();
        this.currentTarget = null;
    }

    public Organism (int startX, int startY, int sizeX, int sizeY) {
        super(startX, startY, sizeX, sizeY);
        this.brain = new StateMachine();
        this.currentState = State.WANDERING;

        this.OrgID = nextOrgID++;
        this.exactX = startX;
        this.exactY = startY;
        this.health = 100;
        this.hunger = 100;
        this.isMoving = false;
        this.maxMove = 100;
        foodPreferance = FoodPreferance.FLORA;
        birthFrame = GameConstants.CurrentFrame;
        deathFrame = birthFrame+5000+random.nextInt(4000);

        this.movementQueue = new LinkedList<>();
        this.currentTarget = null;
    }

    public void updateLogic(List<Organism> worldEntities, List<Organism> spawnQueue) {
        tryForChild(spawnQueue);
        tryToDie();
//        decreaseHunger();
        currentState = brain.determineNextState(this, worldEntities);
        executeState(currentState);
        processMovement();
        if (childCooldown > 0) {
            childCooldown--;
        }
        if (currentState == State.FLEEING) {
            if (random.nextInt(4) == 0) {
                if (hunger > 0) {
                    hunger-=5;
                }
            }
        }
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

        if (currentTarget.x < 0 || currentTarget.y < 0 || currentTarget.x > GameConstants.AppWidth || currentTarget.y > GameConstants.AppHeight) {
            currentTarget = null;
            return;
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

            exactY += directionY*speed;
            exactX += directionX*speed;
        }
        this.x = (int) Math.round(exactX);
        this.y = (int) Math.round(exactY);
    }

    public void tryToDie() {
        if (GameConstants.CurrentFrame >= deathFrame) {
            if (this instanceof Predator) {
                Predator.predatorsDiedOfOld++;
            } else {
                preyDiedByOld++;
            }
            die();
        } else {
            if (GameConstants.CurrentFrame % 240 == 0 && random.nextInt(1000) < 8) {
                die();
                preyDiedBySomething++;
            }
        }
    }

    protected void decreaseHunger() {
        if (hunger > 0 && digesting == 0) {
            int randomNumber = random.nextInt(150);
            if (randomNumber == 0) {
                hunger-=1.5;
                if (hunger < 0) {
                    hunger = 0;
                    die();
                    Predator.predatorsStarved++;
                }
            }
        } else if (hunger <= 0) {
            die();
        }
    }

    private int getChildPossiblility(){
        int count = Stats.getPreyCount();
        if (count <= 0) {
            return 1;
        }
        return count;
    }

    public void tryForChild(List<Organism> spawnQueue) {
        if (hunger > 80) {
            if (random.nextInt(getChildPossiblility()) == 0 && childCooldown == 0) {
                childCooldown = 6500;
                Organism child = new Organism(x, y, 15, 15);
                child.parent = OrgID;
                this.child = child.OrgID;
                spawnQueue.add(child);
                hunger-=80;
            }
        }
    }

    public void eat() {
        if (random.nextInt(7) == 0 && digesting == 0) {
            hunger+=5.08;
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
        Predator predator = brain.getClosestPred();
        if (predator == null) return;

        // ONLY calculate a new escape route if we don't already have one.
        // This stops the memory leak of creating thousands of Points per second.
        if (currentTarget == null) {

            // 1. Calculate the vector pointing FROM the predator TO the prey
            // This naturally points away, so we don't need negative speeds
            double deltaX = this.exactX - predator.exactX;
            double deltaY = this.exactY - predator.exactY;
            double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

            if (distance > 0) {
                double directionX = deltaX / distance;
                double directionY = deltaY / distance;

                // 2. Project a target 50 pixels away in that safe direction
                // Make sure we use exactX for X, and exactY for Y!
                int runX = (int) Math.round(this.exactX + (directionX * 50));
                int runY = (int) Math.round(this.exactY + (directionY * 50));

                // 3. Clamp to the window bounds so they don't run off-screen
                runX = Math.max(10, Math.min(GameConstants.AppWidth - 10, runX));
                runY = Math.max(10, Math.min(GameConstants.AppHeight - 10, runY));

                // 4. Set the target directly, bypassing the queue
                movementQueue.clear();
                currentTarget = new Point(runX, runY);
            }
        }
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
