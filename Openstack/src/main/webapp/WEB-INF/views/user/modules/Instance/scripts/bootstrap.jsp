<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
var Server="<%=request.getContextPath()%>/user/instance";
//init pager data from boostrap controller
var pageIndex = ${requestScope.pageIndex};
var pageSize = ${requestScope.pageSize};
var pageTotal = 0;
$(function(){
	//load html template if necessary
	 registerTemplate();
	
    setup();

});

function registerTemplate() {
     $.template("removeTip2", Template_RemoveTips2);
}

function setup() {
    //window.document.title = '<spring:message code="user.navi.instance"/>';
    loadInstances(pageIndex, pageSize);
}
//server返回的数据html,应该为tbody中的内容，父层结构：<div id="mainBody"><table class="dataTable  table  table-striped table-hover"><thead></thead><tbody>json返回的内容<tbody><tfoot></tfoot></table></div>
function loadInstances(pageIndex, pageSize) {
    var tableBodyContainer=$(".dataTable").find("tbody").empty();
    $("<span class='loadingTips'>"+"<spring:message code='message.loading.data'/>"+"</span>").appendTo(tableBodyContainer);
    $.ajax({
        type: "POST",
        dataType: "html",
        cache: false,
        url: Server + "/getPagerInstanceList",  
        data: {
            pageIndex: pageIndex,
            pageSize: pageSize
        },
        success: function(data) {
             $(tableBodyContainer).html(data);
             var pageTotal = $("#pageTotal").val();
             $(".pagination").pagination(pageTotal, {
                callback: pageCallback,
                prev_text: '<spring:message code="pager.previous"/>',    
                next_text: '<spring:message code="pager.next"/>', 
                items_per_page: pageSize,
                num_display_entries: 6,
                //count from 0
                current_page: pageIndex,
                num_edge_entries: 2
            });
             checkPageVMStatus();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            $("<span class='loadingError'>"+"<spring:message code='message.loading.data.error'/>"+"</span>").appendTo($(tableBodyContainer).empty());
        }
    });
    
}

function pageCallback(index,jq){
    pageIndex = index;
	loadInstances(index, pageSize);
}
function showRemoveTips1(which){
    var row = $(which).parents(".dataRow").first();
    var vmid=$(row).find("input[isos='vmId']").val();
    var vmname=$(row).find("input[isos='vmName']").val();
    var vmVolume = $(row).find("input[isos='vmVolume']").val();
    if(isNull(vmVolume)){
        vmVolume ="";
    }
    jConfirm("<spring:message code='instance.remove.tip1'/>",function(){
        showRemoveTips2(vmid,vmVolume);
    });
}
function openVNC(which){
    var row = $(which).parents(".dataRow").first();
    var vnc=$(row).find("input[isos='vnc']").val();
    window.open(vnc);
}
function showRemoveTips2(vmid,vmVolume){
	var removeTip2 = $.tmpl("removeTip2", [{
	        id: "removeTip2"
	    }]).appendTo("#mainBody");
	
	    removeTip2 = $(removeTip2).dialog({
	        title: '<spring:message code="dialog.title.tips"/>',
	        modal: true,
	        resizable: false,
	        show: "slide",
	        autoOpen: false,
	        hide: "slide",
	        width: "400px",
	        buttons: [
	        {
	            text: '<spring:message code="yes.button"/>',
	            click: function() {
	                $(this).dialog("destroy");
	                window.console.log("remove vm id:" + vmid);
	                removeInstance(vmid,true);
	            }
	        },{
                text: '<spring:message code="no.button"/>',
                click: function() {
                    $(this).dialog("destroy");
                    removeInstance(vmid,false);
                }
            }]
	    });
	     $(removeTip2).find(".vmVolume").html(vmVolume);
	    $(removeTip2).dialog("open");
}

