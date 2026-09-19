<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Shipping and installation address"/>
</jsp:include>

<div class="ams-content">
  <h1>Shipping and installation address</h1>
  <jsp:include page="/WEB-INF/common/orderSteps.jsp">
    <jsp:param name="step" value="2"/>
  </jsp:include>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p class="ams-hint">
    Where the hardware is delivered and where it is installed. This address is checked against the
    address validation service when you continue, and it also decides which warehouse region can
    despatch the order.
  </p>

  <s:form action="SaveAddress" namespace="/order" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <fieldset>
      <legend>Address</legend>
      <div class="ams-field-grid">
        <div class="ams-field">
          <label for="attn">Attention to</label>
          <s:textfield name="shippingAddress.attentionTo" id="attn" size="30" maxlength="80"/>
        </div>
        <div class="ams-field">
          <label for="line1">Address line 1</label>
          <s:textfield name="shippingAddress.addressLine1" id="line1" size="30" maxlength="26"/>
          <s:fielderror><s:param>shippingAddress.addressLine1</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="line2">Address line 2</label>
          <s:textfield name="shippingAddress.addressLine2" id="line2" size="30" maxlength="26"/>
          <s:fielderror><s:param>shippingAddress.addressLine2</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="city">City</label>
          <s:textfield name="shippingAddress.city" id="city" size="24" maxlength="20"/>
          <s:fielderror><s:param>shippingAddress.city</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="state">State</label>
          <s:select name="stateCode" id="state" list="stateOptions"
                    listKey="code" listValue="description" headerKey="" headerValue="Choose..."/>
          <s:fielderror><s:param>shippingAddress.state</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="zip">ZIP code</label>
          <s:textfield name="shippingAddress.zipCode" id="zip" size="10" maxlength="15"/>
          <s:fielderror><s:param>shippingAddress.zipCode</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="country">Country</label>
          <s:select name="countryCode" id="country" list="countryOptions"
                    listKey="code" listValue="description"/>
        </div>
      </div>
      <p class="ams-hint">
        Address lines are limited to 26 characters and the city to 20, because that is what fits on
        the carrier's label. A longer line is silently truncated by their label printer.
      </p>
    </fieldset>

    <div class="ams-button-row">
      <s:submit value="Check address and continue" cssClass="ams-primary"/>
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
