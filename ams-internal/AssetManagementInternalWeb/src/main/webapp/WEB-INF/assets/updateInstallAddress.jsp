<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Update installation address"/>
</jsp:include>

<div class="ams-content">
  <h1>Update installation address</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p class="ams-hint">
    This corrects the address on record. It does not move the asset - use a network change request
    for that.
  </p>

  <s:form action="SubmitUpdateInstallAddress" namespace="/assets" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <s:hidden name="assetId"/>
    <s:hidden name="customerId"/>
    <label for="line1">Address line 1</label>
    <s:textfield name="address.addressLine1" id="line1" size="30" maxlength="26"/>
    <label for="line2">Address line 2</label>
    <s:textfield name="address.addressLine2" id="line2" size="30" maxlength="26"/>
    <label for="city">City</label>
    <s:textfield name="address.city" id="city" size="24" maxlength="20"/>
    <label for="zip">ZIP code</label>
    <s:textfield name="address.zipCode" id="zip" size="10"/>
    <s:submit value="Update" cssClass="ams-primary"/>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
