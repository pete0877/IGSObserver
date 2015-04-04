package com.gobaduchi.igsobserver;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class LegendHandler extends Handler {

	TextView legend;

	public LegendHandler(TextView textView) {
		
		super();
		
		legend = textView;
	}

	@Override
	public void handleMessage(Message m) {
		
		legend.setText(m.getData().getString("text"));
	}

}
