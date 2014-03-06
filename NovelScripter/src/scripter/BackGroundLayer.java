package scripter;

import android.graphics.Canvas;

public class BackGroundLayer extends Layer{

	
	@Override
	public void draw(Canvas c){
		super.draw(c);
		
		drawBitmap(c,bitmap,0,0,iWidth,iHeight);
	}
	
	@Override
	public void init(){
		
	}
	
	
}
