# Crane
Crane is a set of extensions to Mojang's official Minecraft obfuscation mappings, providing things such as parameter names and Javadocs not supplied by the original mappings set.

________
Crane is built on the same foundation as Yarn, with gui-based mappings via [Enigma](https://github.com/FabricMC/Enigma).

### Contributing

Run `gradlew run` to launch Enigma, edit and save the mappings, and open a pull request.

### Publications

Artifacts of the mappings are available on the [architectury maven](https://maven.architectury.dev/)
 at `dev.architectury:crane:MCVERSION+build.BUILD`.

A standalone tiny file is available with the tiny classifier, otherwise the jar is an archive with `crane.tiny`.

### Format

The mappings are serialized in tinyv2 with one namespace `named`.

### Gradle Build Plugin

[Crane Plugin](https://github.com/architectury/crane-plugin) is responsible for doing all the work of downloading jars and libraries.
