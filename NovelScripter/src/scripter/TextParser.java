package scripter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***************************************
 * シナリオ解析クラスです。
 * 
 * 各関数は以下の命名規則にしたがっています
 * 
 * 。
 * parseComXX:コマンドをコマンド名・引数に分離する
 * comXXXX:それぞれのコマンドを発見した時の処理
 *
 ***********************************************/
public class TextParser {
	
	/*******************************************************
	 * テキストレイヤーで解析されたコマンドの処理を行う
	 ******************************************************/
	public interface EventListener{
		
		//	背景のセット
		public void setImageBackGround(String fileName);
		//	テキストウインドウに画像をセット
		public void setImageTextWindow(String fileName);
		//	人物レイヤーのセット
		public void setImagePersonLeft(String fileName,int effect);
		public void setImagePersonCenter(String fileName,int effect);
		public void setImagePersonRight(String fileName,int effect);
		//	人物レイヤーの削除
		public void clearImagePersonLeft(int effect);
		public void clearImagePersonCenter(int effect);
		public void clearImagePersonRight(int effect);
		
		//	テキストウインドウにテキストのセット
		public void setTextTextWindow(String str);
		
		//	音楽再生関連
		public void playMp3(String fileName);
		public void playWave(String fileName);
		public void playLoopWave(String fileName);
		public void stopMp3();
		public void stopWave();
		
		
		public void showOption(ArrayList<String> str);
		
		//	ゲーム内変数のインスタンスをリクエストする
		public GameVariable requestVariable();
	}
	
	ArrayList<String> mLine;
	EventListener mListener;
	
	//	ユーザが入力するまで次の行へ進めないようにする
	//	このフラグを入れるとコマンド処理後、自動で次の行へ進まなくなる
	//	選択肢、コールバック待ち等の場合はtrueにする
	boolean bWaitFlag;
	
	
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

