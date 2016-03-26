package org.tequilacat.tcatris.core;

import com.google.gson.JsonObject;

/**
 * stores parameters of certain game type
 */
public class GameDescriptor {
  private final String _gameClassName;
  private final String _label;
  private final JsonObject _gameParameters;

  public GameDescriptor(JsonObject gameParams) {
    _gameParameters = gameParams;
    _gameClassName = GameConstants.GAME_IMPL_PACKAGE + "."
        + _gameParameters.get(GameConstants.JSON_GAMECLASS).getAsString();
    _label = _gameParameters.get(GameConstants.JSON_GAMELABEL).getAsString();
  }

  private String getGameClassName() {
    return _gameClassName;
  }

  public String getLabel() {
    return _label;
  }

  public JsonObject getGameParameters() {
    return _gameParameters;
  }

  public String getId() {
    return getGameClassName() + "/" + getLabel();
  }

  @Override
  public String toString() {
    return getLabel();
  }

  /**
   * creates new game
   * @return new instance of game for this descriptor
   */
  public ABrickGame createGame() {
    ABrickGame game = null;

    try {
      Class<?> gameClass = Class.forName(getGameClassName());
      // call constructor with this descriptor as a parameter
      game = (ABrickGame)gameClass.getDeclaredConstructor(GameDescriptor.class)
          .newInstance(this);
    } catch (Exception e) {
      // TODO process error when creating a game
      Debug.print("Error creating game: " + e);
    }

    return game;
  }
}
