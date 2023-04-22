/**
 * andrewId: qinlinj
 * author: Justin Jia
 */

import com.mongodb.client.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.io.IOException;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * The HearthstoneServlet class is a servlet that handles HTTP requests and responses for a web application
 * related to the Hearthstone card game.
 * It interacts with a MongoDB database to log user interactions with the web application
 * and provide statistics on card usage.
 * The class defines methods for retrieving and processing data from the database,
 * as well as methods for logging user interactions with the web application.
 * The doGet method handles HTTP GET requests and returns either a dashboard page with
 * card usage statistics or a result page with information about a specific card.
 * The doPost method simply calls the doGet method.
 */
@WebServlet(name = "HearthstoneServlet", urlPatterns = {"/cardInfo", "/dashboard"})
public class HearthstoneServlet extends HttpServlet {

    // The name of the MongoDB database used to store user interaction logs
    private static final String DATABASE_NAME = "hearthstone";
    // The name of the MongoDB collection used to store user interaction logs
    private static final String COLLECTION_NAME = "logs";

    // The MongoDB database and client used to connect to the database
    private MongoDatabase database;
    private MongoClient mongoClient;

    // Initializes the MongoDB connection when the servlet is first created
    @Override
    public void init() {
        mongoClient = MongoClients.create("mongodb+srv://justinqinlin:qinlinj@cluster0.otvez1g.mongodb.net/?retryWrites=true&w=majority");
        database = mongoClient.getDatabase(DATABASE_NAME);
    }

