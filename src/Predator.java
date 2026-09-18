import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Red hunting organism that chases prey, digests kills, and reproduces when well fed.
 * <p>
 * Uses a {@link StateMachine_Predator} brain. On catching prey, hunger rises and
 * {@link #digesting} blocks further hunting for a short time. Near a target the
 * predator can sprint; reproduction is capped by {@link #MAX_PREDATORS}.
 * </p>
 */
public class Predator extends Organism {
    /** Predator-specific brain that selects hunting versus wandering. */
    StateMachine_Predator brain;
    /** Live predators registered for O(N) prey vision scans. */
    static List<Predator> predators = new CopyOnWriteArrayList<>();;
    /** Number of predators currently registered in {@link #predators}. */
    static int currentPredators = 0;
    /** Lifetime count of predators that died with hunger at or below zero. */
    static int predatorsStarved = 0;
    /** Lifetime count of predators that reached their {@link #deathFrame}. */
    static int predatorsDiedOfOld = 0;
    /** Hard cap on simultaneous living predators. */
    final int MAX_PREDATORS = 5000000;
    /** Intended hunt scan radius in pixels (assigned at construction; unused by the brain). */
    protected int preySearchRadius;

    /**
     * Constructs a red predator, registers it globally, and applies hunter defaults.
     *
     * @param startX initial x-coordinate in pixels
     * @param startY initial y-coordinate in pixels
     * @param sizeX  sprite width in pixels
     * @param sizeY  sprite height in pixels
     */
    public Predator(int startX, int startY, int sizeX, int sizeY) {
        super(startX, startY, sizeX, sizeY);
        foodPreferance = FoodPreferance.PREY;
        this.brain = new StateMachine_Predator();
        deathFrame = birthFrame+4900+random.nextInt(4000);
        changeColor(Color.RED);
        digesting = 0;
        preySearchRadius = 150;
        hunger = 60;
        childCooldown = 60;
        speed = 1.5;
        currentPredators++;
        predators.add(this);
    }

    /**
     * Queues a waypoint toward the closest cached prey, or wanders when prey is scarce.
     * <p>
     * If few prey remain and several predators are alive, most ticks skip the chase
     * and lower metabolic/reproduction costs instead.
     * </p>
     */
    public void hunt() {
        int currentPrey = Stats.getPreyCount();
        int currentPred = Stats.getPredatorCount();
        if (currentPrey < 100 && hunger > 30 && currentPred > 10) {

            if (random.nextInt(5) != 0) {
                hungerDecrease = .5;
                childHungerReq = 60;
                childHungerCost = 30;
                currentState = State.WANDERING;
                moveRandomly();
                currentTarget = null;
                return;
            }
        }
        childHungerReq = 80;
        childHungerCost = 40;
        hungerDecrease = 1.5;
        Organism prey = brain.getClosestPrey();
        if (prey != null) {
            queueWaypoint(prey.x, prey.y);
        }
    }

    /**
     * Walks toward the current waypoint and resolves a kill if hunting and overlapping prey.
     * <p>
     * When closing on a moving target the queued path is cleared so a fresh chase
     * waypoint can be issued. Near prey, {@link #speed} rises to 1.85.
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

        double deltaX = currentTarget.x - exactX;
        double deltaY = currentTarget.y - exactY;
        double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

        if (distance <= speed) {
            exactX = currentTarget.x;
            exactY = currentTarget.y;
            currentTarget = null;
            this.isMoving = false;
            if (currentState == State.HUNTING) {
                Organism prey = brain.getClosestPrey();
                if (prey != null) {
                    deltaX = prey.x - exactX;
                    deltaY = prey.y - exactY;
                    distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
                    if (distance <= speed*1.5) {
                        prey.die();
                        eatenByPredYr++;
                        preyEaten++;
                        hunger+=12;
                        if (speed > 1.5) {
                            speed = 1.5;
                        }
                        if (hunger >= childHungerReq) {
                            digesting = 600;
                        } else {
                            digesting = 100;
                        }
                    }
                }
            }

        }  else {
            if (currentState == State.HUNTING) {
                Organism prey = brain.getClosestPrey();
                if (prey != null) {
                    if (Math.abs(currentTarget.x-prey.x) > speed || Math.abs(currentTarget.y-prey.y) > speed) {
                        currentTarget = null;
                        movementQueue.clear();
                    } else {
                        if (Math.abs(currentTarget.x-prey.x) < speed*14 || Math.abs(currentTarget.y-prey.y) < speed*14) {
                            speed = 1.85;
                        }
                    }
                }
            }
            double directionX = deltaX / distance;
            double directionY = deltaY / distance;

//            IO.println("Current Pos: " + exactX + ", " + exactY + "\nDegX: " + directionX + " DegY: " + directionY + " Target Pos: " + currentTarget.x + ", " +currentTarget.y);
            exactY += directionY*speed;
            exactX += directionX*speed;
        }
        this.x = (int) Math.round(exactX);
        this.y = (int) Math.round(exactY);
    }

    /**
     * Attempts to spawn another predator when hunger, cooldown, and population cap allow.
     *
     * @param spawnQueue list that receives the child if one is created
     */
    public void tryForChild(List<Organism> spawnQueue) {
        if (hunger >= childHungerReq && childCooldown == 0 && currentPredators < MAX_PREDATORS) {
            if (random.nextInt(900) == 0) {
                Predator child = new Predator(x, y, width, height);
                child.generation = this.generation+1;
                child.parent = OrgID;
                this.child = child.OrgID;
                spawnQueue.add(child);
                hunger-=childHungerCost;
                childCooldown = 7000;
            }
        }
    }

    /**
     * Advances one hunter tick: death, reproduction, metabolism, AI, movement, and timers.
     *
     * @param worldEntities living organisms used by the predator brain for hunting scans
     * @param spawnQueue    list that receives newly created predator children this tick
     */
    @Override
    public void updateLogic(List<Organism> worldEntities, List<Organism> spawnQueue) {
        tryToDie();
        tryForChild(spawnQueue);
        decreaseHunger();
        currentState = brain.determineNextState(this, worldEntities);
        executeState(currentState);
        processMovement();
        if (digesting > 0) {
            digesting--;
        }
        if (childCooldown > 0) {
            childCooldown--;
        }
    }

    /**
     * Dispatches the selected {@link State} to wander, flee, or hunt.
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
            case HUNTING:
                hunt();
                break;
            case IDLE:
            default:
                break;
        }

    }
}
