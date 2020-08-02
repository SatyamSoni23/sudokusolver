package com.secure.puzzle_scanner;

import org.opencv.core.Point;
import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;


enum Orientation{
    horizontal,
    vertical,
    fortyFiveDegree,
}

public class Line {
    Point origin;
    Point destination;
    Line(Point origin, Point destination){
        this.origin = origin;
        this.destination = destination;
    }

    Line(Vector vector, int height, int width){
        Point destination = new Point();
        double a = cos(vector.theta), b = sin(vector.theta);
        double x0 = a*vector.rho, y0 = b*vector.rho;
        origin.x = (x0 + width*(-b));
        origin.y = (y0 + height*(a));
        destination.x = (x0 - width*(-b));
        destination.y = (y0 - height*(a));
        this.origin = origin;
        this.destination = destination;
    }

    Orientation getOrientation(){
        if(getHeight() == getWidth())
            return Orientation.fortyFiveDegree;
        if(getHeight() > getWidth())
            return Orientation.vertical;
        return Orientation.horizontal;
    }

    private double getHeight(){
        return getMaxY() - getMinY();
    }

    private double getWidth(){
        return getMaxX() - getMinX();
    }

    double getMinX(){
        if(origin.x < destination.x)
            return origin.x;
        return destination.x;
    }

    double getMaxX(){
        if(origin.x > destination.x)
            return origin.x;
        return destination.x;
    }

    double getMinY(){
        if(origin.y < destination.y)
            return origin.y;
        return destination.y;
    }

    double getMaxY(){
        if(origin.y > destination.y)
            return origin.y;
        return destination.y;
    }

    Point findIntersection(Line line2){

        double line1DeltaX = destination.x - origin.x;
        double line1DeltaY = destination.y - origin.y;

        double line2DeltaX = line2.destination.x - line2.origin.x;
        double line2DeltaY = line2.destination.y - line2.origin.y;

        double linesDeltaOriginX = origin.x - line2.origin.x;
        double linesDeltaOriginY = origin.y - line2.origin.y;

        double denominator = line1DeltaX*line2DeltaY - line2DeltaX*line1DeltaY;
        double numerator = line2DeltaX*linesDeltaOriginY - linesDeltaOriginX*line2DeltaY;

        double t = numerator/denominator;

        if(linesAreColinear(denominator))
            return null;

        return calculateIntersection(line1DeltaX, line1DeltaY, t);
    }

    private boolean linesAreColinear(double denominator){
        return denominator == 0;
    }

    Point calculateIntersection(double line1DeltaX, double line1DeltaY, double t){
        Point intersection = new Point();
        intersection.x = origin.x + (t*line1DeltaX);
        intersection.y = origin.y + (t*line1DeltaY);
        return intersection;
    }
}
