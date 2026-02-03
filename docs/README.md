# Ares
Ares is a plugin library designed to provide developers with easy access to multiple frameworks and utilities.
It is specifically developed to be compatible with Paper with no guarantees for Bukkit, Spigot or Folia compatibility.

### Features
* Exclusively adapted for Paper (e.g. use of Adventure)
* Automatic YAML config parsing
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
Ares is a standalone plugin. You do not need to initialize it in order to use it. Simply put it in your server's /plugins folder and start the server.
To access the API:
```java
Ares ares = AresPlugin.api();
```

## Custom Configs & Messages
Ares has an integrated configuration system that can deserialize your files with ease. To create a config:
```java
ConfigFile file = new ConfigFile(this, "config");
ExampleConfig config = ares.createConfig(file, ExampleConfig.class);
```
This will automatically create and load the configuration. If you wish to add a custom deserializer that Ares does not support by default, it's very easy:
```java
this.ares.registerConfigAdapter(Tag.class, new ConfigTypeAdapter<>(Tag::serialize, object -> {
    if(object instanceof ConfigurationSection section) {
        Tag tag = new Tag(section);
        return Optional.of(tag);
    }
    
    return Optional.empty();
}));
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
