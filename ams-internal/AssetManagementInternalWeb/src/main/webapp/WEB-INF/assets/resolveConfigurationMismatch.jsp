<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Mismatch resolved"/>
</jsp:include>

<div class="ams-content">
  <h1>Mismatch resolved</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p>The mismatch has been resolved.</p>
  <p>
    <a href="${pageContext.request.contextPath}/assets/CompareConfig.action?assetId=<s:property value='assetId'/>">
      Compare again
    </a>
  </p>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
