# hearthstone-camp-distributed-system

- Implemented statistics and usage tracking by logging all API interactions, and built dashboard views to visualize popular card searches, frequent card types, and API latency metrics.
- Containerized and deployed the REST API and web application onto GitHub Codespaces for cloud-based delivery. Configured MongoDB Atlas cloud database and established connectivity.
- Set up GitHub workflows to continuously build, test and deploy code changes to enable rapid iterative development. Wrote comprehensive unit and integration tests using JUnit.
- Documented the architecture, technologies, design decisions and deployment procedures in detail. Also captured screenshots and samples demonstrating main use cases and capabilities.
- Gained valuable hands-on experience building performant and scalable cloud-native applications using state-of-the-art technologies like Spring Boot, MongoDB Atlas and GitHub Codespaces.

## Description

The mobile application will prompt the user for a Hearthstone card name and use the Hearthstone API to fetch card information. The application will display card details and an image of the card. By identifying the different types of cards, several different fields are returned. The hearthstone-cardinfo-pocket-lookup APP provides users with quick access to card information when playing or discussing Hearthstone. Additionally, users can save card details to a MongoDB database for future reference.

At the same time, I also designed the hearthstone-web-dashboard -interface, which allows administrators to test the functionality of the app, as well as view statistics dashboards and logs.

## 1. Implement a Android application

### a. Views in Layout (TextView, EditText, ImageView, or anything that extends android.view.View)

EditText, TextView, ImageView and Button. See result.xml, prompt.xml and result.xml for details.

Card search screen, Enter the name of the card to return the image and details of the card:

