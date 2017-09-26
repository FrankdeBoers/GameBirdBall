package ghc.ballbird;
//起始页信息
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class LoadingActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.loading);

		final TextView gameStart = (TextView)findViewById(R.id.game_start);
		

		View gameMessage = findViewById(R.id.GameMessage);
		
		int[] data = getSettingData();
		
		TextView levelMessage = (TextView)findViewById(R.id.level_Message);
		
		levelMessage.setText(/*"SCORE: "+data[0]+*/"\n最高关卡: "+data[1]);
		
		gameMessage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				Intent intent = new Intent(LoadingActivity.this, GameBirdActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				
				startActivity(intent);
				
				finish();
			}
		});

	}
	
	private int[] getSettingData() {

		SharedPreferences gb_settings = getSharedPreferences(GameBirdActivity.GameBirdSettingsFile, 0);

		int last = gb_settings.getInt(GameBirdActivity.Settings_LevelLast, 0);
		int top = gb_settings.getInt(GameBirdActivity.Settings_LevelTop, 0);
		
		return new int[]{last, top};
	}
	
	private long exitTime = 0;
	private int id;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "亲爱的，再按一次会退出程序哟 (ˇˍˇ)",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
