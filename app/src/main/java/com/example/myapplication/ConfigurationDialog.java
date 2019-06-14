package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import objects.DetectionConfiguration;
import utils.Globals;

public class ConfigurationDialog extends AppCompatDialogFragment implements AdapterView.OnItemSelectedListener, AdapterView.OnClickListener {

    private DetectionConfiguration config = new DetectionConfiguration();

    private Spinner shapeToDetectSpinner;
    private Spinner colorToDetectSpinner;
    private EditText shapeLabelEditText;
    private CheckBox useFlashCheckBox;
    private CheckBox displayInfoCheckBox;

    private String shapeToDetect;
    private String shapeLabel;
    private String colorToDetect;
    private boolean useFlash = false;
    private boolean displayInfo = false;

    private ConfigurationDialogListener listener;
    private DetectionConfiguration detectionConfiguration = null;

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.configuration_dialogue,null);

        builder.setView(view)
               .setTitle("Detection Configuration")
               .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {

                   }
               })
               .setPositiveButton("save", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                        shapeLabel = shapeLabelEditText.getText().toString();


                        listener.passValues(shapeToDetect,shapeLabel,useFlash,displayInfo,colorToDetect);

                   }
               });

        shapeToDetectSpinner = view.findViewById(R.id.shape_to_detect);
        shapeLabelEditText = view.findViewById(R.id.shape_label);
        useFlashCheckBox = view.findViewById(R.id.useFlash);
        displayInfoCheckBox = view.findViewById(R.id.freezeVideo);
        colorToDetectSpinner = view.findViewById(R.id.color_to_detect);

        useFlashCheckBox.setOnClickListener(this);
        displayInfoCheckBox.setOnClickListener(this);
        shapeToDetectSpinner.setOnItemSelectedListener(this);
        colorToDetectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                colorToDetect = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                colorToDetect = Globals.DETECT_ALL_COLORS;
            }
        });

        return builder.create();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        shapeToDetect = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        shapeToDetect = Globals.ALL_SHAPES;
    }

    @Override
    public void onClick(View v) {
        useFlash = useFlashCheckBox.isChecked();
        displayInfo = displayInfoCheckBox.isChecked();
    }

    public interface ConfigurationDialogListener{
        void passValues(String shapeToDetect, String shapeLabel, boolean useFlash, boolean freezeVideo, String colorToDetect);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (ConfigurationDialogListener) context;
        } catch (Exception e) {
           throw new ClassCastException(context.toString()+ "must implement ConfigurationDialogListener");
        }
    }
}
