package com.secure.sudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

public class puzzle extends AppCompatActivity {
    private  char[] sampleGrid;
    private Adapter adapter;
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        gridView = findViewById(R.id.grid_view);
        Bundle b = getIntent().getExtras();
        sampleGrid = new char[81];

        sampleGrid = b.getString("no").toCharArray();
        adapter = new Adapter(this);
        adapter.initPuzzle(sampleGrid.clone());
        gridView.setAdapter(adapter);
    }
    private boolean isFull(char[] chars){
        for(char aChar : chars){
            if(aChar == '.')
                return false;
        }
        return true;
    }
    private int getTrialCelli(char[] chars){
        for(int i=0; i<chars.length; i++){
            if(chars[i]=='.'){
                return i;
            }
        }
        return -1;
    }
    private boolean isLegal(int tV, int tC, char[] grid){
        int col = 0;
        for(int sq=0; sq<9; sq++){
            int[] tSq = new int[9];
            for(int i=0; i<3; i++){
                tSq[i] = i+col;
            }
            for(int i=0; i<3; i++){
                tSq[i+3] = i+col+9;
            }
            for(int i=0; i<3; i++){
                tSq[i+6] = i+col+18;
            }
            col+= 3;
            if(col == 9 || col == 36){
                col+= 18;
            }
            for(int i=0; i<tSq.length; i++){
                if(tC == tSq[i]){
                    for(int j=0; j<tSq.length; j++){
                        if(grid[tSq[i]]!='.'){
                            if(tV == Integer.parseInt(String.valueOf(grid[tSq[j]]))){
                                Log.e("In solving", Integer.parseInt(String.valueOf(grid[tSq[j]])) + " " + j);
                                return false;
                            }
                        }
                    }
                    i = tSq.length + 10;
                }
            }
        }
        for(int r=0; r<9; r++){
            int[] tR = new int[9];
            for(int i=0; i<9; i++){
                tR[i] = i+(9*r);
            }
            boolean flag = false;
            for(int i=0; i<tR.length; i++){
                if(tC == tR[i]){
                    flag = true;
                    i = tR.length + 10;
                }
            }
            if(flag){
                for(int i=0; i<tR.length; i++){
                    if(grid[tR[i]]!='.'){
                        Log.e("In solving", Integer.parseInt(String.valueOf(grid[tR[i]])) + "" + i);
                        if(tV == Integer.parseInt(String.valueOf(grid[tR[i]]))){
                            return false;
                        }
                    }
                }
            }
        }
        for(int eC=0; eC<9; eC++){
            int[] tCol = new int[9];
            for(int i=0; i<9; i++){
                tCol[i] = (9*i)+eC;
            }
            boolean flag = false;
            for(int i=0; i<tCol.length; i++){
                if(tC == tCol[i]){
                    flag = true;
                    i = tCol.length + 10;
                }
            }
            if(flag){
                for(int i: tCol){
                    if(grid[i] != '.'){
                        if (tV == Integer.parseInt(grid[i] + "")) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    private char[] setCell(int val, int cell, char[] grid){
        grid[cell]=(char)(val+'0');
        return grid;
    }
    private char[] clearCell(int cell,char[] grid){
        grid[cell]='.';
        return grid;
    }
    private boolean hasSolution(char[] grid){
        if (isFull(grid)){
            Log.e("In solving","Solved");
            sampleGrid=grid;
            for (char aGrid : grid) Log.d("In solving", aGrid + "");
            return true;
        }
        else {
            int trialCelli = getTrialCelli(grid);
            int trialVal = 1;
            boolean solution_found = false;
            while (!solution_found && trialVal < 10){
                if (isLegal(trialVal,trialCelli,grid)){
                    grid=setCell(trialVal,trialCelli,grid);
                    if(hasSolution(grid)) {
                        solution_found = true;
                        return true;
                    }
                    else {
                        grid=clearCell(trialCelli,grid);
                    }
                }
                trialVal++;
            }
            return solution_found;
        }
    }
    public void solve(View view){
        if (hasSolution (sampleGrid)){
            adapter.initPuzzle(sampleGrid);
            adapter.notifyDataSetChanged();
        }
        else
            Toast.makeText(this,"Can't find solution",Toast.LENGTH_SHORT).show();
    }

    public void update(View view) {
        StringBuilder no= new StringBuilder();
        for (int i=0;i<adapter.getCount();i++)
            no.append(adapter.getItem(i));
        sampleGrid=no.toString().toCharArray();
        adapter.initPuzzle(sampleGrid.clone());
        adapter.notifyDataSetChanged();
    }
}