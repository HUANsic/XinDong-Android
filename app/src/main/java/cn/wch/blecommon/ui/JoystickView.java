package cn.wch.blecommon.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
    
    // 摇杆中心点
    private float centerX;
    private float centerY;
    
    // 摇杆当前位置
    private float joystickX;
    private float joystickY;
    
    // 摇杆半径
    private float joystickRadius = 50f;
    
    // 摇杆活动范围半径
    private float maxRadius = 100f;
    
    // 画笔
    private Paint backgroundPaint;
    private Paint joystickPaint;
    private Paint borderPaint;
    
    // 摇杆状态监听器
    private OnJoystickMoveListener listener;
    
    // XY偏移量（-1.0 到 1.0）
    private float xOffset = 0f;
    private float yOffset = 0f;
    
    public interface OnJoystickMoveListener {
        void onJoystickMoved(float xOffset, float yOffset);
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
        // 初始化背景画笔
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);
        
        // 初始化摇杆画笔
        joystickPaint = new Paint();
        joystickPaint.setColor(Color.BLUE);
        joystickPaint.setStyle(Paint.Style.FILL);
        joystickPaint.setAntiAlias(true);
        
        // 初始化边框画笔
        borderPaint = new Paint();
        borderPaint.setColor(Color.DKGRAY);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);
        borderPaint.setAntiAlias(true);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // 计算中心点
        centerX = w / 2f;
        centerY = h / 2f;
        
        // 初始化摇杆位置为中心
        joystickX = centerX;
        joystickY = centerY;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制背景圆
        canvas.drawCircle(centerX, centerY, maxRadius, backgroundPaint);
        canvas.drawCircle(centerX, centerY, maxRadius, borderPaint);
        
        // 绘制摇杆
        canvas.drawCircle(joystickX, joystickY, joystickRadius, joystickPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // 计算摇杆位置
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distance <= maxRadius) {
                    // 在活动范围内
                    joystickX = x;
                    joystickY = y;
                } else {
                    // 超出范围，限制在最大半径内
                    float ratio = maxRadius / distance;
                    joystickX = centerX + dx * ratio;
                    joystickY = centerY + dy * ratio;
                }
                
                // 计算偏移量
                xOffset = (joystickX - centerX) / maxRadius;
                yOffset = (joystickY - centerY) / maxRadius;
                
                // 通知监听器
                if (listener != null) {
                    listener.onJoystickMoved(xOffset, yOffset);
                }
                
                invalidate();
                return true;
                
            case MotionEvent.ACTION_UP:
                // 松开时回到中心
                joystickX = centerX;
                joystickY = centerY;
                xOffset = 0f;
                yOffset = 0f;
                
                if (listener != null) {
                    listener.onJoystickMoved(xOffset, yOffset);
                }
                
                invalidate();
                return true;
        }
        
        return super.onTouchEvent(event);
    }
    
    public void setOnJoystickMoveListener(OnJoystickMoveListener listener) {
        this.listener = listener;
    }
    
    // 获取X偏移量
    public float getXOffset() {
        return xOffset;
    }
    
    // 获取Y偏移量
    public float getYOffset() {
        return yOffset;
    }
    
    // 设置摇杆颜色
    public void setJoystickColor(int color) {
        joystickPaint.setColor(color);
        invalidate();
    }
    
    // 设置背景颜色
    public void setBackgroundColor(int color) {
        backgroundPaint.setColor(color);
        invalidate();
    }
}
