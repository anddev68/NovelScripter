package scripter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {
	
	public interface EventListener{
		public void setImageBackGround(String fileName);
		public void setImageTextWindow(String fileName);
		public void setImagePerson(String fileName);
		public void setTextTextWindow(String str);
		
		public void showOption(ArrayList<String> str);
		public GameVariable requestVariable();
	}
	
	ArrayList<String> mLine;
	EventListener mListener;
	
	
	public TextParser(){
		mLine = new ArrayList<String>();
	}
	
	public void setEventListener(EventListener listener){
		mListener = listener;
	}
	
	/*****************************************************************
	 * シナリオファイルの変更
	 * シナリオファイルを変更します
	 * 
	 * @param is テキストデータのストリーム
	 *****************************************************************/
	public void changeFile(InputStream is){
		mLine.clear();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = br.readLine()) != null){
				mLine.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/***********************************************************
	 * 指定のラベルに飛びます。選択肢が選ばれた時に使用します。
	 * 現在の行から後ろに検索します。前には検索しません。
	 * （2/27訂正）
	 * 前から検索します。不都合の場合は訂正してください。
	 * 存在しない場合には警告するだけで何も行いません。
	 **********************************************************/
	public void jumpLabel(String labelName){
		int start = 0;	//	iLineNumに変更すれば現在の番号から開始します
		for(int i=start; i<mLine.size(); i++){
			if(mLine.get(i).equals(labelName)){
				mListener.requestVariable().iLineNum = i;
				return;
			}
		}
		System.out.println("["+labelName+"]が見つかりませんでした。");
	}
	
	/******************************************************************
	 * 次の行へ
	 ******************************************************************/
	public void next(){
		boolean loop = true;
		ArrayList<String> strs = null;	//	コマンド解析用
	
		while(loop){
			if(mLine.get(mListener.requestVariable().iLineNum).equals("")) 
				mListener.requestVariable().iLineNum++;	//	空行無視
			else if(mLine.get(mListener.requestVariable().iLineNum).charAt(0)==';') 
				mListener.requestVariable().iLineNum++;		//	コメント文無視
			else if(mLine.get(mListener.requestVariable().iLineNum).charAt(0)=='*') 
				mListener.requestVariable().iLineNum++;			//	ラベル無視
			else if((strs=parseCommand())!=null){	//	各コマンドの処理
				if(strs.get(0).equals("setwindow")){	//	テキストウインドウの設定
					mListener.setImageTextWindow(strs.get(1));
				}else if(strs.get(0).equals("bg")){	//	背景
					mListener.setImageBackGround(strs.get(1));
					if(!strs.get(2).equals("1")) return;	//	背景切り替えの処理待ちがいる場合は抜ける
				}else if(strs.get(0).equals("select")){	//	選択肢を開く処理
					mListener.showOption(strs);				//	処理待ち
					return;
				}else if(strs.get(0).equals("goto")){
					jumpLabel(strs.get(1));				//	指定のラベルに飛ぶ
				}else if(strs.get(0).equals("mov")){	//	変数を代入する場合
					setValue(strs);						//	変数のセット
				}else if(strs.get(0).equals("add")){	//	変数に値を追加する
					addValue(strs);
				}else if(strs.get(0).equals("ld")){	//	人物レイヤーにセット
					mListener.setImagePerson(strs.get(2));
				}
				mListener.requestVariable().iLineNum++;
			}else{		//	本文の処理
				//	改行待ち機能は今のところ実装していません
				mListener.setTextTextWindow(replaceVariable(mLine.get(mListener.requestVariable().iLineNum)));
				mListener.requestVariable().iLineNum++;
				loop = false;
			}	//	else end
		}	
		
		
		
		
	}
	
	/***********************************************************************
	 * コマンド解析の結果、変数の代入を行います。
	 * 文字列かどうかは自動判定にしてあります。
	 * 将来的にはダブルクオートに対応したいと思います。
	 **********************************************************************/
	private void setValue(ArrayList<String> strs){
		if(strs.get(1).charAt(0)=='%'){
			int num = Integer.parseInt(strs.get(2));
			mListener.requestVariable().setValue(strs.get(1),  num);
		
		}else
			mListener.requestVariable().setValue(strs.get(1), strs.get(2) );	
	}
	
	/**********************************************************************
	 * コマンド解析の結果、変数に値を足します。
	 **********************************************************************/
	private void addValue(ArrayList<String> strs){
		if(strs.get(1).charAt(0)=='%')
			mListener.requestVariable().addValue(strs.get(1), Integer.parseInt(strs.get(2)) );
		else
			mListener.requestVariable().addValue(strs.get(1), strs.get(2) );	
	}
	
	/**********************************************************************
	 * 文章解析を行い、変数を置き換えたものを返します。
	 * 今のところは%xxx、整数のみ対応しています。
	 **********************************************************************/
	private String replaceVariable(String str){
		//	作業用バッファを用意する
		String buffer = new String(str);
		
		//	正規表現で整数変数を示す%xxx部分を抜き出します。
		String regex = "%\\d+";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		
		//	%xxxが抜き出せたら、それぞれに値を代入していきます。
		while( m.find() ){
			//	マッチした文字列（例：%0）を取得
			String xxx = m.group();
			//	値を取り出す
			String s = mListener.requestVariable().getValue(xxx);
			//	置き換える
			buffer = buffer.replaceFirst(xxx, s);
		}
		return buffer;
	}
	
	
	/*********************************************************************
	 * 現在の行をコマンドであると仮定し、コマンド名と引数を取得します。
	 * @return array コマンド名と1以上の引数を配列として返します。
	 * @return null コマンドとして解析できなかった場合に帰ります
	 *********************************************************************/
	private ArrayList<String> parseCommand(){
		try{
			//	現在の行
			String cur = mLine.get(mListener.requestVariable().iLineNum);
			//	空白の位置
			int ptr = cur.indexOf(' ');
			//	空白までがコマンド名
			String name = cur.substring(0, ptr);
			//	引数を配列で取得
			String[] args = cur.substring(ptr+1).split(",");
			//	リストで返す
			ArrayList<String> str = new ArrayList<String>();
			str.add(name);
			for(int i=0; i<args.length; i++)
				str.add(args[i]);
			return str;
		}catch(Exception e){
			return null;
		}
	}
	
	
	
	
	


}
