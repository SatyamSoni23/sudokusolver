package com.secure.sudoku;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

public class Adapter extends BaseAdapter {
    private char num[];
    private boolean filled;
    private Context mContext;
    private int temp_ui;

    @Override
    public int getCount() {
        return num.length;
    }

    @Override
    public Object getItem(int i) {
        return num[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        EditText text;
        if(filled){
            if(view == null){
                text = new EditText(mContext);
                text.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
                text.setPadding(8,8,8,8);
                if((temp_ui%9 == 3 || temp_ui%9==4 || temp_ui%9==5) && (temp_ui<26 || temp_ui>54)){
                    text.setBackgroundResource(R.drawable.rect);
                }
                else{
                    text.setBackgroundResource(R.drawable.red_rect);
                }
                temp_ui++;
                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
            else
                text = (EditText) view;
            Log.d("Puzzle Adapter", num[i]+" for "+i);
            if(num[i]!='.')
                text.setText(num[i]+"");
            else
                text.setText("");
            text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    changeText(s, i);
                }
            });
            return text;
        }
        else
            return null;
    }

    Adapter(Context context){
        num = new char[81];
        mContext = context;
        filled = false;
        temp_ui = 0;
    }

    public void initPuzzle(char[] num){
        this.num = num;
        filled = true;
    }

    private void changeText(Editable editable, int i){
        Log.d("setting", i+"");
        if(editable.toString().toCharArray().length!=0)
            num[i] = editable.toString().toCharArray()[0];
    }
}
