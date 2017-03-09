package com.github.anastr.speedviewlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.github.anastr.speedviewlib.components.Indicators.SpindleIndicator;

/**
 * this Library build By Anas Altair
 * see it on <a href="https://github.com/anastr/SpeedView">GitHub</a>
 */
public class PointerSpeedometer extends Speedometer {

    private Path markPath = new Path();
    private Paint speedometerPaint = new Paint(Paint.ANTI_ALIAS_FLAG),
            pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG),
            pointerBackPaint = new Paint(Paint.ANTI_ALIAS_FLAG),
            circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG),
            markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF speedometerRect = new RectF();

    private int speedometerColor = Color.parseColor("#eeeeee")
            , pointerColor = Color.WHITE;

    public PointerSpeedometer(Context context) {
        this(context, null);
    }

    public PointerSpeedometer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PointerSpeedometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initAttributeSet(context, attrs);
    }

    @Override
    protected void defaultValues() {
        super.setIndicator(new SpindleIndicator(getContext()));
        super.setIndicatorWidth(dpTOpx(16f));
        super.setIndicatorColor(Color.WHITE);
        super.setMarkColor(Color.WHITE);
        super.setTextColor(Color.WHITE);
        super.setCenterCircleColor(Color.WHITE);
        super.setSpeedTextColor(Color.WHITE);
        super.setSpeedTextSize(dpTOpx(24f));
        super.setSpeedometerWidth(dpTOpx(10f));
        super.setBackgroundCircleColor(Color.parseColor("#48cce9"));
        super.setUnitTextSize(dpTOpx(11f));
        super.setSpeedTextTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    private void init() {
        speedometerPaint.setStyle(Paint.Style.STROKE);
        speedometerPaint.setStrokeCap(Paint.Cap.ROUND);
        markPaint.setStyle(Paint.Style.STROKE);
        markPaint.setStrokeCap(Paint.Cap.ROUND);
        markPaint.setStrokeWidth(dpTOpx(2));
    }

    private void initAttributeSet(Context context, AttributeSet attrs) {
        if (attrs == null) {
            initAttributeValue();
            return;
        }
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PointerSpeedometer, 0, 0);

        speedometerColor = a.getColor(R.styleable.PointerSpeedometer_speedometerColor, speedometerColor);
        pointerColor = a.getColor(R.styleable.PointerSpeedometer_pointerColor, pointerColor);
        a.recycle();
        initAttributeValue();
    }

    private void initAttributeValue() {
        pointerPaint.setColor(pointerColor);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        float risk = getSpeedometerWidth()/2f + dpTOpx(8) + getPadding();
        speedometerRect.set(risk, risk, getSize() -risk, getSize() -risk);

        updateRadial();
        updateBackgroundBitmap();
    }

    private void initDraw() {
        speedometerPaint.setStrokeWidth(getSpeedometerWidth());
        speedometerPaint.setShader(updateSweep());
        markPaint.setColor(getMarkColor());
        circlePaint.setColor(getCenterCircleColor());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initDraw();

        canvas.drawArc(speedometerRect, getStartDegree(),  getEndDegree()- getStartDegree(), false, speedometerPaint);

        canvas.save();
        canvas.rotate(90 + getDegree(), getSize()/2f, getSize()/2f);
        canvas.drawCircle(getSize()/2f, getSpeedometerWidth()/2f + dpTOpx(8) + getPadding()
                , getSpeedometerWidth()/2f + dpTOpx(8), pointerBackPaint);
        canvas.drawCircle(getSize()/2f, getSpeedometerWidth()/2f + dpTOpx(8) + getPadding()
                , getSpeedometerWidth()/2f + dpTOpx(1), pointerPaint);
        canvas.restore();

        drawSpeedUnitText(canvas);
        drawIndicator(canvas);

        int c = getCenterCircleColor();
        circlePaint.setColor(Color.argb(120, Color.red(c), Color.green(c), Color.blue(c)));
        canvas.drawCircle(getSize()/2f, getSize()/2f, getWidthPa()/14f, circlePaint);
        circlePaint.setColor(c);
        canvas.drawCircle(getSize()/2f, getSize()/2f, getWidthPa()/22f, circlePaint);

        drawNotes(canvas);
    }

    @Override
    protected void updateBackgroundBitmap() {
        Canvas c = createBackgroundBitmapCanvas();
        initDraw();

        markPath.reset();
        markPath.moveTo(getSize()/2f, getSpeedometerWidth() + dpTOpx(8) + dpTOpx(4) + getPadding());
        markPath.lineTo(getSize()/2f, getSpeedometerWidth() + dpTOpx(8) + dpTOpx(4) + getPadding() + getSize()/60);

        c.save();
        c.rotate(90f + getStartDegree(), getSize()/2f, getSize()/2f);
        float everyDegree = (getEndDegree() - getStartDegree()) * .111f;
        for (float i = getStartDegree(); i < getEndDegree()-(2f*everyDegree); i+=everyDegree) {
            c.rotate(everyDegree, getSize()/2f, getSize()/2f);
            c.drawPath(markPath, markPaint);
        }
        c.restore();

        drawDefMinMaxSpeedPosition(c);
    }

    private SweepGradient updateSweep() {
        int startColor = Color.argb(150, Color.red(speedometerColor), Color.green(speedometerColor), Color.blue(speedometerColor));
        int color2 = Color.argb(220, Color.red(speedometerColor), Color.green(speedometerColor), Color.blue(speedometerColor));
        int color3 = Color.argb(70, Color.red(speedometerColor), Color.green(speedometerColor), Color.blue(speedometerColor));
        int endColor = Color.argb(15, Color.red(speedometerColor), Color.green(speedometerColor), Color.blue(speedometerColor));
        float position = getOffsetSpeed() * (getEndDegree() - getStartDegree())/360f;
        SweepGradient sweepGradient = new SweepGradient(getSize()/2f, getSize()/2f
                , new int[]{startColor, color2, speedometerColor, color3, endColor, startColor}
                , new float[]{0f, position/2f, position, position, .99f, 1f});
        Matrix matrix = new Matrix();
        matrix.postRotate(getStartDegree(), getSize()/2f, getSize()/2f);
        sweepGradient.setLocalMatrix(matrix);
        return sweepGradient;
    }

    private void updateRadial() {
        int centerColor = Color.argb(160, Color.red(pointerColor), Color.green(pointerColor), Color.blue(pointerColor));
        int edgeColor = Color.argb(10, Color.red(pointerColor), Color.green(pointerColor), Color.blue(pointerColor));
        RadialGradient pointerGradient = new RadialGradient(getSize()/2f, getSpeedometerWidth()/2f + dpTOpx(8) + getPadding()
                , getSpeedometerWidth()/2f + dpTOpx(8), new int[]{centerColor, edgeColor}
                , new float[]{.4f, 1f}, Shader.TileMode.CLAMP);
        pointerBackPaint.setShader(pointerGradient);
    }

    public int getSpeedometerColor() {
        return speedometerColor;
    }

    public void setSpeedometerColor(int speedometerColor) {
        this.speedometerColor = speedometerColor;
        invalidate();
    }

    public int getPointerColor() {
        return pointerColor;
    }

    public void setPointerColor(int pointerColor) {
        this.pointerColor = pointerColor;
        pointerPaint.setColor(pointerColor);
        updateRadial();
        invalidate();
    }

    /**
     * this Speedometer doesn't use this method.
     * @return {@code Color.TRANSPARENT} always.
     */
    @Deprecated
    @Override
    public int getLowSpeedColor() {
        return Color.TRANSPARENT;
    }

    /**
     * this Speedometer doesn't use this method.
     * @param lowSpeedColor nothing.
     */
    @Deprecated
    @Override
    public void setLowSpeedColor(int lowSpeedColor) {
    }

    /**
     * this Speedometer doesn't use this method.
     * @return {@code Color.TRANSPARENT} always.
     */
    @Deprecated
    @Override
    public int getMediumSpeedColor() {
        return Color.TRANSPARENT;
    }

    /**
     * this Speedometer doesn't use this method.
     * @param mediumSpeedColor nothing.
     */
    @Deprecated
    @Override
    public void setMediumSpeedColor(int mediumSpeedColor) {
    }

    /**
     * this Speedometer doesn't use this method.
     * @return {@code Color.TRANSPARENT} always.
     */
    @Deprecated
    @Override
    public int getHighSpeedColor() {
        return Color.TRANSPARENT;
    }

    /**
     * this Speedometer doesn't use this method.
     * @param highSpeedColor nothing.
     */
    @Deprecated
    @Override
    public void setHighSpeedColor(int highSpeedColor) {
    }
}
