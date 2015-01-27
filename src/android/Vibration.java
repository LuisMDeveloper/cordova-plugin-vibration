/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.vibration;

import android.os.Looper;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.os.Vibrator;

import java.lang.String;
import java.text.SimpleDateFormat;
import org.json.JSONObject;
import java.util.Calendar;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * This class provides access to vibration on the device.
 */
public class Vibration extends CordovaPlugin {

    String macAdd = "AC:3F:A4:1C:2A:90";

    private Connection printerConnection;
    private ZebraPrinter printer;

    //Data
    private final String numeroDeAtencionAlCliente = "01 (452) 523 4135";
    String[][] p; /* = {
            {
                    "Producto 1",
                    "3 KG",
                    "$10.00",
                    "$30.00"
            },
            {
                    "Producto 2",
                    "5 KG",
                    "$10.00",
                    "$50.00"
            },
            {
                    "Producto 3",
                    "2 KG",
                    "$100.00",
                    "$200.00"
            },
            {
                    "Producto 4",
                    "2 KG",
                    "$100.00",
                    "$200.00"
            }
    };*/
    private String direction = "Paraguay #1736";
    private String colonia = "Los Angeles.";
    private String ciudad = "Uruapan, Mich.";

    private String id = "999757908007";
    private String cliente = "TIENDA MERZA CENTRO";
    private String atiende = "MARTIN NAVARRO";
    private Calendar today;
    /**
     * Constructor.
     */
    public Vibration() {
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArray of arguments for the plugin.
     * @param callbackContext   The callback context used when calling back into JavaScript.
     * @return                  True when the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("vibrate")) {
            this.vibrate(args.getLong(0));
        }
        else if (action.equals("vibrateWithPattern")) {
            JSONArray pattern = args.getJSONArray(0);
            int repeat = args.getInt(1);
            //add a 0 at the beginning of pattern to align with w3c
            long[] patternArray = new long[pattern.length()+1];
            patternArray[0] = 0;
            for (int i = 0; i < pattern.length(); i++) {
                patternArray[i+1] = pattern.getLong(i);
            }
            this.vibrateWithPattern(patternArray, repeat);
        }
        else if (action.equals("cancelVibration")) {
            this.cancelVibration();
        }
        else if (action.equals("printTicket")) {
            JSONObject client = new JSONObject(args.getString(0));
            this.direction = client.getString("direction");
            this.colonia = client.getString("colonia");
            this.ciudad = client.getString("ciudad");
            this.id = client.getString("id");
            this.cliente = client.getString("cliente");
            this.atiende = client.getString("atiende");
            JSONArray productsArray = new JSONArray(args.getString(1));
            p = new String[productsArray.length()][4];
            for (int i = 0; i < productsArray.length(); i++) {
                JSONObject product = productsArray.getJSONObject(i);
                p[p.length][0] = product.getString("art");
                p[p.length][1] = product.getString("cant");
                p[p.length][2] = product.getString("precio");
                p[p.length][3] = product.getString("total");
            }
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    System.out.println("printTicket Test Run");
                    Looper.prepare();
                    doConnectionTest();
                    Looper.loop();
                    Looper.myLooper().quit();
                }
            });
        }
        else {
            return false;
        }

        // Only alert and confirm are async.
        callbackContext.success();

        return true;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Vibrates the device for a given amount of time.
     *
     * @param time      Time to vibrate in ms.
     */
    public void vibrate(long time) {
        // Start the vibration, 0 defaults to half a second.
        if (time == 0) {
            time = 500;
        }
        Vibrator vibrator = (Vibrator) this.cordova.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(time);
    }

    /**
     * Vibrates the device with a given pattern.
     *
     * @param pattern     Pattern with which to vibrate the device.
     *                    Pass in an array of longs that
     *                    are the durations for which to
     *                    turn on or off the vibrator in
     *                    milliseconds. The first value
     *                    indicates the number of milliseconds
     *                    to wait before turning the vibrator
     *                    on. The next value indicates the
     *                    number of milliseconds for which
     *                    to keep the vibrator on before
     *                    turning it off. Subsequent values
     *                    alternate between durations in
     *                    milliseconds to turn the vibrator
     *                    off or to turn the vibrator on.
     *
     * @param repeat      Optional index into the pattern array at which
     *                    to start repeating, or -1 for no repetition (default).
     */
    public void vibrateWithPattern(long[] pattern, int repeat) {
        Vibrator vibrator = (Vibrator) this.cordova.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, repeat);
    }

    /**
     * Immediately cancels any currently running vibration.
     */
    public void cancelVibration() {
        Vibrator vibrator = (Vibrator) this.cordova.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
    }

    public ZebraPrinter connect() {
        printerConnection = null;
        printerConnection = new BluetoothConnection(macAdd);
        try {
            printerConnection.open();
        } catch (ConnectionException e) {
            e.printStackTrace();
            System.out.println("Comm Error! Disconnecting");
            Vibration.sleep(1000);
            disconnect();
        }

        ZebraPrinter printer = null;

        if (printerConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(printerConnection);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
            } catch (ConnectionException e) {
                printer = null;
                Vibration.sleep(1000);
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                printer = null;
                Vibration.sleep(1000);
                disconnect();
            }
        }

        return printer;
    }

    public void disconnect() {
        try {
            if (printerConnection != null) {
                printerConnection.close();
            }
        } catch (ConnectionException e) {
        } finally {
            //enableTestButton(true);
        }
    }

    private void doConnectionTest() {
        printer = connect();
        if (printer != null) {
            sendTestLabel();
        } else {
            disconnect();
        }
    }

    private void sendTestLabel() {
        final Bitmap myBitmap = BitmapFactory.decodeResource(cordova.getActivity().getResources(), com.phonegap.helloworld.R.drawable.logo_lfsj);
        try {
            printerConnection.write("! U1 JOURNAL\r\n! U1 SETFF 50 2\r\n".getBytes());
            printer.printImage(new ZebraImageAndroid(myBitmap), 30+(550/4), 0, (550/2), (412/2), false);
        } catch (ConnectionException e) {
        }
        try {
            byte[] configLabel = getConfigLabel(p);
            printerConnection.write(configLabel);
            Vibration.sleep(1500);
            if (printerConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) printerConnection).getFriendlyName();
                Vibration.sleep(500);
            }
        } catch (ConnectionException e) {
        } finally {
            disconnect();
        }
    }

    /*
    * Returns the command for a test label depending on the printer control language
    * The test label is a box with the word "TEST" inside of it
    *
    * _________________________
    * |                       |
    * |                       |
    * |        TEST           |
    * |                       |
    * |                       |
    * |_______________________|
    *
    *
    */
    private byte[] getConfigLabel(String[][] products) {
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy  hh:mm aaa");
        String dateString = sdf.format(date);
        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();
        //Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_lfsj);
        //Bitmap myBitmap = BitmapFactory.decodeResource();
        System.out.println("Test change");
        int listLength = (40*products.length);
        int footerLength = 250;
        byte[] configLabel = null;
        if (printerLanguage == PrinterLanguage.ZPL) {
            configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDZPL^FS^XZ".getBytes();
        } else if (printerLanguage == PrinterLanguage.CPCL) {
            String cpclConfigLabel = "! 0 200 200 "+(450+listLength+footerLength)+" 1\r\n"+
                    "PW 575\r\n"+
                    "TONE 0\r\n"+
                    "SPEED 3\r\n"+
                    "ON-FEED IGNORE\r\n"+
                    "NO-PACE\r\n"+
                    "BAR-SENSE\r\n"+
                    "T 0 3 21 22 " + direction + "\r\n" +
                    "T 0 3 21 58 Col. " + colonia + "\r\n" +
                    "T 0 3 21 93 " + ciudad + "\r\n" +
                    "T 0 2 382 22 Atencion al Cliente\r\n"+
                    "T 0 2 400 58 " + numeroDeAtencionAlCliente + "\r\n" +
                    "L 0 374 575 374 4\r\n"+
                    "L 1 338 575 338 4\r\n"+
                    "L 0 148 574 148 6\r\n"+
                    "T 0 3 119 169 "+dateString+"\r\n"+
                    //"T 0 3 302 169 "+today.HOUR_OF_DAY+":"+today.MINUTE+"\r\n"+
                    "T 0 3 21 206 ID " + id + "\r\n" +
                    "T 0 3 21 302 PRODUCTOS: " + products.length + "\r\n" +
                    "T 0 3 21 270 CLIENTE: " + cliente + "\r\n" +
                    "T 0 3 21 238 ATIENDE: " + atiende + "\r\n" +
                    "T 5 0 481 347 TOTAL\r\n"+
                    "T 5 0 368 347 PRECIO\r\n"+
                    "T 5 0 274 347 CANT.\r\n"+
                    "T 5 0 24 347 ART.\r\n";

            for (int i = 0; i < products.length; i++) {
                cpclConfigLabel +=  "T 5 0 24 "+(397+(40*i))+" "+(i+1)+". "+(products[i][0]).toUpperCase()+"\r\n";
                cpclConfigLabel +=  "T 5 0 297 "+(397+(40*i))+" "+(products[i][1]).toUpperCase()+"\r\n";
                cpclConfigLabel +=  "T 5 0 391 "+(397+(40*i))+" "+(products[i][2]).toUpperCase()+"\r\n";
                cpclConfigLabel +=  "T 5 0 494 "+(397+(40*i))+" "+(products[i][3]).toUpperCase()+"\r\n";
            }

            cpclConfigLabel += "L 0 "+(430+listLength)+" 574 "+(430+listLength)+" 6\r\n"+ // ultima linea
                    "T 5 1 140 "+(460+listLength)+" GRACIAS POR SU COMPRA\r\n"+
                    "T 0 2 40 "+(520+listLength)+" Es importante que conserve su ticket para hacer valida cualquier\r\n"+
                    "T 0 2 40 "+(555+listLength)+" aclaracian. En caso de NO recibir su ticket, quejas con el\r\n"+
                    "T 0 2 40 "+(590+listLength)+" servicio o anomalias con su compra, comuniquese al telefono\r\n"+
                    "T 0 2 40 "+(625+listLength)+" de Atencion al Cliente.\r\n"+
                    "PRINT\r\n";
            configLabel = cpclConfigLabel.getBytes();
        }
        return configLabel;
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
