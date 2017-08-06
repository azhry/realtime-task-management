package com.example.acer.plnwunderlist;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.util.Output;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloaderTask extends AsyncTask<String, String, String>
    implements DialogInterface.OnCancelListener {

    private ProgressDialog progressDialog;
    private Context context;
    private String fileName;
    public static String filePath = Environment.getExternalStorageDirectory().toString();

    public FileDownloaderTask(Context context, String fileName) {
        this.context    = context;
        this.fileName   = fileName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setMessage("Downloading...");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(String url) {
        progressDialog.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        cancel(true);
        progressDialog.dismiss();
    }

    @Override
    protected String doInBackground(String... params) {
        int count;
        try {
            URL url = new URL(params[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            Log.e("FILEPATH", filePath + "/" + this.fileName);

            int lengthOfFile = connection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            OutputStream output = new FileOutputStream(filePath + "/" + this.fileName);
            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress("" + (int)((total * 100) / lengthOfFile));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (MalformedURLException e) {
            Log.e("MALFORMED", e.getMessage());
        } catch (IOException e) {
            Log.e("IO", e.getMessage());
        }

        return null;
    }

    protected void onProgressUpdate(String... progress) {
        progressDialog.setProgress(Integer.parseInt(progress[0]));
    }
}
