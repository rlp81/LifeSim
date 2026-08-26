import java.awt.*;
import java.util.List;

public class Predator extends Organism {
    StateMachine_Predator brain;
    static int currentPredators = 0;
    final int MAX_PREDATORS = 5000000;
    protected int preySearchRadius;

    public Predator(int startX, int startY, int sizeX, int sizeY) {
        super(startX, startY, sizeX, sizeY);
        foodPreferance = FoodPreferance.PREY;
        this.brain = new StateMachine_Predator();
        deathFrame = birthFrame+4100+random.nextInt(4000);
        changeColor(Color.RED);
        digesting = 0;
        preySearchRadius = 150;
        hunger = 60;
        childCooldown = 60;
        speed = 1.5;
        currentPredators++;
    }

    public void hunt() {
        Organism prey = brain.getClosestPrey();
        if (prey != null) {
            queueWaypoint(prey.x, prey.y);
        }
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
            if (currentState == State.HUNTING) {
                Organism prey = brain.getClosestPrey();
                if (prey != null) {
                    deltaX = prey.x - exactX;
                    deltaY = prey.y - exactY;
                    distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
                    if (distance <= speed*1.5) {
                        prey.die();
                        preyEaten++;
                        hunger+=10;
                        speed = 1.5;
                        if (hunger >= 80) {
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
                        if (Math.abs(currentTarget.x-prey.x) < speed*20 || Math.abs(currentTarget.y-prey.y) < speed*20) {
                            speed = 1.75;
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

    public void tryForChild(List<Organism> spawnQueue) {
        if (hunger >= 80 && childCooldown == 0 && currentPredators < MAX_PREDATORS) {
            if (random.nextInt(800) == 0) {
                Predator child = new Predator(x, y, width, height);
                child.parent = OrgID;
                this.child = child.OrgID;
                spawnQueue.add(child);
                hunger-=40;
                childCooldown = 6000;
            }
        }
    }

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
