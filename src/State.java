/**
 * Behavioral modes assigned by an organism's {@link StateMachine}.
 * <p>
 * Each tick the brain selects one of these states, and the organism then
 * executes the matching action (wander, flee, hunt, eat, or idle).
 * </p>
 */
public enum State {
    /** No action is taken this tick. */
    IDLE,
    /** Move to a random nearby waypoint. */
    WANDERING,
    /** Run away from a nearby predator. */
    FLEEING,
    /** Chase the closest detected prey. */
    HUNTING,
    /** Consume food at the current location (reserved; unused by current brains). */
    FEEDING,
    /** Seek or consume food because hunger is low. */
    HUNGRY
}
