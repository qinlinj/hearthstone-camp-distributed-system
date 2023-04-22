/**
 * andrewId: qinlinj
 * author: Justin Jia
 */

package ds.edu.cmu.hearthstonecardscamp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is a Java class called ResultActivity that extends AppCompatActivity.
 * It fetches information about a card by making an HTTP request to a server
 * and displays the results in a user interface.
 * The fetched information includes the card's name, ID, class, cost, flavor, rarity, image,
 * and extra information that depends on the card's type (hero, minion, spell, or weapon).
 * The user can search for another card by clicking the "Search Again" button,
 * which takes them back to the PromptActivity.
 * This class uses the Volley library for network requests and the Glide library for image loading.
 */
public class ResultActivity extends AppCompatActivity {
    private ImageView mCardImageView;
    private TextView mCardNameTextView;
    private TextView mCardIdTextView;
    private TextView mCardClassTextView;
    private TextView mCardCostTextView;
    private TextView mCardFlavorTextView;
    private TextView mCardRarityTextView;
    private TextView mExtraInfoTextView;
    private TextView mCardTypeTextView;
    private Button mSearchAgainButton;

    private String mCardName;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);

        mCardImageView = findViewById(R.id.cardImage);
        mCardNameTextView = findViewById(R.id.cardName);
        mCardIdTextView = findViewById(R.id.cardId);
        mCardClassTextView = findViewById(R.id.cardClass);
        mCardCostTextView = findViewById(R.id.cardCost);
        mCardFlavorTextView = findViewById(R.id.cardFlavor);
        mCardRarityTextView = findViewById(R.id.cardRarity);
        mCardTypeTextView = findViewById(R.id.cardType);
        mExtraInfoTextView = findViewById(R.id.extraInfo);
        mSearchAgainButton = findViewById(R.id.searchAgain);

        mCardName = getIntent().getStringExtra("cardName");

        fetchCardInfo();
        mSearchAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, PromptActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchCardInfo() {
        String url = "https://justinqinl-cautious-space-halibut-jxrvpvprwxvfvjg-8080.preview.app.github.dev/cardInfo?cardName=" + mCardName;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String cardName = response.getString("name");
                            String cardId = response.getString("cardId");
                            String cardClass = response.getString("cardClass");
                            int cardCost = response.getInt("cardCost");
                            String cardFlavor = response.getString("cardFlavor");
                            String cardRarity = response.getString("cardRarity");
                            String cardImageUrl = response.getString("cardImageUrl");
                            String cardType = "";
                            if (response.has("type")) {
                                cardType = response.getString("type");
                            }

                            mCardNameTextView.setText(cardName.toUpperCase());
                            mCardIdTextView.setText("Card ID: " + cardId);
                            mCardClassTextView.setText("Card Class: " + cardClass);
                            mCardCostTextView.setText("Card Cost: " + cardCost);
                            mCardFlavorTextView.setText("Card Flavor: " + cardFlavor);
                            mCardRarityTextView.setText("Card Rarity: " + cardRarity);
                            mCardTypeTextView.setText("Card Type: " + cardType);
                            Glide.with(ResultActivity.this)
                                    .load(cardImageUrl)
                                    .placeholder(R.drawable.default_image)
                                    .error(R.drawable.default_image)
                                    .into(mCardImageView);

                            // Extra info
                            String extraInfo = "";
                            if (cardType.equals("HERO")) {
                                int armor = response.getInt("armor");
                                extraInfo = "Armor: " + armor;
                            } else if (cardType.equals("MINION")) {
                                if (response.has("race")) {
                                    String race = response.getString("race");
                                    extraInfo = "Race: " + race + "\n";
                                }
                                int attack = response.getInt("attack");
                                int health = response.getInt("health");
                                extraInfo += "Attack: " + attack + "\n" + "Health: " + health;
                            } else if (cardType.equals("SPELL")) {
                                String text = response.getString("text");
                                extraInfo = "Text: " + text;
                            } else if (cardType.equals("WEAPON")) {
                                int attack = response.getInt("attack");
                                int durability = response.getInt("durability");
                                extraInfo = "Attack: " + attack + "\n" + "Durability: " + durability;
                            }
                            mExtraInfoTextView.setText(extraInfo);
                            Log.d("JsonObject", response.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError) {
                            Toast.makeText(ResultActivity.this, "Network error, please try again later", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(ResultActivity.this, "Server error, please try again later", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(ResultActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResultActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        queue.add(jsonObjectRequest);
    }
}