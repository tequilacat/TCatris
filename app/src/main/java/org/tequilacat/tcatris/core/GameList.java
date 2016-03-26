package org.tequilacat.tcatris.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads types of games available in the application from JSon definitions
 */
public class GameList {

  private static final String[] GameDefinitions = new String[]{
      "{class='ClassicGame', label='Tetris', dim = {width=10,height=15}}",
      "{class='ColorShiftGame', label='Xixit', dim={width=5,height=12}, gameType=SHIFT_VERTICALLY}",
      "{class='ColorShiftGame', label='HorizColumns', dim={width=8,height=15}, gameType=SHIFT_HORIZONTALLY}",
      "{class='ColorShiftGame', label='Trix', dim={width=8,height=15}, gameType=ROTATE}",
  };

  public static List<GameDescriptor> readAvailableGameTypes() {
    List<GameDescriptor> descriptors = new ArrayList<>();
    //Gson gson = new Gson();
    JsonParser parser = new JsonParser();

    for (String gameDescriptor : GameDefinitions) {
      JsonObject jsonObj = parser.parse(gameDescriptor).getAsJsonObject();
      descriptors.add(new GameDescriptor(jsonObj));
    }

    return descriptors;
  }
}
