# LifeSim

An Object-Oriented 2D artificial life simulation built entirely in Java. 

## Overview
LifeSim is a custom-built game engine that simulates autonomous biological agents, specifically standard Organisms and Predators, interacting on an 800x800 pixel canvas. Organisms are driven by a decoupled AI State Machine, allowing them to dynamically wander, hunt, flee, and reproduce based on environmental factors and internal states like hunger and health. 

## Engine Features
* **Custom Game Loop:** The engine runs on a dedicated thread locked to 60 frames per second. It utilizes `Toolkit.getDefaultToolkit().sync()` to ensure smooth rendering and synchronization with the OS display.
* **Thread-Safe Memory Management:** The world state uses a `CopyOnWriteArrayList` to allow the active game logic to process entities without crashing the AWT EventQueue during rendering.
* **Spawn Queueing:** New offspring are generated into a separate `LinkedList` staging area (`spawnQueue`) and merged into the active world at the end of the frame, safely bypassing `ConcurrentModificationException` errors. 
* **Garbage Collection:** Dead organisms are cleanly removed from the engine at the end of the logic cycle using `removeIf(Organism::isDead)`.
* **Vector-Based Movement:** Entities calculate distances and normalized directional vectors using floating-point coordinates (`exactX`, `exactY`) for fluid, sub-pixel movement.

## Biological Mechanics

### The Organism (Prey / Base Class)
* **Lifespan:** Organisms have a finite lifespan and will naturally die 10,000 frames after their birth.
* **State Machine:** Governed by a base AI that defaults to `WANDERING`. If their health drops below 30, they transition to `FLEEING`. If their hunger drops below 60, they transition to `HUNGRY` and attempt to eat.
* **Reproduction:** If an organism is well-fed (hunger > 80), it has a random chance to spawn a child entity into the world, which costs 40 hunger.

### The Predator
* **Hunting AI:** Predators (drawn in red) utilize a specialized `StateMachine_Predator` brain to scan for prey within a 150-unit radius. 
* **Feeding & Digestion:** When a predator successfully intercepts prey, the prey dies instantly. The predator gains 10 hunger and enters a 600-frame "digesting" phase. 
* **Dynamic Speed:** Predators move at a base speed of 1.5, but can accelerate to 1.65 when closing in on nearby prey. 
* **Reproduction:** Predators can spawn offspring if their hunger is 60 or above, at the cost of 30 hunger. 

---

## Getting Started

### Prerequisites
* Java Development Kit (JDK) installed on your system.

### Running the Project
1. Compile the Java files in your IDE or via the command line.
2. Run the `GameWindow` class, which contains the `main` execution method. 
3. The simulation will automatically launch an 800x800 window, populating the world with 20 base Organisms and 1 Predator.
