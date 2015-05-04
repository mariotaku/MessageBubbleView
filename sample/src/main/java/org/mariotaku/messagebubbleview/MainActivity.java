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


public class MainActivity extends Activity implements OnItemSelectedListener, OnSeekBarChangeListener {

    private static final int[] POSITIONS = {MessageBubbleView.NONE, MessageBubbleView.TOP_LEFT,
            MessageBubbleView.TOP_RIGHT, MessageBubbleView.BOTTOM_LEFT, MessageBubbleView.BOTTOM_RIGHT,
            MessageBubbleView.TOP_START, MessageBubbleView.TOP_END, MessageBubbleView.BOTTOM_START,
            MessageBubbleView.BOTTOM_END};

    private MessageBubbleView bubble;
    private SeekBar seekBar;
    private Spinner spinner;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        spinner = (Spinner) findViewById(R.id.spinner);
        bubble = (MessageBubbleView) findViewById(R.id.bubble);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBar.setOnSeekBarChangeListener(this);
        spinner.setOnItemSelectedListener(this);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        bubble.setCaretPosition(POSITIONS[position]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

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
}
