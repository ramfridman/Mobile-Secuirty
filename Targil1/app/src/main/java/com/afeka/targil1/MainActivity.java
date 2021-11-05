package com.afeka.targil1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView textView;
    Button button;
    Switch flashSwitch;
    ArrayList<String> phoneNumbers;
    float brightness;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editTextPassword);
        textView = findViewById(R.id.messageToUser);
        button = findViewById(R.id.sendButton);
        flashSwitch = findViewById(R.id.flashlightSwitch);
        phoneNumbers = new ArrayList<>();
        checkReadContactsPermission();


        flashSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleFlashlight(getApplicationContext());
            }
        });
        button.setOnClickListener(v -> {
            String answer = editText.getText().toString();
            brightness = getCurrentBrightness();
            checkReadContactsPermission();

            if(answer.equals(Integer.toString(getBatteryPercentage(v.getContext())))
                    && isContactExist("0505556666")
                    && brightness < 50
                    && isAirplaneModeOn(this)
                    && flashSwitch.isChecked()) {

                textView.setText("Correct!");
                textView.setTextColor(Color.GREEN);
            } else {
                textView.setText("Wrong! Try again!");
                textView.setTextColor(Color.RED);
            }
        });
    }


    private void toggleFlashlight(Context context) {
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null;
            try {
                cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, flashSwitch.isChecked());
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private boolean isContactExist(String phoneNumber) {
        for(int i=0; i < phoneNumbers.size(); i++) {
            if(phoneNumbers.get(i).equals(phoneNumber)) {
                return true;
            }
        }
        return false;
    }
    private float getCurrentBrightness() {
            float brightness =
                    Settings.System.getInt(getApplicationContext().getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, 0);
            System.out.println("Current Brightness level " + brightness);
        return brightness;
    }

    private void checkReadContactsPermission() {
        if(!hasPhoneContactsPermission(Manifest.permission.READ_CONTACTS))
        {
            String requestPermissionArray[] = {Manifest.permission.READ_CONTACTS};
            ActivityCompat.requestPermissions(this, requestPermissionArray, 1);
        } else {
            readContacts();
            System.out.println("Contacts data loaded Successfully");
        }
    }

    private void readContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    System.out.println("name : " + name + ", ID : " + id);

                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNumbers.add(phone);
                        System.out.println("Phone: " + phone);
                    }
                    pCur.close();
                }
            }
        }
    }

    private static int getBatteryPercentage(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);

            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

            double batteryPct = level / (double) scale;
            return (int) (batteryPct * 100);
        }
    }

    private boolean hasPhoneContactsPermission(String permission)
    {
        boolean ret = false;
        // If android sdk version is bigger than 23 the need to check run time permission.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // return phone read contacts permission grant status.
            int hasPermission = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            // If permission is granted then return true.
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                ret = true;
            }
        }else {
            ret = true;
        }
        return ret;
    }
}