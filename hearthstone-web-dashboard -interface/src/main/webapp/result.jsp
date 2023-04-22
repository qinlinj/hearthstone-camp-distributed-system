<%--andrewId: qinlinj--%>
<%--author: Justin Jia--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Card Information</title>
    <style>
        .card-container {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            border: 1px solid #ccc;
            border-radius: 5px;
            background-color: #f8f8f8;
            margin-bottom: 20px;
        }
        .card-image {
            max-width: 250px;
            margin-right: 20px;
        }
        .card-details {
            font-family: Arial, sans-serif;
        }
    </style>
</head>
<body>
<h1>Card Information</h1>
<div class="card-container">
    <img class="card-image" src="${card.cardImageUrl}" alt="${card.name}" />
    <div class="card-details">
        <h2>${card.name.toUpperCase()}</h2>
        <p><strong>Card ID:</strong> ${card.cardId}</p>
        <p><strong>Card Type:</strong> ${card.type}</p>
        <p><strong>Card Class:</strong> ${card.cardClass}</p>
        <p><strong>Card Cost:</strong> ${card.cardCost}</p>
        <p><strong>Card Flavor:</strong> ${card.cardFlavor}</p>
        <p><strong>Card Rarity:</strong> ${card.cardRarity}</p>
        <c:choose>
            <c:when test="${card.type == 'HERO'}">
                <p><strong>Armor:</strong> ${card.toJson().get("armor").getAsString()}</p>
            </c:when>
            <c:when test="${card.type == 'MINION'}">
                <c:if test="${not empty card.toJson().get('race')}">
                    <p><strong>Race:</strong> ${card.toJson().get("race")}</p>
                </c:if>
                <p><strong>Attack:</strong> ${card.toJson().get("attack").getAsInt()}</p>
                <p><strong>Health:</strong> ${card.toJson().get("health").getAsInt()}</p>
            </c:when>
            <c:when test="${card.type == 'SPELL'}">
                <p><strong>Text:</strong> ${card.toJson().get("text").getAsString()}
            </c:when>
            <c:when test="${card.type == 'WEAPON'}">
                <p><strong>Attack:</strong> ${card.toJson().get("attack").getAsInt()}</p>
                <p><strong>Durability:</strong> ${card.toJson().get("durability").getAsInt()}</p>
            </c:when>
        </c:choose>

    </div>
</div>
<div style="display: flex; align-items: center; justify-content: center; margin-top: 2rem;">
    <button type="button" onclick="location.href='prompt.jsp'" style="background-color: #4CAF50; color: white; padding: 0.5rem 1rem; border: none; border-radius: 4px; font-size: 1.5rem; cursor: pointer;">
        Search Another Card
    </button>
</div>

</body>
</html>