	/******************************************************************
	 * 指定した行に飛ばします。
	 * 
	 * ①ラベルによる指定
	 * ②行番号による指定
	 *****************************************************************/
	public void jump(String labelName){
		comGoto(labelName);
	}
	
	
	/******************************************************************
	 * 次の行へ行きます
	 * 
	 * 1:isIgnore()により無視すべき行かどうか判別
	 * 1-1:もし無視すべきなら次の行へ進める
	 * 
	 ******************************************************************/
	public void next(){
		if ( isIgnore() ){	//	無視すべき行かどうかの判別
			mListener.requestVariable().iLineNum++;
			next();
		}else if( is2Byte() ){	//	本文のとき
			String str = replaceVariable(mLine.get(mListener.requestVariable().iLineNum));
			mListener.setTextTextWindow(str);
			
			mListener.requestVariable().iLineNum++;
		}else{	//	コマンド行
			parseCom1();
			parseCom2();
			if(!bWaitFlag){	//	待ちがなければ次の行へ
				mListener.requestVariable().iLineNum++;
				next();
			}
			bWaitFlag = false;
		}		
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
	

	
	
	/************************************************************************
	 * 空行とコメントとラベルであるかどうかを判定。
	 * 無視の場合はtrueを返します。
	 * 
	 * parse1:先頭が ; * である場合
	 * parse2:"mov $0,1"のようなコマンドの場合を処理する
	 **********************************************************************/
	private boolean isIgnore(){
		if(mLine.get(mListener.requestVariable().iLineNum).equals("")) return true;
		if(mLine.get(mListener.requestVariable().iLineNum).charAt(0)==';') return true;
		if(mLine.get(mListener.requestVariable().iLineNum).charAt(0)=='*') return true;
		return false;
	}
	
	/***********************************************************************
	 * コマンドではなく文章の行かどうかを判定。
	 * 文章であればtrueを返します。
	 * 先頭1文字が2byteかどうかで判別します。
	 **********************************************************************/
	private boolean is2Byte(){
		char c = mLine.get(mListener.requestVariable().iLineNum).charAt(0);
		return (String.valueOf(c).getBytes().length < 2) ? false : true;
	}
	
	/*********************************************************************
	 * コマンドを解析します。
	 * 引数を取らない特別なコマンドに対して処理を行います。
	 * 
	 * 処理した場合のみtrueを返す
	 ********************************************************************/
	private boolean parseCom1(){
		//		現在の行の内容を取得する
		String cur = mLine.get(mListener.requestVariable().iLineNum);
		
		if(cur.contains("mv")){
			comMv(cur);
			return true;
		}
			
			
		return false;
	}
	
	
	
	/***********************************************************
	 * コマンドを解析します。
	 * ただし、"mov $0,1"のような形のみ対応しています。
	 * 
	 * 該当する関数comXXXに引数を渡します。
	 ************************************************************/
	private void parseCom2(){
		try{
			//	現在の行の内容を取得する
			String cur = mLine.get(mListener.requestVariable().iLineNum);
			//	空白の位置を検索する
			int ptr = cur.indexOf(' ');
			//	空白までがコマンド名である
			String name = cur.substring(0, ptr);
			//	引数を配列で取得
			String[] args = cur.substring(ptr+1).split(",");
			
			//	各コマンドを実行する
			if(name.equals("mov"))
				comMov(args[0],args[1]);
			if(name.equals("add"))
				comAdd(args[0],args[1]);
			else if(name.equals("setwindow"))
				comSetWindow(args[0]);
			else if(name.equals("mv"))
				comMv(args[0]);
			else if(name.equals("wave"))
				comWave(args[0]);
			else if(name.equals("waveloop"))
				comWaveLoop(args[0]);
			else if(name.equals("wavestop"))
				comWaveStop();
			else if(name.equals("movie"))
				comMovie(args[0],args[1]);
			else if(name.equals("ld"))
				comLd(args[0],args[1],args[2]);
			else if(name.equals("cl"))
				comCl(args[0],args[1]);
			else if(name.equals("font"))
				comFont(args[0]);
			else if(name.equals("select"))
				comSelect(args);
			else if(name.equals("goto"))
				comGoto(args[0]);
			else if(name.equals("bg"))
				comBg(args[0],args[1]);
		
			
		}catch(Exception e){
			
		}
		
	}
	
	
	
	
	
	
	/**************************************************************************
	 * 各コマンドに対する処理をここに記述します
	 * 
	 * 関数名 comXXXX(引数）
	 ************************************************************************/
	
	/********************************************************************
	 * テキスト表示の文字を変えます
	 * "font STR"
	 * @param str
	 *********************************************************************/
	private void comFont(String str){
		
	}
	
	/*******************************************************
	 * ウインドウの設定をします
	 * "setwindow FILENAME"
	 *****************************************************/
	private void comSetWindow(String str){
		mListener.setImageTextWindow(str);
	}
	
	/***********************************************************************
	 * 指定された数字.mp3を再生する
	 * "mvNUM:"
	 **********************************************************************/
	private void comMv(String str){
		//	numだけ抽出
		String str2 = subStringEx(str,"mv",":");
		mListener.playMp3(str2+".mp3");
	}
	
	
	
	/*******************************************************
	 * 音楽を鳴らす
	 * "wave"
	 *****************************************************/
	private void comWave(String str){
		
	}
	
	/*******************************************************
	 * 音楽をループ再生する
	 * "waveloop STR"
	 *****************************************************/
	private void comWaveLoop(String str){
		
	}
	
	/*******************************************************
	 * 音を止める
	 * "wavestop"
	 *****************************************************/
	private void comWaveStop(){
		
	}
	
	/*******************************************************
	 * 変数を代入する
	 * "mov %xxx,yy"
	 * "mov $xxx,yy"
	 *****************************************************/
	private void comMov(String name,String value){
		if(name.charAt(0)=='%'){
			int num = Integer.parseInt(value);
			mListener.requestVariable().setValue(name,  num);
		}else
			mListener.requestVariable().setValue(name, value );	
	}
	
	/*******************************************************
	 * 変数に足す
	 * add $xxx,yy
	 *****************************************************/
	private void comAdd(String name,String value){
		if(name.charAt(0)=='%')	//	数値は変換
			mListener.requestVariable().addValue(name, Integer.parseInt(value));
		else	//	文字ならそのまま追加
			mListener.requestVariable().addValue(name, value);	
	}
	
	/********************************************************
	 * 動画を再生する
	 * "movie FILENAME[,click]"
	 *******************************************************/
	private void comMovie(String name,String click){
		
	}
	
	
	/********************************************************
	 * 人物レイヤーを表示する
	 * "ld {l,c,r},FILENAME,EFFECT"
	 ********************************************************/
	private void comLd(String pos,String name,String effect){
		switch(pos.charAt(0)){
		case 'l': mListener.setImagePersonLeft(name,Integer.parseInt(effect)); break;
		case 'c': mListener.setImagePersonCenter(name,Integer.parseInt(effect)); break;
		case 'r': mListener.setImagePersonRight(name,Integer.parseInt(effect)); break;
		default:
			System.out.println("Error at ComLd.");
			System.out.println("人物レイヤの位置指定が無効です");
			break;
		}
	}
	
	
	/********************************************************
	 * 人物レイヤーを消す
	 * "cl,{l,c,r},EFFECT"
	 ********************************************************/
	private void comCl(String pos,String effect){
		switch(pos.charAt(0)){
		case 'l': mListener.clearImagePersonLeft(Integer.parseInt(effect)); break;
		case 'c': mListener.clearImagePersonCenter(Integer.parseInt(effect)); break;
		case 'r': mListener.clearImagePersonRight(Integer.parseInt(effect)); break;
		default:
			System.out.println("Error at ComCl.");
			System.out.println("人物レイヤの位置指定が無効です");
			break;
		}
		
		
	}
	
	/********************************************************
	 * 選択肢を表示する
	 * select STR,LABEL[,STR,LABEL[,...]]
	 **********************************************************/
	private void comSelect(String[] strs){
		//	テキストを進めない
		bWaitFlag = true;
		
	}
	
	/********************************************************
	 * 指定されたラベルに飛ぶ
	 * goto *LABELNAME
	 **********************************************************/
	private void comGoto(String labelName){
		int start = 0;	//	iLineNumに変更すれば現在の番号から開始します
		for(int i=start; i<mLine.size(); i++){
			if(mLine.get(i).equals(labelName)){
				mListener.requestVariable().iLineNum = i;
				return;
			}
		}
		System.out.println("["+labelName+"]が見つかりませんでした。");
	
	}
	
	/********************************************************
	 * 背景変更
	 * bg FILENAME,EFFECT
	 **********************************************************/
	private void comBg(String fileName,String effect){
		mListener.setImageBackGround(fileName);
		
	}
	
	
	
	/****************************************************
	 * 指定された文字列で囲まれた部分の文字を取得する汎用関数
	 * 
	 * 成功：その文字
	 * 失敗：null
	 *******************************************************/
	private String subStringEx(String source,String arg1,String arg2){
		String str;
		int start,end;
		
		try{
			start = source.indexOf(arg1) + arg1.length();	//	開始位置を取得
			end = source.indexOf(arg2);						//	終了位置を取得
			str = source.substring(start,end);				
			return str;
		}catch(Exception e){
			return null;
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	


}