function removeInstance(vmid,_freeResource){
    var pd=showProcessingDialog();
    $.ajax({
        type: "POST",
        url: Server+"/imcontrol",
        cache: false,
        data: {
            executecommand: "removevm",
            vmid: vmid,
            freeResources:_freeResource
        },
        success: function(data) {
            pd.dialog("destroy");
            try{
                var msg="";
                switch(data.status) {
                    case "done": msg="<spring:message code='operation.request.submited'/>".sprintf("<spring:message code='remove.button'/>");
                    break;
                    case "error": ;
                    case "exception": msg="<spring:message code='operation.request.error'/>";break;
                }
                
                printMessage(msg);
                loadInstances(pageIndex, pageSize)
                
            }catch(e) {
                printMessage("Data Broken: ["+e+"]");
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            pd.dialog("destroy");
            printError(jqXHR, textStatus, errorThrown);
        }
    });
}

function ctlInstance(which, command, opeDesc) {
    var vmid=$(which).parents(".dataRow").first().find("input[isos='vmId']").val();
    var vmname=$(which).parents(".dataRow").first().find("input[isos='vmName']").val();
    
    if(!confirm("<spring:message code='operation.confirm'/>".sprintf(opeDesc, vmname))) return;
    var pd=showProcessingDialog();
    $.ajax({
        type: "POST",
        url: Server+"/imcontrol",
        cache: false,
        data: {
            executecommand: command,
            vmid: vmid,
            freeResources:false
        },
        success: function(data) {
            pd.dialog("destroy");
            try{
                var msg="";
                switch(data.status) {
                    case "done": msg="<spring:message code='operation.request.submited'/>".sprintf(opeDesc);
                    break;
                    case "error": ;
                    case "exception": msg="<spring:message code='operation.request.error'/>";break;
                }
                
                printMessage(msg);
                loadInstances(pageIndex, pageSize)
                
            }catch(e) {
                printMessage("Data Broken: ["+e+"]");
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            pd.dialog("destroy");
            printError(jqXHR, textStatus, errorThrown);
        }
    });
    
}
var rTask ;
function checkPageVMStatus(){
	rTask  = setInterval(refreshTaskStatus,2500);

}
function refreshTaskStatus(){
     var hasTask = false;
     $(".dataTable").find(".statusVice:visible").each(function(){
         hasTask = true;
         var row =  $(this).parents(".dataRow").first();
         var vmId = $(row).find("input[isos='vmId']").val();
         getTaskStatus(row, vmId);
     });
     if(!hasTask && !isNull(rTask)){
         clearInterval(rTask);     
     }
 }

function getTaskStatus(row,id){
	$.ajax({
        type: "POST",
        url: Server+"/getInstance",
        cache: false,
        async:false,
        data:{
        	vmId: id
        },
        dataType:"json",
        success: function(data) {
            try{
            	  if (!data || data.length == 0) {
                      
                  }else{
                       if(!isNull(data.addresses)){
                       var addresses = data.addresses;
                       window.console.log(addresses);
                       var ipText = "[";
                       for(var key in addresses){
                        ipText = ipText + key + ":" + addresses[key] + "]<br/>";
                       }
                       if(ipText == "["){
                         ipText = "";
                       }
                        $(row).find(".pipvice").html(ipText);
                       }
                                         
                	  if(!isNull(data.isProcessing) && data.isProcessing == true){
                        $(row).find(".statusVice").text(data.taskStatus);
                       }else{
                           $(row).find(".statusTitle").text(data.statusdisplay);
                           $(row).find("input[isos='vnc']").val(data.vnc);
                    	   $(row).find(".statusVice").remove();
                    	   updateButtonWidthStatus(row,data);
                       }
                  }                
            }catch(e){ clearInterval(rTask);printMessage("Data Broken ["+e+"]");};
        },
        error: function(jqXHR, textStatus, errorThrown) {
         clearInterval(rTask); 
            printError(jqXHR, textStatus, errorThrown);
        }
    });
}
function updateButtonWidthStatus(row,data){
    var status = data.status;
    var vnc = "";
    if(!isNull(data.vnc)){
        vnc = data.vnc;
    }
    if(!isNull(status)){
     $(row).find(".ope").hide();
     $(row).find(".vm"+status).show();
     if(status =="active"){
        $(row).find(".vmunpause").show();
        $(row).find(".vmresuming").show();
        $(row).find(".vmdeleted").show();
     }
     if(!isNull(vnc)){
         $(row).find(".vmvnc").show();
     }
     if(status == "deleted"){
     }
    }
   
}

function showDetails(which){
  var data={};
  var vmId=$(which).parents(".dataRow").first().find("input[isos='vmId']").val();
  data["vmId"] = vmId;
  var instanceForm=new CustomForm();
  instanceForm.show({
       title:'<spring:message code="instance.details.label"/>',
       container:$('#instanceDetails'),
       url:'<c:url value="/user/instance/showInstanceDetails"/>',
       data:data,
       callback:changeBtnStyle,
       width:420,
       buttons: [{
                   text: '<spring:message code="user.cpu.use"/>', 
                    click:function(){
                    }},
                    {
                   text: '<spring:message code="user.memory.use"/>', 
                    click:function(){
                    }},
                    {
                   text: '<spring:message code="user.network.use"/>', 
                    click:function(){
                    }},
                    {
                   text: '<spring:message code="user.io.use"/>', 
                    click:function(){
                    }},
                 {
                    text: '<spring:message code="instance.remove.button"/>', 
                    click:function(){
                     showRemoveTips1(which);
                    }},
                  {
                   text: '<spring:message code="close.button"/>', 
                    click:function(){
                     instanceForm.close();
                    }}
                 ]
   });
}

function changeBtnStyle(container){
    $(container).find(".ui-dialog-buttonpane button:contains('<s:text name='instance.remove.button'/>')").addClass("btn-warning");
}

function updateInstanceName(which){
    var container = $(which).parents("#customForm").first();
    var vmid = container.find("#vmid").val();
    var vmname = container.find("#vmname").val();
     var pd=showProcessingDialog();
    $.ajax({
        type: "POST",
        url: Server+"/updateInstanceWithName",
        cache: false,
        data: {
            vmid: vmid,
            vmname:vmname
        },
        success: function(data) {
            pd.dialog("destroy");
            try{
                printMessage(data.msg);
                if(data.status=="error"){
                    $(container).find("#vmname").val(data.data);
                }
            }catch(e) {
                printMessage("Data Broken: ["+e+"]");
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            pd.dialog("destroy");
            printError(jqXHR, textStatus, errorThrown);
        }
    });
}
function initUI(){
	 $( ".button").button();
}