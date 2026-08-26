import java.util.List;
public class StateMachine {
    public State determineNextState(Organism entity, List<Organism> worldEntities) {
        if (entity.hunger < 80) {
            return State.HUNGRY;
        } else if (entity.health < 30) {
            return State.FLEEING;
        }


        return State.WANDERING;
    }
}
