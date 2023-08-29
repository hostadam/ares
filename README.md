# Ares
Ares is the utility plugin used for the upcoming Etheria SMP server.
It aims to reduce clutter in the core plugin by moving utility classes and frameworks to a common plugin.

### Important Information
* Ares is currently in a pre-release state. 
  * It's not production-ready; may contain bugs and otherwise lack features.
* Ares is built and tested on 1.20.1.
  * Older versions are not guaranteed to work but 1.17-1.19 are generally considered safe for Ares usage.

### Features
* Highly-optimized Scoreboard API
* Nametags API (for player list sorting / rank prefixes)
* Annotation-based command API
* *(Coming Soon)* Menu API with pages support
* Common utilities

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

## Guidelines
Below is a guideline for each framework to get you started. 
It's recommended to experiment by yourself to get an understanding of how the different components work. If you need additional help, contact me on Discord @ Hostadam.

## Scoreboard API
The scoreboard functionality is built into Ares. All you need to do is add player handling, and the scoreboard adapter for what lines, title and tab to show.
A note to remember is that Ares automatically updates the scoreboard every 2 ticks - this is the optimal value for performance and results.

Ares is made for performance; only when something on the scoreboard changes will it call for an update. 

Begin by creating an adapter for your scoreboard:
``` Java
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
``` Java
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
``` Java
Player player = event.getPlayer();
Board board = new Board(YOUR PLUGIN HERE, player, this.adapter);
board.setNametagHandler(YOUR HANDLER);
```

## Command API
The command API is aimed to be simple and convenient to developers. It's annotation-based with automatic parameter conversion.
How does it work?

We need to start by creating a command:
``` Java
public class FeedCommand {

    @AresCommand(
            labels = { "feed", "f" },
            description = "Feed another player",
            requiredArgs = 1,
            usage = "<player>",
            permission = "ares.feed"
    )
    public void feed(CommandSender sender, Player target) {
       target.setFoodLevel(20);
    }
}
```
The first parameter, CommandSender, must be present in the method parameters for the command to work.
Any parameters after that is optional. If you wish to add, let's say, an optional argument ```int feedAmount```, then you can add ```String[] args``` to your parameters and use those, with ```args[0]``` returning the first argument.
``` Java
@AresCommand(
        labels = { "feed", "f" },
        description = "Feed another player",
        requiredArgs = 1,
        usage = "<player> [feedAmount]",
        permission = "ares.feed"
)
public void feed(CommandSender sender, Player target, String[] args) {
    int feedAmount = args.length > 0 ? Integer.parseInt(args[0]) : 20;
    target.setFoodLevel(feedAmount);
}
```

To register the command, get the command handler and register:
``` Java
CommandHandler commandHandler = new CommandHandler();
commandHandler.register(new FeedCommand());
```

#### Subcommands
Same structure as above, except for labels:
``` Java
@AresCommand(
       labels = { "feed example" },
       description = "Example subcommand to feed",
       requiredArgs = 1,
       usage = "<player>",
       permission = "ares.feed"
)
```
Register the sub command in the same way as above. Just ensure that your main command (feed) is registered **before** subcommands.


