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
