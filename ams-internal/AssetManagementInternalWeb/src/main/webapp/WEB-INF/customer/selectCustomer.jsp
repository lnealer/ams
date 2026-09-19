<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Choose a customer"/>
</jsp:include>

<div class="ams-content">
  <h1>Choose a customer</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p class="ams-hint">
    Almost every screen acts on behalf of one customer, so pick one here first. The choice is held
    for the rest of your session and shown in the bar at the top of every page.
  </p>

  <s:form action="CustomerPicker" namespace="/customer" method="get" cssClass="ams-search-form">
    <div>
      <label for="customerTerm">Filter by name or account number</label>
      <s:textfield name="customerTerm" id="customerTerm" size="32"/>
    </div>
    <s:submit value="Filter" cssClass="ams-primary"/>
  </s:form>

  <div class="ams-table-scroll">
    <table class="ams-table">
      <thead>
        <tr>
          <th scope="col">Customer</th>
          <th scope="col">Account</th>
          <th scope="col">Status</th>
          <th scope="col">Ordering</th>
          <th scope="col"></th>
        </tr>
      </thead>
      <tbody>
        <s:iterator value="customerList" var="cust">
          <tr>
            <td><strong><s:property value="#cust.customerName" escapeHtml="true"/></strong></td>
            <td><s:property value="#cust.accountNumber" escapeHtml="true"/></td>
            <td>
              <s:if test="#cust.active">
                <span class="ams-badge ams-badge--ok">Active</span>
              </s:if>
              <s:else>
                <span class="ams-badge ams-badge--neutral">Inactive</span>
              </s:else>
            </td>
            <td>
              <%-- Ordering needs the flag AND an active customer AND an active service, which is
                   why this can read "No" for a customer whose flag is on. --%>
              <s:if test="#cust.canSubmitOrders">
                <span class="ams-badge ams-badge--ok">Enabled</span>
              </s:if>
              <s:else>
                <span class="ams-badge ams-badge--warn">Disabled</span>
              </s:else>
            </td>
            <td>
              <s:form action="SelectCustomer" namespace="/assetManagement" method="post">
                <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
                <input type="hidden" name="selectedCustomerId"
                       value="<s:property value='#cust.customerId'/>"/>
                <s:submit value="Work on this customer" cssClass="ams-primary"/>
              </s:form>
            </td>
          </tr>
        </s:iterator>
        <s:if test="customerList == null || customerList.isEmpty()">
          <tr><td colspan="5" class="ams-empty">No customers matched.</td></tr>
        </s:if>
      </tbody>
    </table>
  </div>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
