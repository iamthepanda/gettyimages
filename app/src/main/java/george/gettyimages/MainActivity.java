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

/*
• Create a simple app to display images from a search term using an image search API of your choice (Getty Images, for example). Please do not use an SDK that encapsulates interaction with the chosen API, as we would like to see you demonstrate how you would design interaction with an API that is not public.

• The app should include one text entry field, a button to request results and a results area to display the images in a scrolling grid. When tapping on an image, it should display fullscreen.

• Implement your own spelling checker that automatically corrects some user input mistakes. Do NOT use any third party code or libraries to implement the spell checker (including UITextChecker), we want to see how you write code to solve a problem, however you may use third party libraries for other parts of the app. Run your spell checker on the input word before submitting the image search requests. If multiple corrections are possible, just pick from any of them. For example, if the user types in 'ce3t', your program should return images for 'cat' even though the user mistyped the word.
• Your spell checker should perform these specific kinds of typo corrections (and only these): - Remove non-letter characters. 'nyl;on' should auto-correct to ‘nylon'
- Mistyped vowels. 'ceku' should auto-correct to ‘cake'
 */

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    //    declare variables for existing xml elements
    EditText textEntryField;
    Button requestResults;
    GridLayout imageGrid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        instantiate xml variables
        textEntryField = (EditText) findViewById(R.id.text_entry_field);
        requestResults = (Button) findViewById(R.id.request_results);
        imageGrid = (GridLayout) findViewById(R.id.image_grid);
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
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });


    }

//    removes non-letter characters and corrects mistyped vowels
    public String spellchecker(String word){
//        remove non-letter characters
        word =  word.replaceAll("[^a-zA-Z]", "");

//        initialize word list
        Words dictionary = new Words();

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