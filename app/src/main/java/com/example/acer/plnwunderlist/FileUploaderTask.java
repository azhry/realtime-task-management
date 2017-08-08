package com.example.acer.plnwunderlist;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class FileUploaderTask extends AsyncTask<Void, Integer, Void>
    implements DialogInterface.OnCancelListener{

    private ProgressDialog progressDialog;
    private String url;
    private File file;
    private Context context;
    private Activity activity;
    private Map<String, String> params;

    public FileUploaderTask(String url, File file, Context context, Activity activity, Map<String, String> params) {
        this.url        = url;
        this.file       = file;
        this.context    = context;
        this.activity   = activity;
        this.params     = params;
    }

    @Override
    protected void onPreExecute() {
        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.progressDialog.setMessage("Uploading...");
        this.progressDialog.setCancelable(false);
        this.progressDialog.setMax((int)file.length());
        this.progressDialog.show();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        cancel(true);
        dialog.dismiss();
    }

    @Override
    protected Void doInBackground(Void... params) {
        uploadFile();
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        this.progressDialog.setProgress((int)progress[0]);
    }

    @Override
    protected void onPostExecute(Void v) {
        this.progressDialog.dismiss();
        Toast.makeText(context, "File uploaded!", Toast.LENGTH_SHORT).show();
    }

    private int uploadFile() {
        int serverResponseCode = 0;

        final HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = this.file;
        final String selectedFilePath = this.file.getPath();

        if (!selectedFile.isFile()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("FILE_DOESNT_EXIST", "Source file doesn't exist: " + selectedFilePath);
                }
            });

            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(this.url);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", selectedFilePath);

                /**
                 *    Content-Type: multipart/form-data; boundary="BOUNDARY"
                 *
                 *    --BOUNDARY
                 *    Content-Disposition: form-data; name="param"
                 *
                 *    123456
                 *    --BOUNDARY
                 *    Content-Disposition: form-data; name="test"; filename="test.zip"
                 *    Content-Type: application/zip
                 *
                 *    BINARY_DATA
                 *
                 *    --BOUNDARY--
                 */

                dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                int progress = 0;
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    progress += bytesRead;
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    publishProgress(progress);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"action\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes("upload_file" + lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);

                int index = 0;
                for (Map.Entry<String, String> param : this.params.entrySet()) {
                    index++;
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + param.getKey() + "\"" + lineEnd);
                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(param.getValue() + lineEnd);
                    if (index < this.params.size()) {
                        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    } else {
                        dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    }
                }

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                final StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                Log.e("RESPONSE", sb.toString());
                Log.e("SERVER_RESPONSE", "Server response is: " + serverResponseMessage + ": " + serverResponseCode);
                if (serverResponseCode == 200) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("RESPONSE", sb.toString());
                        }
                    });
                }

                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();

            } catch (FileNotFoundException e) {
                Log.e("EXCEPTION", "File not found" + e.getMessage());
                e.printStackTrace();
            } catch (MalformedURLException e) {
                Log.e("EXCEPTION", "Malformed");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("EXCEPTION", "IO" + e.getMessage());
                e.printStackTrace();
            }
        }

        return serverResponseCode;
    }
}
