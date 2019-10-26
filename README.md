# How to run
    1. Install IntelliJ
    2. Install the lombok plugin https://projectlombok.org/setup/intellij
    3. Enable 'annotation processing' in settings
    4. Clone project by clicking new->Project from Version Source Control
    5. Import maven project by right clicking on the pom.xml and telling Intelij to import maven project
    6. In the maven tab at the right click the refresh symbol
    7. Click on play

# Implementing a Seeker
    1. Create a class "yourPlayer" in
    src/main/java/com/treasure/hunt/strategy/seeker/implementations/
    2. Implement to the interface "Seeker"

# Implementing a Tipster
    1. Create a class "yourTipser" in
    src/main/java/com/treasure/hunt/strategy/tipster/implementations/
    2. Implement to the interface "Tipster"
    
# Implementing a View
    1. Create a class "yourView" in
    src/main/java/com/treasure/hunt/view/in_game/implementations/
    2. Implement to the interface "View"