package org.tequilacat.tcatris.core;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by avo on 24.02.2016.
 */
public class GameList {
  private static final String GameDefinitions =
    "game=ClassicGame:Tetris:10:15:2:4\n" +
    "game=Columns:Xixit:5:12:1:3:xixit\n" +
    "game=Columns:Columns:8:15:3:1:columns\n" +
    "game=Columns:Trix:8:15:1:3:trix\n";
  private static GameList _instance;

  private List<GameDescriptor> _descriptors = new ArrayList<>();

  public static GameList instance() {
    return _instance;
  }

  public static void init() {
    _instance = new GameList();
  }

  private GameList() {
    //game=flassname:label:w:h:nw:nh:optionalParams

    try {
      Reader reader = new StringReader(GameDefinitions);

      //new InputStreamReader(getClass().getResourceAsStream("/games.txt"));
      StringBuilder stb = new StringBuilder();
      //char[] chunk = new char[1000];
      //int readCount;
      //while((readCount = reader.read(chunk)) > 0){
      int inChar;
      while ((inChar = reader.read()) != -1) {
        if (inChar == '\n') {
          String mayBeDesc = stb.toString();
          if (mayBeDesc.startsWith("game=")) {
            // in form:
            // game=flat.ClassicGame:Tetris:10:15:2:4

            // sep1 after classname
            int sep1 = mayBeDesc.indexOf(':', 5), sep2 = mayBeDesc.indexOf(':', sep1 + 1);
            _descriptors.add(new GameDescriptor("org.tequilacat.tcatris.games." + mayBeDesc.substring(5, sep1),
              mayBeDesc.substring(sep1 + 1, sep2), mayBeDesc.substring(sep2 + 1)));

            //Debug.print("ADD Game: '"+ _descriptors.elementAt() +"'");
          }
          stb.setLength(0);
        } else {
          stb.append((char) inChar);
        }
      }

    } catch (Exception e) {
      Debug.print("Error reading game definitions: " + e);
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
