package com.example.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import objects.DetectionConfiguration;
import utils.Globals;

public class OpticalInspectionActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {



    private static final String TAG = OpticalInspectionActivity.class.getName();
    //camera Vie2
    private CameraBridgeViewBase cameraBridgeView;
    //using a fixed frame size (640 x 480)
    private static final boolean FIXED_FRAME_SIZE = true;
    //frame witdh of 640 and height of 480
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;
    // activity manager required for obtaining the memory info needed for suplying the memory usage
    private ActivityManager activityManager;
    //black and white image thersholded
    private Mat blackWhite;
    //img converted to Hue Saturation Value
    private Mat hsv;
    //thresholded image for the inferior HSV red range
    private Mat inferiorRedRange;
    //thresholded image for the superior HSV red range
    private Mat superiorRedRange;
    //downscaled image
    private Mat downscaled;
    //upscaled image
    private Mat upscaled;
    //image processed by method findCountours
    private Mat contourImage;
    //the objects'  contour as a hierarchy vector
    private Mat hierarchyVector;
    //the approximated polygonal curve with specified precision
    private MatOfPoint2f aproxiamtedCurve;
    //boolean used for seting detection for only red objects
    private boolean DETECT_RED_OBJ_ONLY = false;
    //lower HSV range for RED color (lower limit)
    private Scalar HSV_LOW_RED_1 = new Scalar(0,100,100);
    //lower HSV range for RED color (upper limit)
    private Scalar HSV_LOW_RED_2 = new Scalar(10, 255, 255);
    //upper HSV range for RED color (lower limit)
    private  Scalar HSV_UPPER_RED_1 = new Scalar(160,100,100);
    //upper HSV range for RED color (upper limit)
    private  Scalar HSV_UPPER_RED_2 = new Scalar(179,255,255);
    //lower HSV range for GREEN color (lower limit)
    private Scalar HSV_LOW_GREEN_1 = new Scalar(38,100,20);
    //lower HSV range for GREEN color (upper limit)
    private Scalar HSV_LOW_GREEN_2 = new Scalar(50, 255, 255);
    //upper HSV range for GREEN color (lower limit)
    private  Scalar HSV_UPPER_GREEN_1 = new Scalar(51,100,20);
    //upper HSV range for GREEN color (upper limit)
    private  Scalar HSV_UPPER_GREEN_2 = new Scalar(75,255,255);
    //lower HSV range for BLUE color (lower limit)
    private Scalar HSV_LOW_BLUE_1 = new Scalar(85,100,20);
    //lower HSV range for BLUE color (upper limit)
    private Scalar HSV_LOW_BLUE_2 = new Scalar(100, 255, 255);
    //upper HSV range for BLUE color (lower limit)
    private  Scalar HSV_UPPER_BLUE_1 = new Scalar(101,100,20);
    //upper HSV range for BLUE color (upper limit)
    private  Scalar HSV_UPPER_BLUE_2 = new Scalar(125,255,255);
    //image thresholded for the lower HSV range for RED image
    private Mat lowerRedRange;
    //image thresholded for the upper HSV range for RED image
    private Mat upperRedRange;
    //image thresholded for the lower HSV range for GREEN image
    private Mat lowerGreenRange;
    //image thresholded for the upper HSV range for GREEN image
    private Mat upperGreenRange;
    //image thresholded for the lower HSV range for BLUE image
    private Mat lowerBlueRange;
    //image thresholded for the upper HSV range for BLUE image
    private Mat upperBlueRange;
    //RED in RGB
    private static final Scalar RGB_RED = new Scalar(255,0,0);
    //Object containing the configuration of the detection
    private DetectionConfiguration detectionConfiguration = null;
    //count used for displaying detection message
    private int count = 50;
    //boolean required for displaying text
    private boolean detected = false;
    //objects required for flash
    private Camera camera;
    private Camera.Parameters parameters;
    //booleans for detecting color
    private boolean red_shape = false;
    private boolean blue_shape = false;
    private boolean green_shape = false;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV initialized successfully");
                    blackWhite = new Mat();
                    hsv = new Mat();
                    lowerRedRange = new Mat();
                    upperRedRange = new Mat();
                    lowerGreenRange = new Mat();
                    upperGreenRange = new Mat();
                    lowerBlueRange = new Mat();
                    upperBlueRange = new Mat();
                    downscaled = new Mat();
                    upscaled = new Mat();
                    contourImage = new Mat();
                    hierarchyVector = new Mat();
                    aproxiamtedCurve = new MatOfPoint2f();
                    cameraBridgeView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {



        Log.i(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Keeping the screen turned on
        setContentView(R.layout.activity_optical_inspection);

        cameraBridgeView = (CameraBridgeViewBase) findViewById(R.id.java_camera_view);
        if (FIXED_FRAME_SIZE) {
            cameraBridgeView.setMaxFrameSize(FRAME_WIDTH, FRAME_HEIGHT);
        }
        cameraBridgeView.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeView.setCvCameraViewListener(this);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        Intent intent = getIntent();
        detectionConfiguration = intent.getParcelableExtra(Globals.DETEC_CONFIG);

        if(detectionConfiguration.isUseFlash() == true){
            camera = Camera.open();
            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
        }

    }


    public OpticalInspectionActivity() {

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {

    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {



        // frame that will be outputed at the end
        Mat outcome = inputFrame.rgba();
        Mat grayScale = null;

        if(detected == true && count != 0){
            setDetectionMessage(outcome);
            count --;
            if (count == 0){
                detected = false;
                count = 50;
                green_shape = false;
                red_shape = false;
                blue_shape = false;

            }
        }

        //obtaining grayscale frame or rgb
        if (!detectionConfiguration.getColorToDetect().equals("All Colors")) {
            grayScale = inputFrame.rgba();
        } else {
            grayScale = inputFrame.gray();
        }

        // filtering the noise (downscaleing and upscaling the image)
        Imgproc.pyrDown(grayScale, downscaled, new Size(grayScale.cols()/2, grayScale.rows()/2));
        Imgproc.pyrUp(downscaled, upscaled, grayScale.size());

        //logic used when red object detection is activated
        if (detectionConfiguration.getColorToDetect().equals("Red Color")) {
            //image conversion RGBA -> HSV
            Imgproc.cvtColor(upscaled, hsv, Imgproc.COLOR_RGB2HSV);

            //thresholding the image for the lower and upper HSV range for the color RED
            Core.inRange(hsv, HSV_LOW_RED_1, HSV_LOW_RED_2, lowerRedRange);
            Core.inRange(hsv, HSV_UPPER_RED_1, HSV_UPPER_RED_2, upperRedRange);

            //combining the 2 thresholded images
            Core.addWeighted(lowerRedRange, 1.0, upperRedRange, 1.0, 0.0, blackWhite);

            //applying canny filter for edge retrival
            Imgproc.Canny(blackWhite, blackWhite, 0, 255);


        }

        //logic used when green object detection is activated
        if (detectionConfiguration.getColorToDetect().equals("Green Color")) {
            //image conversion RGBA -> HSV
            Imgproc.cvtColor(upscaled, hsv, Imgproc.COLOR_RGB2HSV);

            //thresholding the image for the lower and upper HSV range for the color GREEN
            Core.inRange(hsv, HSV_LOW_GREEN_1, HSV_LOW_GREEN_2, lowerGreenRange);
            Core.inRange(hsv, HSV_UPPER_GREEN_1, HSV_UPPER_GREEN_2, upperGreenRange);

            //combining the 2 thresholded images
            Core.addWeighted(lowerGreenRange, 1.0, upperGreenRange, 1.0, 0.0, blackWhite);

            //applying canny filter for edge retrival
            Imgproc.Canny(blackWhite, blackWhite, 0, 255);


            }

        //logic used when green object detection is activated
        if (detectionConfiguration.getColorToDetect().equals("Blue Color")) {
            //image conversion RGBA -> HSV
            Imgproc.cvtColor(upscaled, hsv, Imgproc.COLOR_RGB2HSV);

            //thresholding the image for the lower and upper HSV range for the color Blue
            Core.inRange(hsv, HSV_LOW_BLUE_1, HSV_LOW_BLUE_2, lowerBlueRange);
            Core.inRange(hsv, HSV_UPPER_BLUE_1, HSV_UPPER_BLUE_2, upperBlueRange);

            //combining the 2 thresholded images
            Core.addWeighted(lowerBlueRange, 1.0, upperBlueRange, 1.0, 0.0, blackWhite);

            //applying canny filter for edge retrival
            Imgproc.Canny(blackWhite, blackWhite, 0, 255);

        }

        if(detectionConfiguration.getColorToDetect().equals("All Colors")) {
            // Use Canny instead of threshold to catch squares with gradient shading
            Imgproc.Canny(upscaled, blackWhite, 0, 255);

        }

        //dilation of canny output to remove potential holes in between edge segments
        Imgproc.dilate(blackWhite, blackWhite, new Mat(), new Point(-1, 1), 1);

        //find object's contour and save them as a list
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        contourImage = blackWhite.clone();
        Imgproc.findContours(contourImage, contours, hierarchyVector, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        //iterate over all contours
        for (MatOfPoint contour : contours) {
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());


            // approximation of a polygonal curve utilising specified precision
            Imgproc.approxPolyDP(curve, aproxiamtedCurve, 0.02 * Imgproc.arcLength(curve, true), true);

            int nrVertices = (int)aproxiamtedCurve.total();
            double contourArea = Imgproc.contourArea(contour);

            Log.d(TAG, "vertices:" + nrVertices);

            // ignore to small areas
            if (Math.abs(contourArea) < 100) {
                continue;
            }

            //detecting TRIANGLE shapes
            if((nrVertices == 3 && detectionConfiguration.getShapeToDetect().equals("Triangle Shapes") && detected == false) ||
                    nrVertices == 3 && detectionConfiguration.getShapeToDetect().equals("All Shapes") && detected == false) {
                    if(detectionConfiguration.isDisplayInfo()){
                        //detectObjectColorForTriangle(inputFrame.rgba());
                        setDetectionMessage(outcome);
                        detected = true;
                    }else{
                        setLabel(outcome, detectionConfiguration.getShapeLabel(), contour);
                    }



            }

            // logic for detecting RECTANGLE, PENTAGON and HEXAGON shapes
            if (nrVertices >= 4 && nrVertices <= 6) {
                List<Double> cos = new ArrayList<>();
                for (int j = 2; j < nrVertices + 1; j++) {
                    cos.add( angle( aproxiamtedCurve.toArray()[j % nrVertices], aproxiamtedCurve.toArray()[j - 2], aproxiamtedCurve.toArray()[j - 1]));
                }

                Collections.sort(cos);

                double minCos = cos.get(0);
                double maxCos = cos.get(cos.size()-1);

                // RECTANGLE detection
                if ((nrVertices == 4 && minCos >= -0.1 && maxCos <= 0.3 && detectionConfiguration.getShapeToDetect().equals("Rectangle Shapes") && detected == false) ||
                        (nrVertices == 4 && minCos >= -0.1 && maxCos <= 0.3 && detectionConfiguration.getShapeToDetect().equals("All Shapes") && detected == false) ) {

                        if(detectionConfiguration.isDisplayInfo()){
                            setDetectionMessage(outcome);
                            detected = true;
                        }else{
                            setLabel(outcome, detectionConfiguration.getShapeLabel(), contour);
                        }

                }

                //PENTAGON detection
                else if((nrVertices == 5 && minCos >= -0.34 && maxCos <= -0.27 && detectionConfiguration.getShapeToDetect().equals("Pentagon Shapes") && detected == false) ||
                (nrVertices == 5 && minCos >= -0.34 && maxCos <= -0.27 && detectionConfiguration.getShapeToDetect().equals("All Shapes") && detected == false)) {
                    if(detectionConfiguration.isDisplayInfo()){
                        setDetectionMessage(outcome);
                        detected = true;
                    }else{
                        setLabel(outcome, detectionConfiguration.getShapeLabel(), contour);
                    }

                }

                //HEXAGON detection
                else if ( (nrVertices == 6 && minCos >= -0.55 && maxCos <= -0.45 && detectionConfiguration.getShapeToDetect().equals("Hexagon Shapes") && detected == false) ||
                (nrVertices == 6 && minCos >= -0.55 && maxCos <= -0.45 && detectionConfiguration.getShapeToDetect().equals("All Shapes") && detected == false)) {
                    if(detectionConfiguration.isDisplayInfo()){
                        setDetectionMessage(outcome);
                        detected = true;
                    }else{
                        setLabel(outcome, detectionConfiguration.getShapeLabel(), contour);
                    }

                }
            }

            // circle detection
            else {
                Rect r = Imgproc.boundingRect(contour);
                int radius = r.width / 2;
                if ((Math.abs( 1 - (r.width / r.height)) <= 0.2 && Math.abs(1 - (contourArea / (Math.PI * radius * radius))) <= 0.2 && detectionConfiguration.getShapeToDetect().equals("Round Shapes") && detected == false)
                        ||(Math.abs( 1 - (r.width / r.height)) <= 0.2 && Math.abs(1 - (contourArea / (Math.PI * radius * radius))) <= 0.2 && detectionConfiguration.getShapeToDetect().equals("All Shapes") && detected == false) ) {
                    if(detectionConfiguration.isDisplayInfo()){
                        setDetectionMessage(outcome);
                        detected = true;
                    }else{
                        setLabel(outcome, detectionConfiguration.getShapeLabel(), contour);
                    }
                }

            }


        }

        Scalar test = new Scalar(0,255,0);

        if(detected == true && count != 0){
            setDetectionMessage(outcome);
            count --;
            if (count == 0){
                detected = false;
                count = 50;
                green_shape = false;
                red_shape = false;
                blue_shape = false;

            }
        }

        //outcome image with detected shape
        return outcome;

    }

    //detecting color of the detected shape
    private void detectObjectColorForTriangle(Mat inputFrame) {

       Mat clone = new Mat();

        for(int i = 0; i < 3; i++){

            clone = inputFrame.clone();

            downscaled = new Mat();
            upscaled = new Mat();
            hsv = new Mat();
            lowerRedRange = new Mat();
            upperRedRange = new Mat();
            lowerGreenRange = new Mat();
            upperGreenRange = new Mat();
            lowerBlueRange = new Mat();
            upperBlueRange = new Mat();
            blackWhite = new Mat();


            // filtering the noise (downscaleing and upscaling the image)
            Imgproc.pyrDown(clone, downscaled, new Size(clone.cols()/2, clone.rows()/2));
            Imgproc.pyrUp(downscaled, upscaled, clone.size());

            //image conversion RGBA -> HSV
            Imgproc.cvtColor(upscaled, hsv, Imgproc.COLOR_RGB2HSV);

            if(i == 0){
                //thresholding the image for the lower and upper HSV range for the color RED
                Core.inRange(hsv, HSV_LOW_RED_1, HSV_LOW_RED_2, lowerRedRange);
                Core.inRange(hsv, HSV_UPPER_RED_1, HSV_UPPER_RED_2, upperRedRange);
            }
            if(i == 1){
                //thresholding the image for the lower and upper HSV range for the color GREEN
                Core.inRange(hsv, HSV_LOW_GREEN_1, HSV_LOW_GREEN_2, lowerGreenRange);
                Core.inRange(hsv, HSV_UPPER_GREEN_1, HSV_UPPER_GREEN_2, upperGreenRange);
            }
            if(i == 2){
                //thresholding the image for the lower and upper HSV range for the color Blue
                Core.inRange(hsv, HSV_LOW_BLUE_1, HSV_LOW_BLUE_2, lowerBlueRange);
                Core.inRange(hsv, HSV_UPPER_BLUE_1, HSV_UPPER_BLUE_2, upperBlueRange);
            }

            //combining the 2 thresholded images
            Core.addWeighted(lowerRedRange, 1.0, upperRedRange, 1.0, 0.0, blackWhite);

            //applying canny filter for edge retrival
            Imgproc.Canny(blackWhite, blackWhite, 0, 255);

            //dilation of canny output to remove potential holes in between edge segments
            Imgproc.dilate(blackWhite, blackWhite, new Mat(), new Point(-1, 1), 1);

            //find object's contour and save them as a list
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            contourImage = blackWhite.clone();
            Imgproc.findContours(contourImage, contours, hierarchyVector, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            //iterate over all contours
            for (MatOfPoint contour : contours) {
                MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());


                // approximation of a polygonal curve utilising specified precision
                Imgproc.approxPolyDP(curve, aproxiamtedCurve, 0.02 * Imgproc.arcLength(curve, true), true);

                int nrVertices = (int) aproxiamtedCurve.total();
                double contourArea = Imgproc.contourArea(contour);

                Log.d(TAG, "vertices:" + nrVertices);

                // ignore to small areas
                if (Math.abs(contourArea) < 100) {
                    continue;
                }

                if(nrVertices == 3){
                    if(i == 0){
                        red_shape = true;
                    }
                    if( i == 1 ){
                        green_shape = true;
                    }
                    if( i == 2){
                        blue_shape = true;
                    }
                }
            }
        }

    }


    //method that allows to find a cosine of an angle between 2 vectors
    private static double angle(Point point1, Point point2, Point originPoint)
    {
        double dx1 = point1.x - originPoint.x;
        double dy1 = point1.y - originPoint.y;
        double dx2 = point2.x - originPoint.x;
        double dy2 = point2.y - originPoint.y;
        return (dx1 * dx2 + dy1 * dy2)/ Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    //displaying a label over the detected image
    private void setLabel(Mat outcome, String label, MatOfPoint contour) {
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        double scale = 3;
        int thickness = 3;
        int[] baseline = new int[1];

        Size text = Imgproc.getTextSize(label, fontface, scale, thickness, baseline);
        Rect r = Imgproc.boundingRect(contour);

        Point pt = new Point(r.x + ((r.width - text.width) / 2),r.y + ((r.height + text.height) /2));

        Imgproc.putText(outcome, label, pt, fontface, scale, RGB_RED, thickness);



    }

    //display information about the identified object
    private void setDetectionMessage(Mat outcome) {
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        double scale = 0.5;
        int thickness = 1;
        int[] baseline = new int[1];
        String strDateFormat = "hh:mm:ss a";

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

        Imgproc.putText(outcome, "At post 1 at " + dateFormat.format(date) + " a " + detectionConfiguration.getShapeLabel() + " was detected ",
                new Point(0, 30), fontface, scale, RGB_RED, thickness);
        if(red_shape == true){
            Imgproc.putText(outcome, "Shape color was: " + "RED", new Point(20, 60),fontface, scale, RGB_RED, thickness);
        }
        if(green_shape == true){
            Imgproc.putText(outcome, "Shape color was: " + "GREEN", new Point(20, 60),fontface, scale, RGB_RED, thickness);
        }
        if(blue_shape == true){
            Imgproc.putText(outcome, "Shape color was: " + "BLUE", new Point(20, 60),fontface, scale, RGB_RED, thickness);
        }

    }

    private void setLabelAfter(Mat outcome, String test) {
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        double scale = 1;
        int thickness = 1;
        int[] baseline = new int[1];

        Imgproc.putText(outcome, "S-a detectat dupa" + test, new Point(0, 30), fontface, scale, RGB_RED, thickness);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (cameraBridgeView != null)
            cameraBridgeView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraBridgeView != null)
            cameraBridgeView.disableView();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Globals.DETEC_CONFIG, detectionConfiguration);
        startActivity(intent);
        super.onBackPressed();
    }
}
