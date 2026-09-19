<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Administration"/>
</jsp:include>

<div class="ams-content">
  <h1>Administration</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <h2>Work queues</h2>
  <table class="ams-table">
    <thead><tr><th scope="col">Queue</th><th scope="col">Waiting</th></tr></thead>
    <tbody>
      <s:iterator value="queueDepths">
        <tr>
          <td><s:property value="key" escapeHtml="true"/></td>
          <td><s:property value="value"/></td>
        </tr>
      </s:iterator>
    </tbody>
  </table>

  <h2>Nightly load</h2>
  <p>Last run: <s:property value="lastEtlStatus.description" escapeHtml="true"/></p>

  <h2>Stuck work</h2>
  <ul>
    <s:iterator value="stuckEntries">
      <li><s:property escapeHtml="true"/></li>
    </s:iterator>
    <s:if test="stuckEntries == null || stuckEntries.isEmpty()">
      <li class="ams-empty">Nothing is stuck.</li>
    </s:if>
  </ul>

  <h2>Runtime properties</h2>
  <table class="ams-table">
    <thead><tr><th scope="col">Key</th><th scope="col">Value</th></tr></thead>
    <tbody>
      <s:iterator value="properties">
        <tr>
          <td><s:property value="key" escapeHtml="true"/></td>
          <td><s:property value="value" escapeHtml="true"/></td>
        </tr>
      </s:iterator>
    </tbody>
  </table>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
