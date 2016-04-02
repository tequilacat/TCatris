package org.tequilacat.tcatris.core;

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

import org.tequilacat.tcatris.R;

/**
 * Styles colors and sizes read from app resources
 */
public class VisualResources {
  public static VisualResources Defaults;

  public final int HEADER_FONT_SIZE;

  public final int BUTTONAREA_HEIGHT;
  public final int MARGIN_SIZE;

  public final int SCREEN_BG_COLOR;

  public final int FIELD_BG_COLOR;
  public final int FIELD_LINE_COLOR;

  /** background of score area */
  public final int SCORE_BG_COLOR;
  /** Current bar color used as fallback if 9-patch is too wide for current score */
  public final int SCOREBAR_CUR_COLOR;
  /** top scores */
  public final int SCORE_TOP_TEXTCOLOR;
  /** current scores drawn over bar */
  public final int SCORE_CUR_TEXTCOLOR;
  /** current scores drawn over background when there's no bar */
  public final int SCORE_CUR_TEXTCOLOR_ALONE;

  public final Drawable SCOREBAR_CURRENT_DRAWABLE;
  public final Drawable SCOREBAR_TOPSCORE_DRAWABLE;
  public final int SCOREBAR_MIN_CURRENT_WIDTH;

  public final int DARKSHADOW_COLOR;
  public final int LIGHTSHADOW_COLOR;

  public final int GLYPH_FILL_COLOR;
  public final int GLYPH_STROKE_COLOR;

  public final int DYN_SHAPE_STROKE_VALID;
  public final int DYN_SHAPE_STROKE_INVALID;

  public final float DYN_SHAPE_STROKE_WIDTH;

  public final float FALLEN_SHADOW_STROKE_WIDTH;
  public final float ROUNDED_FRAME_STROKE_WIDTH;
  public final float ROUNDED_FRAME_RADIUS;
  public final float ROUNDED_FRAME_PADDING;
  public final float ROUNDED_FRAME_MARGIN;

  public VisualResources(Resources bundle) {
    HEADER_FONT_SIZE = bundle.getDimensionPixelSize(R.dimen.canvas_score_font_size);

    BUTTONAREA_HEIGHT = bundle.getDimensionPixelSize(R.dimen.buttonarea_height);
    MARGIN_SIZE = bundle.getDimensionPixelSize(R.dimen.margin_size);

    DYN_SHAPE_STROKE_WIDTH = bundle.getDimension(R.dimen.dynaShapeStrokeWidth);
    FALLEN_SHADOW_STROKE_WIDTH = bundle.getDimension(R.dimen.fallenShadowStrokeWidth);
    ROUNDED_FRAME_STROKE_WIDTH = bundle.getDimension(R.dimen.uiRoundedFrameStrokeWidth);
    ROUNDED_FRAME_RADIUS = bundle.getDimension(R.dimen.uiRoundedFrameRadius);
    ROUNDED_FRAME_PADDING = bundle.getDimension(R.dimen.uiRoundedFramePadding);
    ROUNDED_FRAME_MARGIN = bundle.getDimension(R.dimen.uiRoundedFrameMargin);

    SCREEN_BG_COLOR = bundle.getColor(R.color.screenBgColor);
    FIELD_BG_COLOR = bundle.getColor(R.color.fieldBgColor);
    FIELD_LINE_COLOR = bundle.getColor(R.color.fieldLineColor);

    DARKSHADOW_COLOR = bundle.getColor(R.color.darkShadow);
    LIGHTSHADOW_COLOR = bundle.getColor(R.color.lightShadow);
    GLYPH_FILL_COLOR = bundle.getColor(R.color.glyphFill);
    GLYPH_STROKE_COLOR = bundle.getColor(R.color.glyphStroke);
    DYN_SHAPE_STROKE_VALID = bundle.getColor(R.color.validDynaShapeStroke);
    DYN_SHAPE_STROKE_INVALID = bundle.getColor(R.color.invalidDynaShapeStroke);


    SCORE_BG_COLOR = bundle.getColor(R.color.scoreBgColor);
    SCOREBAR_CUR_COLOR = bundle.getColor(R.color.scoreBarCurColor);
    SCORE_TOP_TEXTCOLOR= bundle.getColor(R.color.scoreTopTextColor);
    SCORE_CUR_TEXTCOLOR= bundle.getColor(R.color.scoreCurTextColor);
    SCORE_CUR_TEXTCOLOR_ALONE= bundle.getColor(R.color.scoreCurTextColorAlone);

    SCOREBAR_CURRENT_DRAWABLE = bundle.getDrawable(R.drawable.scorebar_current);
    SCOREBAR_TOPSCORE_DRAWABLE = bundle.getDrawable(R.drawable.scorebar_topscore);

    Rect r = new Rect();
    SCOREBAR_CURRENT_DRAWABLE.getPadding(r);
    SCOREBAR_MIN_CURRENT_WIDTH = r.left + r.right;
  }
}
