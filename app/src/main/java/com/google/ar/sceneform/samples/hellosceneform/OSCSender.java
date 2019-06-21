package com.google.ar.sceneform.samples.hellosceneform;

import android.util.Log;
import android.widget.ToggleButton;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class OSCSender {
    private static final String TAG = "OSCSender1";
    private OSCPortOut oscPortOut;

    public OSCSender(String remoteIP, int remotePort) {
        try {
            oscPortOut = new OSCPortOut(InetAddress.getByName(remoteIP), remotePort);
        } catch (UnknownHostException e) {
            // Error handling when your IP isn't found
            Log.e(TAG, "UnknownHostException");
        } catch (Exception e) {
            // Error handling for any other errors
            Log.e(TAG, "other connection error");
        }
    }

    public void send(float value, char axis, int delay) {
        if (oscPortOut != null) {
            Object[] message = new Object[1];

            message[0] = value;

            OSCMessage oscMessage = new OSCMessage("/oscApp/" + axis, Arrays.asList(message));

            try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            try {
                oscPortOut.send(oscMessage);
                Log.d(TAG, axis + ": " + value);
            } catch (Exception e) {
                // Error handling for some error
                Log.e(TAG, "error sending");
                e.printStackTrace();
            }

        }
    }

    public void send(float xValue, float yValue, float zValue) {
        int sleepTime = 30;
        if (oscPortOut != null) {
            Object[] xArg = {xValue};
            OSCMessage xMessage = new OSCMessage("/oscApp/x", Arrays.asList(xArg));
            Object[] yArg = {yValue};
            OSCMessage yMessage = new OSCMessage("/oscApp/y", Arrays.asList(yArg));
            Object[] zArg = {zValue};
            OSCMessage zMessage = new OSCMessage("/oscApp/z", Arrays.asList(zArg));

            //OSCPacket[] packets = {xMessage, zMessage};
            //OSCBundle bundle = new OSCBundle(Arrays.asList(packets));

            try {
                oscPortOut.send(xMessage);

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                oscPortOut.send(yMessage);

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                oscPortOut.send(zMessage);
            } catch (Exception e) {
                // Error handling for some error
                Log.e(TAG, "error sending");
                e.printStackTrace();
            }

        }
    }
}
