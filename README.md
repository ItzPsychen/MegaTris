## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

MegaTris sources are organized by responsibility:

- `src/megatris/ui`: Swing entry point and game presentation
- `src/megatris/game`: game-state model
- `src/megatris/ai`: computer-player logic

Compile and run with:

```text
javac -d bin src\megatris\ai\*.java src\megatris\game\*.java src\megatris\ui\*.java
java -cp bin megatris.ui.MegaTris
```

To build the game and create or refresh `MegaTris.lnk` on the Windows desktop, run the VS Code task `Build MegaTris desktop app` with `Ctrl+Shift+B`. The task converts `src/megatris/images/icon.jpeg` into a 256x256 `bin/MegaTris-icon.ico` for the shortcut. You can then launch the game by opening that desktop shortcut.

The task requires a JDK with both `javac.exe` and `javaw.exe` available on `PATH`.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).
