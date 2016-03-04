package org.tequilacat.tcatris;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ViewFlipper;

import org.tequilacat.tcatris.core.GameList;
import org.tequilacat.tcatris.core.GameView;
import org.tequilacat.tcatris.core.Tetris;

import java.util.List;

public class MainActivity extends Activity {

  private ViewFlipper _viewFlipper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    _viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);

    // fill game list
    GameList.init();

    final List<GameList.GameDescriptor> gameTypes = GameList.instance().getGameDescriptors();
    final ArrayAdapter<GameList.GameDescriptor> adapter = new ArrayAdapter<>(this,
      android.R.layout.simple_list_item_1, gameTypes);

    ListView gameListView = (ListView) findViewById(R.id.lvMainOptionList);
    gameListView.setAdapter(adapter);
    gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Toast.makeText(GameSelectorActivity.this, gameTypes.get(position).getLabel(), Toast.LENGTH_SHORT).show();
        runGame(gameTypes.get(position));
      }
    });
  }


  private void runGame(GameList.GameDescriptor gameDescriptor) {
    //Intent intent = new Intent(this, GameActivity.class);
    //intent.putExtra(GameActivity.GAME_DESCRIPTOR, gameDescriptor.getId());
    //startActivity(intent);
/*
GameView gameView = (GameView) findViewById(R.id.gameView);
    String gameId = getIntent().getStringExtra(GAME_DESCRIPTOR);
    Tetris game = GameList.instance().createGame(GameList.instance().findDescriptor(gameId));
    gameView.setGame(game);
 */

    Tetris game = GameList.instance().createGame(gameDescriptor);
    GameView gameView = (GameView) findViewById(R.id.gameView);
    gameView.setGame(game);
    _viewFlipper.showNext();
  }
}
