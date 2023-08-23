# Ares
Ares is the utility library used for the upcoming Etheria SMP server.
It aims to reduce clutter in the core plugin by moving utility classes and frameworks to a library as opposed to having it in a server plugin.

### Features
* Highly-optimized Scoreboard API
* Nametags API (for player list sorting / rank prefixes)
* Common utilities
* *(Coming Soon)* Annotation-based command API
* *(Coming Soon)* Menu API with pages support

### For developers
If you wish to use Ares for your own server as a library, add to your project's pom.xml:

```
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.hostadam</groupId>
    <artifactId>ares</artifactId>
    <version>0.2</version>
</dependency>
```

### Important Note
Ares is currently in a pre-release state. It's not production-ready; may contain bugs and otherwise lack features.

## Guidelines
Below is a guideline for each framework to get you started. 
It's recommended to experiment by yourself to get an understanding of how the different components work. If you need additional help, contact me on Discord @ Hostadam.

## Scoreboard API
The scoreboard functionality is built into Ares. All you need to do is add player handling, and the scoreboard adapter for what lines, title and tab to show.
A note to remember is that Ares automatically updates the scoreboard every 2 ticks - this is the optimal value for performance and results.

Ares is made for performance; only when something on the scoreboard changes will it call for an update. 

Begin by creating an adapter for your scoreboard:
```
public class DefaultBoardAdapter implements BoardAdapter {
    @Override
    public String title(Player player) {
        return player.getName();
    }

    @Override
    public String[] tab(Player player) {
        return new String[] { "First line here", "Second line here" };
    }

    @Override
    public List<String> lines(Player player) {
        return Arrays.asList("Test line");
    }
}
```

Save the adapter to use for player handling:
```
public class PlayerListener implements Listener {

    private final BoardAdapter adapter;

    public PlayerListener() {
        this.adapter = new DefaultBoardAdapter();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(this.adapter == null) return;

        Player player = event.getPlayer();
        new Board(YOUR PLUGIN HERE, player, this.adapter);
    }
```
The initialization of the scoreboard and the update task is automatically handled, so now we're done!

For nametags, Ares has no default handler, so you need to make your own ```NametagHandler``` and implement it yourself. 
Provided methods within the ```NametagHandler``` are ```getTeam(String name)```, ```replace(String oldTeamName, String newTeamName, String playerEntry)```, ```createTeam(String name, int priority)```. We recommend using priorities between 0-26 (based of the alphabet) with 0 being highest priority.

When you've created the handler, go back to your previous ```PlayerListener``` and change/add the following code:
```
Player player = event.getPlayer();
Board board = new Board(YOUR PLUGIN HERE, player, this.adapter);
board.setNametagHandler(YOUR HANDLER);
```
