package com.secure.puzzle_scanner;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static com.secure.puzzle_scanner.Constants.BLACK;
import static com.secure.puzzle_scanner.Constants.GREY;
import static com.secure.puzzle_scanner.Constants.WHITE;
import static org.opencv.imgproc.Imgproc.MARKER_TILTED_CROSS;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class PuzzleFinder {

    private Mat originalMat;
    private Mat greyMat;
    private Mat thresholdMat;
    private Mat largestBlobMat;
    private Mat houghLinesMat;
    private Mat outLineMat;

    PuzzleFinder(Mat mat){
        originalMat = mat;
    }

    Mat getGreyMat(){
        if(greyMat == null){
            generateGreyMat();
        }
        return greyMat;
    }

    private void generateGreyMat(){
        greyMat = originalMat.clone();
        Imgproc.cvtColor(originalMat, greyMat, Imgproc.COLOR_RGB2GRAY);
    }

    Mat getThresholdMat(){
        if(thresholdMat == null){
            generateThresholdMat();
        }
        return thresholdMat;
    }

    private void generateThresholdMat() {
        thresholdMat = getGreyMat().clone();
        Imgproc.adaptiveThreshold(thresholdMat, thresholdMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 7, 5);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(2, 2));
        Imgproc.erode(thresholdMat, thresholdMat, kernel);
        Mat kernelDil = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(2, 2));
        Imgproc.dilate(thresholdMat, thresholdMat, kernelDil);
        Core.bitwise_not(thresholdMat, thresholdMat);
    }

    Mat getLargestBlobMat() {
        if (largestBlobMat == null) {
            generateLargestBlobMat();
        }
        return largestBlobMat;
    }

    private void generateLargestBlobMat(){
        largestBlobMat = getThresholdMat().clone();
        int height = largestBlobMat.height();
        int width = largestBlobMat.width();

        Point maxBlobOrigin = new Point(0, 0);

        int maxBlobSize = 0;
        Mat greyMask = new Mat(height + 2, width + 2, CvType.CV_8U, new Scalar(0, 0, 0));
        Mat blackMask = new Mat(height + 2, width + 2, CvType.CV_8U, new Scalar(0, 0, 0));
        for (int y = 0; y < height; y++) {
            Mat row = largestBlobMat.row(y);
            for (int x = 0; x < width; x++) {
                double[] value = row.get(0, x);
                Point currentPoint = new Point(x, y);

                if (value[0] > Constants.THRESHOLD) {
                    int blobSize = Imgproc.floodFill(largestBlobMat, greyMask, currentPoint, GREY);
                    if (blobSize > maxBlobSize) {
                        Imgproc.floodFill(largestBlobMat, blackMask, maxBlobOrigin, BLACK);
                        maxBlobOrigin = currentPoint;
                        maxBlobSize = blobSize;
                    } else {
                        Imgproc.floodFill(largestBlobMat, blackMask, currentPoint, BLACK);
                    }
                }
            }
        }
        Mat largeBlobMask = new Mat(height + 2, width + 2, CvType.CV_8U, BLACK);
        Imgproc.floodFill(largestBlobMat, largeBlobMask, maxBlobOrigin, WHITE);
    }

    Mat getHoughLinesMat() {
        if (houghLinesMat == null)
            generateHoughLinesMat();
        return houghLinesMat;
    }

    private void generateHoughLinesMat() {

        houghLinesMat = getLargestBlobMat().clone();

        List<Line> houghLines = getHoughLines();
        for (Line line : houghLines) {
            Imgproc.line(houghLinesMat, line.origin, line.destination, GREY);
        }
    }

    private List<Line> getHoughLines() {
        Mat linesMat = getLargestBlobMat().clone();
        Mat largestBlobMat = getLargestBlobMat();
        int width = largestBlobMat.width();
        int height = largestBlobMat.height();
        Imgproc.HoughLines(largestBlobMat, linesMat, (double) 1, Math.PI / 180, 400);
        List<Line> houghLines = new ArrayList<>();
        int lines = linesMat.rows();
        for (int x = 0; x < lines; x++) {
            double[] vec = linesMat.get(x, 0);
            Vector vector = new Vector(vec[0], vec[1]);
            Line line = new Line(vector, height, width);

            houghLines.add(line);
        }
        return houghLines;
    }

    PuzzleOutLine findOutLine() throws PuzzleNotFoundException {

        PuzzleOutLine location = new PuzzleOutLine();

        int height = getLargestBlobMat().height();
        int width = getLargestBlobMat().width();

        int countHorizontalLines = 0;
        int countVerticalLines = 0;

        List<Line> houghLines = getHoughLines();

        for (Line line : houghLines) {
            if (line.getOrientation() == Orientation.horizontal) {
                countHorizontalLines++;

                if (location.top == null) {
                    location.top = line;
                    location.bottom = line;
                    continue;
                }

                if (line.getAngleFromXAxis() > 6)
                    continue;
                if (line.getAngleFromXAxis() < 1 && (line.getMinY() < 5 || line.getMaxY() > height - 5))
                    continue;

                if (line.getMinY() < location.bottom.getMinY())
                    location.bottom = line;
                if (line.getMaxY() > location.top.getMaxY())
                    location.top = line;
            } else if (line.getOrientation() == Orientation.vertical) {
                countVerticalLines++;

                if (location.left == null) {
                    location.left = line;
                    location.right = line;
                    continue;
                }

                if (line.getAngleFromXAxis() < 84)
                    continue;
                if (line.getAngleFromXAxis() > 89 && (line.getMinX() < 5 || line.getMaxX() > width - 5))
                    continue;

                if (line.getMinX() < location.left.getMinX())
                    location.left = line;
                if (line.getMaxX() > location.right.getMaxX())
                    location.right = line;
            }
        }

        if (houghLines.size() < 4)
            throw new PuzzleNotFoundException("not enough possible edges found. Need at least 4 for a rectangle.");
        if (countHorizontalLines < 2)
            throw new PuzzleNotFoundException("not enough horizontal edges found. Need at least 2 for a rectangle.");
        if (countVerticalLines < 2)
            throw new PuzzleNotFoundException("not enough vertical edges found. Need at least 2 for a rectangle.");


        location.topLeft = location.top.findIntersection(location.left);
        if (location.topLeft == null)
            throw new PuzzleNotFoundException("Cannot find top left corner");

        location.topRight = location.top.findIntersection(location.right);
        if (location.topRight == null)
            throw new PuzzleNotFoundException("Cannot find top right corner");

        location.bottomLeft = location.bottom.findIntersection(location.left);
        if (location.topLeft == null)
            throw new PuzzleNotFoundException("Cannot find bottom left corner");

        location.bottomRight = location.bottom.findIntersection(location.right);
        if (location.topLeft == null)
            throw new PuzzleNotFoundException("Cannot find bottom right corner");

        return location;
    }

    Mat getOutLineMat() throws PuzzleNotFoundException {
        if (outLineMat == null)

            generateOutlineMat();
        return outLineMat;
    }

    private void generateOutlineMat() throws PuzzleNotFoundException {
        outLineMat = getGreyMat().clone();

        PuzzleOutLine location = findOutLine();

        Imgproc.drawMarker(outLineMat, location.topLeft, GREY, MARKER_TILTED_CROSS, 30, 10, 8);
        Imgproc.drawMarker(outLineMat, location.topRight, GREY, MARKER_TILTED_CROSS, 30, 10, 8);
        Imgproc.drawMarker(outLineMat, location.bottomLeft, GREY, MARKER_TILTED_CROSS, 30, 10, 8);
        Imgproc.drawMarker(outLineMat, location.bottomRight, GREY, MARKER_TILTED_CROSS, 30, 10, 8);

        Imgproc.line(outLineMat, location.top.origin, location.top.destination, GREY);
        Imgproc.line(outLineMat, location.bottom.origin, location.bottom.destination, Constants.DARK_GREY);
        Imgproc.line(outLineMat, location.left.origin, location.left.destination, GREY);
        Imgproc.line(outLineMat, location.right.origin, location.right.destination, Constants.DARK_GREY);
    }

}
