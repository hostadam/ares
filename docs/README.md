# Ares
Ares is a plugin library designed to provide developers with easy access to multiple frameworks and utilities.

### 3.0.0
Please note that the latest version, 3.0.0, brings many changes to Ares.
* Instead of a singleton, it's now integrated into native Bukkit services. Review the updated section below for initialization.
* The scoreboard system has been optimized heavily, and a critical issue with concurrency has been fixed.
* Improved chat input API

### Important Information
* Ares has been tested multiple times, but it may contain bugs or other issues. You are encouraged to report this here on GitHub.
* Ares is built with 1.20+ but should work from 1.16 and onwards.

### Features
* Highly-optimized Scoreboard API
* Nametags API (for player list sorting / rank prefixes)
* Annotation-based command API
* Menu API with pagination
* Player chat input API 
* Static utilities

### For developers
If you wish to use Ares for your own server as a library, add to your project's pom.xml. 
```
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.hostadam</groupId>
    <artifactId>ares</artifactId>
    <version>3.0.0</version>
</dependency>
```
Since Ares is an API and not a plugin, it's your responsibility to make sure it's accessible during runtime.
Shading is likely the most convenient way to do this;
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
            <pattern>com.github.hostadam.ares</pattern>
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
        AresImpl ares = new AresImpl(this);
        Bukkit.getServicesManager().register(Ares.class, ares, this, ServicePriority.Normal);
    }
}
```
After it's been initialized, you can retrieve Ares through Bukkit services.
```java
Bukkit.getServicesManager().load(Ares.class);
```

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
ares.scoreboard().setAdapter(YOUR ADAPTER);
```
And we are done!

### Nametags
Ares has no default nametag implementation. You need to create your own ```NametagHandler```.
The ```NametagHandler``` contains the following provided methods: ```getTeam(String teamName)```, ```switchTeamOfPlayer(String oldTeamName, String newTeamName, String playerEntry)```, ```createTeam(String name, int priority)``` and ```shutdown``` which should be overridden.

To register your nametag handler, create an event listener to assign the nametag handler to the player's board.
```
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    Board board = ares.scoreboard().getScoreboard(event.getPlayer());
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
The first parameter, CommandSender, must be present in the method parameters for the command to work. If your command should only be executed by players, you can replace CommandSender with Player.

Any parameters after that is optional. If you wish to add, let's say, an optional argument ```int feedAmount```, then you can add utilize the @Param annotation. If no arg is provided by the player, this value will be null by default. Some parameter types - the primitive types - cannot be null however. Instead, they return values we consider "null", like -1. The optional ```errorIfEmpty``` field of the annotation can be used to send an error message to the player if their arg is invalid.

``` Java
@AresCommand(
        labels = { "feed", "f" },
        description = "Feed another player",
        requiredArgs = 1,
        usage = "<player> [feedAmount]",
        permission = "ares.feed"
)
public void feed(CommandSender sender, Player target, @Param(optional=true) int feedAmount) {
    target.setFoodLevel((feedAmount == -1 ? 20 : feedAmount));
}
```

To register the command, get the command handler and register:
``` java
ares.commands().register(new FeedCommand());
```

#### Subcommands
To create subcommands, the ```AresCommand``` annotation has a ```parent``` field.
``` Java
@AresCommand(
       parent = "feed"
       labels = { "example" },
       description = "Example subcommand to feed",
       requiredArgs = 1,
       usage = "<player>",
       permission = "ares.feed"
)
```
Register the sub command in the same way as above.

## Chat Input
Sometimes you want players to enter an input in chat. Ares has an API to streamline this process. 
Here is an example of the chat input
``` java
ChatInput.newInput(player)
        .nonCancellable()
        .validator(string -> !string.isEmpty())
        .read(Bukkit::broadcastMessage);
```
The ```nonCancellable``` method ensures the user cannot opt-out of the input. Otherwise, by typing 'cancel', they can end the process without submitting an input.


