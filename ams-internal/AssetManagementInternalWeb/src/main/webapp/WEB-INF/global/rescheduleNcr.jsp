<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Reschedule change request"/>
</jsp:include>

<div class="ams-content">
  <h1>Reschedule change request</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:form action="SubmitReschedule" namespace="/networkChangeRequest" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <s:hidden name="networkChangeRequestId"/>
    <p>
      Currently scheduled for <strong><s:property value="request.scheduledDate"/></strong>.
    </p>
    <label for="newDate">New date</label>
    <s:textfield name="newDate" id="newDate" cssClass="ams-date"/>
    <p class="ams-warning">
      The existing booking is released before the new one is taken. If the new date turns out to be
      unavailable the request will be left unscheduled.
    </p>
    <s:submit value="Reschedule" cssClass="ams-primary"/>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
