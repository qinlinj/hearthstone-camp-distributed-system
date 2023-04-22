/**
 * andrewId: qinlinj
 * author: Justin Jia
 */

package ds.edu.cmu.hearthstonecardscamp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;


/**
 * The PromptActivity class is an Android activity that prompts the user to enter a
 * card name and then sends a request to a remote server to retrieve information about that card.
 * Once the server returns a response, the activity starts either the ResultActivity or
 * NotFoundActivity depending on whether the card name was found or not.
 * The user can also submit the form by pressing the submit button or the enter key
 * on the soft keyboard.
 * The activity also shows a Toast message if
 * the user tries to submit the form with an empty card name field.
 */
public class PromptActivity extends AppCompatActivity {

    private EditText mCardNameEditText;
    private Button mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prompt);

        mCardNameEditText = findViewById(R.id.cardName);
        mSubmitButton = findViewById(R.id.submit);

        // Set focus on the EditText and show the keyboard when the activity is started
        mCardNameEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mCardNameEditText, InputMethodManager.SHOW_IMPLICIT);

        mCardNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submitForm();
                    return true;
                }
                return false;
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    private void submitForm() {
        String cardName = mCardNameEditText.getText().toString();
        if (cardName.isEmpty()) {
            Toast.makeText(PromptActivity.this, "Please enter a card name", Toast.LENGTH_SHORT).show();
        } else {
            String url = "https://justinqinl-cautious-space-halibut-jxrvpvprwxvfvjg-8080.preview.app.github.dev/cardInfo?cardName=" + cardName;
            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Intent intent = new Intent(PromptActivity.this, ResultActivity.class);
                            intent.putExtra("cardName", cardName);
                            intent.putExtra("response", response.toString());
                            startActivity(intent);
                            mCardNameEditText.clearFocus();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Intent intent = new Intent(PromptActivity.this, NotFoundActivity.class);
                            intent.putExtra("cardName", cardName);
                            startActivity(intent);
                            mCardNameEditText.clearFocus();
                        }
                    }
            );
            queue.add(jsonObjectRequest);
        }
    }
}



