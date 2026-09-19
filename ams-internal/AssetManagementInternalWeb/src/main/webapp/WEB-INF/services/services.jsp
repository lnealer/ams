<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Services"/>
</jsp:include>

<div class="ams-content">
  <h1>Services</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <table class="ams-table">
    <thead>
      <tr>
        <th scope="col">Service</th>
        <th scope="col">Status</th>
        <th scope="col">Started</th>
        <th scope="col">Reference</th>
      </tr>
    </thead>
    <tbody>
      <s:iterator value="services">
        <tr>
          <td><s:property value="serviceType.description" escapeHtml="true"/></td>
          <td><s:property value="serviceStatusType.description" escapeHtml="true"/></td>
          <td><s:property value="startDate"/></td>
          <td><s:property value="externalServiceReference" escapeHtml="true"/></td>
        </tr>
      </s:iterator>
      <s:if test="services == null || services.isEmpty()">
        <tr><td colspan="4" class="ams-empty">This customer has no services.</td></tr>
      </s:if>
    </tbody>
  </table>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
