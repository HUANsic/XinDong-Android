package cn.wch.blecommon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
    private Paint backgroundPaint;
    private Paint knobPaint;
    private PointF center;
    private PointF knobPosition;
    private float radius;
    private float knobRadius;
    private OnJoystickMoveListener listener;
    
    public interface OnJoystickMoveListener {
        void onJoystickMoved(float x, float y);
    }
    
    public JoystickView(Context context) {
        super(context);
        init();
    }
    
    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFFE0E0E0);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);
        
        knobPaint = new Paint();
        knobPaint.setColor(0xFF2196F3);
        knobPaint.setStyle(Paint.Style.FILL);
        knobPaint.setAntiAlias(true);
        
        center = new PointF();
        knobPosition = new PointF();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        center.set(w / 2f, h / 2f);
        knobPosition.set(center);
        radius = Math.min(w, h) / 2f - 20;
        knobRadius = 15;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制背景圆
        canvas.drawCircle(center.x, center.y, radius, backgroundPaint);
        
        // 绘制摇杆旋钮
        canvas.drawCircle(knobPosition.x, knobPosition.y, knobRadius, knobPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // 计算距离中心的距离
                float distance = (float) Math.sqrt(Math.pow(x - center.x, 2) + Math.pow(y - center.y, 2));
                
                if (distance <= radius) {
                    // 在圆内，直接设置位置
                    knobPosition.set(x, y);
                } else {
                    // 在圆外，限制在圆周上
                    float angle = (float) Math.atan2(y - center.y, x - center.x);
                    knobPosition.set(
                        center.x + radius * (float) Math.cos(angle),
                        center.y + radius * (float) Math.sin(angle)
                    );
                }
                
                // 计算偏移量 (-1.0 到 1.0)
                float offsetX = (knobPosition.x - center.x) / radius;
                float offsetY = (knobPosition.y - center.y) / radius;
                
                if (listener != null) {
                    listener.onJoystickMoved(offsetX, offsetY);
                }
                
                invalidate();
                break;
                
            case MotionEvent.ACTION_UP:
                // 松开时回到中心
                knobPosition.set(center);
                if (listener != null) {
                    listener.onJoystickMoved(0, 0);
                }
                invalidate();
                break;
        }
        
        return true;
    }
    
    public void setOnJoystickMoveListener(OnJoystickMoveListener listener) {
        this.listener = listener;
    }
}
