import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Autonomous prey agent with hunger, lifespan, reproduction, and a fleeing AI.
 * <p>
 * Each tick {@link #updateLogic(List, List)} asks {@link #brain} for a
 * {@link State}, then wanders, flees, or eats. Offspring are staged into a
 * spawn queue; death is flagged via {@link #dead} so the world can remove the
 * organism after the frame. {@link Predator} subclasses this type and replaces
 * hunting and movement behavior.
 * </p>
 */
public class Organism extends Pixie {
    /** Unique identifier assigned at construction from {@link #nextOrgID}. */
    int OrgID;
    /** Next identifier to assign to a newly constructed organism. */
    static int nextOrgID = 0;
    /** Lifetime count of prey killed by predators. */
    static int preyEaten = 0;
    /** Lifetime count of prey that reached their {@link #deathFrame}. */
    static int preyDiedByOld = 0;
    /** Lifetime count of prey that died from the random hazard check. */
    static int preyDiedBySomething = 0;
    /** FIFO of pixel waypoints the organism still intends to visit. */
    protected Queue<Point> movementQueue;
    /** Waypoint currently being walked toward, or {@code null} if idle. */
    protected Point currentTarget;
    /** Per-instance RNG used for movement, death, eating, and reproduction. */
    protected Random random = new Random();
    /** Hit points; values below 30 cause the prey brain to prefer fleeing. */
    protected int health;
    /** Satiety from 0 to 100; low values trigger hunger behavior and can cause death. */
    protected double hunger;
    /** Sub-pixel horizontal position used for smooth movement. */
    protected double exactX;
    /** Sub-pixel vertical position used for smooth movement. */
    protected double exactY;
    /** Maximum pixel offset applied when generating a random wander step. */
    protected int maxMove;
    /** Pixels advanced toward the current waypoint each movement tick. */
    protected double speed = 1.55;
    /** Yearly samples of deaths caused by old age, consumed by {@link Stats}. */
    static List<Integer> diedOfOld = new ArrayList<>();
    /** Yearly samples of deaths caused by predators, consumed by {@link Stats}. */
    static List<Integer> eatenByPred = new ArrayList<>();
    /** Deaths by old age accumulated during the current simulated year. */
    static int diedOfOldYr = 0;
    /** Deaths by predation accumulated during the current simulated year. */
    static int eatenByPredYr = 0;
    /** Diet category that hunting predators use to decide whether this organism is valid prey. */
    protected FoodPreferance foodPreferance;
    /** {@code true} while this organism is walking toward {@link #currentTarget}. */
    protected boolean isMoving;
    /** Decision maker that selects the next {@link State} each tick. */
    protected StateMachine brain;
    /** State chosen on the most recent brain update. */
    protected State currentState;
    /** Frame at which this organism dies of old age if it survives until then. */
    protected long deathFrame;
    /** Frame on which this organism was created. */
    protected long birthFrame;
    /** Remaining ticks before this organism may reproduce again. */
    protected int childCooldown = 850;
    /** {@code true} once this organism should be removed from the world. */
    boolean dead = false;
    /** {@link #OrgID} of the parent, or {@code -1} if this organism was spawned at world start. */
    protected int parent = -1;
    /** Hunger subtracted on a successful metabolism tick (used by predators). */
    protected double hungerDecrease = 1.5;
    /** Minimum hunger required before a predator may attempt to reproduce. */
    protected int childHungerReq = 80;
    /** Hunger spent when a predator successfully produces offspring. */
    protected int childHungerCost = 40;
    /** {@link #OrgID} of the most recently spawned child. */
    protected int child;
    /** Remaining ticks of post-meal inactivity during which hunting is suppressed. */
    protected int digesting = 0;
    /** Generational depth; founders start at 1 and each child increments by one. */
    protected int generation = 1;
    /** Highest {@link #generation} observed among organisms that have died. */
    static int largestGeneration = 1;

    /**
     * Constructs prey at the given position using a caller-supplied brain.
     *
     * @param startX initial x-coordinate in pixels
     * @param startY initial y-coordinate in pixels
     * @param sizeX  sprite width in pixels
     * @param sizeY  sprite height in pixels
     * @param brain  state machine that will select this organism's actions
     */
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

    /**
     * Constructs prey at the given position with a newly created {@link StateMachine}.
     *
     * @param startX initial x-coordinate in pixels
     * @param startY initial y-coordinate in pixels
     * @param sizeX  sprite width in pixels
     * @param sizeY  sprite height in pixels
     */
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

    /**
     * Advances one simulation tick: reproduction, death, AI, movement, and cooldowns.
     * <p>
     * Fleeing occasionally spends hunger to raise {@link #speed}; otherwise speed
     * returns to the prey default of 1.55.
     * </p>
     *
     * @param worldEntities living organisms used by the brain for vision checks
     * @param spawnQueue    list that receives newly created children this tick
     */
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
                    speed = 1.65;
                    hunger-=5;
                }
            }
        } else {
            speed = 1.55;
        }
    }

    /**
     * Marks this organism dead and updates {@link #largestGeneration} if needed.
     */
    public void die () {
        if (this.generation > largestGeneration) {
            largestGeneration = this.generation;
        }
        dead = true;
    }

    /**
     * Reports whether this organism has been marked for removal.
     *
     * @return {@code true} if {@link #die()} has been called
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Walks toward {@link #currentTarget}, pulling the next queued waypoint when needed.
     * <p>
     * Targets outside the window are discarded. Arrival snaps to the waypoint;
     * otherwise position advances by {@link #speed} along the line to the target.
     * </p>
     */
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

    /**
     * Applies old-age death at {@link #deathFrame}, or a rare random hazard death.
     */
    public void tryToDie() {
        if (GameConstants.CurrentFrame >= deathFrame) {
            if (this instanceof Predator) {
                Predator.predatorsDiedOfOld++;
            } else {
                preyDiedByOld++;
            }
            diedOfOldYr++;
            die();
        } else {
            if (GameConstants.CurrentFrame % 240 == 0 && random.nextInt(1000) < 8) {
                die();
                preyDiedBySomething++;
            }
        }
    }

    /**
     * Occasionally reduces hunger while not digesting; starvation calls {@link #die()}.
     * <p>
     * Currently invoked by {@link Predator#updateLogic(List, List)}; prey metabolism
     * is commented out in {@link #updateLogic(List, List)}.
     * </p>
     */
    protected void decreaseHunger() {
        if (hunger > 0 && digesting == 0) {
            int randomNumber = random.nextInt(150);
            if (randomNumber == 0) {
                hunger-=hungerDecrease;
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

    /**
     * Population used as the denominator for prey birth chance; never returns zero.
     *
     * @return current prey census, or {@code 1} if the census is empty
     */
    private int getChildPossiblility(){
        int count = Stats.getPreyCount();
        if (count <= 0) {
            return 1;
        }
        return count;
    }

    /**
     * Attempts to spawn a child when hunger is high and the reproduction cooldown is finished.
     * <p>
     * Birth probability is {@code 1 /} {@link #getChildPossiblility()}, which scales
     * inversely with the global prey count. A successful birth costs 80 hunger.
     * </p>
     *
     * @param spawnQueue list that receives the child if one is created
     */
    public void tryForChild(List<Organism> spawnQueue) {
        if (hunger > 80) {
            if (random.nextInt(getChildPossiblility()) == 0 && childCooldown == 0) {
                childCooldown = 6500;
                Organism child = new Organism(x, y, 15, 15);
                child.generation = this.generation+1;
                child.parent = OrgID;
                this.child = child.OrgID;
                spawnQueue.add(child);
                hunger-=80;
            }
        }
    }

    /**
     * Chance-based flora feeding that raises hunger by 5.08, capped at 100.
     * <p>
     * Has no effect while {@link #digesting} is greater than zero.
     * </p>
     */
    public void eat() {
        if (random.nextInt(7) == 0 && digesting == 0) {
            hunger+=5.08;
            if (hunger > 100) {
                hunger = 100;
            }
        }
    }

    /**
     * Appends an absolute pixel waypoint to {@link #movementQueue}.
     *
     * @param targetX destination x-coordinate in pixels
     * @param targetY destination y-coordinate in pixels
     */
    public void queueWaypoint(int targetX, int targetY) {
        movementQueue.add(new Point(targetX, targetY));
    }

    /**
     * Queues a waypoint offset from the current position if the result stays on-screen.
     *
     * @param x horizontal offset in pixels
     * @param y vertical offset in pixels
     */
    public void move(int x, int y) {
        if (this.x+x <= 0 || this.x+x >= GameConstants.AppWidth || this.y+y <= 0 || this.y+y >= GameConstants.AppHeight) {return;}
        movementQueue.add(new Point(this.x+x, this.y+y));
    }

    /**
     * Queues a random wander step when this organism is not already moving.
     */
    public void moveRandomly() {
        if (!isMoving) {
            move(random.nextInt(maxMove)-maxMove/2, random.nextInt(maxMove)-maxMove/2);
        }
    }

    /**
     * Sets a single escape waypoint away from the closest known predator.
     * <p>
     * Does nothing if no predator is cached or a target is already in progress,
     * preventing a flood of waypoints each tick.
     * </p>
     */
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

    /**
     * Dispatches the selected {@link State} to wander, flee, eat, or idle.
     *
     * @param state behavioral mode chosen by {@link #brain}
     */
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
