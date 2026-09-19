<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Device details"/>
</jsp:include>

<div class="ams-content">
  <h1>Device details</h1>
  <jsp:include page="/WEB-INF/common/orderSteps.jsp">
    <jsp:param name="step" value="3"/>
  </jsp:include>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:form action="SaveDevice" namespace="/order" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>

    <fieldset>
      <legend>Device nickname</legend>
      <div class="ams-field">
        <label for="nickname">Nickname</label>
        <s:textfield name="deviceNickname" id="nickname" size="40" maxlength="60"/>
        <s:fielderror><s:param>deviceNickname</s:param></s:fielderror>
      </div>
      <p class="ams-hint">
        The customer's own name for this device - "Front desk router", "Warehouse AP". It is what
        they will quote on the phone; the asset tag we assign means nothing to them.
      </p>
    </fieldset>

    <fieldset>
      <legend>Maintenance window</legend>
      <p class="ams-hint">
        The recurring weekly window when firmware updates and reboots are allowed. Everything in
        it is disruptive, so choose a time the site is closed.
      </p>
      <div class="ams-field-grid">
        <div class="ams-field">
          <label for="mw-day">Day</label>
          <s:select name="maintenanceDayCode" id="mw-day" list="dayOptions"
                    listKey="code" listValue="description"/>
          <s:fielderror><s:param>maintenanceWindow.dayType</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="mw-start">Starts</label>
          <s:select name="maintenanceStartHourCode" id="mw-start" list="hourOptions"
                    listKey="code" listValue="description"/>
          <s:fielderror><s:param>maintenanceWindow.startHour</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="mw-end">Ends</label>
          <s:select name="maintenanceEndHourCode" id="mw-end" list="hourOptions"
                    listKey="code" listValue="description"/>
          <s:fielderror><s:param>maintenanceWindow.endHour</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="mw-tz">Time zone</label>
          <s:select name="maintenanceWindow.timeZone" id="mw-tz" list="timeZoneOptions"/>
          <s:fielderror><s:param>maintenanceWindow.timeZone</s:param></s:fielderror>
        </div>
      </div>
    </fieldset>

    <div class="ams-button-row">
      <s:submit value="Continue to configuration" cssClass="ams-primary"/>
      <%--
        formaction, not <s:submit action="...">. That tag renders name="action:SaveForLater",
        which Struts only honours when struts.mapper.action.prefix.enabled is on - and it is off
        by default in Struts 6, for the same reason dynamic method invocation is off here. Left as
        it was, the button would silently submit the form's own action instead. formaction posts
        the whole form, CSRF token included, to a different URL.
      --%>
      <button type="submit"
              formaction="${pageContext.request.contextPath}/order/SaveForLater.action">Save for later</button>
    </div>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
