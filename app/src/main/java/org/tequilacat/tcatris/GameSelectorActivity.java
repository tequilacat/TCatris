package org.tequilacat.tcatris;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.tequilacat.tcatris.core.GameList;

import java.util.List;

public class GameSelectorActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_gameselector);

    GameList gameList = new GameList();
    final List<GameList.GameDescriptor> gameTypes = gameList.getGameDescriptors();
    final ArrayAdapter<GameList.GameDescriptor> adapter = new ArrayAdapter<>(this,
      android.R.layout.simple_list_item_1, gameTypes);

    ListView gameListView = (ListView) findViewById(R.id.lvMainOptionList);
    gameListView.setAdapter(adapter);
    gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(GameSelectorActivity.this, gameTypes.get(position).getLabel(), Toast.LENGTH_SHORT).show();
      }
    });

    //
    //TetrisCanvas gameView = new TetrisCanvas(this, gameList);
    //setContentView(gameView);
  }

}
