package com.fallendeductionist.evaluacion4;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddSolicitudeActivity extends AppCompatActivity {

    private static final String TAG = AddSolicitudeActivity.class.getSimpleName();

    private Spinner solicitudeTitle;
    private EditText solicitudeEmail;
    private EditText solicitudeNavigation;
    private ImageView solicitudeVoucher;
    private String navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_solicitude);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        solicitudeTitle = findViewById(R.id.spinner1);
        List<String> spinnerArray =  new ArrayList<String>();
        spinnerArray.add("Solicitud de matricula");
        spinnerArray.add("Solicitud de ingreso");
        spinnerArray.add("Solicitud de salida");
        spinnerArray.add("Solicitud extra");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        solicitudeTitle.setAdapter(adapter);
        solicitudeEmail = findViewById(R.id.email);
        solicitudeNavigation = findViewById(R.id.navigation);
        solicitudeVoucher = findViewById(R.id.voucher_image);

    }

    private static final int CAPTURE_IMAGE_REQUEST = 300;

    private Uri mediaFileUri;


    public void takePicture(View view) {
        try {

            if (!permissionsGranted()) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_LIST, PERMISSIONS_REQUEST);
                return;
            }

            File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    throw new Exception("Failed to create directory");
                }
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            mediaFileUri = Uri.fromFile(mediaFile);

            // Iniciando la captura
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaFileUri);
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Error en captura: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_REQUEST) {
            // Resultado en la captura de la foto
            if (resultCode == RESULT_OK) {
                try {
                    Log.d(TAG, "ResultCode: RESULT_OK");
                    // Toast.makeText(this, "Image saved to: " + mediaFileUri.getPath(), Toast.LENGTH_LONG).show();

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mediaFileUri);

                    // Reducir la imagen a 800px solo si lo supera
                    bitmap = scaleBitmapDown(bitmap, 800);

                    solicitudeVoucher.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                    Toast.makeText(this, "Error al procesar imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "ResultCode: RESULT_CANCELED");
            } else {
                Log.d(TAG, "ResultCode: " + resultCode);
            }
        }
    }

    public void callRegister(View view) {

        String title = solicitudeTitle.getSelectedItem().toString();
        String email = solicitudeEmail.getText().toString();
        navigation = solicitudeNavigation.getText().toString();

        if (title.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nombre y Precio son campos requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService service = ApiServiceGenerator.createService(ApiService.class);

        Call<Solicitude> call = null;

        if (mediaFileUri == null) {

            call = service.createSolicitude(title, email, navigation);
        } else {

            File file = new File(mediaFileUri.getPath());
            Log.d(TAG, "File: " + file.getPath() + " - exists: " + file.exists());

            Bitmap bitmap = BitmapFactory.decodeFile(mediaFileUri.getPath());

            bitmap = scaleBitmapDown(bitmap, 800);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            RequestBody titlePart = RequestBody.create(MultipartBody.FORM, title);
            RequestBody emailPart = RequestBody.create(MultipartBody.FORM, email);
            RequestBody navigationPart = RequestBody.create(MultipartBody.FORM, navigation);

            call = service.createSolicitudeWithImage(titlePart, emailPart, navigationPart, imagePart);
        }

        call.enqueue(new Callback<Solicitude>() {
            @Override
            public void onResponse(Call<Solicitude> call, Response<Solicitude> response) {
                try {

                    int statusCode = response.code();
                    Log.d(TAG, "HTTP status code: " + statusCode);

                    if (response.isSuccessful()) {

                        Solicitude solicitude = response.body();
                        Log.d(TAG, "solicitud: " + solicitude);

                        Toast.makeText(AddSolicitudeActivity.this, "Registro satisfactorio", Toast.LENGTH_LONG).show();
                        finish();

                    } else {
                        Log.e(TAG, "onError: " + response.errorBody().string());
                        throw new Exception("Error en el servicio");
                    }

                } catch (Throwable t) {
                    try {
                        Log.e(TAG, "onThrowable: " + t.toString(), t);
                        Toast.makeText(AddSolicitudeActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (Throwable x) {
                    }
                }
            }

            @Override
            public void onFailure(Call<Solicitude> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.toString());
                Toast.makeText(AddSolicitudeActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }


    private static final int PERMISSIONS_REQUEST = 200;

    private static String[] PERMISSIONS_LIST = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private boolean permissionsGranted() {
        for (String permission : PERMISSIONS_LIST) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                for (int i = 0; i < grantResults.length; i++) {
                    Log.d(TAG, "" + grantResults[i]);
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, PERMISSIONS_LIST[i] + " permiso rechazado!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                Toast.makeText(this, "Permisos concedidos, intente nuevamente.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Redimensionar una imagen bitmap
    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private LocationListener locationListener;

    private boolean locationUpdating = false;

    public void myLocation(View view) {
        Log.d(TAG, "myLocation");

        // Verify permissions
        if (ContextCompat.checkSelfPermission(AddSolicitudeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddSolicitudeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {

            // Verify GPS enabled
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                new AlertDialog.Builder(this)
                        .setMessage("Para verificar su ubicación se requiere activar el GPS.")
                        .setPositiveButton("Habilitar GPS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        }).create().show();
                return;
            }

            /**
             * Periodical request LocationUpdates
             */
            if (!locationUpdating) {
                Toast.makeText(this, "Start LocationUpdates", Toast.LENGTH_SHORT).show();

                // Listener to location status change
                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d(TAG, "onLocationChanged by " + location.getProvider());

                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        solicitudeNavigation.setText(latLng.toString());
                        Log.d(TAG, "LatLng: " + latLng);

                        Toast.makeText(AddSolicitudeActivity.this, "latLng: " + latLng, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                };

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);  // Alternative

                locationUpdating = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)   // Change FAB color
                    view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent)));
            } else {
                Toast.makeText(this, "Stop LocationUpdates", Toast.LENGTH_SHORT).show();
                locationManager.removeUpdates(locationListener);    // Remove All location updates

                locationUpdating = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)   // Change FAB color
                    view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.darker_gray)));
            }

            /*Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null) locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location != null) {
                Log.d(TAG, "getLastKnownLocation by " + location.getProvider());

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "getLastKnownLocation LatLng: " + latLng);

                Toast.makeText(MainActivity.this, "LastKnownLocation: " + latLng, Toast.LENGTH_SHORT).show();
            }*/

        }
    }

}
