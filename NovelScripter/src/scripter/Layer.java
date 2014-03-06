package scripter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Layer {
	protected Bitmap bitmap;		//	メインの画像
	protected Bitmap bitmap2;	//	画面切り替え時に使用するバッファ

	private boolean bInit;			//	初期化確認
	protected int iWidth,iHeight;	//	キャンバスサイズ

	private int display;		//	０は消す、それ以外はつける
	
	public Layer(){
		bInit = true;
		display = 0;
	}
	
	public void setBitmap(Bitmap b){
		bitmap = b;
		bInit = true;
	}
	
	public void draw(Canvas c){
		iWidth = c.getWidth();
		iHeight = c.getHeight();
		if(bInit){
			bInit = false;
			init();
		}
	}
	
	public void update(){
		bInit = true;
	}
	
	/**********************************************************
	 * インスタンス生成後、初めてdrawが呼ばれた場合に実行します
	 * あるいは明示的にupdate()が呼ばれた場合に実行します
	 * 
	 * これはonCreate()でcanvasサイズが取得できないためです
	 * drawを必ずオーバーライドしてください
	 ************************************************************/
	public void init(){
		
	}

	
	public boolean isDisplay(){
		if(display>0) return true; return false;
	}
	
	public void enable(){
		display = 1;
	}
	
	public void disable(){
		display = 0;
	}
	
	
	/******************************************************************
	 * 指定した大きさに変更してbitmapを描画する関数
	 * @param c
	 * @param bitmap
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 *******************************************************************/
	protected void drawBitmap(Canvas c,Bitmap bitmap,int left,int top,int width,int height){
		drawBitmap(c,bitmap,left,top,width,height,null);
	}
	protected void drawBitmap(Canvas c,Bitmap bitmap,int left,int top,int width,int height,Paint p){
		if(bitmap!=null){
			if( bitmap.getWidth()!=width || bitmap.getHeight() !=height )
				bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
			c.drawBitmap(bitmap, left, top, p);
		}
		
	}
}
