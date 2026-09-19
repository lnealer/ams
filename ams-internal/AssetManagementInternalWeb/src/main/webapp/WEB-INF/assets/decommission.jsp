<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Schedule decommission"/>
</jsp:include>

<div class="ams-content">
  <h1>Schedule decommission</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:form action="SubmitDecommission" namespace="/assets" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <s:hidden name="assetId"/>
    <s:hidden name="customerId"/>

    <p>Decommissioning <strong><s:property value="assetTag" escapeHtml="true"/></strong>.</p>
    <p class="ams-hint">
      Decommissions can be booked up to
      <strong><s:property value="latestSchedulableDate"/></strong>. Work further out has to be
      raised nearer the time.
    </p>

    <label for="scheduledDate">Decommission date</label>
    <s:textfield name="scheduledDate" id="scheduledDate" cssClass="ams-date"/>

    <label for="reason">Reason</label>
    <s:textfield name="reason" id="reason" size="60"/>

    <label>
      <s:checkbox name="hardwareReturnRequired"/> The hardware must be returned
    </label>

    <s:submit value="Schedule" cssClass="ams-primary"/>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
