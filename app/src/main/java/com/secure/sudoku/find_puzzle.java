package com.secure.sudoku;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class find_puzzle {
    Mat rgbc;
    List<MatOfPoint> contour;
    int puzzle_num;
    boolean wrap;

    find_puzzle(Mat frame){
        rgbc = frame.clone();
        Mat temp = new Mat();
        Imgproc.cvtColor(rgbc, temp, Imgproc.COLOR_RGB2GRAY);
        Mat rgbg = temp.clone();
        Imgproc.Canny(rgbg, temp, 150, 255);
        Imgproc.GaussianBlur(temp, temp, new Size(5,5), 0);
        Mat rgb_blur = temp.clone();
        contour = new ArrayList<>();
        Imgproc.findContours(rgb_blur, contour, new Mat(), Imgproc.CV_SHAPE_RECT, Imgproc.CHAIN_APPROX_SIMPLE);
        puzzle_num = -1;
        wrap = false;
    }

    private List<MatOfPoint> sortBasedOnArea(List<MatOfPoint> input){
        Double[] area = new Double[input.size()];
        MatOfPoint[] ip = new MatOfPoint[input.size()];
        for(int i=0; i<input.size(); i++){
            double contourArea = Imgproc.contourArea(contour.get(i));
            area[i] = contourArea;
        }
        for(int i=0; i<input.size()-1; i++){
            int index = i;
            for(int j=0; j<input.size(); j++){
                if(area[j]>area[index]){
                    index = j;
                }
            }
            Double smallerNumber = area[index];
            area[index] = area[i];
            area[i] = smallerNumber;
            MatOfPoint smaller = ip[index];
            ip[index] = ip[i];
            ip[i] = smaller;
        }
        return new ArrayList<>(Arrays.asList(ip).subList(0, input.size()));
    }

    private MatOfPoint approx(MatOfPoint cnt){
        MatOfPoint2f double_max_area_contours = new MatOfPoint2f(cnt.toArray());
        double peri = Imgproc.arcLength(double_max_area_contours, true);
        MatOfPoint app = new MatOfPoint();
        Imgproc.approxPolyDP(double_max_area_contours, app, 0.01*peri, true);
        return app;
    }

    private List<Point> get_rectangle_corners(MatOfPoint img){
        double[] temp_double = img.get(0, 0);
        Point p1 = new Point(temp_double[0], temp_double[1]);
        temp_double = img.get(1, 0);
        Point p2 = new Point(temp_double[0], temp_double[1]);
        temp_double = img.get(2, 0);
        Point p3 = new Point(temp_double[0], temp_double[1]);
        temp_double = img.get(3, 0);
        Point p4 = new Point(temp_double[0], temp_double[1]);
        List<Point> source = new ArrayList<>();
        source.add(p1);
        source.add(p2);
        source.add(p3);
        source.add(p4);
        return source;
    }

    private Mat warp(Mat inputMat, Mat startM){
        int resultWidth = 1000;
        int resultHeight = 1000;

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);
        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(0, resultHeight);
        Point ocvPOut3 = new Point(resultWidth, resultHeight);
        Point ocvPOut4 = new Point(resultWidth, 0);
        List<Point> dest = new ArrayList<>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);
        Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform, new Size(resultWidth, resultHeight), Imgproc.INTER_CUBIC);
        return outputMat;
    }

    Mat getPuzzle(boolean wraped){
        Log.d("In PuzzleFinder", "starting getPuzzle");
        contour = sortBasedOnArea(contour);
        int optimizedLen = (contour.size()<5)?contour.size():5;
        System.out.println("Optimized length is" + optimizedLen);
        Mat temp = rgbc.clone();
        for(int i=0; i<optimizedLen; i++){
            System.out.print("Contour is");
            System.out.println(approx(contour.get(i)).dump());
            System.out.print("First element is");
            for(int il=0; il<approx(contour.get(i)).get(0, 0).length; il++)
                System.out.println(approx(contour.get(i)).get(0, 0)[i]);
            System.out.print("size is");
            System.out.println(approx(contour.get(i)).rows());
            if(approx(contour.get(i)).total() == 4){
                puzzle_num = i;
            }
        }
        System.out.println("---------------------------------------------");
        Imgproc.drawContours(temp, contour, puzzle_num, new Scalar(0,255,0),2);
        if(puzzle_num!=-1 && wraped){
            MatOfPoint rect = approx(contour.get(puzzle_num));
            Log.d("In PuzzleFinder", "starting get_rect");
            List<Point>corners = get_rectangle_corners(rect);
            Mat startM = Converters.vector_Point2d_to_Mat(corners);
            Mat result = warp(temp, startM);
            wrap = true;
            return result;
        }
        return temp;
    }
}
