<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Identity changed"/>
</jsp:include>

<div class="ams-content">
  <h1>Identity changed</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p>Your session now uses the groups you selected.</p>
  <p><a href="${pageContext.request.contextPath}/assetManagement/InitDashboard.action">Return to the dashboard</a></p>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
