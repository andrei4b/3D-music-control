package com.google.ar.sceneform.samples.hellosceneform;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.ArFragment;

import android.support.design.widget.FloatingActionButton;
import android.widget.ToggleButton;

import static java.lang.Thread.interrupted;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private OSCSender oscSender;
    private Thread xyzThread;
    private Vector3 firstValues;

    private FloatingActionButton resetButton;
    private ToggleButton xToggleButton, yToggleButton, zToggleButton;
    private ImageButton settingsButton;
    private ToggleButton portOpt1Button, portOpt2Button, portOpt3Button;

    boolean[] activeAxes = new boolean[3];
    private float xFactor, yFactor, zFactor;
    private int delay;
    private String ip;
    private int[] port_opt_value = new int[3];
    private int selected_port_opt;

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        ip = prefs.getString("ip_preference", "");
        port_opt_value[0] = Integer.valueOf(prefs.getString("port_opt1_preference", ""));
        port_opt_value[1] = Integer.valueOf(prefs.getString("port_opt2_preference", ""));
        port_opt_value[2] = Integer.valueOf(prefs.getString("port_opt3_preference", ""));
        portOpt1Button.setTextOff(String.valueOf(port_opt_value[0]));
        portOpt1Button.setTextOn(String.valueOf(port_opt_value[0]));
        portOpt2Button.setTextOff(String.valueOf(port_opt_value[1]));
        portOpt2Button.setTextOn(String.valueOf(port_opt_value[1]));
        portOpt3Button.setTextOff(String.valueOf(port_opt_value[2]));
        portOpt3Button.setTextOn(String.valueOf(port_opt_value[2]));

        portOpt2Button.performClick();
        portOpt3Button.performClick();
        portOpt1Button.performClick();

        xFactor = Float.valueOf(prefs.getString("x_range_preference", ""));
        yFactor = Float.valueOf(prefs.getString("y_range_preference", ""));
        zFactor = Float.valueOf(prefs.getString("z_range_preference", ""));
        delay = Integer.parseInt(prefs.getString("delay_preference",""));
    }

    private void stopSending() {
        if (xyzThread != null)
            if (xyzThread.isAlive()) {
                xyzThread.interrupt();
            }
        resetButton.setVisibility(View.INVISIBLE);
        settingsButton.setVisibility(View.GONE);
        settingsButton.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        resetButton = findViewById(R.id.startReset);
        xToggleButton = findViewById(R.id.xToggleButton);
        yToggleButton = findViewById(R.id.yToggleButton);
        zToggleButton = findViewById(R.id.zToggleButton);
        settingsButton = findViewById(R.id.settingsButton);
        portOpt1Button = findViewById(R.id.port_opt1);
        portOpt2Button = findViewById(R.id.port_opt2);
        portOpt3Button = findViewById(R.id.port_opt3);
        android.support.v7.preference.PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        portOpt1Button.setChecked(true);
        portOpt2Button.setChecked(false);
        portOpt3Button.setChecked(false);
        selected_port_opt = port_opt_value[0];
        resetButton.setVisibility(View.INVISIBLE);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startSending();
            }
        });

        xToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                activeAxes[0] = isChecked;

                if(!activeAxes[0] && !activeAxes[1] && !activeAxes[2])
                    stopSending();

                if(activeAxes[0] && !activeAxes[1] && !activeAxes[2]) {
                    startSending();
                }
            }
        });

        yToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                activeAxes[1] = isChecked;

                if(!activeAxes[0] && !activeAxes[1] && !activeAxes[2])
                    stopSending();

                if(!activeAxes[0] && activeAxes[1] && !activeAxes[2]) {
                    startSending();
                }
            }
        });

        zToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                activeAxes[2] = isChecked;

                if(!activeAxes[0] && !activeAxes[1] && !activeAxes[2])
                    stopSending();

                if(!activeAxes[0] && !activeAxes[1] && activeAxes[2]) {
                    startSending();
                }
            }
        });

        portOpt1Button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    portOpt2Button.setChecked(false);
                    portOpt3Button.setChecked(false);
                    selected_port_opt = port_opt_value[0];

                    if (xyzThread != null)
                        if (xyzThread.isAlive())
                            startSending();
                }
                else
                if(!portOpt2Button.isChecked() && !portOpt3Button.isChecked())
                    portOpt1Button.setChecked(true);
            }
        });

        portOpt2Button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    portOpt1Button.setChecked(false);
                    portOpt3Button.setChecked(false);
                    selected_port_opt = port_opt_value[1];
                    if (xyzThread != null)
                        if (xyzThread.isAlive())
                            startSending();
                }
                else
                    if(!portOpt1Button.isChecked() && !portOpt3Button.isChecked())
                        portOpt2Button.setChecked(true);
            }
        });

        portOpt3Button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    portOpt1Button.setChecked(false);
                    portOpt2Button.setChecked(false);
                    selected_port_opt = port_opt_value[2];
                    if (xyzThread != null)
                        if (xyzThread.isAlive())
                            startSending();
                }
                else
                if(!portOpt1Button.isChecked() && !portOpt2Button.isChecked())
                    portOpt3Button.setChecked(true);
            }
        });
    }

    private void startSending() {
        if (xyzThread != null)
            if (xyzThread.isAlive())
                xyzThread.interrupt();


        if(arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
            Toast.makeText(MainActivity.this, "Not tracking, move the phone around!", Toast.LENGTH_LONG).show();
            return;
        }

        settingsButton.setVisibility(View.INVISIBLE);
        resetButton.setVisibility(View.VISIBLE);

        Camera camera = arFragment.getArSceneView().getScene().getCamera();
        ArSceneView arSceneView = arFragment.getArSceneView();

        float bias = 0.5f;

        Runnable xyzRunnable = new Runnable() {
            @Override
            public void run() {
                float xValue;
                float yValue;
                float zValue;
                Vector3 currentPosition;

                while (!interrupted()) {
                    currentPosition = camera.getWorldPosition();

                    if (activeAxes[0]) {
                        xValue = ((currentPosition.x - firstValues.x) * xFactor + bias);
                        if (xValue > 1.0) xValue = 1.0f;
                        if (xValue < 0.0) xValue = 0.0f;
                        oscSender.send(xValue, 'x', delay);
                    }

                    if (activeAxes[1]) {
                        yValue = ((currentPosition.y - firstValues.y) * yFactor + bias);
                        if (yValue > 1.0) yValue = 1.0f;
                        if (yValue < 0.0) yValue = 0.0f;
                        oscSender.send(yValue, 'y', delay);
                    }

                    if (activeAxes[2]) {
                        zValue = ((currentPosition.z - firstValues.z) * zFactor + bias);
                        if (zValue > 1.0) zValue = 1.0f;
                        if (zValue < 0.0) zValue = 0.0f;
                        oscSender.send(zValue, 'z', delay);
                    }
                }
            }
        };

        Vector3 cameraPos = arSceneView.getScene().getCamera().getWorldPosition();
        Vector3 cameraForward = arSceneView.getScene().getCamera().getForward();
        Vector3 position = Vector3.add(cameraPos, cameraForward.scaled(0.5f));

        // Create an ARCore Anchor at the position.
        Pose pose = Pose.makeTranslation(position.x, position.y, position.z);
        Anchor anchor = arSceneView.getSession().createAnchor(pose);

        // Create the Sceneform AnchorNode
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arSceneView.getScene());

        // Create the node relative to the AnchorNode
        Node node = new Node();
        node.setParent(anchorNode);

        firstValues = camera.getWorldPosition();

        xyzThread = new Thread(xyzRunnable);

        oscSender = new OSCSender(ip, selected_port_opt);

        xyzThread.start();
    }
}
