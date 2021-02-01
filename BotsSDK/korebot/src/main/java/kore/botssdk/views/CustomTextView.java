package kore.botssdk.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import kore.korebotsdklib.R;

public class CustomTextView extends AppCompatTextView {

    String customFont;

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        style(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        style(context, attrs);

    }

    private void style(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBar);
        Typeface tfRegular = ResourcesCompat.getFont(context, R.font.latomediu);
        setTypeface(tfRegular);
        a.recycle();
    }
}
