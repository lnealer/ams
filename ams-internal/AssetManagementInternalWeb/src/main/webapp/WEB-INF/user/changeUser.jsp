<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Change user"/>
</jsp:include>

<div class="ams-content">
  <h1>Change user</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <div class="ams-danger-panel" role="alert">
    <h2>Non-production only</h2>
    <p>
      This screen rebuilds your authentication with different directory groups and can move the
      application's clock. It is available only because this is a non-production environment with
      impersonation switched on. Everything you do here is logged against your real user id.
    </p>
  </div>

  <s:form action="SubmitChangeUser" namespace="/user" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <label for="username">Act as</label>
    <s:textfield name="username" id="username" size="30"/>

    <fieldset>
      <legend>Directory groups</legend>
      <s:checkboxlist name="ldapGroups" list="availableGroups"/>
    </fieldset>

    <label for="timeOverride">Set the clock to</label>
    <s:textfield name="currentTimeOverride" id="timeOverride" cssClass="ams-date"/>

    <s:submit value="Apply" cssClass="ams-primary"/>
  </s:form>

  <s:form action="ClearTimeOverride" namespace="/user" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <s:submit value="Clear the clock override"/>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
