<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Network change request"/>
</jsp:include>

<div class="ams-content">
  <h1>Network change request</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:if test="request != null">
    <dl class="ams-detail">
      <dt>Reference</dt><dd><s:property value="request.requestNumber" escapeHtml="true"/></dd>
      <dt>Status</dt><dd><s:property value="request.networkChangeRequestStatusType.description" escapeHtml="true"/></dd>
      <dt>Changes</dt><dd><s:property value="request.changeTypeDescription" escapeHtml="true"/></dd>
      <dt>Requested</dt><dd><s:property value="request.requestedDate"/></dd>
      <dt>Scheduled</dt><dd><s:property value="request.scheduledDate"/></dd>
    </dl>

    <s:if test="request.cancellable">
      <s:form action="CancelRequest" namespace="/networkChangeRequest" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
        <s:hidden name="networkChangeRequestId"/>
        <label for="cancellationReason">Reason for cancelling</label>
        <s:textfield name="cancellationReason" id="cancellationReason" size="60"/>
        <s:submit value="Cancel this request" cssClass="ams-danger"/>
      </s:form>
    </s:if>
  </s:if>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
