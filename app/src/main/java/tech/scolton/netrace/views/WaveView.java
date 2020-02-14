package tech.scolton.netrace.views;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import tech.scolton.netrace.MainActivity;
import tech.scolton.netrace.R;

public class WaveView extends View {
  private Paint foregroundWavePaint;
  private Paint backgroundWavePaint;

  private final int FOREGROUND_WAVELENGTH = 300;
  private final int BACKGROUND_WAVELENGTH = 500;

  private int canvasWidth = 0;
  private int canvasHeight = 0;

  private int foregroundWaveOffset = 0;
  private int backgroundWaveOffset = 0;

  private final Path wavePath = new Path();
  private final Rect waveBase = new Rect();

  public WaveView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.init();
  }

  private void init() {
    this.backgroundWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.backgroundWavePaint.setColor(getResources().getColor(R.color.backgroundWave));
    this.backgroundWavePaint.setStyle(Paint.Style.FILL);

    this.foregroundWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.foregroundWavePaint.setColor(getResources().getColor(R.color.foregroundWave));
    this.foregroundWavePaint.setStyle(Paint.Style.FILL);

    ValueAnimator foregroundWaveAnimator = ValueAnimator.ofInt(0, FOREGROUND_WAVELENGTH);
    ValueAnimator backgroundWaveAnimator = ValueAnimator.ofInt(0, BACKGROUND_WAVELENGTH);

    foregroundWaveAnimator.setDuration(8000);
    backgroundWaveAnimator.setDuration(18000);

    foregroundWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        WaveView.this.foregroundWaveOffset = (int) animation.getAnimatedValue();
        WaveView.this.invalidate();
      }
    });

    backgroundWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        WaveView.this.backgroundWaveOffset = (int) animation.getAnimatedValue();
        WaveView.this.invalidate();
      }
    });

    foregroundWaveAnimator.setRepeatCount(ValueAnimator.INFINITE);
    backgroundWaveAnimator.setRepeatCount(ValueAnimator.INFINITE);

    foregroundWaveAnimator.setInterpolator(new LinearInterpolator());
    backgroundWaveAnimator.setInterpolator(new LinearInterpolator());

    foregroundWaveAnimator.start();
    backgroundWaveAnimator.start();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.drawColor(getResources().getColor(R.color.colorPrimary));

    drawWave(canvas, (int) (canvasHeight * 0.88), BACKGROUND_WAVELENGTH, this.backgroundWavePaint,
             this.backgroundWaveOffset);
    drawWave(canvas, (int) (canvasHeight * 0.95), FOREGROUND_WAVELENGTH, this.foregroundWavePaint,
             this.foregroundWaveOffset);
  }

  @Override
  protected void onSizeChanged(int w, int h, int old_w, int old_h) {
    this.canvasWidth = w;
    this.canvasHeight = h;

    int bottom = (int) (h * 0.12) + BACKGROUND_WAVELENGTH / 8 + 20;

    Context c = getContext();
    while (c instanceof ContextWrapper) {
      if (c instanceof Activity) break;
      c = ((ContextWrapper) c).getBaseContext();
    }

    if (!(c instanceof MainActivity)) return;

    MainActivity activity = (MainActivity) c;
    activity.updateHolderConstraint(bottom);

    post(new Runnable() {
      @Override
      public void run() {
        WaveView.this.requestLayout();
      }
    });
  }

  private void drawWave(Canvas canvas, int y, int wavelength, Paint paint, int offset) {
    int cycles = ((int) Math.ceil(this.canvasWidth / (double) wavelength)) + 2;

    int halfWavelength = wavelength / 2;
    int quarterWavelength = halfWavelength / 2;
    int eighthWaveLength = quarterWavelength / 2;

    this.wavePath.reset();

    for (int i = -1; i < cycles; i++) {
      int start = i * wavelength - offset;

      this.wavePath.moveTo(start, y);
      this.wavePath
              .quadTo(start + quarterWavelength, y - eighthWaveLength, start + halfWavelength, y);
      this.wavePath
              .quadTo(start + 3 * quarterWavelength, y + eighthWaveLength, start + wavelength, y);

      this.wavePath.lineTo(start + wavelength, y + halfWavelength);
      this.wavePath.lineTo(start, y + halfWavelength);
      this.wavePath.close();

      canvas.drawPath(this.wavePath, paint);
    }

    this.waveBase.set(0, y + eighthWaveLength, this.canvasWidth, this.canvasHeight);
    canvas.drawRect(this.waveBase, paint);
  }
}
