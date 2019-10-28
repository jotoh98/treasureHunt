# How to run
    1. Install IntelliJ
    2. Install the lombok plugin https://projectlombok.org/setup/intellij
    3. Enable 'annotation processing' in settings
    4. Clone the project by clicking new->Project from Version Source Control
    5. Import maven project by right clicking on the pom.xml and telling IntelliJ to import maven project
    6. In the maven tab at the right click the refresh symbol
    7. Click on play

# Implementing a Seacher
    1. Create a class "yourSearcher" in
    src/main/java/com/treasure/hunt/strategy/searcher/implementations/
    2. Implement to the interface "Seacher"

# Implementing a Hider
    1. Create a class "yourHider" in
    src/main/java/com/treasure/hunt/strategy/hider/implementations/
    2. Implement to the interface "Hider"
    
# Implementing a View
    Starting the GameManager with your View Class applied will
    run it **concurrent**!
    1. Create a class "yourView" in
    src/main/java/com/treasure/hunt/view/in_game/implementations/
    2. Implement to the interface "View"
    3. Access the Products created after each step via
    GameHistory.giveProductsCopy()