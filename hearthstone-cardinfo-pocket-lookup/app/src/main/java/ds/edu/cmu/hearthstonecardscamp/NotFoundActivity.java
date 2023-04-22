/**
 * andrewId: qinlinj
 * author: Justin Jia
 */

package ds.edu.cmu.hearthstonecardscamp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The function NotFoundActivity is an Android activity that is triggered
 * when a particular card is not found in the application.
 * It displays a message informing the user that the card they
 * were looking for could not be found and provides a button to allow the user to perform
 * a new search.
 * The activity uses the cardName parameter to dynamically create the message shown to the user.
 * When the user clicks on the "Search Again" button, the onClick method of the
 * mSearchAgainButton object creates a new intent to
 * the PromptActivity to allow the user to start a new search.
 */
public class NotFoundActivity extends AppCompatActivity {
    private TextView mCardNameNotFoundTextView;
    private Button mSearchAgainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notfound);
        String cardName = getIntent().getStringExtra("cardName");

        mCardNameNotFoundTextView = findViewById(R.id.cardNameNotFound);

        mCardNameNotFoundTextView.setText("Sorry, we were unable find the \"" + cardName + "\" care you were looking for.");

        mSearchAgainButton = findViewById(R.id.searchAgain);

        mSearchAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotFoundActivity.this, PromptActivity.class);
                startActivity(intent);
            }
        });
    }
}
