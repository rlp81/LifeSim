/**
 * Dietary categories that control which other organisms a hunter will target.
 * <p>
 * Prey default to {@link #FLORA}. Predators typically hunt {@link #PREY}, and
 * may switch to {@link #ALL} when extremely hungry so they will also consider
 * other predators.
 * </p>
 */
public enum FoodPreferance {
    /** Accepts any organism as a valid food source. */
    ALL,
    /** Targets non-predator organisms (standard prey). */
    PREY,
    /** Targets other predators. */
    PREDATOR,
    /** Does not hunt organisms; used by plant-eating prey. */
    FLORA
}
