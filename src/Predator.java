import java.awt.*;
import java.util.List;

public class Predator extends Organism {
    StateMachine_Predator brain;
    protected int digesting;
    protected int preySearchRadius;

    public Predator(int startX, int startY, int sizeX, int sizeY) {
        super(startX, startY, sizeX, sizeY);
        foodPreferance = FoodPreferance.PREY;
        this.brain = new StateMachine_Predator();
        changeColor(Color.RED);
        digesting = 0;
        preySearchRadius = 150;
        hunger = 50;
        speed = 1.5;
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
                        hunger+=10;
                        speed = 1.5;
                        if (hunger >= 60) {
                            digesting = 600;
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
                        if (Math.abs(currentTarget.x-prey.x) < speed*4 || Math.abs(currentTarget.y-prey.y) < speed*4) {
                            speed = 1.65;
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
        if (hunger >= 60) {
            if (random.nextInt(320) == 0) {
                Predator child = new Predator(x, y, width, height);
                child.parent = OrgID;
                this.child = child.OrgID;
                spawnQueue.add(child);
                hunger-=30;
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
    }

    private void decreaseHunger() {
        if (hunger > 0 && digesting == 0) {
            int randomNumber = random.nextInt(150);
            if (randomNumber == 0) {
                hunger-=5;
                if (hunger > 0) {
                    hunger = 0;
                }
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
            case HUNTING:
                hunt();
                break;
            case IDLE:
            default:
                break;
        }

    }
}
