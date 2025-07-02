# Ares
Ares is a plugin library designed to provide developers with easy access to multiple frameworks and utilities.
As of version 4 and beyond, support for Spigot has been dropped in favor of PaperMC.

### Important Information
* Ares is not compatible with Bukkit or Spigot servers, only Paper and its' forks.
* Ares strives to stay up-to-date meaning backward compatibility cannot be guaranteed. It was originally built on 1.21.6.

### Features
* 100% Paper functionality (e.g. Adventure)
* Optimized Scoreboard API
* Player tab list ordering
* Intuitive Command API
* Menu framework with pagination support and 100% configurability
* Basic player chat input API 
* Set of static utilities 

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
    <version>4.1.2</version>
</dependency>
```
Since Ares is an API and not a plugin, it's your responsibility to make sure it's accessible during runtime.
Shading or using the fancier Paper methods are likely the most convenient ways to do this.
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
        new AresImpl(this);
    }
}
```
Ares will automatically register itself in the Bukkit services API. There are two ways you can access Ares during runtime.

Method 1: Assigning instance to variable on enable.
```java
Ares ares = new AresImpl(this);
```

Method 2: Accessing via Bukkit services.
```java
Ares ares = Bukkit.getServicesManager().load(Ares.class);
```

## Scoreboard API
The scoreboard framework is all built into Ares. All you need to implement a scoreboard adapter for what lines, title and tab to show.

### Features
* Support for custom tab header & tab footer
* Optimized scoreboard updates, minimizing performance impact

### How to use
Begin by creating a board style for your scoreboard by implementing the BoardStyle interface.
``` Java
public class DefaultBoardAdapter implements BoardAdapter {

    @Override
    public Component title(Player player) {
        return Component.text("Ares Demo", NamedTextColor.GREEN);
    }

    @Override
    public Component header(Player player) {
        return Component.text("Ares Demo", NamedTextColor.BLUE);
    }

    @Override
    public Component footer(Player player) {
        return Component.text("Ares Demo", NamedTextColor.GRAY);
    }

    @Override
    public List<Component> lines(Player player) {
        List<Component> lines = new ArrayList<>();

        for(int i = 0; i < ThreadLocalRandom.current().nextInt(16); i++) {
            lines.add(Component.text("Ares Demo", NamedTextColor.WHITE));
        }

        return lines;
    }
}
```
Then, to apply the style and customize any settings, you can use the ```BoardSettings``` builder.
```java
this.ares.scoreboard().updateSettings(
        new BoardSettings()
                .interval(5)
                .scoreFormat(NumberFormat.blank())
                .style(new DevPluginBoard())
                .tabListOrdering(HumanEntity::getFoodLevel)
);
```

The ```interval(int)``` sets how often the scoreboard should update (in ticks).
The ```scoreFormat(NumberFormat)``` allows changing how the score numbers should be rendered.
The ```style(BoardStyle)``` is for your newly created adapter.
The ```tabListOrdering(Function<Player, Integer>)``` is for deciding on how players should be ordered on the tablist.

## Command API
The command API aims to be intuitive, yet simple and convenient to developers. 

We need to start by creating a command:
``` Java
public class FeedCommand {

    @AresCommand(
            labels = { "feed" },
            description = "Feed yourself or another player",
            usage = "[player]",
            permission = "ares.command.feed"
    )
    public void feed(CommandContext ctx) {
        Optional<Player> optional = ctx.getArgument("player", Player.class, Component.text("Invalid player.", NamedTextColor.RED));
        optional.ifPresentOrElse(target -> {
            target.setFoodLevel(20);
            target.setSaturation(0.0f);
            target.setExhaustion(0.0f);
            target.sendMessage(Component.text("You have been fed!", NamedTextColor.GREEN));
        }, () -> {
            Player sender = ctx.sender(Player.class, Component.text("Only players can do this", NamedTextColor.RED));
            sender.setFoodLevel(20);
            sender.setSaturation(0.0f);
            sender.setExhaustion(0.0f);
            sender.sendMessage(Component.text("You have been fed!", NamedTextColor.GREEN));
        });
    }
}
```
The ```CommandContext``` parameter is required on all commands. Like Brigadier, it's where you can fetch the sender & resolve arguments.
I recommend digging into the API and experimenting to understand each argument method since they all serve different purposes.

In this case, the ```getArgument(String placeholderName, Class<T> type, Component errorMessage)``` is used because it allows sending a custom error message if the user does not enter a valid player. The ```Optional``` ensures that you do not need to specify a player. If the Optional is empty (no player was provided), then it will saturate the sender instead. 

**The usage field in the AresCommand annotation is critical. For required arguments, surround your argument name in <> and for optional arguments, use [].**

To register the command, get the command handler and register:
``` java
ares.commands().register(new FeedCommand());
```

#### Subcommands
To create subcommands, the ```AresSubCommand``` annotation can be used which follows the exact same style and syntax as above, it just has an extra ```parent``` field.
``` Java
@AresSubCommand(
       parent = "feed"
       labels = { "example" },
       description = "Example subcommand to feed",
       usage = "<player>",
       permission = "ares.feed"
)
```
Register the sub command in the same way as above.

#### Custom Argument Parsers
Ares has a wide range of supported types of arguments for parsing.
If you wish to add your own parser, create one using the ```ParameterArgParser<T>``` interface and register it using ```ares.commands().context().registerParser(Class<T> type, ParameterArgParser<T>)```.

#### Tab Completion
Ares provides support for custom tab completion using the ```@TabCompletionMapper``` interface. Here, you can map the name of an argument to a class that will get resolved automatically.

If you wish to add a custom tab completer, create a ```ParameterTabCompleter``` interface and register it using ```ares.commands().context().registerTabCompleter(Class<T> type, ParameterTabCompleter)```. Now, your custom tab completor can be used as the ```mappedClass``` field in an interface.

## Chat Input
Sometimes you want players to enter an input in chat. Ares has an API to streamline this process. 
Here is an example of the chat input
``` java
ChatInput.newInput(player)
        .nonCancellable()
        .read(Bukkit::broadcastMessage);
```

The ```nonCancellable``` method ensures the user cannot opt-out of the input. Otherwise, by typing 'cancel', they can end the process without submitting an input.
The ```read()``` method also accepts a ```Predicate<String>```. If it resolves to false, an error message will be sent and they will need to re-type their input.


