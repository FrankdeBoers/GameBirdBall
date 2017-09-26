package ghc.ballbird;

//画画线程
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

public class GameBirdSurfaceView extends SurfaceView implements Callback, Runnable {
	
	private SurfaceHolder sfh;
	private Paint paint;
	private Paint paintRe;
	private Paint paintScore ;
	private Paint paintResultPaint ; 
	
	
	private Thread th;
	private boolean flag;
	
	private Canvas canvas;
	private static int screenW, screenH;

	private static final int GAME_MENU = 0;
	private static final int GAMEING = 1;
	private static final int GAME_OVER = -1;
	private static final int GAME_WIN = 2 ;

	private static int gameState = GAME_MENU;
	
	private int[] floor = new int[2];
	private int floor_width = 15;
	
	
	
	private int speed = 3;
	private int speeds = 3 ;
	
	private int[] level = new int[2];
	private int level_value = 0;
	
	private int[] bird = new int[2];
	private int bird_width = 10;
	private int bird_v = 0;
	private int bird_a = 2;
	private int bird_vUp = -16;//向下降的权值
	
	
	private ArrayList<int[]> walls = new ArrayList<int[]>();//就是动态数组
	private ArrayList<int[]> remove_walls = new ArrayList<int[]>();
	private ArrayList<int[]> values = new ArrayList<int[]>();
	Vector v=new Vector(); 
	private int wall_w = 50;  //一堵墙的宽度
	private int wall_h = 400;  //上下两堵墙的间距
	
	private int wall_step = 30;
	
	private int[] array = new int[500];
	
	private int your_goal = 0;
	
