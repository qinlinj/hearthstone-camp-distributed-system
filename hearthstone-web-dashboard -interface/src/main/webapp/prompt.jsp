<%--andrewId: qinlinj--%>
<%--author: Justin Jia--%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Hearthstone Web Management System</title>
</head>
<body>
<h1 style="text-align: center;">Hearthstone Web Management System</h1>
<form action="cardInfo" method="post" style="display: flex; align-items: center; justify-content: center;">
    <label for="cardName" style="font-size: 1.5rem;">Enter the card name for testing:</label>
    <input type="text" id="cardName" name="cardName" required style="padding: 0.5rem; font-size: 1.5rem; border: 2px solid #ccc; border-radius: 4px; margin-left: 1rem; width: 20rem; max-width: 80%;">
    <button type="submit" style="background-color: #4CAF50; color: white; padding: 0.5rem 1rem; border: none; border-radius: 4px; font-size: 1.5rem; margin-left: 1rem; cursor: pointer;">
        Submit
    </button>
</form>
<div style="display: flex; align-items: center; justify-content: center; margin-top: 2rem;">
    <button type="button" onclick="location.href='dashboard'" style="background-color: #008CBA; color: white; padding: 0.5rem 1rem; border: none; border-radius: 4px; font-size: 1.5rem; cursor: pointer;">
        Browse the Statistics Dashboard and Logs
    </button>
</div>
</body>
</html>