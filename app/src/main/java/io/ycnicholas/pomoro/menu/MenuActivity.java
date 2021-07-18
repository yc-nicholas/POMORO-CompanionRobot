package io.ycnicholas.pomoro.menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import io.ycnicholas.pomoro.controller.ControllerSetupActivity;
import io.ycnicholas.pomoro.robot.RobotSetupActivity;
import io.ycnicholas.pomoro.R;

public class MenuActivity extends Activity {
	private Button btnController;
	private Button btnRobot;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		setContentView(R.layout.activity_main);

		btnController = (Button)findViewById(R.id.btn_controller);
        btnController.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, ControllerSetupActivity.class);
				startActivity(intent);
			}
		});

        btnRobot = (Button)findViewById(R.id.btn_robot);
        btnRobot.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, RobotSetupActivity.class);
				startActivity(intent);
			}
		});
    }
}
