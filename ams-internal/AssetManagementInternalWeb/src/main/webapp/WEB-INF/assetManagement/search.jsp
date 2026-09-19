<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Search assets"/>
</jsp:include>

<div class="ams-content">
  <h1>Search assets</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:form action="Search" namespace="/assetManagement" method="get" cssClass="ams-search-form">
    <label for="searchCriteria">Search by</label>
    <s:select name="searchCriteria" id="searchCriteria" list="searchCriteriaTypes"
              listKey="code" listValue="description"/>

    <label for="searchTerm">Search for</label>
    <s:textfield name="searchTerm" id="searchTerm" size="40"/>

    <s:submit value="Search" cssClass="ams-primary"/>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
