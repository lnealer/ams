<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Saved work"/>
</jsp:include>

<div class="ams-content">
  <h1>Saved work</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <h2>Saved orders</h2>
  <ul>
    <s:iterator value="savedOrders">
      <li>Customer <s:property/></li>
    </s:iterator>
    <s:if test="savedOrders == null || savedOrders.isEmpty()">
      <li class="ams-empty">No saved orders.</li>
    </s:if>
  </ul>

  <h2>Saved change requests</h2>
  <ul>
    <s:iterator value="savedRequests">
      <li>Customer <s:property/></li>
    </s:iterator>
    <s:if test="savedRequests == null || savedRequests.isEmpty()">
      <li class="ams-empty">No saved change requests.</li>
    </s:if>
  </ul>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
