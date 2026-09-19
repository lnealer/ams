<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Order placed"/>
</jsp:include>

<div class="ams-content">
  <h1>Order placed</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p>
    Order <strong><s:property value="order.orderNumber" escapeHtml="true"/></strong> has been
    placed. A confirmation email is on its way to the ordering contact.
  </p>

  <%--
    This page is the receipt. Because there is no review step, it is the first and only time the
    user sees everything they keyed in one place, so it shows all of it rather than a summary.
  --%>

  <s:if test="shippingWindowLost">
    <div class="ams-warning" role="status">
      <p>
        The despatch window you chose was taken while the order was being placed, so the order has
        been raised without one. Operations will assign a new window.
      </p>
    </div>
  </s:if>

  <div class="ams-panel">
    <h2>Order</h2>
    <dl class="ams-detail">
      <dt>Status</dt>
      <dd><s:property value="order.orderStatusType.description" escapeHtml="true"/></dd>
      <dt>Type</dt>
      <dd><s:property value="order.orderType.description" escapeHtml="true"/></dd>
      <dt>Device nickname</dt>
      <dd><s:property value="order.deviceNickname" escapeHtml="true"/></dd>
      <dt>Despatch window</dt>
      <dd>
        <s:if test="order.shippingWindow != null">
          <s:date name="order.shippingWindow.startTime" format="EEEE d MMMM yyyy"/>,
          <s:property value="order.shippingWindow.displayLabel" escapeHtml="true"/>
        </s:if>
        <s:else>Not yet assigned</s:else>
      </dd>
      <dt>Earliest installation</dt>
      <dd><s:date name="earliestInstallationDate" format="EEEE d MMMM yyyy"/></dd>
    </dl>
  </div>

  <div class="ams-panel">
    <h2>Shipping and installation address</h2>
    <p class="ams-address">
      <s:if test="order.shippingAddress.attentionTo != null">
        ATTN: <s:property value="order.shippingAddress.attentionTo" escapeHtml="true"/><br/>
      </s:if>
      <s:property value="order.shippingAddress.addressLine1" escapeHtml="true"/><br/>
      <s:if test="order.shippingAddress.addressLine2 != null">
        <s:property value="order.shippingAddress.addressLine2" escapeHtml="true"/><br/>
      </s:if>
      <s:property value="order.shippingAddress.city" escapeHtml="true"/>,
      <s:property value="order.shippingAddress.state.code" escapeHtml="true"/>
      <s:property value="order.shippingAddress.zipCode" escapeHtml="true"/>
    </p>
    <p>
      <s:if test="order.shippingAddress.validated">
        <span class="ams-badge ams-badge--ok">Address verified</span>
      </s:if>
      <s:else>
        <span class="ams-badge ams-badge--warn">Address not verified</span>
      </s:else>
    </p>
  </div>

  <div class="ams-panel">
    <h2>Contacts</h2>
    <div class="ams-table-scroll">
      <table class="ams-table">
        <thead>
          <tr>
            <th scope="col">Role</th>
            <th scope="col">Name</th>
            <th scope="col">Email</th>
            <th scope="col">Phone</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Ordering</td>
            <td><s:property value="order.orderingContact.firstName" escapeHtml="true"/>
                <s:property value="order.orderingContact.lastName" escapeHtml="true"/></td>
            <td><s:property value="order.orderingContact.emailAddress" escapeHtml="true"/></td>
            <td><s:property value="order.orderingContact.phoneNumber" escapeHtml="true"/></td>
          </tr>
          <tr>
            <td>Shipping</td>
            <td><s:property value="order.shippingContact.firstName" escapeHtml="true"/>
                <s:property value="order.shippingContact.lastName" escapeHtml="true"/></td>
            <td><s:property value="order.shippingContact.emailAddress" escapeHtml="true"/></td>
            <td><s:property value="order.shippingContact.phoneNumber" escapeHtml="true"/></td>
          </tr>
          <tr>
            <td>Installation</td>
            <td><s:property value="order.installationContact.firstName" escapeHtml="true"/>
                <s:property value="order.installationContact.lastName" escapeHtml="true"/></td>
            <td><s:property value="order.installationContact.emailAddress" escapeHtml="true"/></td>
            <td><s:property value="order.installationContact.phoneNumber" escapeHtml="true"/></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <div class="ams-panel">
    <h2>Maintenance window</h2>
    <p><s:property value="order.maintenanceWindow.displayLabel" escapeHtml="true"/></p>
  </div>

  <div class="ams-panel">
    <h2>External configuration</h2>
    <dl class="ams-detail">
      <dt>Addressing</dt>
      <dd><s:property value="order.assetConfiguration.networkConfigurationType.description"
                      escapeHtml="true"/></dd>
      <dt>LAN type</dt>
      <dd><s:property value="order.assetConfiguration.assetConfigurationType.description"
                      escapeHtml="true"/></dd>
      <dt>WAN</dt>
      <dd>
        <s:if test="order.assetConfiguration.wanIpAddress != null">
          <s:property value="order.assetConfiguration.wanIpAddress" escapeHtml="true"/>
          / <s:property value="order.assetConfiguration.wanSubnetMask" escapeHtml="true"/>
          via <s:property value="order.assetConfiguration.defaultGateway" escapeHtml="true"/>
        </s:if>
        <s:else>Assigned by the carrier</s:else>
      </dd>
      <dt>LAN</dt>
      <dd><s:property value="order.assetConfiguration.lanIpAddress" escapeHtml="true"/>
          / <s:property value="order.assetConfiguration.lanSubnetMask" escapeHtml="true"/></dd>
      <dt>DNS</dt>
      <dd><s:property value="order.assetConfiguration.primaryDnsAddress" escapeHtml="true"/>
          <s:if test="order.assetConfiguration.secondaryDnsAddress != null">,
            <s:property value="order.assetConfiguration.secondaryDnsAddress" escapeHtml="true"/>
          </s:if></dd>
    </dl>
  </div>

  <div class="ams-panel">
    <h2>Subscriber PCs</h2>
    <div class="ams-table-scroll">
      <table class="ams-table">
        <thead>
          <tr>
            <th scope="col">Host name</th>
            <th scope="col">Type</th>
            <th scope="col">Operating system</th>
            <th scope="col">MAC address</th>
            <th scope="col">IP address</th>
            <th scope="col">Users</th>
          </tr>
        </thead>
        <tbody>
          <s:iterator value="order.subscriberPcs" var="pc">
            <tr>
              <td><s:property value="#pc.hostName" escapeHtml="true"/></td>
              <td><s:property value="#pc.subscriberPcType.description" escapeHtml="true"/></td>
              <td><s:property value="#pc.operatingSystem" escapeHtml="true"/></td>
              <td><s:property value="#pc.macAddress" escapeHtml="true"/></td>
              <td>
                <s:property value="#pc.ipAddress" escapeHtml="true"/>
                <s:if test="#pc.staticAddress">
                  <span class="ams-badge ams-badge--neutral">static</span>
                </s:if>
              </td>
              <td><s:property value="#pc.userCount"/></td>
            </tr>
          </s:iterator>
          <s:if test="order.subscriberPcs == null || order.subscriberPcs.isEmpty()">
            <tr><td colspan="6" class="ams-empty">None recorded.</td></tr>
          </s:if>
        </tbody>
      </table>
    </div>
  </div>

  <p class="ams-actions">
    <a href="${pageContext.request.contextPath}/assetManagement/InitDashboard.action">
      Return to the dashboard
    </a>
    &middot;
    <a href="${pageContext.request.contextPath}/order/InitOrder.action">Place another order</a>
  </p>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
