<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Change request history"/>
</jsp:include>

<div class="ams-content">
  <h1>Change request history</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <table class="ams-table">
    <thead>
      <tr>
        <th scope="col">Reference</th>
        <th scope="col">Changes</th>
        <th scope="col">Status</th>
        <th scope="col">Requested</th>
        <th scope="col">Scheduled</th>
      </tr>
    </thead>
    <tbody>
      <s:iterator value="requests">
        <tr>
          <td><s:property value="requestNumber" escapeHtml="true"/></td>
          <td><s:property value="changeTypeDescription" escapeHtml="true"/></td>
          <td><s:property value="networkChangeRequestStatusType.description" escapeHtml="true"/></td>
          <td><s:property value="requestedDate"/></td>
          <td><s:property value="scheduledDate"/></td>
        </tr>
      </s:iterator>
      <s:if test="requests.isEmpty()">
        <tr><td colspan="5" class="ams-empty">No change requests have been raised for this asset.</td></tr>
      </s:if>
    </tbody>
  </table>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