	private int up_dis = dp2px(50) ;
	
	Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),
			R.mipmap.c);


	Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),
			R.mipmap.d);

	private int GetRandom()
	{
		Random rand = new Random();
		aim = rand.nextInt(30)+10;
		return aim;
	}
	
	int aim= GetRandom(); 
	
	private int[] a = new int[1000];
	public GameBirdSurfaceView(Context context) {
		
		super(context);
		sfh = this.getHolder();
		sfh.addCallback(this);
		paint = new Paint();//创建新画笔
		paint.setColor(Color.WHITE);//设置画笔的颜色为白色
		paint.setAntiAlias(true);
		paint.setTextSize(50);//设置字体大小50
		//paint.setStyle(Style.STROKE); //设置字体效果
		
		paintScore = new Paint();//创建新画笔
		paintScore.setColor(Color.WHITE);//设置画笔的颜色为白色
		paintScore.setAntiAlias(true);
		paintScore.setTextSize(40);//设置字体大小50
		//paintCircle.setStyle(Style.STROKE); //设置字体效果
		
		paintRe = new Paint();//创建新画笔
		paintRe.setColor(Color.rgb(85,68,32));//设置画笔的颜色为白色
//		paintRe.setAntiAlias(true);
//		paintRe.setTextSize(40);//设置字体大小50
		//paintCircle.setStyle(Style.STROKE); //设置字体效果
		
		
		paintResultPaint = new Paint();//创建新画笔
		paintResultPaint.setColor(Color.BLACK);//设置画笔的颜色为白色
//		paintResultPaint.setAntiAlias(true);
		paintResultPaint.setTextSize(50);//设置字体大小50
		
		setFocusable(true);//将控件设置成可获取焦点状态，默认是无法获取焦点的，只有设置成true，才能获取控件的点击事件  
		setFocusableInTouchMode(true);//设置焦点联系方式(正确的)

		this.setKeepScreenOn(true);//设置运行过程中不锁屏
	}

	//判断某个数在某个范围内
	public boolean RangeJudge(int current , int min , int max)
	{
		
		return Math.max(min, current) == Math.min(current, max); 
	}
	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		screenW = this.getWidth();//获取屏幕大小
		screenH = this.getHeight();
		
		//System.out.println("=======screenW="+screenW+"=======screenH="+screenH);
		
		initGame();
		
		flag = true;
		
		th = new Thread(this);//开启新线程
		
		th.start();
	}
	
	

	private void initGame() {
//////////////////////////////////////////////////////////////
	
		//生成目标分数
		

		if ( gameState == GAME_MENU) {//gameState 的初值就是GAME_MENU
			
			floor[0] = 0;					//第0层是0
			floor[1] = screenH - screenH/20;			//第1层是屏幕高度的4/5
			
			level[0] = screenW/2;				//level[0]  是屏幕宽度的一半
			level[1] = screenH/5;				//level[1]  是屏幕高度的
			
			level_value = 0;		//初值就是0

			bird[0] = screenW/3;		//bird[0]  是屏幕宽度的1/3
			bird[1] = screenH/2;		//bird[1]  是屏幕高度的1/2
			
			walls.clear();					//清空动态数组的内容
			
			//dp to px
			floor_width = dp2px(15);			//墙的宽度
			
			speed = dp2px(speeds);			//速度
			
			bird_width = dp2px(12);		//鸟的宽度
			bird_a = dp2px(2);						//一次前进的权值
			bird_vUp = -dp2px(16);				//一次上升的权值
			
			wall_w = dp2px(40);				//墙的宽度权值
			wall_h = dp2px(85);				//墙的高度权值
			
			wall_step = wall_w*3   ;				//墙之间的间距
		}
	}
	
	private int dp2px(float dp){//将dp转换为px
		int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
		//Math.round就是四舍五入   TypedValue.applyDimension这个方法是转变为标准尺寸的一个函数 转变dp,dip,sp,px
		return px;
	}

	public void myDraw() {
		try {
			canvas = sfh.lockCanvas();//将画布锁住
			if (canvas != null) {
				//clear
				canvas.drawColor(Color.rgb(85,68,32));//整张画布画黑
//				canvas.drawColor(Color.rgb(42,43,18));//整张画布画黑
				//background
				int floor_start = floor[0] ; //=0
//				while(floor_start<screenW){   //如果小于屏幕宽度###################画虚线函数!!!!!!!!!!!!!!!!!!!!!!
//					canvas.drawLine(floor_start, floor[1], floor_start+floor_width, floor[1], paint);
//					//x_start,y,
//					floor_start += floor_width * 2;//每次增加30
//				}
				
				
				
				
				canvas.drawLine(0 , up_dis , screenW, up_dis , paint);
				canvas.drawLine(0 , floor[1], screenW, floor[1], paint);
				
				//wall
				for (int i = 0; i < walls.size(); i++) {
					int[] wall = walls.get(i);//遍历动态数组，###########下面画墙
					
					float[] ptsWall = {
							wall[0],up_dis,wall[0], wall[1] - 70,//画向下的
							wall[0] , wall[1]+ 2* wall_h + 50 , wall[0],floor[1],     //wall_h = 100   ,floor1 = 屏幕高度的4/5
							wall[0]+wall_w -35 , up_dis , wall[0]+wall_w - 35 ,wall[1]  - 70,   //wall_w = 50
							wall[0]+wall_w -35 , wall[1]+ 2* wall_h + 50 , wall[0]+wall_w -35, floor[1] ,
							wall[0],wall[1] - 70 , wall[0]+wall_w - 35, wall[1]  - 70,
							wall[0],wall[1]+ 2*wall_h + 50 , wall[0]+wall_w - 35 , wall[1]+ 2* wall_h + 50 
							//,wall[0],floor[1], wall[0]+wall_w, floor[1]
					};
					int[] pts = {
							wall[0],wall[1],//画向下的
							wall[0],wall[1]+ wall_h ,    //wall_h = 100   ,floor1 = 屏幕高度的4/5
							wall[0], wall[1] + 2 *  wall_h,   //wall_w = 50
//							wall[0]+wall_w , wall[1]+wall_h 						
											};
					
//					values.add(pts);
//					String[] array = (String[])values.toArray(new String[values.size()]);
//					v.add(pts[1]);
					
					canvas.drawLines(ptsWall, paint);//画墙，正好画出一面对称的墙
					
				
					
					
//					for( int j =0 ; j < pts.length ; j = j +2 )
//					{
////						a[j + 4 * counts] = pts[j] ;
////						a[j + 4 * counts +1 ] = pts[j +1] ;
////						a[j + 4 * counts +2 ] = pts[j +2] ;
////						a[j + 4 * counts +3 ] = pts[j +3] ;
//						canvas.drawText(String.valueOf(pts[1]),//设置显示的数字
//								pts[j],
//								pts[j+1]  ,
//								paint);
//					}
					canvas.drawText(String.valueOf(pts[1]/100),//设置显示的数字
							pts[0],
							pts[1]  ,
							paint);
					canvas.drawText(String.valueOf(pts[3]/100),//设置显示的数字
							pts[2],
							pts[3]  ,
							paint);
					
					canvas.drawText(String.valueOf(pts[5]/100),//设置显示的数字
							pts[4],
							pts[5]  ,
							paint);
					
					//canvas.drawRect(wall[0], 0, wall[0]+wall_w, wall[1], paint);
					//canvas.drawRect(wall[0], wall[1]+wall_h, wall[0]+wall_w, floor[1], paint);
				}
				
				//bird
				//canvas.drawCircle(bird[0], bird[1], bird_width, paint);
				canvas.drawBitmap( bitmap1 ,bird[0] - 39 , bird[1] - 33, paint);
				//画矩形
				 canvas.drawRect(-1, -1 , screenW , up_dis, paintRe);  
				 canvas.drawRect(-1 , floor[1] , screenW  , screenH  , paintRe);  
//				canvas.drawCircle(0, 0, bird_width, paint);
				//1/3宽，1/2高，画了一个圆
				//level
				
//				canvas.drawText(String.valueOf(your_goal), level[0], level[1], paintScore);
				String your_aim = "目标 : " + String.valueOf(aim);
				canvas.drawText(your_aim, screenW /40 , screenH / 25  , paintScore);
			
				String your_score = "得分 : "+String.valueOf(your_goal);
				String guanka = "关卡 : "+String.valueOf(level_value);
				
				canvas.drawText(your_score , 3* screenW /4  - dp2px(120) , screenH / 25 , paintScore);
				canvas.drawText(guanka , 3* screenW /4  + dp2px(10)    , screenH / 25   , paintScore);
				
				
				
				
				
				
//				canvas.drawText(String.valueOf(your_goal), 0, 0, paint);
				//得分显示
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}
	
	
	public void myDrawWin() 
	{
		try
		{
			canvas = sfh.lockCanvas();//将画布锁住
			if (canvas != null)
			{
				//clear
				String result  = "    你过了  " + String.valueOf(level_value) + " 关 ! " ;
				canvas.drawColor(Color.rgb(248 , 239 ,230));//整张画布画黑
				canvas.drawText("恭喜你，你赢了！", screenW/3 - dp2px(40), screenH / 5, paintResultPaint);
				canvas.drawText(result ,  screenW/3 - dp2px(40), 2* screenH / 5 - dp2px(70), paintResultPaint);
				canvas.drawBitmap(bitmap2, -dp2px(30) ,dp2px(250) ,paintResultPaint);
				GetRandom();
			} 
		}
			catch (Exception e)
			{
				// TODO: handle exception
			} finally 
			{
				if (canvas != null)
					sfh.unlockCanvasAndPost(canvas);
			}
		}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if(event.getAction() == MotionEvent.ACTION_DOWN){

			switch (gameState) {
			
			case GAME_WIN :
				gameState = GAME_MENU;
				break;
				case GAME_MENU:
					gameState = GAMEING;
					//正在游戏
//					bird_v = bird_vUp;
//					break;
				case GAMEING:
					bird_v = bird_vUp;//向下降的权值
					break;
				case GAME_OVER:
					//游戏结束
					//bird down
//					GetRandom();
					if(bird[1] > floor[1] - bird_width || bird[1] < up_dis){
						if(bird[1] < 5)
						{	
							bird_v+=bird_a;
							bird[1] += bird_v;
						}
						if(bird[1] >= floor[1] - bird_width){
							bird[1] = floor[1] - bird_width;
							gameState = GAME_MENU;
							
							initGame();
						}
						
					}
					
					break;
					
				
			}
		}
		return true;
	}

//	@Override//返回键捕获
//	public boolean onKeyDown(int keyCode, KeyEvent event)
//	{
//
//		if (keyCode == KeyEvent.KEYCODE_BACK)
//		{
//
//				GameBirdActivity.instance.finish();
//				System.exit(0);
//				
//			return true;
//		}
//
//		return super.onKeyDown(keyCode, event);
//	}
	
	private long exitTime = 0;
	private int id;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(GameBirdActivity.instance.getApplicationContext(), "亲爱的，再次点击会退出游戏哟 (ˇˍˇ)",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				GameBirdActivity.instance.finish();
//				gameState = GAME_MENU ;
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	

	
	
	private int move_step = 0;
	private void logic() {
		
		switch (gameState) {
		
			case GAME_MENU :
				GetRandom();
				initGame();
				break;
				
			case GAMEING :  //正在游戏的情况
				
				//bird
				bird_v +=bird_a;
				bird[1] += bird_v;//bird1指的是 圆的 高 的位置改变
				if(bird[1] > floor[1] - bird_width )
				{//#############判断有没有撞到地上
																		  //floor1  ， 屏幕高度的4/5
					bird[1] = floor[1] - bird_width;   //停在当前位置，地上
					
					gameState = GAME_OVER ;
				}
				
				else if( bird[1] < up_dis + dp2px(10))
				{
					bird_v+=bird_a;
					bird[1] += bird_v;
					if(bird[1] >= floor[1] - bird_width){
						bird[1] = floor[1] - bird_width;
					gameState = GAME_OVER ;
					}
				}
				//top
//				if(bird[1]<=bird_width){
//					bird[1]=bird_width;
//				}
				
				//floor
				if(floor[0] < - floor_width)
				{  //floor_w  = 15  ，每次虚线增加的步长
					
					floor[0] += floor_width * 2 ; //floor0 每次增加30
					
				}
				else
				{
					floor[0] -= speed;		//否则floor0 = floor0 - 3
				}
					
				
				//wall
				remove_walls.clear(); //动态数组清零
				
				for (int i = 0; i < walls.size(); i++) {
					int[] wall = walls.get(i);
					wall[0] -= speed;//wall0 = wall0 - 3 ;
					if(wall[0]<- wall_w){			//判断画墙的起始位置是否小于墙的宽度
						remove_walls.add(wall);
					}else if(  ((wall[0]-bird_width<=bird[0] && wall[0]+wall_w+bird_width - 35 >=bird[0]	&& ( bird[1] <= wall[1] +  bird_width - 70  ||   bird[1]>=wall[1]+2*wall_h-bird_width+50) ) || bird[1] < up_dis + dp2px(20) )						 						
						||( RangeJudge(wall[0] - bird[0], -( 7 + bird_width) , bird_width) && RangeJudge(bird[1] -wall[1] , -( 7 + bird_width) , bird_width) ) //碰到了上面的									
						|| ( RangeJudge(wall[0] - bird[0], -( 7 + bird_width) , bird_width)  && RangeJudge(bird[1] - (wall[1] + wall_h ) , -( 7 + bird_width) , bird_width)	 )
						|| ( RangeJudge(wall[0] - bird[0], -( 7 + bird_width) , bird_width)  && RangeJudge(bird[1] - (wall[1] + wall_h * 2 ) , -( 7 + bird_width) , bird_width)	 )
							)
						
					{		
						if(  (wall[0]-bird_width<=bird[0] && wall[0]+wall_w+bird_width - 35 >=bird[0]	&& ( bird[1] <= wall[1] +  bird_width - 70  ||   bird[1]>=wall[1]+2*wall_h-bird_width+50) ) || bird[1] < up_dis + dp2px(20))
						{
							gameState = GAME_OVER;
						}
						
						if( RangeJudge(wall[0] - bird[0], -( 7 + bird_width) , bird_width) && RangeJudge(bird[1] -wall[1] , -( 7 + bird_width) , bird_width) )							
						{
							level_value ++;
							your_goal =your_goal + wall[1]  / 100 ;
							wall[0] =-100;		
							if( your_goal ==aim)
							{
								gameState = GAME_WIN;
								your_goal= 0;
								
//								initGame();
							}
							if(your_goal > aim)
							{
								gameState = GAME_OVER;
//								your_goal= 0;
							}
							
						}
						//第二层的
						if (RangeJudge(wall[0] - bird[0], -( 7 + bird_width) , bird_width)  && RangeJudge(bird[1] - (wall[1] + wall_h ) , -( 7 + bird_width) , bird_width) )							
						{
							level_value ++;
							your_goal =your_goal + (wall[1] + wall_h ) / 100 ;
							wall[0] =-100;		
							if( your_goal ==aim)
							{
								gameState = GAME_WIN;
								your_goal= 0;
//								initGame();
							}
							if(your_goal > aim)
							{
								gameState = GAME_OVER;
//								your_goal= 0;
							}
							
						}
						//第三层的
						if(   RangeJudge(wall[0] - bird[0], -( 7 + bird_width) , bird_width)  && RangeJudge(bird[1] - (wall[1] + wall_h * 2 ) , -( 7 + bird_width) , bird_width) )							
						{
							level_value ++;
							your_goal =your_goal +(wall[1] + wall_h * 2) / 100 ;
							wall[0] =-100;		
							if( your_goal ==aim)
							{
								gameState = GAME_WIN;
								your_goal= 0;
//								initGame();
							}
							if(your_goal > aim)
							{
								gameState = GAME_OVER;
//								your_goal= 0;
							}
							
						}
						

					}
					
					
					int pass = wall[0] + wall_w + bird_width- 35 -bird[0];
					if(pass<0 && -pass<=speed){
						level_value ++;
					}
				}
				//out of screen
				if(remove_walls.size()>0){
					walls.removeAll(remove_walls);
				}

				//new wall
				move_step += speed;
				if(move_step>wall_step){//walls增加过程
					int[] wall = new int[]{screenW, (int)(Math.random()*(floor[1]-2*wall_h)+0.1*wall_h)};
					walls.add(wall);
					move_step = 0;
				}
				break;
			case GAME_OVER:
				//bird
				if(bird[1] < floor[1] - bird_width ){
					bird_v+=bird_a;
					bird[1] += bird_v;
					if(bird[1] >= floor[1] - bird_width){
						bird[1] = floor[1] - bird_width;
					}
				}else{
					GameBirdActivity.instance.showMessage(level_value);
					gameState = GAME_MENU;
					initGame();
				}
				break;
				
			case GAME_WIN :
//				gameState = GAME_MENU;
				
				myDrawWin();
//				gameState = GAME_MENU;
				
				break;

		}
	}

	@Override
	public void run() {//##########刷新函数
		while (flag) {
			long start = System.currentTimeMillis();
			
			if(gameState == GAME_WIN)
			{
				myDrawWin();
			}
			else
			{
//				GetRandom();
				myDraw();
				
			}			
			
			logic();
			
			long end = System.currentTimeMillis();
			try {
				if (end - start < 50) {
					Thread.sleep(50 - (end - start));
//					speeds ++ ;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		flag = false;
	}
}
