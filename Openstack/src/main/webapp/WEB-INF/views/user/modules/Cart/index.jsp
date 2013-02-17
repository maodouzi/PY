<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page trimDirectiveWhitespaces="true"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
<!-- <script src="<%=request.getContextPath()%>/user/cart/scripts/bootstrap" language="javascript"></script>
<script src="<%=request.getContextPath()%>/user/cart/scripts/template" language="javascript"></script>
 -->
<script src="<%=request.getContextPath()%>/resource/normaluser/cart/js/bootstrap.js" language="javascript"></script>
</head>
<body>
<div class="banner">
    <label>
        <span id="banner" ></span>
    </label>
</div>

<div id="mainBody" style="margin:30px 20px 0 20px;">

		<div class="accordion" id="accordion2">
			<div class="accordion-group">
				<div class="accordion-heading">
					<a class="accordion-toggle" data-toggle="collapse"
						data-parent="#accordion2" href="#collapseOne"> <spring:message code="cart.image.title"/><span class="cartPriceLabel"><spring:message code="price.label"/>:<span class="ImgPriceValue"></span></span></a>
				</div>
				<div id="collapseOne" class="accordion-body collapse in">
					<div class="accordion-inner">
                        <ul class="imgList selectable">
                            <c:forEach items="${imgList}" var="img" varStatus="status">
                                <li>
                                <input type="hidden" value="${img.id}" name="imgId"/>
                                <c:forEach items="${img.name}" var="i18Name">
							    ${i18Name.content}
							    </c:forEach>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
				</div>
			</div>
			<div class="accordion-group">
				<div class="accordion-heading">
					<a class="accordion-toggle" data-toggle="collapse"
						data-parent="#accordion2" href="#collapseTwo"> <spring:message code="cart.flavor.title"/><span class="cartPriceLabel"><spring:message code="price.label"/>:<span class="flavorPriceValue"></span></span></a>
				</div>
				<div id="collapseTwo" class="accordion-body collapse">
					<div class="accordion-inner ">
                        <ul class="flavorList selectable">
                            <c:forEach items="${flavorList}" var="flavor" varStatus="status">
                                <li>
                                <input type="hidden" value="${flavor.id}" name="flavorId"/>
                                <span> <c:forEach items="${flavor.name}" var="i18Name">
                                ${i18Name.content}
                                </c:forEach> -- <span>
                                <c:forEach items="${flavor.details}" var="detail">
                                <span>${detail.key} : ${detail.value} / </span>
                                </c:forEach>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
				</div>
			</div>
			<div class="accordion-group">
                <div class="accordion-heading">
                    <a class="accordion-toggle" data-toggle="collapse"
                        data-parent="#accordion2" href="#collapseTwo"> <spring:message code="cart.volume.title"/></a>
                </div>
                <div id="collapseTwo" class="accordion-body collapse">
                    <div class="accordion-inner">
                        <ul class="volumeList selectable">
                        </ul>
                    </div>
                </div>
            </div>
		</div>

	</div>
    <script>setServer("<%=request.getContextPath()%>/user/cart");</script>
</body>
</html>
