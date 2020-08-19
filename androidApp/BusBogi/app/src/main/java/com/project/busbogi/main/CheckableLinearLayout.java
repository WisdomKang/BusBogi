package com.project.busbogi.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;

import com.project.busbogi.R;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
    public CheckableLinearLayout(Context context , AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    public void setChecked(boolean b) {
        CheckBox checkBox = findViewById(R.id.checkBox);
        checkBox.setChecked(b);
    }

    @Override
    public boolean isChecked() {
        CheckBox checkBox = findViewById(R.id.checkBox);
        return checkBox.isChecked();
    }

    private Path path;
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (path == null) {
            path = new Path();
            path.addRoundRect(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), 10, 10, Path.Direction.CW);
        }
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
    }

    @Override
    public void toggle() {
        CheckBox checkBox = findViewById(R.id.checkBox);
        setChecked( checkBox.isChecked() ? false : true );
    }
}
