# Java Chess Game

A real-time multiplayer chess game built with Java and JavaFX.

## Features

- Real-time multiplayer gameplay
- Simple game code-based connection system
- Standard chess rules implementation
- Clean and intuitive user interface

## Requirements

- Java 17 or higher
- Maven

## Building and Running

1. Clone the repository:
```bash
git clone <repository-url>
cd java-chess-project
```

2. Build the project:
```bash
mvn clean package
```

3. Start the server:
```bash
java -cp target/java-chess-1.0-SNAPSHOT.jar com.chess.network.ChessServer
```

4. Start the client application:
```bash
java -cp target/java-chess-1.0-SNAPSHOT.jar com.chess.Main
```

## How to Play

1. Start the server on one machine
2. Start two instances of the client application on different machines
3. On the first client:
   - Click "New Game" to create a new game
   - Share the game code with the second player
4. On the second client:
   - Enter the game code in the text field
   - Click "Join Game" to connect to the game
5. Play chess!

## Game Controls

- Click on a piece to select it
- Click on a valid destination square to move the piece
- The game will automatically switch turns between players

## Project Structure

- `src/main/java/com/chess/`
  - `controllers/` - UI controllers
  - `models/` - Game logic and data models
  - `network/` - Network communication code
  - `Main.java` - Application entry point
- `src/main/resources/`
  - `com/chess/views/` - FXML UI files

## License

This project is licensed under the MIT License - see the LICENSE file for details. 