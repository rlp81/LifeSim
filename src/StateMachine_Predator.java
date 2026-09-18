import java.util.List;

/**
 * Predator brain that prefers {@link State#HUNTING} when hungry and prey is in sight.
 * <p>
 * While hunger is below 80 and the predator is not digesting, this machine scans
 * for the nearest valid target. Hunger below 40 widens diet to {@link FoodPreferance#ALL}.
 * Low health still yields {@link State#FLEEING}; otherwise the predator wanders.
 * </p>
 */
public class StateMachine_Predator extends StateMachine {
    /** Maximum distance, in pixels, at which a target can be selected for hunting. */
    private int visionRadius = 200;
    /** Prey (or other valid target) selected by the most recent hunt scan, if any. */
    private Organism foundClosestPrey;

    /**
     * Selects hunting, wandering, or fleeing for the given predator.
     *
     * @param entity        the predator whose state is being decided; must be a {@link Predator}
     * @param worldEntities all living organisms that can be considered as prey
     * @return the state the predator should execute this tick
     */
    @Override
    public State determineNextState(Organism entity, List<Organism> worldEntities) {
        Predator predator = (Predator) entity;

        if (entity.hunger < 80 && predator.digesting == 0) {
            if (entity.hunger < 40) {
                entity.foodPreferance = FoodPreferance.ALL;
            }
            Organism closestPrey = findClosestPrey(predator, worldEntities);
            if (closestPrey != null) {
                foundClosestPrey = closestPrey;
                return State.HUNTING;
            } else {
                return State.WANDERING;
            }
        } else if (entity.health < 30) {
            return State.FLEEING;
        }


        return State.WANDERING;
    }

    /**
     * Replaces the hunting vision radius used by {@link #findClosestPrey(Predator, List)}.
     *
     * @param visionRadius new scan radius in pixels
     */
    public void setVisionRadius(int visionRadius) {
        this.visionRadius = visionRadius;
    }

    /**
     * Returns the current hunting vision radius.
     *
     * @return scan radius in pixels
     */
    public int getVisionRadius() {
        return visionRadius;
    }

    /**
     * Returns the target cached by the last successful hunt scan.
     *
     * @return the closest valid prey, or {@code null} if none has been stored
     */
    public Organism getClosestPrey() {
        return foundClosestPrey;
    }

    /**
     * Finds the nearest organism within {@link #visionRadius} that this predator may eat.
     * <p>
     * Other predators are skipped unless their diet is {@link FoodPreferance#PREDATOR}
     * or {@link FoodPreferance#ALL}.
     * </p>
     *
     * @param predator      the hunting predator
     * @param worldEntities all living organisms to scan
     * @return the closest valid target, or {@code null} if none is in range
     */
    private Organism findClosestPrey(Predator predator, List<Organism> worldEntities) {
        Organism closestPrey = null;
        double closestDist = visionRadius;

        for (Organism other : worldEntities) {
            if (other != predator) {
                if (other instanceof Predator ) {
                    if (other.foodPreferance != FoodPreferance.PREDATOR && other.foodPreferance != FoodPreferance.ALL) {
                        continue;
                    }
//                    if (predator.child == other.OrgID || predator.parent == other.OrgID) {
//                        continue;
//                    }
                }
                double deltaX = other.x - predator.x;
                double deltaY = other.y - predator.y;
                double distance = Math.sqrt((deltaX * deltaX) + (deltaY + deltaY));
                if (distance < closestDist) {
                    closestDist = distance;
                    closestPrey = other;
                }
            }
        }
        return closestPrey;
    }
}
