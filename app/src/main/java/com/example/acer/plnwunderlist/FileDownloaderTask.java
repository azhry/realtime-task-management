package com.example.acer.plnwunderlist;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloaderTask extends AsyncTask<Void, Integer, Long>
    implements DialogInterface.OnCancelListener {

    private ProgressDialog progressDialog;
    private Context context;
    private String url;
    private String fileName;
    private long fileSize;
    private URL fileUrl;
    private URLConnection connection;

    public FileDownloaderTask(String url, String fileName, Context context) {
        this.url        = url;
        this.context    = context;
        this.fileName   = fileName;
    }

    @Override
    protected Long doInBackground(Void... params) {
        String root = Environment.getExternalStorageDirectory().toString();

        try {
            InputStream inputStream = new BufferedInputStream(fileUrl.openStream(), 8192);
            OutputStream outputStream = new FileOutputStream(root + this.fileName);
            byte data[] = new byte[1024];
            int total = 0, count;
            while ((count = inputStream.read(data)) != -1) {
                if (isCancelled()) break;
                total += count;
                publishProgress(total);
                outputStream.write(data, 0, count);
            }

            outputStream.flush();

            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        try {
            fileUrl = new URL(this.url);
            connection = fileUrl.openConnection();
            connection.connect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.fileSize = connection.getContentLength();

        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.progressDialog.setMessage("Downloading...");
        this.progressDialog.setCancelable(false);
        this.progressDialog.setMax((int)this.fileSize);
        this.progressDialog.show();
    }

    @Override
    protected void onPostExecute(Long v) {
        this.progressDialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        this.progressDialog.setProgress((int)progress[0]);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        cancel(true);
        dialog.dismiss();
    }

}
