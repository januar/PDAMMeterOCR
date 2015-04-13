package com.pdammeterocr.camera;

import com.pdammeterocr.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public final class CameraFrame extends View {
	private final Paint paint;
	private final int maskColor;
	private final int frameColor;
	private final int cornerColor;

	private Rect previewFrame;
	private Rect rect;

	public CameraConfiguration cameraManager;

	public CameraFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		// Initialize these once for performance rather than calling them every
		// time in onDraw().
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		frameColor = resources.getColor(R.color.viewfinder_frame);
		cornerColor = resources.getColor(R.color.viewfinder_corners);

		// bounds = new Rect();
		previewFrame = new Rect();
		rect = new Rect();
		cameraManager = new CameraConfiguration(context);
	}

	@SuppressWarnings("unused")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		Rect frame = cameraManager.getFramingRect();
		if (frame == null) {
			return;
		}
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// Draw the exterior (i.e. outside the framing rect) darkened
		paint.setColor(maskColor);
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		// If we have an OCR result, overlay its information on the viewfinder.
		/*if (resultText != null) {

			// Only draw text/bounding boxes on viewfinder if it hasn't been
			// resized since the OCR was requested.
			Point bitmapSize = resultText.getBitmapDimensions();
			previewFrame = cameraManager.getFramingRectInPreview();
			if (bitmapSize.x == previewFrame.width()
					&& bitmapSize.y == previewFrame.height()) {

				float scaleX = frame.width() / (float) previewFrame.width();
				float scaleY = frame.height() / (float) previewFrame.height();

				if (DRAW_REGION_BOXES) {
					regionBoundingBoxes = resultText.getRegionBoundingBoxes();
					for (int i = 0; i < regionBoundingBoxes.size(); i++) {
						paint.setAlpha(0xA0);
						paint.setColor(Color.MAGENTA);
						paint.setStyle(Style.STROKE);
						paint.setStrokeWidth(1);
						rect = regionBoundingBoxes.get(i);
						canvas.drawRect(frame.left + rect.left * scaleX,
								frame.top + rect.top * scaleY, frame.left
										+ rect.right * scaleX, frame.top
										+ rect.bottom * scaleY, paint);
					}
				}

				if (DRAW_TEXTLINE_BOXES) {
					// Draw each textline
					textlineBoundingBoxes = resultText
							.getTextlineBoundingBoxes();
					paint.setAlpha(0xA0);
					paint.setColor(Color.RED);
					paint.setStyle(Style.STROKE);
					paint.setStrokeWidth(1);
					for (int i = 0; i < textlineBoundingBoxes.size(); i++) {
						rect = textlineBoundingBoxes.get(i);
						canvas.drawRect(frame.left + rect.left * scaleX,
								frame.top + rect.top * scaleY, frame.left
										+ rect.right * scaleX, frame.top
										+ rect.bottom * scaleY, paint);
					}
				}

				if (DRAW_STRIP_BOXES) {
					stripBoundingBoxes = resultText.getStripBoundingBoxes();
					paint.setAlpha(0xFF);
					paint.setColor(Color.YELLOW);
					paint.setStyle(Style.STROKE);
					paint.setStrokeWidth(1);
					for (int i = 0; i < stripBoundingBoxes.size(); i++) {
						rect = stripBoundingBoxes.get(i);
						canvas.drawRect(frame.left + rect.left * scaleX,
								frame.top + rect.top * scaleY, frame.left
										+ rect.right * scaleX, frame.top
										+ rect.bottom * scaleY, paint);
					}
				}

				if (DRAW_WORD_BOXES || DRAW_WORD_TEXT) {
					// Split the text into words
					wordBoundingBoxes = resultText.getWordBoundingBoxes();
					// for (String w : words) {
					// Log.e("ViewfinderView", "word: " + w);
					// }
					// Log.d("ViewfinderView", "There are " + words.length +
					// " words in the string array.");
					// Log.d("ViewfinderView", "There are " +
					// wordBoundingBoxes.size() +
					// " words with bounding boxes.");
				}

				if (DRAW_WORD_BOXES) {
					paint.setAlpha(0xFF);
					paint.setColor(0xFF00CCFF);
					paint.setStyle(Style.STROKE);
					paint.setStrokeWidth(1);
					for (int i = 0; i < wordBoundingBoxes.size(); i++) {
						// Draw a bounding box around the word
						rect = wordBoundingBoxes.get(i);
						canvas.drawRect(frame.left + rect.left * scaleX,
								frame.top + rect.top * scaleY, frame.left
										+ rect.right * scaleX, frame.top
										+ rect.bottom * scaleY, paint);
					}
				}

				if (DRAW_WORD_TEXT) {
					words = resultText.getText().replace("\n", " ").split(" ");
					int[] wordConfidences = resultText.getWordConfidences();
					for (int i = 0; i < wordBoundingBoxes.size(); i++) {
						boolean isWordBlank = true;
						try {
							if (!words[i].equals("")) {
								isWordBlank = false;
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							e.printStackTrace();
						}

						// Only draw if word has characters
						if (!isWordBlank) {
							// Draw a white background around each word
							rect = wordBoundingBoxes.get(i);
							paint.setColor(Color.WHITE);
							paint.setStyle(Style.FILL);
							if (DRAW_TRANSPARENT_WORD_BACKGROUNDS) {
								// Higher confidence = more opaque, less
								// transparent background
								paint.setAlpha(wordConfidences[i] * 255 / 100);
							} else {
								paint.setAlpha(255);
							}
							canvas.drawRect(frame.left + rect.left * scaleX,
									frame.top + rect.top * scaleY, frame.left
											+ rect.right * scaleX, frame.top
											+ rect.bottom * scaleY, paint);

							// Draw the word in black text
							paint.setColor(Color.BLACK);
							paint.setAlpha(0xFF);
							paint.setAntiAlias(true);
							paint.setTextAlign(Align.LEFT);

							// Adjust text size to fill rect
							paint.setTextSize(100);
							paint.setTextScaleX(1.0f);
							// ask the paint for the bounding rect if it were to
							// draw this text
							Rect bounds = new Rect();
							paint.getTextBounds(words[i], 0, words[i].length(),
									bounds);
							// get the height that would have been produced
							int h = bounds.bottom - bounds.top;
							// figure out what textSize setting would create
							// that height of text
							float size = (((float) (rect.height()) / h) * 100f);
							// and set it into the paint
							paint.setTextSize(size);
							// Now set the scale.
							// do calculation with scale of 1.0 (no scale)
							paint.setTextScaleX(1.0f);
							// ask the paint for the bounding rect if it were to
							// draw this text.
							paint.getTextBounds(words[i], 0, words[i].length(),
									bounds);
							// determine the width
							int w = bounds.right - bounds.left;
							// calculate the baseline to use so that the entire
							// text is visible including the descenders
							int text_h = bounds.bottom - bounds.top;
							int baseline = bounds.bottom
									+ ((rect.height() - text_h) / 2);
							// determine how much to scale the width to fit the
							// view
							float xscale = ((float) (rect.width())) / w;
							// set the scale for the text paint
							paint.setTextScaleX(xscale);
							canvas.drawText(words[i], frame.left + rect.left
									* scaleX, frame.top + rect.bottom * scaleY
									- baseline, paint);
						}

					}
				}
			}

		}*/
		// Draw a two pixel solid border inside the framing rect
		paint.setAlpha(0);
		paint.setStyle(Style.FILL);
		paint.setColor(frameColor);
		canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2,
				paint);
		canvas.drawRect(frame.left, frame.top + 2, frame.left + 2,
				frame.bottom - 1, paint);
		canvas.drawRect(frame.right - 1, frame.top, frame.right + 1,
				frame.bottom - 1, paint);
		canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1,
				frame.bottom + 1, paint);

		// Draw the framing rect corner UI elements
		paint.setColor(cornerColor);
		canvas.drawRect(frame.left - 15, frame.top - 15, frame.left + 15,
				frame.top, paint);
		canvas.drawRect(frame.left - 15, frame.top, frame.left, frame.top + 15,
				paint);
		canvas.drawRect(frame.right - 15, frame.top - 15, frame.right + 15,
				frame.top, paint);
		canvas.drawRect(frame.right, frame.top - 15, frame.right + 15,
				frame.top + 15, paint);
		canvas.drawRect(frame.left - 15, frame.bottom, frame.left + 15,
				frame.bottom + 15, paint);
		canvas.drawRect(frame.left - 15, frame.bottom - 15, frame.left,
				frame.bottom, paint);
		canvas.drawRect(frame.right - 15, frame.bottom, frame.right + 15,
				frame.bottom + 15, paint);
		canvas.drawRect(frame.right, frame.bottom - 15, frame.right + 15,
				frame.bottom + 15, paint);

		// Request another update at the animation interval, but don't repaint
		// the entire viewfinder mask.
		// postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
		// frame.right, frame.bottom);
	}

}