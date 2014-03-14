package scripter;

import android.graphics.Canvas;


/*********************************************
 * 人物レイヤー
 *********************************************/
public class PersonLayer extends Layer{
	
	public static int LEFT = 1;
	public static int RIGHT = 2;
	public static int CENTER = 3;

	//	どの位置用に初期化するか
	private int pos;
	
	//	座標
	private int top,left,width,height;

	public PersonLayer(int pos){
		super();
		this.pos = pos;
	}

	@Override
	public void draw(Canvas c){
		super.draw(c);
		
		this.drawBitmap(c, bitmap, left, top, width, height);
	}

	@Override
	public void init(){
		top = (int) (iHeight* 0.2f);
		left = (int) (iWidth * 0.2f);
		width = (int) (iWidth * 0.6f);
		height = (int) (iHeight * 0.8f);
	}
}
