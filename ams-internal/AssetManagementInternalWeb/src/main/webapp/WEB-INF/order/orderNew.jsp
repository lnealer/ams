<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="New order"/>
</jsp:include>

<div class="ams-content">
  <h1>New order</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:form action="SelectOrderType" namespace="/order" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <fieldset>
      <legend>Order type</legend>
      <s:radio name="orderTypeCode" list="orderTypeOptions" listKey="code" listValue="description"/>
    </fieldset>
    <s:fielderror><s:param>orderTypeCode</s:param></s:fielderror>
    <s:submit value="Continue" cssClass="ams-primary"/>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
