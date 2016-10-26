package george.gettyimages;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    //    variables for existing xml elements
    EditText textEntryField;
    Button requestResults;
    GridLayout imageGrid;

    Words dictionary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textEntryField = (EditText) findViewById(R.id.text_entry_field);
        requestResults = (Button) findViewById(R.id.request_results);
        imageGrid = (GridLayout) findViewById(R.id.image_grid);

//        initialize word list
        dictionary = new Words();
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


//                grab the user's search phrase from the text entry box
                String phrase = textEntryField.getText().toString();

                phrase = spellchecker(phrase);

//                make api call only if search field is not empty
                if(phrase.matches("")){
                    Toast.makeText(MainActivity.this, "Please provide a search phrase",
                            Toast.LENGTH_LONG).show();
                }else{
//                    plugs phrase in as query
                    new JsonTask(MainActivity.this).execute("https://api.gettyimages" +
                            ".com:443/v3/search/images/creative?phrase=" + phrase);

//                hide soft keyboard after performing search
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                }
            }
        });
    }

//    removes non-letter characters and corrects mistyped vowels
    public String spellchecker(String word){
//        remove non-letter characters
        word =  word.replaceAll("[^a-zA-Z]", "");

//        only perform vowel correction if mistyped word is not in the dictionary
        if(!dictionary.wordSet.contains(word)){

//            match mistyped word to word in the word list using regex
            if(dictionary.wordSet.containsRegEx(word.replaceAll("[aeiou]","[aeiou]"))) {

//                iterate through word list and set word equal to the first match
                for (int i = 0; i < dictionary.wordArray.length; i++) {
                    if (word.replaceAll("[aeiou]", " ").equals(dictionary.wordArray[i].replaceAll
                            ("[aeiou]", " "))) {
                        word = dictionary.wordArray[i];

                        break;
                    }
                }
            }
        }

//        update the search field with the cleaned phrase
        textEntryField.setText(word);

        return word;
    }
}