<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Contact information"/>
</jsp:include>

<div class="ams-content">
  <h1>Contact information</h1>
  <jsp:include page="/WEB-INF/common/orderSteps.jsp">
    <jsp:param name="step" value="1"/>
  </jsp:include>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p class="ams-hint">
    Three roles, and they are often three different people: who is raising the order, who will
    sign for the box, and who the engineer rings when they arrive. Tick the boxes to reuse the
    same person.
  </p>

  <s:form action="SaveContacts" namespace="/order" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>

    <fieldset>
      <legend>Ordering contact</legend>
      <div class="ams-field-grid">
        <div class="ams-field">
          <label for="oc-first">First name</label>
          <s:textfield name="orderingContact.firstName" id="oc-first" size="24" maxlength="60"/>
          <s:fielderror><s:param>orderingContact.firstName</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="oc-last">Last name</label>
          <s:textfield name="orderingContact.lastName" id="oc-last" size="24" maxlength="60"/>
          <s:fielderror><s:param>orderingContact.lastName</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="oc-email">Email address</label>
          <s:textfield name="orderingContact.emailAddress" id="oc-email" size="32" maxlength="120"/>
          <s:fielderror><s:param>orderingContact.emailAddress</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="oc-phone">Phone number</label>
          <s:textfield name="orderingContact.phoneNumber" id="oc-phone" size="18" maxlength="30"/>
          <s:fielderror><s:param>orderingContact.phoneNumber</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="oc-ext">Extension</label>
          <s:textfield name="orderingContact.phoneExtension" id="oc-ext" size="6" maxlength="10"/>
        </div>
        <div class="ams-field">
          <label for="oc-mobile">Mobile</label>
          <s:textfield name="orderingContact.mobileNumber" id="oc-mobile" size="18" maxlength="30"/>
        </div>
      </div>
    </fieldset>

    <fieldset>
      <legend>Shipping contact</legend>
      <p class="ams-check">
        <s:checkbox name="shippingSameAsOrdering" id="ship-same"/>
        <label for="ship-same">Same as the ordering contact</label>
      </p>
      <div class="ams-field-grid">
        <div class="ams-field">
          <label for="sc-first">First name</label>
          <s:textfield name="shippingContact.firstName" id="sc-first" size="24" maxlength="60"/>
          <s:fielderror><s:param>shippingContact.firstName</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="sc-last">Last name</label>
          <s:textfield name="shippingContact.lastName" id="sc-last" size="24" maxlength="60"/>
          <s:fielderror><s:param>shippingContact.lastName</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="sc-email">Email address</label>
          <s:textfield name="shippingContact.emailAddress" id="sc-email" size="32" maxlength="120"/>
          <s:fielderror><s:param>shippingContact.emailAddress</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="sc-phone">Phone number</label>
          <s:textfield name="shippingContact.phoneNumber" id="sc-phone" size="18" maxlength="30"/>
          <s:fielderror><s:param>shippingContact.phoneNumber</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="sc-ext">Extension</label>
          <s:textfield name="shippingContact.phoneExtension" id="sc-ext" size="6" maxlength="10"/>
        </div>
        <div class="ams-field">
          <label for="sc-mobile">Mobile</label>
          <s:textfield name="shippingContact.mobileNumber" id="sc-mobile" size="18" maxlength="30"/>
        </div>
      </div>
    </fieldset>

    <fieldset>
      <legend>Installation contact</legend>
      <p class="ams-hint">
        The engineer calls this person from the car park, so a mobile number is worth having.
      </p>
      <p class="ams-check">
        <s:checkbox name="installationSameAsShipping" id="install-same"/>
        <label for="install-same">Same as the shipping contact</label>
      </p>
      <div class="ams-field-grid">
        <div class="ams-field">
          <label for="ic-first">First name</label>
          <s:textfield name="installationContact.firstName" id="ic-first" size="24" maxlength="60"/>
          <s:fielderror><s:param>installationContact.firstName</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="ic-last">Last name</label>
          <s:textfield name="installationContact.lastName" id="ic-last" size="24" maxlength="60"/>
          <s:fielderror><s:param>installationContact.lastName</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="ic-email">Email address</label>
          <s:textfield name="installationContact.emailAddress" id="ic-email" size="32" maxlength="120"/>
          <s:fielderror><s:param>installationContact.emailAddress</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="ic-phone">Phone number</label>
          <s:textfield name="installationContact.phoneNumber" id="ic-phone" size="18" maxlength="30"/>
          <s:fielderror><s:param>installationContact.phoneNumber</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="ic-ext">Extension</label>
          <s:textfield name="installationContact.phoneExtension" id="ic-ext" size="6" maxlength="10"/>
        </div>
        <div class="ams-field">
          <label for="ic-mobile">Mobile</label>
          <s:textfield name="installationContact.mobileNumber" id="ic-mobile" size="18" maxlength="30"/>
        </div>
      </div>
    </fieldset>

    <div class="ams-button-row">
      <s:submit value="Continue to address" cssClass="ams-primary"/>
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

  <s:if test="existingContacts != null && !existingContacts.isEmpty()">
    <details class="ams-panel">
      <summary>Contacts already on file for this customer</summary>
      <div class="ams-table-scroll">
        <table class="ams-table">
          <thead>
            <tr>
              <th scope="col">Name</th>
              <th scope="col">Role</th>
              <th scope="col">Email</th>
              <th scope="col">Phone</th>
            </tr>
          </thead>
          <tbody>
            <s:iterator value="existingContacts" var="known">
              <tr>
                <td><s:property value="#known.firstName" escapeHtml="true"/>
                    <s:property value="#known.lastName" escapeHtml="true"/></td>
                <td><s:property value="#known.contactType.description" escapeHtml="true"/></td>
                <td><s:property value="#known.emailAddress" escapeHtml="true"/></td>
                <td><s:property value="#known.phoneNumber" escapeHtml="true"/></td>
              </tr>
            </s:iterator>
          </tbody>
        </table>
      </div>
      <p class="ams-hint">
        Shown for reference. Copy the details across by hand - reusing a stored contact would tie
        this order to a record someone may later edit or deactivate.
      </p>
    </details>
  </s:if>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
