<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%--
  Each link is shown only when the user holds the role behind it, so the navigation never offers
  something that would be refused on clicking.
--%>
<nav class="ams-nav">
  <ul>
    <c:if test="${pageContext.request.isUserInRole('INT_VIEW_DASHBOARD')}">
      <li><a href="${pageContext.request.contextPath}/assetManagement/InitDashboard.action">Dashboard</a></li>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('INT_SEARCH_ASSETS')}">
      <li><a href="${pageContext.request.contextPath}/assetManagement/InitSearch.action">Search</a></li>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('INT_SEARCH_CUSTOMERS')}">
      <li><a href="${pageContext.request.contextPath}/customer/CustomerPicker.action">Customers</a></li>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('INT_CREATE_ORDER')}">
      <li><a href="${pageContext.request.contextPath}/order/InitOrder.action">New order</a></li>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('INT_CUSTOMER_ADMIN')}">
      <li><a href="${pageContext.request.contextPath}/customer/CustomerAdmin.action">Customer admin</a></li>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('INT_ADMIN_UTILITIES')}">
      <li><a href="${pageContext.request.contextPath}/admin/InitAdminUtilities.action">Administration</a></li>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('INT_CHANGE_USER')}">
      <li class="ams-nav-nonprod">
        <a href="${pageContext.request.contextPath}/user/ChangeUser.action">Change user</a>
      </li>
    </c:if>
  </ul>
</nav>
