package scripter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Layer {
	
	//	エフェクトの終了時に行う処理
	public interface OnFinishEffectListener{
		public void onFinishEffect();
	}
	
	//	エフェクトの基本クラス
	class Effect{
		
		void draw(Canvas c){}
		void release(){}
		
	}
	
	//	単純にフェードアウトして重ねるエフェクト
	class EffectFade extends Effect{
		
		
		@Override
		void draw(Canvas c){
			
			
		}
		
	}
	
	
	protected Bitmap bitmap;		//	メインの画像
	protected Bitmap bitmap2;		//	画面切り替え時に使用するバッファ
	protected int alpha1;			//	メインの透過度
	protected int alpha2;			//	切り替えの透過度
	
	
	private boolean bInit;			//	初期化確認
	protected int iWidth,iHeight;	//	キャンバスサイズ

	private int display;		//	０は消す、それ以外はつける
	
	private Effect mEffect;		//	エフェクト
	
	private OnFinishEffectListener mListener;	//	リスナー
	
	public Layer(){
		bInit = true;
		display = 0;
	}
	
	/*********************************
	 * 画像ファイルをセットする
	 * @param b
	 *********************************/
	public void setBitmap(Bitmap b){
		bitmap = b;
		bInit = true;
	}
	
	/*****************************************
	 * エフェクト付で画像ファイルをセットする
	 * 
	 * @param b 画像ファイル
	 * @param effect 効果番号
	 *****************************************/
	public void setBitmapEffect(Bitmap b,int effect,OnFinishEffectListener l){
		mListener = l;
		switch(effect){
		default:
			mEffect = new EffectFade();
			break;
		}
		
	}
	
	
	/*******************************************
	 * 描画する
	 * @param c
	 *******************************************/
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

	/************************************************************
	 * 表示する
	 * @return
	 *******************************************************/
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
