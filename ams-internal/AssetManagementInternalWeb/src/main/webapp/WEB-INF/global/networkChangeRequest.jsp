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

  <s:form action="SubmitRequest" namespace="/networkChangeRequest" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <s:hidden name="customerId"/>
    <s:hidden name="assetId"/>

    <fieldset>
      <legend>What is changing</legend>
      <s:checkboxlist name="changeTypes" list="changeTypeOptions" listKey="code"
                      listValue="description"/>
    </fieldset>

    <fieldset>
      <legend>When</legend>
      <label for="requestedDate">Requested date</label>
      <s:textfield name="requestedDate" id="requestedDate" cssClass="ams-date"/>
    </fieldset>

    <fieldset>
      <legend>Notes</legend>
      <s:textarea name="comments" rows="4" cols="60"/>
    </fieldset>

    <s:submit value="Submit request" cssClass="ams-primary"/>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
