package org.tequilacat.tcatris.core;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by avo on 24.02.2016.
 */
public class GameList {

  private static final String[] GameDefinitions = new String[]{
      "ClassicGame:Tetris:10:15:",
      "Columns:Xixit:5:12:xixit",
      "Columns:Columns:8:15:columns",
      "Columns:Trix:8:15:trix"
  };

  private static GameList _instance;

  private List<GameDescriptor> _descriptors = new ArrayList<>();

  public static GameList instance() {
    return _instance;
  }

  public static void init() {
    _instance = new GameList();
  }

  private GameList() {
    // classname:label:w:h:optionalParams

    for(String mayBeDesc: GameDefinitions) {
      int sep1 = mayBeDesc.indexOf(':'), sep2 = mayBeDesc.indexOf(':', sep1 + 1);
      _descriptors.add(new GameDescriptor("org.tequilacat.tcatris.games." + mayBeDesc.substring(0, sep1),
          mayBeDesc.substring(sep1 + 1, sep2), mayBeDesc.substring(sep2 + 1)));
    }
  }

  public GameDescriptor findDescriptor(String gameId) {
    GameDescriptor found = null;

    for(GameDescriptor descriptor : _descriptors){
      if(descriptor.getId().equals(gameId)) {
        found = descriptor;
        break;
      }
    }

    return found;
  }

  /**
   *
   * @return available game types
   */
  public List<GameDescriptor> getGameDescriptors() {
    return _descriptors;
  }

  /**
   * Creates game of given type, inits from last saved props
   * */
  public Tetris createGame(GameDescriptor descriptor) {
    Tetris game = null;

    try {
      Class<?> gameClass = Class.forName(descriptor.getGameClassName());
      game = (Tetris)gameClass.getDeclaredConstructor(GameDescriptor.class).newInstance(descriptor);
    } catch (Exception e) {
      // TODO process error when creating a game
      Debug.print("Error creating game: " + e);
    }

    return game;
  }


}
