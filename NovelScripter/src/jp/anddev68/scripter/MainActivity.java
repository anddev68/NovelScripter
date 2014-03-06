package jp.anddev68.scripter;

import java.io.FileNotFoundException;

import scripter.GameVariable;
import scripter.NovelView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
	
	NovelView nv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		nv = new NovelView(this);
		setContentView(nv);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch(item.getItemId()){
		case R.id.action_settings:
			intent = new Intent(this,PrefActivity.class);
			startActivity(intent);
			break;
		case R.id.item1:
			intent = new Intent(this,SaveLoadActivity.class);
			intent.putExtra("currentText",nv.getCurrentText());
			startActivityForResult(intent,0);
			break;
		case R.id.item2:
			intent = new Intent(this,SaveLoadActivity.class);
			intent.putExtra("mode", SaveLoadActivity.LOAD_MODE);
			startActivityForResult(intent,0);
			break;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	  @Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  super.onActivityResult(requestCode, resultCode, data);
	    
		  //	キャンセルの場合
		  if(data==null) return;
		  
		  	//	セーブ・ロード画面で選択されたファイルとモードを取得
		  	int mode = data.getIntExtra("mode",-1);
		  	String fileName = data.getStringExtra("filename");
	   
		  	switch(mode){
		  	case SaveLoadActivity.SAVE_MODE:
		  		try {
		  			GameVariable.writeVariableStream(nv.getVariable(),openFileOutput(fileName,MODE_PRIVATE));
				} catch (FileNotFoundException e) {}
		  		break;
		  	case SaveLoadActivity.LOAD_MODE:
				try {
					nv.setVariable(GameVariable.readVariableStream(openFileInput(fileName)));
					nv.update();
				} catch (FileNotFoundException e) {}
		  		break;
		  	default:
		  	}
	    
	  }

}
