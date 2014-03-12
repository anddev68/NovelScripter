package scripter;

import java.io.IOException;
import java.util.ArrayList;

import scripter.OptionLayer.ClickListener;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;

public class NovelView extends GameView implements TextLayer.FinishListener,TextParser.EventListener{

	//	テキストレイヤー
	TextLayer mTextLayer;

	//	背景レイヤー
	BackGroundLayer mBackGroundLayer;
	
	//	テキスト解析
	TextParser mTextParser;
	
	//	ゲーム変数
	GameVariable mVariable;

	//	人物レイヤー（とりあえず中央のみ）
	PersonLayer mPersonLayer;
	
	//	選択肢レイヤー
	OptionLayer mOptionLayer;
	
	//	スキップモード
	boolean bSkipMode;
	
	//	自動再生モード
	boolean bAutoMode;
	
	//	画面全消しフラグ・高速化
	boolean bScreenUpdate;
	
	//	読み終わった現在の行
	String mCurrentText;
	
	//	音用
	MediaPlayer mMediaPlayer = null;
	
	
	protected boolean bWait;
	
	
	public NovelView(Context context) {
		super(context);
		
		//	ゲーム変数初期化
		mVariable = new GameVariable();
		
		//	テキストレイヤーの初期化
		mTextLayer = new TextLayer();
		mTextLayer.setListener(this);
		mTextLayer.enable();
		
		//	背景レイヤーの初期化
		mBackGroundLayer = new BackGroundLayer();
		mBackGroundLayer.enable();
		
		//	人物レイヤーの初期化
		mPersonLayer = new PersonLayer();
		
		//	選択肢レイヤー
		mOptionLayer = new OptionLayer();
		
		
		//	テキスト解析クラスの初期化
		mTextParser = new TextParser();
		mTextParser.setEventListener(this);
		try {
			mTextParser.changeFile(getStream("start.sn"));
			mTextParser.next();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
		bScreenUpdate = true;
		
	}
	
	public void setVariable(GameVariable value){
		this.mVariable = value;
		//	画面の更新
		bScreenUpdate = true;
		//	テキストレイヤーの更新
		mVariable.iLineNum--;
		mTextParser.next();
		
	}
	
	public GameVariable getVariable(){
		return mVariable;
	}
	
	public void update(){
		bScreenUpdate = true;
		
	}
	
	public String getCurrentText(){
		return mCurrentText;
	}
	

	@Override
	public void render(Canvas c) {
		
		if(bScreenUpdate || bWait){
			bScreenUpdate = false;
			
			//	全消
			c.drawColor(Color.WHITE);
			
			//	背景
			if(mBackGroundLayer.isDisplay()) mBackGroundLayer.draw(c);
		
			//	人物レイヤー描画
			if(mPersonLayer.isDisplay()) mPersonLayer.draw(c);
			
			//	テキストレイヤー描画
			if(mTextLayer.isDisplay()) mTextLayer.draw(c);
			
			//	オプションレイヤー描画
			if(mOptionLayer.isDisplay()) mOptionLayer.draw(c);
			
			//	オートモード
			if( bAutoMode ) c.drawText("AUTO", 50, 50, Config.whiteFont());
		
		}else{
			//	テキストレイヤーのみ描画
			if(mTextLayer.isDisplay()) mTextLayer.draw(c);
		}
	}

	@Override
	protected void calc() {
		
		
	}

	@Override
	protected void onDown(){
		if(bAutoMode){	//	オートモード停止
			bAutoMode = false;
			update();
		}
		if(bSkipMode){
			
		}
		
	}
	
	@Override
	protected void onDown(int x,int y){
		if(mOptionLayer.isDisplay())
			mOptionLayer.onClick(x, y);
	}
	
	@Override
	protected void onSingleTap(){
		if(!bWait)
			//	テキストを進める
			mTextParser.next();
		System.out.println("onSingileTap");
	}
	
	
	@Override
	protected void onLeftSlide() {
		bAutoMode = true;
	}

	@Override
	protected void onRightSlide() {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	protected void onUpSlide() {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	protected void onDownSlide() {
		// TODO 自動生成されたメソッド・スタブ
		
	}




	@Override
	public void onFinish(String str) {
		mCurrentText = str;
		if(bAutoMode) mTextParser.next();
	}




	@Override
	public void setImageBackGround(String fileName) {
		mBackGroundLayer.setBitmap(getBitmap(fileName));
		update();
	}




	@Override
	public void setImageTextWindow(String fileName) {
		//	テキストレイヤーに画像をセットする
		mTextLayer.setBitmap(getBitmap(fileName));
		update();
	}




	@Override
	public void setTextTextWindow(String str) {
		//	テキスト解析クラスからテキストレイヤーに与える文字列を指定する
		mTextLayer.setText(str);
	}




	@Override
	public GameVariable requestVariable() {
		return this.mVariable;
	}

	@Override
	public void setImagePerson(String fileName) {
		mPersonLayer.setBitmap(getBitmap(fileName));
		mPersonLayer.enable();
		update();
	}

	@Override
	public void showOption(ArrayList<String> str) {
		//	選択肢レイヤーを有効にし、コールバックと選択肢を設定する
		mOptionLayer.enable();
		mOptionLayer.setOption(str, new ClickListener(){

			@Override
			public void onClick(String label) {
				mOptionLayer.disable();
				bWait = false;
				bScreenUpdate = true;
				System.out.println(label+"へ飛びます。");
				mTextParser.jumpLabel(label);
			}
		
		});
		
		bWait = true;	//	終わるまで処理を待つ
		bScreenUpdate = true;	//	画面更新
		
		
	}

	@Override
	public void playMp3(String fileName) {
		
		
		//	"mv"コマンドに対する処理
		try{
			mMediaPlayer = new MediaPlayer();
			AssetFileDescriptor afd = mContext.getAssets().openNonAssetFd(fileName);
		
			mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
		
			afd.close();
			mMediaPlayer.setOnPreparedListener(new OnPreparedListener(){

				@Override
				public void onPrepared(MediaPlayer arg0) {
					mMediaPlayer.start();
					
				}
            });
			mMediaPlayer.prepareAsync();
            
			
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener(){

				@Override
				public void onCompletion(MediaPlayer arg0) {
					mMediaPlayer.release();
					mMediaPlayer = null;
				}
				
			});
            
		}catch(Exception e){
			if(mMediaPlayer!=null){
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
		}
		
		
		
	}
	

	
	
	

}
