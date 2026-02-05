# Ares
Ares is a plugin library designed to provide developers with easy access to multiple frameworks and utilities.
It is specifically developed to be compatible with Paper with no guarantees for Bukkit, Spigot or Folia compatibility.

### Features
* Easy-to-use Config library for reading and writing to YAML files with ease

* 
* Optimized Scoreboard API
* Player tab list ordering
* Intuitive Command API
* Messages (language) system with placeholder support
* Menu framework with pagination support
* Basic player chat input API
* Item utilities - including a builder & configuration deserializer
* Set of static utilities including thread-safe location & chunk objects

## Guidelines
Below is a guideline to get started with Ares. 
It's recommended to experiment by yourself to get an understanding of how the different systems work.

### Maven library
If you wish to hook into Ares to use its features, add the following to your project's pom.xml:
```
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.hostadam</groupId>
    <artifactId>ares</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Initializing Ares
Ares is a standalone plugin. To make it work, you need to put in your server's plugins folder and start the server.
The API is easily accessible using the provided method:
```java
Ares ares = Ares.api();
```

## Custom Configs
Ares has an integrated configuration system that can read your config files with ease. 

Initialize a config file, create the Config and load it.
```java
ConfigFile file = new ConfigFile(this, "config.yml");
AresConfig config = ares.createConfig(file, AresConfig.class);
config.load();
```

The ```AresConfig.class``` is the actual configuration object. Below is an example of how this may look.
```java
public class AresConfig extends Config {

    // Example config: represents the server's name
    public String serverName;
    // Example config: represents whether players can connect to the server
    public boolean allowConnections;

    public AresConfig(ConfigFile file) {
        super(file);
    }

    @Override
    public void load() {
        this.serverName = this.read("settings.server-name", DataCodecs.STRING, () -> "test"); // default value is 'test'
        this.allowConnections = this.read("settings.allow-connections", DataCodecs.BOOLEAN, () -> true); // default value is true
    }
}
```
The DataCodecs class contains an extensive set of pre-made readers. 
If you wish to add your own, we recommend using the ```DataCodecs.value()```, ```DataCodecs.map()``` and ```DataCodecs.arrayOf()``` methods for simpler codecs.
For more complex objects, the DataCodecBuilder is the best alternative and can be initialized using the ```DataCodec.newBuilder()``` method.

## Built-in Messages
Extending the Config system, Ares also features a built-in system for parsing, caching and sending configurable messages.
It is extremely simple to use.
```java
ConfigFile file = new ConfigFile(this, "messages.yml");
MessageConfig messageConfig = ares.createMessageConfig(file);
messageConfig.load();
```
Ares will automatically read all messages in any provided YAML file as MiniMessage strings, construct Adventure components from them, and cache them.
To retrieve any message, simply use the ```messageConfig.message()``` method.

Below is an example of this system in use:
```java
this.messageConfig.message("general.player-kicked")
    .withPlaceholder("player", Component.text(player.getName())
    .withPlaceholder("reason", Component.text(reason))
    .broadcast();
```
```yaml
player-kicked: "<red><b>{player}</b> has been kicked from the server for {reason}.</red>
```

## Scoreboard API
The scoreboard framework is all built into Ares. You need to create your own ```BoardStyle``` to add your own lines, title and tab. Through this object, you can also customize settings such as the update interval(s) and scoreboard theme. 

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
Register your new BoardStyle:

```java

```
