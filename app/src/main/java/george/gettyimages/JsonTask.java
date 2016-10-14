package george.gettyimages;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by george on 10/14/16.
 */

public class JsonTask extends AsyncTask<String, String, String> {

    static ProgressDialog pd;
    MainActivity activity;

    static JSONArray imagesJarray;
    //    global for button access of image json array
    View.OnClickListener imageButtonListener;

    public JsonTask(MainActivity activity){
        this.activity = activity;
    }

    protected void onPreExecute() {
        super.onPreExecute();

        pd = new ProgressDialog(activity);
        pd.setMessage("Please wait");
        pd.setCancelable(true);
        pd.show();

        //        functionality for each image button that populates the activity
        imageButtonListener = new View.OnClickListener(){
            public void onClick(View v){
//                set up tag to pass image link to fullscreen activity
                Object tag = v.getTag();

//                intent to fullscreen activity
                Intent intent = new Intent(activity, FullscreenActivity.class);

                try {
//                    pass unique uri using tag
                    intent.putExtra("key", JsonTask.imagesJarray.getJSONObject((Integer)tag)
                            .getJSONArray
                                    ("display_sizes").getJSONObject(0).get("uri").toString());

                    activity.startActivity(intent);
                }catch (JSONException e){
                    Log.e(activity.TAG, "issue obtaining uri from json",e);
                }
            }
        };
    }

    protected String doInBackground(String... params) {
        URLConnection connection;

        BufferedReader reader = null;

        try {
            URL url = new URL(params[0]);
            connection = url.openConnection();
            connection.setRequestProperty("Api-Key","cm9cxkktahs8db3n6jqvxwas");
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
            }

            return buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        extractImages(result);
    }

    //    obtains image uri from json and sets them to imagebuttons
    public void extractImages(String result){
        ArrayList<ImageButton> imageButtons;

        JSONObject object;
        JSONObject imagesObject;
        JSONArray dsJarray;
        imageButtons = new ArrayList<>();

        try {
            object = new JSONObject(result);
            imagesJarray  = object.getJSONArray("images");

            for (int i = 0; i < imagesJarray.length(); i++)
            {
                imagesObject = imagesJarray.getJSONObject(i);

                dsJarray = imagesObject.getJSONArray("display_sizes");

                imageButtons.add(new ImageButton(activity));

                imageButtons.get(i).setLayoutParams(new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f)));

                imageButtons.get(i).setScaleType(ImageButton.ScaleType.CENTER_INSIDE);

                imageButtons.get(i).setBackgroundColor(1);

                ShapeDrawable shapedrawable = new ShapeDrawable();
                shapedrawable.setShape(new RectShape());
                shapedrawable.getPaint().setColor(Color.BLACK);
                shapedrawable.getPaint().setStrokeWidth(1f);
                shapedrawable.getPaint().setStyle(Paint.Style.STROKE);
                imageButtons.get(i).setBackground(shapedrawable);

                imageButtons.get(i).setTag(i);
                imageButtons.get(i).setOnClickListener(imageButtonListener);

                new ImageLoadTask(dsJarray.getJSONObject(0).get("uri").toString(),
                        imageButtons.get(i), activity).execute();
            }

            while (!imageButtons.isEmpty()){
                activity.imageGrid.addView(imageButtons.get(0));

                imageButtons.remove(0);
            }

        }catch (JSONException e){

            String sb = e.toString();
            if (sb.length() > 4000) {
                Log.v(activity.TAG, "sb.length = " + sb.length());
                int chunkCount = sb.length() / 4000;     // integer division
                for (int i = 0; i <= chunkCount; i++) {
                    int max = 4000 * (i + 1);
                    if (max >= sb.length()) {
                        Log.v(activity.TAG, "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i));
                    } else {
                        Log.v(activity.TAG, "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i, max));
                    }
                }
            } else {
                Log.v(activity.TAG, sb.toString());
            }
        }

    }
}