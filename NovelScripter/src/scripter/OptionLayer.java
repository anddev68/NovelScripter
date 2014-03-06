package scripter;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Rect;

public class OptionLayer extends Layer{
	
	public interface ClickListener{
		/***********************************
		 * ボタンがクリックされたときの処理
		 * @param String ラベル名
		 ************************************/
		public void onClick(String label);	
	}
	
	class Option{
		public Rect r;			//	選択肢四方の情報
		public String text;		//	テキスト
		public String label;		//	飛ぶラベル
	}
	
	Option option[];
	
	private ClickListener mListener;
	private ArrayList<String> mArray;	//	解析データ
	
	
	public OptionLayer(){
		super();
	}
	
	public void setListener(ClickListener l){
		mListener = l;
	}
	
	
	@Override
	public void draw(Canvas c){
		super.draw(c);
		
		//	描画
		if(option!=null){
			for(int i=0; i<option.length; i++){
				c.drawRect(option[i].r, Config.blue());
				c.drawText(option[i].text, option[i].r.left+20, option[i].r.top+20, Config.textLayerFont());

			}
		
		}
		
		
	}
	
	@Override
	public void init(){
		initOption2();
	}
	
	/***************************************
	 * 選択肢を作成します
	 ****************************************/
	public void setOption(ArrayList<String> strs,ClickListener l){
		//	選択肢が2つなのか3つなのか判別
		//	とりあえず2で作成します
		mArray = strs;
		
		//	コールバックをセット
		mListener = l;
		
		update();
	}
	
	/*******************************************
	 * 選択肢2個で作成
	 *******************************************/
	public void initOption2(){
		int top = (int) (iHeight * 0.2f);
		int left = (int) (iWidth * 0.2f);
		int right = (int) (iWidth * 0.8f);
		int height = (int) (iHeight * 0.2f);
		int margin = (int) (iWidth * 0.2f);
		
		option = new Option[2];
		for(int i=0; i<2; i++){
			option[i] = new Option();
		}
		option[0].r = new Rect(left,top,right,top+height);
		option[1].r = new Rect(left,top+height+margin,right,top+height*2+margin);
		option[0].text = mArray.get(1); 
		option[1].text = mArray.get(3);
		option[0].label = mArray.get(2);
		option[1].label = mArray.get(4);
	}
	
	/*******************************************
	 * 選択肢3個で作成
	 * draw内initで呼んでください。画面情報が必要になります。
	 *******************************************/
	public void initOption3(){
		int top = (int) (iHeight * 0.1f);
		int left = (int) (iWidth * 0.1f);
		int margin = (int) (iWidth * 0.1f);
		
		
		option = new Option[3];
		for(int i=0; i<3; i++){
			option[i] = new Option();
		}
		option[0].r = new Rect(0,0,0,0);
		option[1].r = new Rect(0,0,0,0);
		option[2].r = new Rect(0,0,0,0);
		
		
		
	}
	
	
	
	/****************************************************
	 * クリックされたとき、どのボタンが押されたか判定する
	 ****************************************************/
	public void onClick(int x,int y){
		for(int i=0; i<option.length; i++){
			if(option[i].r.contains(x, y)){
				//	コールバック実行
				if(mListener!=null) mListener.onClick(option[i].label);
			}
		}
	}
	
}
