package com.secure.sudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.GridView;

import com.secure.puzzle_solver.Puzzle;
import com.secure.puzzle_solver.Solver;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    private Puzzle puzzle;

    public void updatePuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
        GridView gridView = (GridView) findViewById(R.id.gridView1);
        PuzzleAdaptor puzzleAdapter = new PuzzleAdaptor(this, this.puzzle);
        gridView.setAdapter(puzzleAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPuzzleOrGetFromExtras();

        updatePuzzle(puzzle);
    }
    private void initPuzzleOrGetFromExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.get("Puzzle") != null) {
            puzzle = new Puzzle((Integer[][]) bundle.get("Puzzle"));
        } else {
            puzzle = new Puzzle();
        }
    }

    public void TakeAPicture(View v) throws Exception {
        Intent takeAPictureIntent = new Intent(this, TakeAPictureActivity.class);
        startActivity(takeAPictureIntent);
    }

    public void SolvePuzzle(View v) throws Exception {
        Solver puzzleSolver = new Solver(this.puzzle);
        Puzzle solvedPuzzle = puzzleSolver.solvePuzzle();
        updatePuzzle(solvedPuzzle);

    }
}