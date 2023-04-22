
<%--andrewId: qinlinj--%>
<%--author: Justin Jia--%>


<%@ page import="com.mongodb.client.MongoClients" %>
<%@ page import="com.mongodb.client.MongoClient" %>
<%@ page import="com.mongodb.client.MongoDatabase" %>
<%@ page import="com.mongodb.client.MongoCollection" %>
<%@ page import="org.bson.Document" %>
<%@ page import="com.mongodb.client.FindIterable" %>
<%@ page import="com.mongodb.client.MongoCursor" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mongodb.client.model.Filters" %>
<%@ page import="org.bson.conversions.Bson" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.Map" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Analysis Dashboard</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
    <style>
        body {
            margin: 0;
            padding: 0;
        }

        h1 {
            text-align: center;
            margin-top: 2rem;
            margin-bottom: 2rem;
        }

        h2 {
            margin-top: 3rem;
            margin-bottom: 1.5rem;
            font-size: 2rem;
        }

        #dashboard-container {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
            grid-gap: 2rem;
            padding: 1rem;
            justify-items: center;
            align-items: start;
            margin: 2rem auto;
            max-width: 1200px;
        }

        .card-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            border: 1px solid #ccc;
            padding: 1rem;
            box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.2);
            background-color: #fff;
            width: 100%;
            max-width: 600px;
        }

        .chart-label {
            font-size: 1.5rem;
        }

        .chart-label-type {
            font-weight: bold;
        }

        .top-cards-image-container {
            display: flex;
            justify-content: space-around;
            flex-wrap: wrap;
            margin: 2rem auto;
            max-width: 1200px;
        }

        .top-card-image {
            width: 200px;
            height: auto;
            margin: 10px;
        }

        .top-cards-table-container {
            max-width: 1200px;
            margin: 2rem auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th,
        td { border: 1px solid #ccc;
            padding: 0.5rem;
            text-align: center;
        }

        th {
            background-color: #f2f2f2;
        }

        td:first-child {
            font-weight: bold;
        }

        @media screen and (max-width: 768px) {
            #dashboard-container {
                grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            }
        }
    </style>


</head>
<body>
<h1>Analysis Dashboard</h1>
<div id="dashboard-container">
    <div class="card-container">
        <h2>Card Type Usage Statistics</h2>
        <canvas id="card-type-chart"></canvas>
    </div>
    <div class="card-container">
        <h2>Top 5 Searched Cards</h2>
        <div class="top-cards-image-container" id="top-cards-images"></div>
    </div>
    <div class="card-container">
        <h2>Top 20 Searched Cards</h2>
        <canvas id="top-cards-chart"></canvas>
    </div>
    <div class="card-container">
        <h2>Top 20 Searched Cards Rank</h2>
        <div class="top-cards-table-container">
            <table>
                <thead>
                <tr>
                    <th>Rank</th>
                    <th>Card Name</th>
                    <th>Searches</th>
                </tr>
                </thead>
                <tbody id="top-cards-table-body"></tbody>
            </table>
        </div>
    </div>
</div>
<h1>Logs</h1>
<table border="1">
    <tr>
        <th>User ID</th>
        <th>User Agent</th>
        <th>Request Type</th>
        <th>Message</th>
        <th>Timestamp</th>
        <th>Phone Model</th>
    </tr>
    <%
        MongoClient mongoClient = MongoClients.create("mongodb+srv://justinqinlin:qinlinj@cluster0.otvez1g.mongodb.net/?retryWrites=true&w=majority");
        MongoDatabase database = mongoClient.getDatabase("hearthstone");
        MongoCollection<Document> collection = database.getCollection("logs");
        // Adding a user ID filter
        String userId = request.getParameter("userId");
        FindIterable<Document> logs;
        if (userId != null && !userId.trim().isEmpty()) {
            Bson filter = Filters.eq("userId", userId);
            logs = collection.find(filter);
        } else {
            logs = collection.find();
        }
        MongoCursor<Document> cursor = logs.iterator();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (!cursor.hasNext()) {
            out.println("<p>No logs found.</p>");
        } else {
            while (cursor.hasNext()) {
                Document log = cursor.next();
    %>
    <tr>
        <td><%= log.getString("userId") %></td>
        <td><%= log.getString("userAgent") %></td>
        <td><%= log.getString("requestType") %></td>
        <td><%= log.getString("message") %></td>
        <td><%= sdf.format(log.getDate("timestamp")) %></td>
        <td><%= log.getString("phoneModel") %></td>
    </tr>
    <%
            }
        }
        cursor.close(); // Close the cursor to free up resources
        mongoClient.close(); // Closing MongoDB connections to free up resources
    %>
