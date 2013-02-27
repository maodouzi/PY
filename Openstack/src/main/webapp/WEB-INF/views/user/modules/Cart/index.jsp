<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page trimDirectiveWhitespaces="true"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
<script src="<%=request.getContextPath()%>/resource/normaluser/cart/js/bootstrap.js" language="javascript"></script>
</head>
<body>
<div class="banner">
    <label>
        <span id="banner" ></span>
    </label>
</div>

<div id="mainBody" >
    <div id="cartCheck">
        <table>
        <thead><tr><th class="cartLabel"></th><th class="cartCategory"></th></tr></thead>
        <tbody>
        <tr>
            <td><spring:message code="cart.image.title"/></td>
            <td>
                <select class="imgList selectable" isos="img">
                <option value="-1" selected><--></option>
                    <c:forEach items="${imgList}" var="img" varStatus="status">
                                <option value="${img.id}">
                                <input type="hidden" value="${img.defaultPrice}" name="defaultPrice"/>
                                <c:forEach items="${img.name}" var="i18Name">
                                ${i18Name.content}
                                </c:forEach> -- 
                                <c:forEach items="${img.details}" var="detail">
                                ${detail.key} : ${detail.value}
                                </c:forEach>
                                </option>
                            </c:forEach>
                </select>
            </td>
        </tr>
        <tr>
            <td><spring:message code="cart.flavor.title"/></td>
            <td>
                <select class="flavorList selectable" isos="flavor">
                <option value="-1" selected><--></option>
                    <c:forEach items="${flavorList}" var="flavor" varStatus="status">
                                <option value="${img.id}">
                                <input type="hidden" value="${flavor.defaultPrice}" name="defaultPrice"/>
                                <c:forEach items="${flavor.name}" var="i18Name">
                                ${i18Name.content}
                                </c:forEach> -- 
                                <c:forEach items="${flavor.details}" var="detail">
                                ${detail.key} : ${detail.value}
                                </c:forEach>
                                </option>
                            </c:forEach>
                </select>
            </td>
        </tr>
        <tr>
            <td><spring:message code="cart.plan.title"/></td>
            <td>
                <select class="planList selectable" isos="plan">
                 <option value="-1" selected><--></option>
                    <c:forEach items="${planList}" var="plan" varStatus="status">
                                <option value="${plan.id}">
                                <input type="hidden" value="${plan.defaultPrice}" name="defaultPrice"/>
                                <c:forEach items="${plan.name}" var="i18Name">
                                ${i18Name.content}
                                </c:forEach> -- 
                                <c:forEach items="${plan.details}" var="detail">
                                ${detail.key} : ${detail.value}
                                </c:forEach>
                                </option>
                            </c:forEach>
                </select>
            </td>
        </tr>
        <tr>
            <td><spring:message code="cart.volume.title"/></td>
            <td>
                <select class="volumeTypeList selectable" isos="volumeType">
                <option value="-1" selected><spring:message code="cancel.button"/></option>
                    <c:forEach items="${volumeTypeList}" var="volumeType" varStatus="status">
                                <option value="${volumeType.id}">
                                <input type="hidden" value="${volumeType.defaultPrice}" name="defaultPrice"/>
                                <c:forEach items="${volumeType.name}" var="i18Name">
                                ${i18Name.content}
                                </c:forEach> -- 
                                <c:forEach items="${volumeType.details}" var="detail">
                                ${detail.key} : ${detail.value}
                                </c:forEach>
                                </option>
                            </c:forEach>
                </select>
            </td>
        </tr>
        </tbody>
        </table>
    </div>
	
	
        <div class="cartTotalLine">
            <spring:message code="cart.total.label"/> : ￥ <span class="cartTotal">0</span>
        </div>
        <div class="cartButton">
            <a class="btn btn-large submitorder">
		      <spring:message code="cart.submit"/>
		    </a>
        </div>
	</div>
	<div class="selectPayMethods" >
	   <p class="cartSubmitted"><spring:message code="cart.submitted"/></p>
	   <div class="payMethodsContainer">
	   </div>
	</div>
    <script>setServer("<%=request.getContextPath()%>/user/cart");</script>
</body>
</html>
