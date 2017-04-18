# Settlers of Catan [![Build Status](https://travis-ci.com/keananf/SettlersOfCatan.svg?token=iWpNDxUwjxU9xS4Np7Kw&branch=master)](https://travis-ci.com/keananf/SettlersOfCatan)
Settlers of Catan Implementation in Java using LibGDX

## Building
### Linux and macOS
To build and start the game, navigate to the project directory and run the following command:

`./gradlew build`

## Hosting a Game
Clicking "Start Game" will bring up the menu for hosting new games.

From this menu the name of the human player or one of the AI players can be set.

If "Play as AI" is selected then the difficulty level of the AI can be selected.

The number of opposing AI players can be set as well as the difficulty of the AI opponents.

Clicking start will create a new game with the selected settings.
### Single Player against AI
To play a single player game against the AI, set the value of "Number of AI opponents" to 3.

You can then select the difficulty level of the three AI players.
### AI Only Game
To play a game with only the AI simply select the "Play as AI" option and select a difficulty level for the AI.
### Host a Game Against Network Players
To host a multiplayer game with multiple human players, set the value of "Number of AI opponents" below three.

The game will then wait for other players to connect until there are a total of four players connected.

Other players connecting to the server must connect using the ip address of the host machine.

## Joining a Game
Clicking "Join remote game" will bring up the menu for joining another server.

From this menu the name of the human or AI player joining the server can be set.

If an AI player is selected, the difficulty level of the AI can be selected.

To connect to a server, replace the text reading "Host" with the ip of the target server.

Clicking start will cause the game to attempt to join a remote game with the selected settings.





