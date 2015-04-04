package com.gobaduchi.igsobserver;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.gobaduchi.igsobserver.util.LogProxy;

class BoardView extends SurfaceView implements SurfaceHolder.Callback,
		GameSubscriber {

	private static LogProxy log = new LogProxy(BoardView.class.getName());

	private Context mContext;
	private LegendHandler legendHandler;

	private Game game;

	private SurfaceHolder mSurfaceHolder;
	private Bitmap mBackgroundImage;

	private Drawable white_stone_marked;
	private Drawable white_stone;
	private Drawable black_stone_marked;
	private Drawable black_stone;

	private int mCanvasWidth;
	private int mCanvasHeight;

	private int mBoardImageSize;
	private int mFirstLineOffsetX;
	private int mFirstLineOffsetY;
	private int mLineSpacingX;
	private int mLineSpacingY;
	private int mStoneSize;

	private TextView textView;

	public ProgressDialog myProgressDialog = null;

	public BoardView(Context context, AttributeSet attrs) {

		super(context, attrs);

		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);

		mContext = context;

		mBackgroundImage = BitmapFactory.decodeResource(
				mContext.getResources(), R.drawable.board);

		white_stone_marked = mContext.getResources().getDrawable(
				R.drawable.white_marked);

		white_stone = mContext.getResources().getDrawable(R.drawable.white);

		black_stone_marked = mContext.getResources().getDrawable(
				R.drawable.black_marked);

		black_stone = mContext.getResources().getDrawable(R.drawable.black);

		setFocusable(true);

	}

	public TextView getTextView() {
		return textView;
	}

	public void setTextView(TextView textView) {

		this.textView = textView;
		this.legendHandler = new LegendHandler(textView);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		mSurfaceHolder = holder;

		synchronized (mSurfaceHolder) {
			mCanvasWidth = width;
			mCanvasHeight = height;

			mBoardImageSize = (mCanvasWidth >= mCanvasHeight ? mCanvasHeight
					: mCanvasWidth);

			mFirstLineOffsetX = 24 * mBoardImageSize / 480;
			mFirstLineOffsetY = 24 * mBoardImageSize / 480;

			mLineSpacingX = 24 * mBoardImageSize / 480;
			mLineSpacingY = 24 * mBoardImageSize / 480;

			mStoneSize = 24 * mBoardImageSize / 480;

			mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage,
					mBoardImageSize, mBoardImageSize, true);

			log.debug("created with w: " + mCanvasWidth + " h: "
					+ mCanvasHeight);
		}
	}

	private void placeStone(Canvas canvas, Drawable stone, int x, int y) {

		int startX, endX, startY, endY;

		// need to flip the board to match IGS's orientation:
		int adjustedY = 18 - y;
		int adjustedX = x;

		startX = (mFirstLineOffsetX + adjustedX * mLineSpacingX) - mStoneSize
				/ 2;
		startY = (mFirstLineOffsetY + adjustedY * mLineSpacingY) - mStoneSize
				/ 2;

		endX = startX + mStoneSize;
		endY = startY + mStoneSize;

		stone.setBounds(startX, startY, endX, endY);
		stone.draw(canvas);

	}

	private void doDraw(Canvas canvas) {

		canvas.drawColor(Color.BLACK);

		if (game != null && game.board != null) {

			canvas.drawBitmap(mBackgroundImage, 0, 0, null);

			Move lastMove = null;
			if (game.moves.size() > 0) {
				lastMove = game.moves.get(game.moves.size() - 1);
			}

			for (int x = 0; x < 19; x++)
				for (int y = 0; y < 19; y++) {
					Board.SPOT spot = game.board.spots[x][y];
					if (spot.equals(Board.SPOT.BLACK)) {
						if (lastMove != null && lastMove.position != null
								&& lastMove.position.equals(new Position(x, y)))
							placeStone(canvas, black_stone_marked, x, y);
						else
							placeStone(canvas, black_stone, x, y);
					} else if (spot.equals(Board.SPOT.WHITE)) {
						if (lastMove != null && lastMove.position != null
								&& lastMove.position.equals(new Position(x, y)))
							placeStone(canvas, white_stone_marked, x, y);
						else
							placeStone(canvas, white_stone, x, y);

					}
				}
		}
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		mSurfaceHolder = holder;

		redraw();
	}

	private void redraw() {

		Canvas c = null;
		try {
			c = mSurfaceHolder.lockCanvas(null);
			synchronized (mSurfaceHolder) {

				doDraw(c);

				if (legendHandler != null && game != null) {

					String legendString = game.getSummary();

					Message msg = legendHandler.obtainMessage();
					Bundle b = new Bundle();
					b.putString("text", legendString);
					msg.setData(b);
					legendHandler.sendMessage(msg);
				}
			}

		} finally {
			if (c != null) {
				mSurfaceHolder.unlockCanvasAndPost(c);
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	public void update(Game game, boolean isNew) {

		this.game = game;

		log.debug("got update for game " + game.gameID);

		if (myProgressDialog != null) {
			myProgressDialog.dismiss();
			myProgressDialog = null;
		}

		redraw();

	}

}
