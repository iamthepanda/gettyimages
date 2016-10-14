package george.gettyimages;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageButton;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by george on 10/13/16.
 */
public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    private ImageButton imageView;

    Activity activity;

    public ImageLoadTask(String url, ImageButton imageView, Activity activity) {
        this.url = url;
        this.imageView = imageView;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            URL urlConnection = new URL(url);
            URLConnection connection = urlConnection
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (JsonTask.pd.isShowing()){
            JsonTask.pd.dismiss();
        }
        imageView.setImageBitmap(result);
    }

}