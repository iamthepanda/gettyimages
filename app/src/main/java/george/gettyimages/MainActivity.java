package george.gettyimages;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


/*
• Create a simple app to display images from a search term using an image search API of your choice (Getty Images, for example). Please do not use an SDK that encapsulates interaction with the chosen API, as we would like to see you demonstrate how you would design interaction with an API that is not public.

• The app should include one text entry field, a button to request results and a results area to display the images in a scrolling grid. When tapping on an image, it should display full- screen.

• Implement your own spelling checker that automatically corrects some user input mistakes. Do NOT use any third party code or libraries to implement the spell checker (including UITextChecker), we want to see how you write code to solve a problem, however you may use third party libraries for other parts of the app. Run your spell checker on the input word before submitting the image search requests. If multiple corrections are possible, just pick from any of them. For example, if the user types in 'ce3t', your program should return images for 'cat' even though the user mistyped the word.
• Your spell checker should perform these specific kinds of typo corrections (and only these): - Remove non-letter characters. 'nyl;on' should auto-correct to ‘nylon'
- Mistyped vowels. 'ceku' should auto-correct to ‘cake'
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    //    declare variables for xml elements
    EditText textEntryField;
    Button requestResults;
    GridLayout imageGrid;
    ArrayList<ImageButton> imageButtons;

    JSONObject object;
    JSONArray imagesJarray;

    String globalResults;

    int x = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        instantiate xml variables
        textEntryField = (EditText) findViewById(R.id.text_entry_field);
        requestResults = (Button) findViewById(R.id.request_results);
        imageGrid = (GridLayout) findViewById(R.id.image_grid);
    }

    //    TODO: for testing purposes. remove before submitting challenge
    public void refreshActivity(View v){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void displayImages(){

//        createDummyImages();

//        display entire list of image buttons
//        while (!imageButtons.isEmpty()){
//            imageGrid.addView(imageButtons.get(imageButtons.size()-1));
//
//            imageButtons.remove(imageButtons.size()-1);
//        }
    }

    @Override
    protected void onStart(){
        super.onStart();

//        soft keyboard 'return' button functionality
        textEntryField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    requestResults.performClick();
                    return true;
                }
                return false;
            }
        });

//        functionality for search button
        requestResults.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                imageGrid.removeAllViews();
//                grab images and display them


                String phrase = textEntryField.getText().toString();

                phrase = spellchecker(phrase);

                if(phrase.matches("")){
                    Toast.makeText(MainActivity.this, "Please provide a search phrase",
                            Toast.LENGTH_LONG).show();
                }else{
                    new JsonTask().execute("https://api.gettyimages" +
                            ".com:443/v3/search/images/creative?phrase="+phrase);



                    displayImages();

//                hide soft keyboard after performing search
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }



            }
        });
    }

    public String spellchecker(String word){

//        remove non-letter characters
        word =  word.replaceAll("[^a-zA-Z]", "");

        Words dictionary =new Words();
        if(dictionary.wordSet.containsRegEx(word)){
        }else{
            if(dictionary.wordSet.containsRegEx(word.replaceAll("[aeiou]","[aeiou]"))) {
                for (int i = 0; i < dictionary.wordArray.length - 1; i++) {
                    if (word.replaceAll("[aeiou]", " ").equals(dictionary.wordArray[i].replaceAll
                            ("[aeiou]", " "))) {
                        word = dictionary.wordArray[i];
                        break;
                    }
                }

                System.out.println("we has " + word);
            }
        }

        textEntryField.setText(word);

        return word;
    }

    ProgressDialog pd;

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
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
            if (pd.isShowing()){
                pd.dismiss();
            }

            extractImages(result);
        }
    }


    public void extractImages(String result){
        globalResults = result;

        imageButtons = new ArrayList<>();




        try {

            JSONObject imagesObject;
            JSONArray dsJarray;
            JSONObject dsObject;

            object = new JSONObject(result);
            imagesJarray  = object.getJSONArray("images");

            View.OnClickListener myListener = new View.OnClickListener(){
                public void onClick(View v){
                    Intent intent = new Intent(MainActivity.this, FullscreenActivity.class);
                    try {
                        Object tag = v.getTag();
                        intent.putExtra("key", imagesJarray.getJSONObject((Integer)tag).getJSONArray
                                ("display_sizes")
                                .getJSONObject(0).get
                                        ("uri")
                                .toString());
                        startActivity(intent);
                    }catch (JSONException e){
                        Log.e(TAG, "shits fucked",e);
                    }
                }
            };

            for (int i = 0; i < imagesJarray.length(); i++)
            {

                imagesObject = imagesJarray.getJSONObject(i);

                dsJarray = imagesObject.getJSONArray("display_sizes");

                imageButtons.add(new ImageButton(this));


                imageButtons.get(i).setImageResource(R.mipmap.ic_launcher);
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
                imageButtons.get(i).setOnClickListener(myListener);


                for (int j = 0; j < dsJarray.length(); j++)
                {
                    dsObject =dsJarray.getJSONObject(j);

                    new ImageLoadTask(dsJarray.getJSONObject(j).get("uri").toString(),
                            imageButtons.get(i))
                            .execute();
                }
            }

            while (!imageButtons.isEmpty()){
                imageGrid.addView(imageButtons.get(imageButtons.size()-1));

                imageButtons.remove(imageButtons.size()-1);
            }

        }catch (JSONException e){

            String sb = e.toString();
            if (sb.length() > 4000) {
                Log.v(TAG, "sb.length = " + sb.length());
                int chunkCount = sb.length() / 4000;     // integer division
                for (int i = 0; i <= chunkCount; i++) {
                    int max = 4000 * (i + 1);
                    if (max >= sb.length()) {
                        Log.v(TAG, "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i));
                    } else {
                        Log.v(TAG, "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i, max));
                    }
                }
            } else {
                Log.v(TAG, sb.toString());
            }
        }

    }

}