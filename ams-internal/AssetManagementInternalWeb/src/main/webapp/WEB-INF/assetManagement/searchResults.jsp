<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Search results"/>
</jsp:include>

<div class="ams-content">
  <h1>Search results</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p class="ams-result-count">
    <s:property value="totalMatches"/> matching assets.
    <s:if test="truncated">
      Showing the first <s:property value="results.size()"/>.
    </s:if>
  </p>

  <c:set var="assetRows" value="${model.results}" scope="request"/>
  <jsp:include page="/WEB-INF/common/assetTable.jsp"/>

  <p><a href="${pageContext.request.contextPath}/assetManagement/InitSearch.action">New search</a></p>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
