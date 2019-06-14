package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import objects.DetectionConfiguration;
import utils.Globals;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener, ConfigurationDialog.ConfigurationDialogListener {

    private Button goToOpticalInspectionButton;
    private Button configureDetectionSetings;

    private DetectionConfiguration detectionConfiguration = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goToOpticalInspectionButton =(Button) findViewById(R.id.startDetection);
        configureDetectionSetings = (Button) findViewById(R.id.configureDetection) ;

        goToOpticalInspectionButton.setOnClickListener(this);
        configureDetectionSetings.setOnClickListener(this);

        Intent intent = getIntent();
        detectionConfiguration = intent.getParcelableExtra(Globals.DETEC_CONFIG);

    }

    void openOpticalInspectionActivity(){
        Intent intent = new Intent(this, OpticalInspectionActivity.class);

        if(detectionConfiguration == null){
            intent.putExtra(Globals.DETEC_CONFIG, new DetectionConfiguration(Globals.ALL_SHAPES,Globals.STANDARD_SHAPE_LABEL,false,false, Globals.DETECT_ALL_COLORS));
        }else{
            intent.putExtra(Globals.DETEC_CONFIG, detectionConfiguration);
        }
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startDetection:
                openOpticalInspectionActivity();
                break;
            case R.id.configureDetection:
                openConfigurationDialog();

        }
    }

    private void openConfigurationDialog() {
        ConfigurationDialog configDialog = new ConfigurationDialog();
        configDialog.show(getSupportFragmentManager(), "Configuration Dialog");
    }

    @Override
    public void passValues(String shapeToDetect, String shapeLabel, boolean useFlash, boolean displayInfo, String colorToDetect) {

        detectionConfiguration = new DetectionConfiguration();
        detectionConfiguration.setShapeToDetect(shapeToDetect);
        detectionConfiguration.setShapeLabel(shapeLabel);
        detectionConfiguration.setUseFlash(useFlash);
        detectionConfiguration.setDisplayInfo(displayInfo);
        detectionConfiguration.setColorToDetect(colorToDetect);
    }
}
