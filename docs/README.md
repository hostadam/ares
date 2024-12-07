# Ares
Ares is a plugin library designed to provide developers with easy access to multiple frameworks and utilities.

### Important Information
* Ares is currently in a pre-release state. 
  * It's not production-ready; may contain bugs and otherwise lack features.
* Ares is built and tested on 1.20.1.
  * Older versions are not guaranteed to work but 1.17-1.19 are generally considered safe for Ares usage.

### Features
* Highly-optimized Scoreboard API
* Nametags API (for player list sorting / rank prefixes)
* Annotation-based command API
* Menu API with pages support
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
    <version>0.6.1</version>
</dependency>
```

Then, you need to make sure Ares is included in your build, so shade it:
```
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>3.1.0</version>
      <configuration>
        <relocations>
          <relocation>
            <pattern>com.github.hostadam</pattern>
            <!-- Replace this with your package! -->
            <shadedPattern>your.package</shadedPattern>
          </relocation>
        </relocations>
      </configuration>
      <executions>
        <execution>
          <phase>package</phase>
          <goals>
            <goal>shade</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

## Guidelines
Below is a guideline for each framework to get you started. 
It's recommended to experiment by yourself to get an understanding of how the different components work. If you need additional help, contact me on Discord @ Hostadam.

## Initializing Ares
Ares needs a plugin to rely on, so you need to make sure Ares is initialized properly.
```java
public class YourPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Ares.init(this);
    }
}
```
After it's been initialized, you can retrieve Ares with ```Ares.get();```.

## Scoreboard API
The scoreboard framework is all built into Ares. All you need to implement a scoreboard adapter for what lines, title and tab to show.

### Features
* Support for custom tab header & tab footer
* Automatic scoreboard updating
* Optimized for performance - will only update when it's necessary.

### How to use
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
Then, register your adapter:
``` Java
Ares ares = Ares.get();
ares.setScoreboardAdapter(YOUR ADAPTER);
```
And we are done!

For nametags, Ares has no default handler, so you need to make your own ```NametagHandler``` and implement it yourself. 
Provided methods within the ```NametagHandler``` are ```getTeam(String name)```, ```replace(String oldTeamName, String newTeamName, String playerEntry)```, ```createTeam(String name, int priority)```. We recommend using priorities between 0-26 (based of the alphabet) with 0 being highest priority.

And to use your NametagHandler, create an event listener like below:
```
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    Ares ares = Ares.get();
    Board board = ares.getScoreboard(event.getPlayer());
    board.setNametagHandler(YOUR HANDLER);
}
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


