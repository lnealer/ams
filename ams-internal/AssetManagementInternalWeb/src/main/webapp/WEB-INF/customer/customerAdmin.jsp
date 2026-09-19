<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Customer administration"/>
</jsp:include>

<div class="ams-content">
  <h1>Customer administration</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:if test="customer != null">
    <dl class="ams-detail">
      <dt>Customer</dt><dd><s:property value="customer.displayName" escapeHtml="true"/></dd>
      <dt>Account number</dt><dd><s:property value="customer.accountNumber" escapeHtml="true"/></dd>
      <dt>Active</dt><dd><s:property value="customer.active"/></dd>
      <dt>Installed assets</dt><dd><s:property value="installedAssetCount"/></dd>
    </dl>

    <c:if test="${pageContext.request.isUserInRole('INT_ENABLE_ORDERING')}">
      <jsp:include page="/WEB-INF/customer/enableOrdering.jsp"/>
    </c:if>

    <h2>Services</h2>
    <table class="ams-table">
      <thead>
        <tr><th scope="col">Service</th><th scope="col">Status</th><th scope="col">Started</th></tr>
      </thead>
      <tbody>
        <s:iterator value="services">
          <tr>
            <td><s:property value="serviceType.description" escapeHtml="true"/></td>
            <td><s:property value="serviceStatusType.description" escapeHtml="true"/></td>
            <td><s:property value="startDate"/></td>
          </tr>
        </s:iterator>
      </tbody>
    </table>
  </s:if>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
