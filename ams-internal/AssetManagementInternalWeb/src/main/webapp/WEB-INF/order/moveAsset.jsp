<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Move asset"/>
</jsp:include>

<div class="ams-content">
  <h1>Move asset</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p>
    Moving an asset is raised as a network change request, because it needs an engineer and a
    change window.
  </p>
  <p>
    <a href="${pageContext.request.contextPath}/networkChangeRequest/InitRequest.action?assetId=<s:property value='assetId'/>">
      Raise a move request
    </a>
  </p>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
