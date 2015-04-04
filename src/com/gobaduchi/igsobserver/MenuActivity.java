package com.gobaduchi.igsobserver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.gobaduchi.igsobserver.util.LogProxy;

public class MenuActivity extends Activity {

	private static LogProxy log = new LogProxy(MenuActivity.class.getName());

	private Intent intent;

	private OnClickListener onClickRefreshListener = new OnClickListener() {
		public void onClick(View v) {

			log.debug("refresh button clicked");

			Bundle bundle = new Bundle();
			bundle.putBoolean("refresh", true);

			intent.putExtras(bundle);
			
			setResult(RESULT_OK, intent);
			finish();
		}
	};

	private OnClickListener onClickCancelListener = new OnClickListener() {
		public void onClick(View v) {

			log.debug("cancel button clicked");

			setResult(RESULT_CANCELED);
			finish();
		}
	};

	public void onCreate(Bundle bundle) {

		super.onCreate(bundle);

		intent = getIntent();

		setContentView(R.layout.menu);

		Button button_refresh = (Button) findViewById(R.id.button_refresh);
		button_refresh.setOnClickListener(onClickRefreshListener);

		Button button_cancel = (Button) findViewById(R.id.button_cancel);
		button_cancel.setOnClickListener(onClickCancelListener);

	}
}
