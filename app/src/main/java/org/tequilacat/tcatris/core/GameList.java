package org.tequilacat.tcatris.core;

import android.content.res.Resources;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.tequilacat.tcatris.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reads types of games available in the application from JSon definitions
 */
public class GameList {
  public static List<GameDescriptor> readAvailableGameTypes(Resources resources) {
    JsonParser parser = new JsonParser();
    List<GameDescriptor> descriptors = new ArrayList<>();

    BufferedReader reader = null;

    try {
      reader = new BufferedReader(new InputStreamReader(resources.openRawResource(R.raw.game_types)));
      JsonArray gameTypeArray = parser.parse(reader).getAsJsonArray();
      Iterator<JsonElement> it = gameTypeArray.iterator();

      while (it.hasNext()) {
        JsonElement el = it.next();
        if(el instanceof  JsonObject) {
          descriptors.add(new GameDescriptor(el.getAsJsonObject()));
        }
        //
      }
    } finally {
      if(reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
        }
      }
    }

    return descriptors;
  }
}
