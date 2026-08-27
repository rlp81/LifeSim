# LifeSim

An Object-Oriented 2D artificial life simulation built entirely in Java.

## Overview
LifeSim is a custom-built game engine that simulates autonomous biological agents, specifically standard Organisms and Predators, interacting on an 800x800 pixel canvas. Organisms are driven by a decoupled AI State Machine, allowing them to dynamically wander, hunt, flee, and reproduce based on environmental factors and internal states like hunger and health.

## Engine Features
* **Custom Game Loop:** The engine runs on a dedicated thread locked to a target of 600 frames per second for high-speed simulation.
* **Headless Mode:** The simulation can be run without GUI rendering by passing the `--headless` argument, allowing the math to process at maximum CPU speed for data harvesting.
* **Optimized AI Vision (O(N)):** Predators register to a global, thread-safe list upon creation, eliminating O(N²) bottlenecks and allowing prey to scan for threats instantly.
* **Thread-Safe Memory Management:** The world state uses a `CopyOnWriteArrayList` to allow the active game logic to process entities without crashing the AWT EventQueue during rendering.
* **Spawn Queueing:** New offspring are generated into a separate `LinkedList` staging area (`spawnQueue`) and merged into the active world at the end of the frame, safely bypassing `ConcurrentModificationException` errors.
* **Post-Game Analytics:** When the simulation ends (or is manually quit), the engine renders a statistics screen tracking peak populations, causes of death, and total survival time.

## Biological Mechanics

### The Organism (Prey / Base Class)
* **Lifespan & Hazards:** Organisms have a randomized finite lifespan (between roughly 4,500 and 11,000 frames) and face a small constant risk of dying from environmental hazards.
* **Metabolism & Exhaustion:** Prey actively burn hunger over time. If they are forced to flee from a predator, they burn energy at an accelerated rate, risking starvation.
* **State Machine:** Governed by a base AI that defaults to `WANDERING`. If their health drops below 30, or they spot a predator, they transition to `FLEEING`. If their hunger drops below 80, they transition to `HUNGRY` and attempt to eat.
* **Dynamic Reproduction:** Well-fed prey can spawn offspring, but birth rates are dynamically throttled based on the current global population to prevent exponential overpopulation.

### The Predator
* **Hunting AI:** Predators (drawn in red) utilize a specialized `StateMachine_Predator` brain to scan for prey.
* **Dynamic Digestion:** When a predator intercepts prey, the prey dies. The predator gains hunger and enters a digesting phase. Digestion takes 600 frames if they are fully satiated (hunger >= 80), or 100 frames if they are still hungry.
* **Dynamic Speed:** Predators move at a base speed of 1.5, but can accelerate to a sprint speed of 1.85 when closing in on nearby prey.
* **Reproduction:** Predators can spawn offspring if their hunger is 80 or above and their breeding cooldown has expired.

---

## Getting Started

### Prerequisites
* Java Development Kit (JDK) 25 or higher installed on your system.
* Python 3.x (Optional, for running mass simulations).

### Running the Project
1. Compile the Java files in your IDE or via the command line.
2. Run the `GameWindow` class, which contains the `main` execution method.
3. The simulation will automatically launch an 800x800 window, populating the world with 100 base Organisms and 2 Predators.
4. **To run without graphics:** Execute the compiled program with the `--headless` flag.

### Experimental: MultiSimRunner
For users wanting to run multiple simulations concurrently and view population curves, the project includes an early-development Python script (`MultiSimRunner`).
* **Setup:** To use this script, you must first compile the Java project into a standalone executable JAR (e.g., `LifeSim.jar`) and place it in the exact same directory as the Python script. The Python script will automatically launch multiple background threads utilizing the engine's `--headless` mode.