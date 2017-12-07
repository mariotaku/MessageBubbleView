package org.mariotaku.messagebubbleview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;

import org.mariotaku.messagebubbleview.library.MessageBubbleView;


public class MainActivity extends Activity {

    private static final int[] POSITIONS = {
            MessageBubbleView.NONE,
            MessageBubbleView.TOP | MessageBubbleView.LEFT,
            MessageBubbleView.TOP | MessageBubbleView.RIGHT,
            MessageBubbleView.BOTTOM | MessageBubbleView.LEFT,
            MessageBubbleView.BOTTOM | MessageBubbleView.RIGHT,
            MessageBubbleView.TOP | MessageBubbleView.START,
            MessageBubbleView.TOP | MessageBubbleView.END,
            MessageBubbleView.BOTTOM | MessageBubbleView.START,
            MessageBubbleView.BOTTOM | MessageBubbleView.END
    };

    private MessageBubbleView bubble;
    private SeekBar seekBar;
    private SeekBar widthSeekBar;
    private Spinner spinner;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        seekBar = findViewById(R.id.seek_bar);
        widthSeekBar = findViewById(R.id.width_seek_bar);
        spinner = findViewById(R.id.spinner);
        bubble = findViewById(R.id.bubble);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bubble.setCornerRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        widthSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bubble.setWrapContentMaxWidthPercent(progress / (float) seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bubble.setCaretPosition(POSITIONS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(indexOf(POSITIONS, bubble.getCaretPosition()));
        seekBar.setProgress((int) bubble.getCornerRadius());
        widthSeekBar.setProgress((int) (bubble.getWrapContentMaxWidthPercent() * widthSeekBar.getMax()));
    }

    private int indexOf(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) return i;
        }
        return -1;
    }

}
