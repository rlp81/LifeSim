import java.util.List;

public class StateMachine {
    private int visionRadius = 50;
    private Predator foundClosestPred;

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

    public Predator getClosestPred() {
        return foundClosestPred;
    }

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
