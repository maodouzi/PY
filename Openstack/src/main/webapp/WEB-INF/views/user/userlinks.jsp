<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="com.inforstack.openstack.utils.SecurityUtils"%>
<!-- user common link for modules -->
<link href="<%=request.getContextPath()%>/resource/normaluser/common/css/common.css" rel="Stylesheet" type="text/css"  />

<script src="<%=request.getContextPath()%>/normaluser/scripts/template" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/normaluser/scripts/bootstrap" type="text/javascript"></script>
<%
    String userName =  SecurityUtils.getUserName();
%>
<script>

function getUsername(){
    /*return "<%=userName%>";*/
    return "admin";
}
</script>