package jp.anddev68.scripter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import scripter.NovelView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/*****************************************************
 * セーブとロードを1枚のアクティビティーで行います
 * 
 * 【セーブの概要】
 * ・Variableクラスの行番号と変数を指定されたファイルに保存する（x.dat)
 * ・それとは別にSS,日付、そのときの文章をファイルに保存する（x.header）
 * 
 *******************************************************/
public class SaveLoadActivity extends Activity implements OnItemClickListener{

	//	セーブモードかロードモードか
	public static final int SAVE_MODE = 1;
	public static final int LOAD_MODE = 2;
	int mode;
	
	
	//	セーブ/ロードファイル一覧表
	ListView list;
	
	//	リストに表示するデータ
	List<Record> dataList;
	
	//	アダプター
	RecordAdapter adapter;
	
	//	現在表示されている文字列
	String currentText;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_saveload);
		
		//	モードの決定
		mode = getIntent().getIntExtra("mode", SAVE_MODE);
		
		currentText = getIntent().getStringExtra("currentText");
		
		//	モードによって表示される文字列を振り分ける
		TextView tv1 = (TextView) findViewById(R.id.title);
		if(mode == SAVE_MODE)
			tv1.setText("セーブ");
		else
			tv1.setText("ロード");
		
			
		//	Listviewに一覧表をセットする
		list = (ListView) findViewById(R.id.ListView1);
		list.setOnItemClickListener(this);
		setAdapters();
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
		//	返すデータの作成
		Intent intent = new Intent();
		intent.putExtra("filename", pos+".dat");
		intent.putExtra("mode", mode);
		
		//	セーブならヘッダーを書き出す
		if(mode==SAVE_MODE){
			writeSS(pos+".jpg");
			writeHeader(pos+".header");
		}
		
		//	返すデータをセットし終了
		setResult(RESULT_OK, intent);
		finish();
	}	
	
	
	/***************************************
	 * 一覧表を作成します
	 ***************************************/
	private void setAdapters(){
		
		dataList = new ArrayList<Record>();
		
		//	とりあえずセーブ数20で作成する
		for(int i=0; i<20; i++){
			Record record = null;
			try{
				BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(i+".header")));
				record = new Record();
				record.date = br.readLine();
				record.sentence =br.readLine();
				br.close();
				record.screenShot = readSS(i+".jpg");
			}catch(Exception e){
				record = null;
			}
			
			dataList.add(record);
		}		
		adapter = new RecordAdapter(this,0,dataList);
		list.setAdapter(adapter);
		
		
	}
	
	/************************************
	 * 表示するデータを書き込みます
	 **********************************/
	private void writeHeader(String fileName){
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(openFileOutput(fileName,MODE_PRIVATE)));
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy'年'MM'月'dd'日'　kk'時'mm'分'ss'秒'");
			bw.write(sdf.format(date));
			bw.newLine();
			bw.write(currentText);
			bw.newLine();
		
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
	/************************************
	 * 表示するSSを書きこむ
	 *************************************/
	private void writeSS(String fileName){
		try {
			OutputStream stream = openFileOutput(fileName,MODE_PRIVATE);
			NovelView.mSavedBuffer.compress(CompressFormat.JPEG, 100, stream);
		
			stream.close();
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
		
	}

	/************************************
	 * 表示するSSを取得する
	 *************************************/
	private Bitmap readSS(String fileName){
		Bitmap b = null;
		try {
			InputStream stream = openFileInput(fileName);
			b = BitmapFactory.decodeStream(stream);
			stream.close();
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return b;
		
	}
	
	
	/***************************************
	 * 一覧表
	 **************************************/
	class RecordAdapter extends ArrayAdapter<Record>{
		private LayoutInflater layoutInflater;

		public RecordAdapter(Context context, int resource, List<Record> objects) {
			super(context, resource, objects);
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView1 = null;
			TextView textView2 = null;
			ImageView imageView1 = null;

			View v = convertView;

			if(v==null){
				v = layoutInflater.inflate(R.layout.list_row, null);
			}

			Record record = (Record)getItem(position);
			textView1 = (TextView) v.findViewById(R.id.textView1);
			textView2 = (TextView) v.findViewById(R.id.textView2);
			imageView1 = (ImageView) v.findViewById(R.id.imageView1);
			
			if(record != null){

				//	日付を設定
				textView1.setText(record.date);

				//	文章を設定
				textView2.setText(record.sentence);

				//	画像を設定
				imageView1.setImageBitmap(record.screenShot);

			}else{
				textView1 = (TextView) v.findViewById(R.id.textView1);
				textView1.setText("データがありません");
				textView2.setText("");
				imageView1.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
			}




			return v;
		}
	}



		
	
	/****************************************
	 * 表示するデータ
	 * 
	 * ・セーブ日時
	 * ・スクリーンショット
	 * ・そのときの表示されている文章
	 ***************************************/
	class Record{
		String date;
		Bitmap screenShot;
		String sentence;
	}
	
}
