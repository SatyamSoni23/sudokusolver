package com.secure.sudoku;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.util.List;

public class find_puzzle {
    Mat rgba;
    List<MatOfPoint> contour;
    int puzzle_num;
    boolean wrap;

    find_puzzle(Mat frame){
        rgba = frame.clone();

    }
}
