import java.util.List;

/**
 * Prey brain that chooses {@link State#WANDERING}, {@link State#HUNGRY}, or {@link State#FLEEING}.
 * <p>
 * Hunger below 80 forces eating. Health below 30 forces fleeing. Otherwise the
 * brain periodically scans {@link Predator#predators} within {@link #visionRadius}
 * and flees if a hunter is nearby.
 * </p>
 */
public class StateMachine {
    /** Maximum distance, in pixels, at which a predator is treated as a threat. */
    private int visionRadius = 50;
    /** Predator selected by the most recent successful vision scan, if any. */
    private Predator foundClosestPred;

    /**
     * Selects the next behavioral state for {@code entity} given the current world.
     *
     * @param entity        the organism whose state is being decided
     * @param worldEntities all living organisms in the simulation
     * @return the state the organism should execute this tick
     */
    public State determineNextState(Organism entity, List<Organism> worldEntities) {
        if (entity.hunger < 80) {
            return State.HUNGRY;
        } else if (entity.health < 30) {
            return State.FLEEING;
        }

        if (entity.hunger >= 40) {
            if ((GameConstants.CurrentFrame + entity.OrgID) % 15 == 0) {
                Predator closestPred = findClosestPred(entity, worldEntities);
                if (closestPred != null) {
                    foundClosestPred = closestPred;
                    return State.FLEEING;
                }
            }
        }


        return State.WANDERING;
    }

    /**
     * Returns the predator cached by the last vision scan that triggered fleeing.
     *
     * @return the closest detected predator, or {@code null} if none has been stored
     */
    public Predator getClosestPred() {
        return foundClosestPred;
    }

    /**
     * Finds the nearest registered predator within {@link #visionRadius} of {@code self}.
     *
     * @param self          the organism looking for threats
     * @param worldEntities unused by this implementation; scans {@link Predator#predators} instead
     * @return the closest predator in range, or {@code null} if none is close enough
     */
    private Predator findClosestPred(Organism self, List<Organism> worldEntities) {
        Predator closestPred = null;
        double closestDist = visionRadius;

        for (Predator other : Predator.predators) {
            double deltaX = other.x - self.x;
            double deltaY = other.y - self.y;
            double distance = Math.sqrt((deltaX * deltaX) + (deltaY + deltaY));
            if (distance < closestDist) {
                closestDist = distance;
                closestPred = other;
            }
        }
        return closestPred;
    }

}
