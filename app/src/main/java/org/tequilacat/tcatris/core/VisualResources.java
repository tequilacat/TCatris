package org.tequilacat.tcatris.core;

import android.content.res.Resources;
import android.graphics.Color;

import org.tequilacat.tcatris.R;

/**
 * Created by avo on 11.03.2016.
 */
public class VisualResources {
  public static VisualResources Defaults;

  public final int HEADER_FONT_SIZE;

  public final int BUTTONAREA_HEIGHT;
  public final int MARGIN_SIZE;

  public final int SCREEN_BG_COLOR;

  public final int FIELD_BG_COLOR;
  public final int FIELD_LINE_COLOR;

  public final int SCORE_BG_COLOR;
  public final int SCORE_TEXT_COLOR;
  public final int SCOREBAR_COLOR;
  public final int SCOREBAR_WIN_COLOR;

  public final int DARKSHADOW_COLOR;
  public final int LIGHTSHADOW_COLOR;

  public final int GLYPH_FILL_COLOR;
  public final int GLYPH_STROKE_COLOR;

  public final int DYN_SHAPE_STROKE_VALID;
  public final int DYN_SHAPE_STROKE_INVALID;

  public VisualResources(Resources bundle) {
    HEADER_FONT_SIZE = bundle.getDimensionPixelSize(R.dimen.gameinfo_font_size);

    BUTTONAREA_HEIGHT = bundle.getDimensionPixelSize(R.dimen.buttonarea_height);
    MARGIN_SIZE = bundle.getDimensionPixelSize(R.dimen.margin_size);

    SCREEN_BG_COLOR = bundle.getColor(R.color.screenBgColor);
    FIELD_BG_COLOR = bundle.getColor(R.color.fieldBgColor);
    FIELD_LINE_COLOR = bundle.getColor(R.color.fieldLineColor);
    SCORE_BG_COLOR = bundle.getColor(R.color.scoreBgColor);
    SCORE_TEXT_COLOR = bundle.getColor(R.color.scoreTextColor);
    DARKSHADOW_COLOR = bundle.getColor(R.color.darkShadow);
    LIGHTSHADOW_COLOR = bundle.getColor(R.color.lightShadow);
    GLYPH_FILL_COLOR = bundle.getColor(R.color.glyphFill);
    GLYPH_STROKE_COLOR = bundle.getColor(R.color.glyphStroke);
    DYN_SHAPE_STROKE_VALID = bundle.getColor(R.color.validDynaShapeStroke);
    DYN_SHAPE_STROKE_INVALID = bundle.getColor(R.color.invalidDynaShapeStroke);
    SCOREBAR_COLOR = bundle.getColor(R.color.scoreBarColor);
    SCOREBAR_WIN_COLOR = bundle.getColor(R.color.scoreBarWinColor);
  }
}