[![image-20230930141646017](/Users/macbook/Library/Application Support/typora-user-images/image-20230930141646017.png)](https://claude.ai/chat/prompt.png)

The cards in Hearthstone are divided into four main categories: HERO, MINION, SPELL and WEAPON. They have the same fields and unique fields, with the following logic:

- collectible (bool) determines whether the card is collectible.
- cost (int) is the card's mana cost. Always shown, even if 0, except on hero and buff cards.
- attack (int) is the card's attack value. Always shown on minions and weapons.
- health (int) is the health value of the card. Always shown for minions and heroes.
- durability (int) is the durability value of weapons. Always shown for weapons.
- armor (int) is the armor value of heroes.
- text (String) documented function of spell cards.
- rarity (Rarity enum). Note that the FREE rarity does not determine the presence of a rarity gem. Cards without a rarity gem are those in the CORE set.
- type (CardType enum). The only used types are HERO, MINION, SPELL, WEAPON
- race (Race enum). Usually only available on minions.

### b. Input from the user

The user needs to enter the name of the card to be searched, users can either click on the "submit" button, or "enter" in the bottom right corner of the keyboard:

![image-20230930141653490](https://p.ipic.vip/0agdge.png)

### c. Makes an HTTP request (using HTTP method) to web service

HTTP GET request in ResultActivity.java. The HTTP request is:

```
String url = "http://192.168.1.151:8080/HearthstoneWeb-1.0-SNAPSHOT/cardInfo?cardName=" + mCardName; 

Or

String url = "https://justinqinl-glowing-system-px745457xj7frrg4-8080.preview.app.github.dev/cardInfo?cardName=" + mCardName;
java


JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(      
        Request.Method.GET,        
        url,        
        null,        
        new Response.Listener<JSONObject>() {
```

When the submit button is clicked, the fetchCardInfo method makes a request to my web application, parses the returned JSON to retrieve the card information and downloads the image, and displays it in the View.

### d. Receives and parses an XML or JSON formatted reply from your web service

An example of the JSON reply is:

```
MinionCard{name='Ragnaros the Firelord', cardId='CORE_EX1_298', cardClass='NEUTRAL', cardCost=8, cardFlavor='Ragnaros was summoned by the Dark Iron dwarves, who were eventually enslaved by the Firelord. Summoning Ragnaros often doesn’t work out the way you want it to.', cardRarity='LEGENDARY', cardImageUrl='https://art.hearthstonejson.com/v1/render/latest/enUS/512x/CORE_EX1_298.png', race='ELEMENTAL', attack=8, health=8, collectible=true, type=MINION}
```

### e. Displays new information to users

Here is the screenshot after the submit button is clicked by the user.

![image-20230930141720804](https://p.ipic.vip/h76vni.png)![image-20230930141728435](https://p.ipic.vip/7y3hym.png)

### f. Repeatable (the user can repeatedly reuse the application without restarting it.)

Users can click on search another card to search again

![image-20230930141752189](https://p.ipic.vip/mexf2j.png)![image-20230930141811442](https://p.ipic.vip/e0cd7a.png)

![image-20230930141831206](https://p.ipic.vip/tk6ig5.png)If no card with the corresponding name is searched for, the default screen will be returned:

![image-20230930141839548](https://p.ipic.vip/29ygzi.png)

If the user does not enter anything, then it will prompt:

![image-20230930141846411](https://p.ipic.vip/ha0i43.png)

## 2. Implement a web service

URL: [https://justinqinl-glowing-system-px745457xj7frrg4-8080.preview.app.github.dev](https://justinqinl-glowing-system-px745457xj7frrg4-8080.preview.app.github.dev/)

### a. Implement a simple (can be a single path) API.

In my web app project:
 Model: HearthstoneAPI.java
 View: prompt.jsp, result.jsp, notfound.jsp, dashboard.jsp
 Controller: HearthstoneServlet.java

### b. Receives an HTTP request from the native Android application

HearthstoneServlet.java receives the HTTP GET request with the argument "cardName". It passes this search string on to the model.

### c. Executes business logic appropriate to your application. This includes fetching XML or JSON information from some 3rd party API and processing the response.

HearthstoneServlet.java makes an HTTP request to https://api.hearthstonejson.com/. It then stores the record to MongoDB and create a GSON Object and send it back to the client.

### d. Replies to the Android application with an XML or JSON formatted response. The schema of the response can be of your own design.

If a request is detected from a mobile device (Android), the data from the API is returned in a designed JSON template.

```java
if (userAgent.contains("Android")) {
    ......
    if (card != null){                           
        logInteraction(userId, userAgent, "Response from API", card.getName(), card.getType(), "Card Name: " + cardName + "\n Card Description: " + card.toString(), phoneModel,true,card.cardImageUrl,new Date());
        response.setContentType("application/json");
        String jsonResponse = String.valueOf(card.toJson());
        logInteraction(userId, userAgent, "Response to Phone", card.getName(), card.getType(), "Card Name: " + card.getName() + "\n Card Description: " + card.toString(), phoneModel,true,card.cardImageUrl,new Date());
        response.getWriter().println(jsonResponse);
    }
```

## 3. Handle error conditions

Handle most of the cases where the card name is entered incorrectly, and in such cases I also return an interactive page for the user to re-enter into the plan. In addition, a series of general error messages are handled.

## 4. Log useful information

Logs requests from the phone, requests to the third party API, replies from the third party API and replies to the phone. The log includes the following information:

1. Request path: The program checks if the requested path is "/dashboard" and forwards the request to the corresponding JSP page.
2. User interaction information: If the requested path is not "/dashboard," the program logs the user's interaction with the application, including the user's ID (generated using UUID), the name of the card requested, the user-agent header, and the phone model (if the request comes from an Android phone).
3. API request/response information: The program fetches data from the Hearthstone API based on the requested card name and logs the user's interaction with the API, including the user's ID, the user-agent header, and the phone model (if applicable). If the requested card is found in the API response, the program logs the card's details (name, type, and description) and its image URL. The program also sets the card object as an attribute of the request and forwards it to the result JSP page. If the requested card is not found in the API response, the program logs an error message and forwards the request to the notfound JSP page.

The logging functionality is used in several methods to generate usage statistics, including:

1. getCardTypeUsageStats(): This method retrieves logs that have the "Response from API" request type and a true value for the "cardImageUrl" field. It then generates a map that counts the number of times each card type appears in the logs.
2. getCardUsageStats(): This method aggregates logs with the "Response from API" request type and groups them by card name. It then generates a map that counts the number of times each card name appears in the logs and sorts the results by count.
3. getCardImageUrls(): This method retrieves logs that have the "Response from API" request type and generates a map that maps each card name to its image URL.

## 5. Store the log information in a database

mongodb+srv://justinqinlin:[qinlinj@cluster0.otvez1g.mongodb.net](mailto:qinlinj@cluster0.otvez1g.mongodb.net)/?retryWrites=true&w=majority

## 6. Display operations analytics and full logs on a web-based dashboard

An initial interface for the webmaster to uses.

![image-20230930141934386](https://p.ipic.vip/4903dq.png)

User can test it by using the search box.

Results screen

![image-20230930141952867](https://p.ipic.vip/wc888o.png)

Error screen

![image-20230930141958159](https://p.ipic.vip/65xdk8.png)

In addition, click to browse the statistics dashboard and logs, The following statistical chart will appear:

### Pie chart of statistical user queries for card types

![image-20230930142018407](https://p.ipic.vip/h3azbe.png)

The getCardTypeUsageStats function returns a map of card type usage statistics based on interaction logs. It retrieves logs that contain the string "Response from API" and loops through each log to extract the card type. The function then populates a hashmap with the card types and the number of times they were used.

The createCardTypeUsageChart function generates a pie chart using the data from the getCardTypeUsageStats function.

### Top 5 frequently searched card images

![image-20230930142035761](https://p.ipic.vip/08dd6y.png)

The displayTopCardsImages function generates HTML code for displaying the images of the top 5 most frequently used cards. It retrieves the sortedCardUsageStats array that contains the top 20 most frequently used cards and their usage counts. It then loops through the first 5 elements of this array and retrieves the image URL for each card from the cardImageUrls hashmap. Finally, it creates an HTML img tag with the URL and some styling, and appends it to the cardImageContent variable. The innerHTML property of the element with the ID "top-cards-images" is then set to cardImageContent, which displays the images on the web page.

### Bar chart of the number of times a user has looked up the name of a card

![image-20230930142051070](https://p.ipic.vip/aown4q.png)

The createTopCardsChart function generates a bar chart that displays the top 20 most frequently used cards and their usage counts. It retrieves the sortedCardUsageStats array that contains the top 20 most frequently used cards and their usage counts. It then loops through this array and retrieves the card name and usage count for each card. It populates two arrays, cardLabels and cardData, with the card names and usage counts, respectively. Finally, it creates a bar chart using the Chart.js library, sets various options such as the y-axis scale and step size, and renders the chart in the element with the ID "top-cards-chart".

### A descending list of the number of times a user has looked up the name of a card.

![image-20230930142056834](https://p.ipic.vip/dwiaj8.png)

The displayTopCardsTable function generates an HTML table that displays the top 20 most frequently used cards and their usage counts. It retrieves the sortedCardUsageStats array that contains the top 20 most frequently used cards and their usage counts. It then loops through this array and retrieves the card name and usage count for each card. It creates an HTML tr tag with two td tags for the rank, card name, and usage count, respectively. Finally, it sets the innerHTML property of the element with the ID "top-cards-table-body" to cardTableContent, which displays the table on the web page.

### These are log files.

![image-20230930142104015](https://p.ipic.vip/asydyt.png)

The code above is a JSP file that generates an HTML table displaying log information from a MongoDB collection. The table has six columns: User ID, User Agent, Request Type, Message, Timestamp, and Phone Model. The JSP file connects to the MongoDB database using the MongoClient and MongoDatabase classes from the MongoDB Java driver. It retrieves the logs collection and checks for a user ID filter. If the user ID filter is present, it creates a Bson filter using the Filters.eq() method and applies the filter to the logs collection using the find() method. If the user ID filter is not present, it retrieves all logs using the find() method.