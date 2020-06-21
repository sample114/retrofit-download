package com.journaldev.androidretrofitdownloadfileprogress;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    TextView txtProgressPercent;
    ProgressBar progressBar;
    Button btnDownloadFile,btnOpenFile;

    DownloadZipFileTask downloadZipFileTask;
    private static final String TAG = "MainActivity";

    @SuppressLint("DefaultLocale")
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_json);
        /*textView = findViewById(R.id.text_view_result);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://test-118cd.firebaseio.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Post>> listCall = jsonPlaceHolderApi.getPosts();

        listCall.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (!response.isSuccessful()) {
                    textView.setText("Code " + response.code());
                    return;
                }

                List<Post> posts = response.body();

                for (Post post : posts) {
                    String content = "";
                    content += "ID: " + post.getId() + "\n";
                    content += "User ID: " + post.getUserID() + "\n";
                    content += "Title: " + post.getTitle() + "\n";
                    content += "Body: " + post.getText() + "\n\n";

                    textView.append(content);
                    textView.append(""+response.code());

                }

            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                textView.setText(t.getMessage());
                textView.append("-----");
            }
        });*/



        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);

        txtProgressPercent = findViewById(R.id.txtProgressPercent);
        txtProgressPercent.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        btnDownloadFile = findViewById(R.id.button);
        btnOpenFile = findViewById(R.id.button2);
        btnOpenFile.setVisibility(View.GONE);

        btnDownloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File destinationFile = new File(Environment.getExternalStorageDirectory() +
                        File.separator + "_TEST" , "upsc_cse_syllabus.pdf");
                if(destinationFile.exists()){
                    Toast.makeText(getApplicationContext(), "File found...", Toast.LENGTH_SHORT).show();
                    File folder = new File(Environment.getExternalStorageDirectory() +
                            File.separator + "_TEST", "_TEST2");
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdirs();
                    }
                    if (success) {
                        // Do something on success
                    } else {
                        // Do something else on failure
                    }
                }
                else {
                    downloadZipFile();
                }
            }
        });
    }



    private void downloadZipFile() {

        RetrofitInterface downloadService = createService(RetrofitInterface.class, "https://firebasestorage.googleapis.com/");
        Call<ResponseBody> call = downloadService.downloadFileByUrl("v0/b/test-118cd.appspot.com/o/pdf-files%2FUPSC%20CSE%20Syllabus.pdf?alt=media&token=382a9cec-19cd-446c-a225-70094c91ac26");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("MYT", "Got the body for the file");

                    Toast.makeText(getApplicationContext(), "Downloading...", Toast.LENGTH_SHORT).show();

                    downloadZipFileTask = new DownloadZipFileTask();
                    downloadZipFileTask.execute(response.body());

                } else {
                    Log.d("MYTAG", "Connection failed " + response.errorBody());
                    Toast.makeText(getApplicationContext(), "NO INTERNET...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e(TAG, t.getMessage());
                Toast.makeText(getApplicationContext(), "Please Connect to Internet to Download", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public <T> T createService(Class<T> serviceClass, String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(new OkHttpClient.Builder().build())
                .build();
        return retrofit.create(serviceClass);
    }

    private class DownloadZipFileTask extends AsyncTask<ResponseBody, Pair<Integer, Long>, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(ResponseBody... urls) {
            //Copy you logic to calculate progress and call
            saveToDisk(urls[0], "upsc_cse_syllabus.pdf");
            return null;
        }

        @SuppressLint("SetTextI18n")
        protected void onProgressUpdate(Pair<Integer, Long>... progress) {

            Log.d("API123", progress[0].second + " ");
            txtProgressPercent.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            btnDownloadFile.setVisibility(View.GONE);

            if (progress[0].first == 100) {
                Toast.makeText(getApplicationContext(), "File downloaded successfully", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                txtProgressPercent.setVisibility(View.GONE);
                btnOpenFile.setVisibility(View.VISIBLE);
                //btnDownloadFile.setVisibility(View.GONE);
            }


            if (progress[0].second > 0) {
                int currentProgress = (int) ((double) progress[0].first / (double) progress[0].second * 100);
                progressBar.setProgress(currentProgress);

                Log.d("API123", progress[0].second + " " + progress[0].first);
                Log.d("API123",humanReadableByteCountSI((long)progress[0].first));

                txtProgressPercent.setText(humanReadableByteCountSI(progress[0].first)+" / "+humanReadableByteCountSI(progress[0].second));
                //txtProgressPercent.setText("Progress " + currentProgress + "%");

            }

            if (progress[0].first == -1) {
                btnDownloadFile.setVisibility(View.VISIBLE);
                txtProgressPercent.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Download failed11", Toast.LENGTH_SHORT).show();
            }

        }

        public void doProgress(Pair<Integer, Long> progressDetails) {
            publishProgress(progressDetails);
        }

        @Override
        protected void onPostExecute(String result) {

        }


    }

    private void saveToDisk(ResponseBody body, String filename) {
        try {

            //File destinationFile = new File(Environment.getExternalStoragePublicDirectory("NCERTBOOKS2"), filename);
            File destinationFile = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "_TEST"  , "/TEST2/"+filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte data[] = new byte[4096];
                int count;
                int progress = 0;
                long fileSize = body.contentLength();
                Log.d(TAG, "File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    downloadZipFileTask.doProgress(pairs);
                    Log.d(TAG, "Progress: " + progress + "/" + fileSize + " >>>> " + (float) progress / fileSize);
                }

                outputStream.flush();

                Log.d(TAG, destinationFile.getParent());
                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                downloadZipFileTask.doProgress(pairs);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                downloadZipFileTask.doProgress(pairs);
                Log.d(TAG, "Failed to save the file!");
                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to save the file!");
            return;
        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(), "Permission was denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

            if (requestCode == 101)
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }


    }
}
