<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Cancel configuration change"/>
</jsp:include>

<div class="ams-content">
  <h1>Cancel configuration change</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p>This will discard the pending configuration change. The device keeps its current settings.</p>

  <s:form action="SubmitCancelModifyConfiguration" namespace="/cancel" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <s:hidden name="configurationId"/>
    <s:hidden name="assetId"/>
    <s:submit value="Cancel the change" cssClass="ams-danger"/>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