    // Handles HTTP GET requests
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/dashboard".equals(path)) {
            // Returns the dashboard page with card usage statistics
            Map<String, Integer> cardTypeStats = getCardTypeUsageStats();
            request.setAttribute("cardTypeStats", cardTypeStats);
            request.setAttribute("cardUsageStats", getCardUsageStats());
            request.setAttribute("cardImageUrls", getCardImageUrls());
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
        } else {
            // Gets or creates a user ID
            String userId = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("userId".equals(cookie.getName())) {
                        userId = cookie.getValue();
                        break;
                    }
                }
            }

            if (userId == null) {
                userId = UUID.randomUUID().toString();
                Cookie userIdCookie = new Cookie("userId", userId);
                userIdCookie.setMaxAge(60 * 60 * 24 * 365); // Set cookie validity to 1 year
                response.addCookie(userIdCookie);
            }

            if ("/dashboard".equals(path)) {
                // Returns the dashboard page with card usage statistics
                request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
            } else {
                // Gets the name of the card from the request parameters and logs the user interaction
                String cardName = request.getParameter("cardName");
                String userAgent = request.getHeader("User-Agent");
                String phoneModel = request.getHeader("X-Phone-Model");
                userId = UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 4);
                try {
                    // Logs the user interaction with the application
                    logInteraction(userId, "", "", "", null, "Logging user "+userId+"'s access information" , "",false,"",new Date());
                    // Fetches the card data from the Hearthstone API and logs the user interaction
                    Card card = HearthstoneAPI.fetchCardByName(cardName);
                    if (userAgent.contains("Android")) {
                        logInteraction(userId, userAgent, "Request from Phone", cardName,null,"Card Name: " + cardName, phoneModel,false,"",new Date());
                        logInteraction(userId, userAgent, "Request to API", cardName,null,"Search: " + cardName, phoneModel,false,"",new Date());
                        if (card != null){                            logInteraction(userId, userAgent, "Response from API", card.getName(), card.getType(), "Card Name: " + cardName + "\n Card Description: " + card.toString(), phoneModel,true,card.cardImageUrl,new Date());
                            response.setContentType("application/json");
                            String jsonResponse = String.valueOf(card.toJson());
                            logInteraction(userId, userAgent, "Response to Phone", card.getName(), card.getType(), "Card Name: " + card.getName() + "\n Card Description: " + card.toString(), phoneModel,true,card.cardImageUrl,new Date());
                            response.getWriter().println(jsonResponse);
                        } else {
// Logs the interaction if the card was not found
                            logInteraction(userId, userAgent, "None Response from API", cardName, null, "Card Name: " + cardName + "\n Card Description: " + "Card not found", phoneModel,false,"",new Date());
                            String jsonResponse = "Not Found";
                            logInteraction(userId, userAgent, "Response to Phone", cardName, null, "Card Name: " + cardName + "\n Card Description: " + "Card not found", phoneModel,false,"",new Date());
                        }

                    } else {
                        logInteraction(userId, userAgent, "Request from Browser", cardName,null,"Card Name: " + cardName, "Browser",false,"",new Date());
                        logInteraction(userId, userAgent, "Request to API", cardName,null,"Search: " + cardName, "Browser",false,"",new Date());
                        if (card != null) {
                            // Logs the interaction if the card was found and sets the card object as an attribute of the request before forwarding to the result page
                            logInteraction(userId, userAgent, "Response from API", card.getName(), card.getType(), "Card Name: " + cardName + "\n Card Description: " + card.toString(), "Browser",true,card.cardImageUrl,new Date());
                            request.setAttribute("card", card);
                            request.getRequestDispatcher("/result.jsp").forward(request, response);
                            logInteraction(userId, userAgent, "Response to Browser", card.getName(), card.getType(), "Card Name: " + card.getName() + "\n Card Description: " + card.toString(), "Browser",true,card.cardImageUrl,new Date());
                        } else {
                            // Logs the interaction if the card was not found and forwards to the notfound.jsp page
                            logInteraction(userId, userAgent, "None Response from API", cardName,null,"Card Name: " + cardName + "\n Card Description: " + "Card not found", "Browser",false,"",new Date());
                            logInteraction(userId, userAgent, "Response to Browser", cardName, null, "Card Name: " + cardName + "\n Card Description: " + "Card not found", "Browser",false,"",new Date());
                            request.getRequestDispatcher("/notfound.jsp").forward(request, response);
                        }
                    }
                } catch (Exception e) {
                    // Logs an error if an exception occurs while processing the request
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing the request.");
                    e.printStackTrace();
                }
            }
        }
    }

    // Handles HTTP POST requests by calling doGet
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    // Logs a user interaction to the MongoDB database
    private void logInteraction(String userId, String userAgent, String requestType, String cardName, CardType cardType, String message, String phoneModel, boolean results, String cardImageUrl, Date time) {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        Document log = new Document("userId", userId)
                .append("userAgent", userAgent)
                .append("requestType", requestType)
                .append("cardName", cardName.toUpperCase());
        if (cardType != null) {
            log.append("cardType", cardType.toString()); // convert to string or integer
        }
        log.append("message", message)
                .append("phoneModel", phoneModel)
                .append("timestamp", time)
                .append("results", results)
                .append("URL",cardImageUrl);
        collection.insertOne(log);
//        collection.drop();
    }
    // Returns a list of filtered interaction logs from the MongoDB database
    public List<Document> getFilteredLogs(String requestType, boolean results) {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        Bson filter = and(eq("requestType", requestType), eq("results", results));
        FindIterable<Document> iterable = collection.find(filter);
        List<Document> logs = new ArrayList<>();
        for (Document document : iterable) {
            logs.add(document);
        }
        return logs;
    }

    // Returns a map of card type usage statistics based on interaction logs
    public Map<String, Integer> getCardTypeUsageStats() {
        List<Document> logs = getFilteredLogs("Response from API", true);
        Map<String, Integer> cardTypeStats = new HashMap<>();

        for (Document log : logs) {
            String cardType = log.getString("cardType");
            if (cardType != null) {
                cardTypeStats.putIfAbsent(cardType, 0);
                cardTypeStats.put(cardType, cardTypeStats.get(cardType) + 1);
            }
        }

        return cardTypeStats;
    }

    // Returns a map of card usage statistics based on interaction logs
    private Map<String, Integer> getCardUsageStats() {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        Map<String, Integer> cardUsageStats = new HashMap<>();

        // Aggregate logs with the specified requestType and group them by cardName
        List<Document> cardStats = collection.aggregate(
                Arrays.asList(
                        new Document("$match", new Document("requestType", "Response from API")),
                        new Document("$group", new Document("_id", "$cardName").append("count", new Document("$sum", 1))),
                        new Document("$sort", new Document("count", -1)),
                        new Document("$limit", 20)
                )
        ).into(new ArrayList<>());

        for (Document stat : cardStats) {
            cardUsageStats.put(stat.getString("_id"), stat.getInteger("count"));
        }

        return cardUsageStats;
    }

    // Returns a map of card image URLs based on interaction logs
    private Map<String, String> getCardImageUrls() {
        Map<String, String> cardImageUrls = new HashMap<>();
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

        // Get all unique card names and their image URLs
        List<Document> cardImageUrlDocuments = collection.aggregate(
                Arrays.asList(
                        new Document("$match", new Document("requestType", "Response from API")),
                        new Document("$group", new Document("_id", "$cardName").append("cardImageUrl", new Document("$first", "$URL"))),
                        new Document("$project", new Document("cardName", "$_id").append("cardImageUrl", 1).append("_id", 0))
                )
        ).into(new ArrayList<>());

        for (Document document : cardImageUrlDocuments) {
            cardImageUrls.put(document.getString("cardName"), document.getString("cardImageUrl"));
        }

        return cardImageUrls;
    }

}

