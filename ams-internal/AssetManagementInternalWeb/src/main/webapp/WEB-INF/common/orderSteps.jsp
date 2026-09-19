<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  The ordering flow's progress bar.

  Included by each step with its own number, e.g.:
      <jsp:include page="/WEB-INF/common/orderSteps.jsp">
        <jsp:param name="step" value="3"/>
      </jsp:include>

  Steps already completed are links, so going back to change something does not mean starting
  again; steps not yet reached are plain text, because their actions would only bounce a user who
  has not filled in what they depend on.
--%>
<c:set var="current" value="${param.step}"/>
<c:set var="furthest">
  <c:choose>
    <c:when test="${not empty furthestStepReached}">${furthestStepReached}</c:when>
    <c:otherwise>${current}</c:otherwise>
  </c:choose>
</c:set>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<ol class="ams-steps">
  <c:forEach var="s" begin="1" end="6">
    <c:set var="label">
      <c:choose>
        <c:when test="${s == 1}">Contacts</c:when>
        <c:when test="${s == 2}">Address</c:when>
        <c:when test="${s == 3}">Device</c:when>
        <c:when test="${s == 4}">Configuration</c:when>
        <c:when test="${s == 5}">Subscriber PCs</c:when>
        <c:otherwise>Despatch</c:otherwise>
      </c:choose>
    </c:set>
    <c:set var="action">
      <c:choose>
        <c:when test="${s == 1}">BeginOrder</c:when>
        <c:when test="${s == 2}">OrderAddress</c:when>
        <c:when test="${s == 3}">OrderDevice</c:when>
        <c:when test="${s == 4}">OrderConfiguration</c:when>
        <c:when test="${s == 5}">OrderSubscriberPcs</c:when>
        <c:otherwise>ShippingWindow</c:otherwise>
      </c:choose>
    </c:set>
    <li class="ams-step
               <c:if test="${s == current}">ams-step--current</c:if>
               <c:if test="${s < current}">ams-step--done</c:if>"
        <c:if test="${s == current}">aria-current="step"</c:if>>
      <span class="ams-step-number">${s}</span>
      <c:choose>
        <c:when test="${s < current or (s <= furthest and s != current)}">
          <a href="${ctx}/order/${action}.action"><c:out value="${label}"/></a>
        </c:when>
        <c:otherwise>
          <c:out value="${label}"/>
        </c:otherwise>
      </c:choose>
    </li>
  </c:forEach>
</ol>
