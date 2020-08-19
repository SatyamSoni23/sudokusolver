package com.secure.sudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase cameraBridgeViewBase;
    private boolean sudoreg;
    private Bitmap puzzle;
    Button rotate_left, rotate_right, scan, recapture;
    ProgressBar progressBar;

    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
    Mat mInterMat;
    ImageView imageView;
    extract_sudoku extractor;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:{
                    Log.i(TAG, "Opencv loadeed Successfully");
                    cameraBridgeViewBase.enableView();
                    imageView.setVisibility(View.INVISIBLE);
                }break;
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = findViewById(R.id.camera);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);

        //--------------
        FirebaseOptions options = new FirebaseOptions.Builder().setApplicationId("com.secure").build();
        FirebaseApp.initializeApp(this);
        //-------------

        cameraBridgeViewBase.setCvCameraViewListener(this);

        imageView = findViewById(R.id.image);
        rotate_left = findViewById(R.id.rotate_left);
        rotate_right = findViewById(R.id.rotate_right);

        sudoreg = false;
        rotate_right.setVisibility(View.INVISIBLE);
        rotate_left.setVisibility(View.INVISIBLE);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        scan = findViewById(R.id.button);
        recapture = findViewById(R.id.button2);
        recapture.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else{
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        changeUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(height, width, CvType.CV_8UC4);
        mInterMat = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mInterMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
        Core.flip(mRgbaF, mRgba, 1);
        find_puzzle finder = new find_puzzle(mRgba);
        return finder.getPuzzle(false);
    }

    public void scan(View view) {
        rotate_right.setVisibility(View.VISIBLE);
        rotate_left.setVisibility(View.VISIBLE);
        recapture.setVisibility(View.VISIBLE);
        if(!sudoreg) {
            Log.d("Scanning", "In scanner");
            Log.d("Channel",mRgba.channels()+"");
            Mat source = mRgba;
            find_puzzle finder = new find_puzzle(source);
            Mat m = finder.getPuzzle(true);
            Mat temp = m.clone();
            Bitmap bm = Bitmap.createBitmap(temp.cols(), temp.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(temp, bm);
            sudoreg=true;
            puzzle=bm;
            cameraBridgeViewBase.disableView();
            cameraBridgeViewBase.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bm);
            scan.setText(R.string.Extract);
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            extractor = new extract_sudoku(puzzle,this);
            extractor.getNum();
        }
    }

    private void changeUI(){
        cameraBridgeViewBase.setVisibility(View.VISIBLE);
        cameraBridgeViewBase.enableView();
        imageView.setVisibility(View.INVISIBLE);
        sudoreg=false;
        rotate_right.setVisibility(View.INVISIBLE);
        rotate_left.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        recapture.setVisibility(View.INVISIBLE);
        scan.setText(R.string.Scan);
    }
    public void recapture(View view) {
        changeUI();
    }

    public void rotateLeft(View view) {
        Matrix mat = new Matrix();
        mat.postRotate(90);
        puzzle=Bitmap.createBitmap(puzzle,0,0,puzzle.getWidth(),puzzle.getHeight(),mat,true);
        imageView.setImageBitmap(puzzle);
    }

    public void rotateRight(View view) {
        Matrix mat = new Matrix();
        mat.postRotate(-90);
        puzzle=Bitmap.createBitmap(puzzle,0,0,puzzle.getWidth(),puzzle.getHeight(),mat,true);
        imageView.setImageBitmap(puzzle);
    }

}