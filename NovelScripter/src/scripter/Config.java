package scripter;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * スクリプターの各種設定（デフォルト値）
 * フォント、音のボリュームなどを調整します
 * 
 * 定数なのでこのままいじってもよし
 * あるいは設定画面を作っていじるのもよし
 * 
 * @author hide
 *
 */
public class Config {
	

	/****************************************
	 * テキストレイヤーで使用するフォントの設定
	 ****************************************/
	public static int TEXT_LAYER_FONT_COLOR = Color.RED;
	public static int TEXT_LAYER_FONT_SIZE = 30;
	public static Paint pTextLayerFont;
	public static Paint textLayerFont(){
		if(pTextLayerFont==null) pTextLayerFont = new Paint();
		pTextLayerFont.setColor(TEXT_LAYER_FONT_COLOR);
		pTextLayerFont.setTextSize(TEXT_LAYER_FONT_SIZE);
		return pTextLayerFont;
	}

	/***********************************************
	 * テキストレイヤーの枠を設定
	 * 透過情報はテキストレイヤーがpngのみに限る
	 ************************************************/
	public static int TEXT_LAYER_FRAME_ALPHA = 255;
	public static Paint textLayerFrame(){
		Paint p = new Paint();
		p.setAlpha(TEXT_LAYER_FRAME_ALPHA);
		return p;
	}

	/***************************************************
	 * その他テキストレイヤーにおける設定
	 ***************************************************/
	public static final int TEXT_LAYER_MAX_LEN = 30;	//	１行の最大文字数。超えると折り返す。	
	public static int TEXT_LAYER_SPEED = 150;			//	1文字の表示間隔[ms]。数値が少ないほうが早い。

	/**************************************************
	 * バックログレイヤの設定
	 */
	public static final int BACKLOG_LAYER_MAX_LEN = 32;	//	バックログレイヤの最大行数


	/**************************************************:
	 * 汎用白文字フォント
	 **************************************************/
	public static Paint whiteFont(){
		Paint p = new Paint();
		p.setColor(Color.WHITE);
		p.setTextSize(30);
		return p;
	}


	/**
	 * デバッグ用フォント設定
	 */
	public static Paint debugFont(){
		Paint p = new Paint();
		p.setColor(Color.GREEN);
		p.setTextSize(50);
		return p;
	}

	/**
	 * 汎用青
	 */
	public static Paint blue(){
		Paint p = new Paint();
		p.setAlpha(150);
		p.setColor(Color.rgb(0, 0, 200));
		return p;
	}


}