</table>

<script>
    // Get the cardUsageStats and cardImageUrls from the servlet
    var cardUsageStats = <%= new Gson().toJson((Map<String, Integer>) request.getAttribute("cardUsageStats")) %>;
    var cardImageUrls = <%= new Gson().toJson((Map<String, String>) request.getAttribute("cardImageUrls")) %>;
    var sortedCardUsageStats = Object.entries(cardUsageStats).sort((a, b) => b[1] - a[1]);

    // Card Type Usage Chart
    createCardTypeUsageChart();

    // Top 5 Searched Cards Images
    displayTopCardsImages();

    // Top 20 Searched Cards Chart
    createTopCardsChart();

    // Top 20 Searched Cards Table
    displayTopCardsTable();

    function createCardTypeUsageChart() {
        var cardTypeStats = <%= new Gson().toJson((Map<String, Integer>) request.getAttribute("cardTypeStats")) %>;
        var labels = [];
        var data = [];
        for (var key in cardTypeStats) {
            labels.push(key);
            data.push(cardTypeStats[key]);
        }
        var ctx = document.getElementById('card-type-chart').getContext('2d');
        var chart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Card type usage',
                    data: data,
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.2)',
                        'rgba(54, 162, 235, 0.2)',
                        'rgba(255, 206, 86, 0.2)',
                        'rgba(75, 192, 192, 0.2)',
                        'rgba(153, 102, 255, 0.2)',
                        'rgba(255, 159, 64, 0.2)'
                    ],
                    borderColor: [
                        'rgba(255, 99, 132, 1)',
                        'rgba(54, 162, 235, 1)',
                        'rgba(255, 206, 86, 1)',
                        'rgba(75, 192, 192, 1)',
                        'rgba(153, 102, 255, 1)',
                        'rgba(255, 159, 64, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    title: {
                        display: true,
                        text: 'Card Type Usage'
                    },
                    layout: {
                        padding: 0
                    }
                },
                tooltips: {
                    callbacks: {
                        label: function(tooltipItem, data) {
                            var label = data.labels[tooltipItem.index] || '';
                            var cardType = data.datasets[0].data[tooltipItem.index];
                            return label + ': ' + cardType + ' (' + ((cardType/data.datasets[0]._meta.total)*100).toFixed(2) + '%)';
                        }
                    }
                }
            }
        });
    }

    function displayTopCardsImages() {
        var cardImageContent = "";
        for (var j = 0; j < 5; j++) {
            var key = sortedCardUsageStats[j][0];
            var imageUrl = cardImageUrls[key];
            cardImageContent += "<img src='" + imageUrl + "' style='width: 240px; height: auto; margin: 5px;' />";
        }
        document.getElementById("top-cards-images").innerHTML = cardImageContent;
    }

    function createTopCardsChart() {
        var cardLabels = [];
        var cardData = [];
        for (var i = 0; i < sortedCardUsageStats.length; i++) {
            var key = sortedCardUsageStats[i][0];
            var value = sortedCardUsageStats[i][1];
            cardLabels.push(key);
            cardData.push(value);
        }
        var cardCtx = document.getElementById('top-cards-chart').getContext('2d');
        var topCardsChart = new Chart(cardCtx, {
            type: 'bar',
            data: {
                labels: cardLabels,
                datasets: [{
                    label: 'Number of Searches',
                    data: cardData,
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
    }
    function displayTopCardsTable() {
        var cardTableContent = "";
        for (var i = 0; i < sortedCardUsageStats.length; i++) {
            var key = sortedCardUsageStats[i][0];
            var value = sortedCardUsageStats[i][1];
            cardTableContent += "<tr><td>" + (i + 1) + "</td><td>" + key + "</td><td>" + value + "</td></tr>";
        }
        document.getElementById("top-cards-table-body").innerHTML = cardTableContent;
    }
</script>
</body>
</html>


