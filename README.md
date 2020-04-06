![Gradle Test](https://github.com/jotoh98/treasureHunt/workflows/Gradle%20Test/badge.svg)

# Java docs
[here](https://jotoh98.github.io/treasureHunt/)

# How to run
    1. Install IntelliJ
    2. Install the lombok plugin https://projectlombok.org/setup/intellij
    3. Enable 'annotation processing' in settings
    4. Clone the project by clicking new->Project from Version Source Control
    5. Import maven project by right clicking on the pom.xml and telling IntelliJ to import maven project
    6. In the maven tab at the right click the refresh symbol
    7. Click on play

# Implementing a Searcher
    1. Create a class "YourSearcher" in
    src/main/java/com/treasure/hunt/strategy/searcher/impl/
    2. Implement the interface Searcher<T>, where T is the Type of Hint
    the Searcher is compatible with.

# Implementing a Hider
    1. Create a class "YourHider" in
    src/main/java/com/treasure/hunt/strategy/hider/impl/
    2. Implement the interface Hider<T>, where T is the Type of Hint
    the Hider returns.
    3. Adapt the method GameEngine#VerifyHint, such that it allows your new type of Hint.
    
# Implementing a GameEngine
    1. Create a class "yourgameengine" in
    src/main/java/com/treasure/hunt/game/mods/yourMod/YourGameEngine/
    implementing GameEngine.
    2. Overwrite the necessary methods of the GameEngine, like
    init(), move(), hiderMove(), searcherMove(), verifyHint()
    in order to implement your wished rules.
    3. Create an interface "YourHider", extending Hider<T>.
    4. Create an interface "YourSearcher", extending Searcher<T>.
    5. In
    src/main/java/com/treasure/hunt/strategy/hider/impl/ and
    src/main/java/com/treasure/hunt/strategy/searcher/impl/
    implement your custom Hider and Searcher, playing your custom game modification.
