<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:url value="/resource/common" var="rPath"></c:url>
<c:url value="/resource/common/template/jquerybootstrap" var="bootPath"></c:url>
<c:url value="/resource/common/template/bootstrap2.2.1.ie" var="bootIEPath"></c:url>

<link type="text/css" href="${bootPath}/css/bootstrap.min.css" rel="stylesheet" />
<link type="text/css" href="${bootPath}/css/jquery-ui-1.9.2.custom.css" rel="stylesheet" />

<!--[if lte IE 6]>
<link ref="${bootIEPath}/css/bootstrap-ie6.css" rel="stylesheet" type="text/css" />
<![endif]-->
<!--[if lte IE 7]>
<link ref="${bootIEPath}/css/ie.css" rel="stylesheet" type="text/css" />
<link type="text/css" href="${bootPath}/css/jquery.ui.1.9.2.ie.css" rel="stylesheet" />
<![endif]-->
<link href="${rPath}/css/jquery.ui.selectmenu.css" rel="Stylesheet" type="text/css"  />
<link href="${rPath}/css/pagination.css" rel="Stylesheet" type="text/css"  />
<link href="${rPath}/css/jquery.multiselect.css" rel="Stylesheet" type="text/css"  />
<link href="${rPath}/css/common.css" rel="Stylesheet" type="text/css"  />

<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
<script src="${rPath}/js/html5.js"></script>
<![endif]-->

<script type="text/javascript" src="${rPath}/js/jquery-1.8.3.min.js"></script>
<script  type="text/javascript" src="${rPath}/js/jquery.bgiframe-2.1.2.js"></script>
<script type="text/javascript" src="${bootPath}/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${rPath}/js/jquery-ui-1.9.2.custom.min.js"></script>
<script src="${rPath}/js/jquery.json-2.3.min.js" type="text/javascript"></script>
<script src="${rPath}/js/jquery.pagination.js" type="text/javascript"></script>
<script src="${rPath}/js/jquery.form.js" type="text/javascript"></script>
<script src="${rPath}/js/jquery.tmpl.js" type="text/javascript"></script>
<script src="${rPath}/js/String.js" type="text/javascript"></script>
<script src="${rPath}/js/check.js" type="text/javascript"></script>
<script src="${rPath}/js/common.js" type="text/javascript"></script>
<script src="${rPath}/js/jquery.tableSelect.js" language="javascript"></script>
<script src="${rPath}/js/jquery.ui.position.js" type="text/javascript"></script>
<script src="${rPath}/js/jquery.ui.selectmenu.js" type="text/javascript"></script>
 <script src="${rPath}/js/jquery.multiselect.min.js" type="text/javascript"></script>
<!--[if lte IE 6]>
 <script src="${bootIEPath}/js/bootstrap-ie.js" type="text/javascript"></script>
<![endif]-->