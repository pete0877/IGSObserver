package com.gobaduchi.igsobserver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.gobaduchi.igsobserver.util.LogProxy;
import com.gobaduchi.igsobserver.util.SocketCommunicator;
import com.gobaduchi.igsobserver.util.SocketCommunicatorImpl;

public class IGSObserverActivity extends Activity implements OnClickListener {

	private static LogProxy log = new LogProxy(
			IGSObserverActivity.class.getName());

	public final static String igsHost = "210.155.158.200";
	public final static int igsPort = 6969;
	public final static int MENU_REQUEST = 0;

	private GameCorrespondent correspondent;
	private SocketCommunicator communicator;
	private GamePublisher publisher;
	private BoardView boardView;
	private ProgressDialog myProgressDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		log.debug("onCreate called");

		setContentView(R.layout.default_layout);

		boardView = (BoardView) findViewById(R.id.board_view);
		boardView.setOnClickListener(this);
		boardView.setTextView((TextView) findViewById(R.id.legend_view));

		ApplicationState state = (ApplicationState) getLastNonConfigurationInstance();
		if (state != null) {

			log.debug("onCreate is restoring state");
			
			communicator = state.communicator;
			publisher = state.publisher;
			correspondent = state.correspondent;

		} else {

			log.debug("onCreate is starting a new state");

			myProgressDialog = ProgressDialog.show(this, "Please wait...",
					"looking for a game ...", true);
			
			communicator = new SocketCommunicatorImpl();
			communicator.setServer(igsHost, igsPort);

			publisher = new GamePublisherImpl();

			correspondent = new GameCorrespondent();
			correspondent.setCommunicator(communicator);
			correspondent.setTracker(publisher);
			correspondent.start();

		}

		super.onCreate(savedInstanceState);
		
		log.debug("onCreate completed");

	}
 
	@Override
	protected void onDestroy() {
		
		log.debug("onDestroy called");

		if (correspondent != null && correspondent.isRunning()) {

			log.debug("onDestroy is stopping the correpondant");
			
			correspondent.stop();
			
			log.debug("onDestroy finished stopping the correpondant");

		}
		
		
		super.onDestroy();

		log.debug("onDestroy completed");

		
	}

	@Override
	protected void onResume() {

		log.debug("onResume called");

		if (publisher != null)
			publisher.addGameSubscriber(boardView);
		
		
		if (myProgressDialog != null) {
			this.boardView.myProgressDialog = myProgressDialog;
			myProgressDialog = null;
		}
			
			
		super.onResume();

		log.debug("onResume completed");
	}

	@Override
	protected void onPause() {

		log.debug("onPause called");

		if (publisher != null)
			publisher.removeAllSubscribers();

		super.onPause();
		
		log.debug("onPause completed");

	}

	@Override
	public void onClick(View v) {

		log.debug("onClick called");

		Intent i = new Intent(IGSObserverActivity.this, MenuActivity.class);
		startActivityForResult(i, MENU_REQUEST);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		log.debug("onActivityResult called");

		if (requestCode == MENU_REQUEST) {
			if (resultCode != RESULT_CANCELED) {

				Bundle bundle = data.getExtras();

				if (bundle != null) {
					
					if (bundle.getBoolean("refresh")) {
						
						log.debug("starting sending new game request");

						myProgressDialog = ProgressDialog.show(this, "Please wait...",
								"looking for a game ...", true);
						
						correspondent.requestNewGame();
						
						log.debug("done sending new game request");

					}
				}
			} 
		}
		
		super.onActivityResult(requestCode, resultCode, data);

		log.debug("onActivityResult completed");
	}


	@Override
	public Object onRetainNonConfigurationInstance() {

		log.debug("onRetainNonConfigurationInstance called");

		final ApplicationState state = new ApplicationState();
		state.communicator = this.communicator;
		state.correspondent = this.correspondent;
		state.publisher = this.publisher;

		return state;
	}

}
