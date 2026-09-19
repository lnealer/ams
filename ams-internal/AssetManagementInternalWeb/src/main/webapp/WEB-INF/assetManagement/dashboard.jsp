<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Dashboard"/>
</jsp:include>

<div class="ams-content">
  <h1>Dashboard</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <div class="ams-dashboard">
    <s:iterator value="layout.panels" var="panel">
      <section class="ams-panel" id="panel-<s:property value='#panel.panelId' escapeHtml='true'/>">
        <h2><s:property value="#panel.title" escapeHtml="true"/></h2>
        <div class="ams-panel-body"
             data-content-url="${pageContext.request.contextPath}<s:property value='#panel.contentUrl' escapeHtml='true'/>">
          Loading&hellip;
        </div>
      </section>
    </s:iterator>
  </div>

  <section class="ams-panel">
    <h2>Assets needing attention</h2>
    <table class="ams-table">
      <thead>
        <tr>
          <th scope="col">Asset tag</th>
          <th scope="col">Customer</th>
          <th scope="col">Reason</th>
        </tr>
      </thead>
      <tbody>
        <s:iterator value="attentionQueue">
          <tr class="ams-row-attention">
            <td><s:property value="displayTag" escapeHtml="true"/></td>
            <td><s:property value="customer.customerName" escapeHtml="true"/></td>
            <td><s:property value="attentionReason" escapeHtml="true"/></td>
          </tr>
        </s:iterator>
        <s:if test="attentionQueue.isEmpty()">
          <tr><td colspan="3" class="ams-empty">Nothing needs attention.</td></tr>
        </s:if>
      </tbody>
    </table>
  </section>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
