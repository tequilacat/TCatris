package org.tequilacat.tcatris.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  /**
   * stores parameters of certain game type
   */
  public static class GameDescriptor {
    private final String _gameClassName;
    private final String _label;
    private final String _gameParameters;

    public GameDescriptor(String gameClassName, String label, String gameParams) {
      _gameClassName = gameClassName;
      _label = label;
      _gameParameters = gameParams;
    }

    public String getGameClassName() {
      return _gameClassName;
    }

    public String getLabel() {
      return _label;
    }

    public String getGameParameters() {
      return _gameParameters;
    }

    public String getId() { return getGameClassName()+"/"+getLabel(); }

    @Override
    public String toString() {
      return getLabel();
    }
  }

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

      //Debug.print("Create game: class = "+gameClass+", '"+ gameName +"', -> "+gameDesc);
      game = (Tetris) Class.forName( descriptor.getGameClassName()).newInstance();
      game.setId(descriptor.getId());
      game.init(descriptor.getLabel(), descriptor.getGameParameters());

    } catch (Exception e) {
      // TODO process error when creating a game
      Debug.print("Error creating game: " + e);
    }

    return game;
  }


}
