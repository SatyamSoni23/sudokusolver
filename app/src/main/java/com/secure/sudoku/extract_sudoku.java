package com.secure.sudoku;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class extract_sudoku {
    private Mat puzzle;
    private Context context;
    private String num;

    extract_sudoku(Bitmap img, Context context){
        puzzle = new Mat();
        Bitmap bmp32 = img.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, puzzle);
        this.context = context;
    }

    private String Recognize_text_in_images(FirebaseVisionText text){
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        if(blocks.size() == 0){
            return ".";
        }
        for(int i=0; i<blocks.size(); i++){
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for(int j=0; j<lines.size(); j++){
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for(int k=0; k<elements.size(); k++){
                    if(blocks.size()!=0){
                        Log.d("Rec text", elements.get(k).getBoundingBox().flattenToString());
                        Log.d("Detect text", elements.get(k).getText());
                    }
                    return elements.get(k).getText();
                }
            }
        }
        return ".";
    }

    private void getNum(Mat img){
        Point[] h = new Point[10];
        Point[] v = new Point[10];
        h[0] = new Point(0, 0);
        v[0] = new Point(0, 0);

        Bitmap masked = null;
        for(int i=0; i<9; i++){
            h[i+1] = new Point(0, (i+1)*img.cols()/9);
            v[i+1] = new Point((i+1)*img.width()/9,0);
        }

        Mat canvas=null;
        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++){
                canvas = Mat.zeros(img.rows(), img.cols(), img.type());
                Imgproc.rectangle(canvas, new org.opencv.core.Point(v[j].x-20, h[i].y-20), new org.opencv.core.Point(v[j+1].x+20, h[i+1].y+20), new Scalar(255, 255, 255), -1);
                Core.bitwise_and(img, canvas, canvas);
                masked = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(canvas, masked);
                FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(masked);
                FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

                textRecognizer.processImage(visionImage)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText text) {
                                String temp = Recognize_text_in_images(text);
                                if(!(temp.toLowerCase().toCharArray()[0]>'a' && temp.toLowerCase().toCharArray()[0]<'z')){
                                    num+= temp;
                                }
                                else{
                                    num+= '.';
                                }
                                if(num.length() == 81){
                                    Intent intent = new Intent(context, MainActivity.class);
                                    intent.putExtra("num", num);
                                    context.startActivity(intent);
                                }
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Error in Recognition", e.getMessage());
                                    }
                                });
            }
        }
    }

    public void getNum(){
        if(!puzzle.empty()){
            num = "";
            getNum(puzzle);
        }
    }

}
