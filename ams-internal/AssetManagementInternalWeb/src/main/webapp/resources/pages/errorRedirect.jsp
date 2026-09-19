<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%--
  The 404 and 405 page.

  Deliberately standalone: it does not include the common header, because a request for a path that
  does not exist has not been through the application and nothing in the request scope can be
  assumed. It also says nothing about what does exist.
--%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>Page not found</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/main.css"/>
</head>
<body>
<div class="ams-content">
  <h1>Page not found</h1>
  <p>That address does not exist.</p>
  <p><a href="<%= request.getContextPath() %>/">Return to the application</a></p>
</div>
</body>
</html>
