package com.project.busbogi.main;

import android.content.Context;
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

    @Override
    public void toggle() {
        CheckBox checkBox = findViewById(R.id.checkBox);
        setChecked( checkBox.isChecked() ? false : true );
    }
}
