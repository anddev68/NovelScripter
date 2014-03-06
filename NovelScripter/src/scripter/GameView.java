package scripter;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;


/**************************************************
 * surfaceViewを継承したゲーム専用のViewです。
 * activityでインスタンスを生成してください。
 *
 **************************************************/
public abstract class GameView extends SurfaceView implements Runnable,Callback,
	GestureDetector.OnGestureListener,OnDoubleTapListener{

	public static final int DETECT_SLIDE = 30;	//	検出するスライド量
	
	public GameView(Context context) {
		super(context);
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		gesture = new GestureDetector(context, this);
		mContext = context;
	}

	//	タップ検出用
	GestureDetector gesture;	//	ジェスチャーデコーダ
	float oldX1,oldY1,oldX2,oldY2;	//	前回の位置
	
	//	描画用
	protected Bitmap mBuffer = null;						//	ダブルバッファリング用Bitmap
	public static Bitmap mSavedBuffer = null;;			//	Activity切り替え時に保存しておくbitmap
	
	//	スレッド用
	SurfaceHolder surfaceHolder;
	boolean loop;
	Thread thread = null;

	protected Context mContext;
	
	protected abstract void render(Canvas c);
	protected abstract void calc();
	protected void onLeftSlide(){}
	protected void onRightSlide(){}
	protected void onUpSlide(){}
	protected void onDownSlide(){}
	protected void onDown(){}
	protected void onDown(int x,int y){}
	protected void onTap(){}
	protected void onSingleTap(){}
	
	/********************************
	 * Assetからストリームを取得する
	 *******************************/
	protected InputStream getStream(String fileName) throws IOException{
		AssetManager asset = mContext.getAssets();
		return asset.open(fileName);
	}
	
	/***********************************
	 * AssetからBitmapを取得する
	 * @return 成功時：bitmap 失敗時：null
	 *************************************/
	protected Bitmap getBitmap(String fileName){
		try {
			return BitmapFactory.decodeStream(getStream(fileName));
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	
	

	@Override
	public void run() {
		while(loop){
			
			//	ここで描画処理
			Canvas canvas = surfaceHolder.lockCanvas();
			if(canvas==null) continue;
			
			//	ダブルバッファリングで書いてみる
			if( mBuffer!=null ){
				Canvas c2 = new Canvas(mBuffer);
			
				//	ここで描画を行う
				render(c2);
				
				
				canvas.drawBitmap(mBuffer, 0, 0, null);
			}
			
			surfaceHolder.unlockCanvasAndPost(canvas);
			
			//	ここでその他の処理
            calc();
		}
		
		
		
	}
	
	@Override
	public boolean onDoubleTap(MotionEvent arg0) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent arg0) {
		System.out.println("onDobleTapEvent");
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent arg0) {
		onSingleTap();
		System.out.println("onSingileTapConfirmed");
		return false;
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		onDown();
		onDown((int)arg0.getX(),(int)arg0.getY());
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		int rangeY = (int) (arg0.getRawY() - arg1.getRawY());
		int rangeX = (int) (arg0.getRawX() - arg1.getRawX());
		if(rangeY > DETECT_SLIDE){
			//	下方向にスライド
			onDownSlide();
		}else if(rangeY < DETECT_SLIDE*(-1)){
			//	上方向にスライド
			onUpSlide();
		}
		if( rangeX > DETECT_SLIDE){
			//	右方向にスライド
			onRightSlide();
		}else if(rangeY < DETECT_SLIDE*(-1)){
			//	左方向にスライド
			onRightSlide();
		}
			
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		
		return false;
	}



	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		if(mSavedBuffer==null)
			//	Activityの初回起動時は何もない画像で作成
			mBuffer = Bitmap.createBitmap(arg2, arg3, Bitmap.Config.ARGB_8888);
		else{
			//	保存しておいた画像を元に戻す
			mBuffer = mSavedBuffer;
			mSavedBuffer = null;
		}
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		thread = new Thread(this);
		thread.start();
		loop = true;	
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		//	スレッドを停止する
		loop = false;
		thread = null;
		
		//	Activity再開時用に現在の画像を保存する
		mSavedBuffer = mBuffer;
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent me){
		if(me.getPointerCount() > 1){	//	マルチタッチの時
			int id1 = me.getPointerId( 0 );
			int id2 = me.getPointerId( 1 );
			int index1 = me.findPointerIndex(id1);
			int index2 = me.findPointerIndex(id2);
			float y1 = me.getY(index1);
			float y2 = me.getY(index2);
			
			switch(me.getAction()){
			case MotionEvent.ACTION_MOVE:
				if( y1+40>oldY1&& y2+40>oldY2){
					//	2本とも下へ
					System.out.println("ダブル下スライド！");
				
					
				}else if( y1-40 < oldY1 && y2-40 < oldY2){
					//	2本とも上へ
					System.out.println("ダブル上スライド！");
				
				}
				
				break;
			}
			oldY1 = y1;
			oldY2 = y2;
			return true;
		}
		gesture.onTouchEvent(me);
		return true;
	}
	
	
	
	
	
	

}
