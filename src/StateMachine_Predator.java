import java.util.List;

public class StateMachine_Predator extends StateMachine {
    private int visionRadius = 200;
    private Organism foundClosestPrey;

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

    public void setVisionRadius(int visionRadius) {
        this.visionRadius = visionRadius;
    }

    public int getVisionRadius() {
        return visionRadius;
    }

    public Organism getClosestPrey() {
        return foundClosestPrey;
    }

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
