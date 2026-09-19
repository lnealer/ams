<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Compare configuration"/>
</jsp:include>

<div class="ams-content">
  <h1>Compare configuration</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:if test="matching">
    <p class="ams-messages">The stored configuration matches what the device reports.</p>
  </s:if>
  <s:else>
    <p class="ams-warning">These differ. Choose which one is correct.</p>
    <ul class="ams-differences">
      <s:iterator value="differences">
        <li><s:property escapeHtml="true"/></li>
      </s:iterator>
    </ul>

    <div class="ams-resolution">
      <s:form action="AcceptDeviceConfig" namespace="/assets" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
        <s:hidden name="assetId"/>
        <s:submit value="The device is right; record what it reports"/>
      </s:form>
      <s:form action="ReapplyStoredConfig" namespace="/assets" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
        <s:hidden name="assetId"/>
        <s:submit value="AMS is right; apply the stored configuration again"/>
      </s:form>
    </div>
  </s:else>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
