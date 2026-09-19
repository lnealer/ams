<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Decommission scheduled"/>
</jsp:include>

<div class="ams-content">
  <h1>Decommission scheduled</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p>The decommission has been recorded.</p>
  <p><a href="${pageContext.request.contextPath}/assetManagement/InitDashboard.action">Return to the dashboard</a></p>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
