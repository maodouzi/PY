<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
var Server="<%=request.getContextPath()%>/admin/product";
//init pager data from boostrap controller
var pageIndex = ${requestScope.pageIndex};
var pageSize = ${requestScope.pageSize};
var pageTotal = 0;
$(function(){
	//load html template if necessary
	 registerTemplate();
	
    setup();

    initUI();
});

function registerTemplate() {
   $.template("createProduct", Template_CreateProduct);
   $.template("editProduct", Template_EditProduct);
   $.template("createImgOption",  Template_ImgModelOption);
   $.template("createFlavorOption",  Template_FlavorModelOption);
   $.template("createPlanOption",  Template_PlanOption);
   $.template("editPrice",  Template_EditPrice);
   $.template("createCategoryOption",  Template_CategoryModelOption);
}

function setup() {
    window.document.title = '<spring:message code="admin.product.title"/>';
    loadProducts(pageIndex, pageSize);
}
//server返回的数据html,应该为tbody中的内容，父层结构：<div id="mainBody"><table class="dataTable table  table-striped table-hover"><thead></thead><tbody>json返回的内容<tbody><tfoot></tfoot></table></div>
function loadProducts(pageIndex, pageSize) {
    var tableBodyContainer=$(".dataTable").find("tbody").empty();
    $("<span class='loadingTips'>"+"<spring:message code='message.loading.data'/>"+"</span>").appendTo(tableBodyContainer);
    $.ajax({
        type: "POST",
        dataType: "html",
        cache: false,
        url: Server + "/listForJsp",  
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
        },
        error: function(jqXHR, textStatus, errorThrown) {
            $("<span class='loadingError'>"+"<spring:message code='message.loading.data.error'/>"+"</span>").appendTo($(tableBodyContainer).empty());
        }
    });
    
}

function pageCallback(index,jq){
    pageIndex = index;
	loadProducts(index, pageSize);
}

function showCreatProduct(){
    //remove old elements
    if($("#createProduct").length > 0){
        $("#createProduct").remove();
    }
    var createProduct = $.tmpl("createProduct", [{
        id: "createProduct"
    }]).appendTo("#mainBody");

    createProduct = $(createProduct).dialog({
        title: "<spring:message code="product.create.title"/>",
        modal: true,
        autoOpen: false,
        resizable: false,
        show: "slide",
        hide: "slide",
        width: "400px",
        buttons: [{
            text: '<spring:message code="confirm.button"/>',
            click: function() {
               var jsonData = {};
               var nameArray = [];
               $(this).find(".i18n").each(function(){
                    var data = {};
                    var languageId = $(this).find(".isos_id").val();
                    data["languageId"]=languageId;
                    var content = $(this).find(".isos_content").val();
                    data["content"] = content;
                    nameArray.push(data);
               });
               jsonData["name"] = nameArray;
                var available = $(this).find("select[isos='available']").val();
                 /* if(!jQuery.checkstr(name,"vmname")) {
                     printMessage("<spring:message code='vmname.check'/>");
                    return false;
                 } */
                 jsonData["available"] = available;
                 
                 //todo 
                 var categoryArray = [];
                 var categorySelect = $(this).find("select[id='categoriesSelect']").val();
                 if(!isNull(categorySelect) && categorySelect.length >0){
                 window.console.log("cateogry ids: "+categorySelect);
                    for(var i=0;i<categorySelect.length;i++){
                        var cData = {};
                        cData["id"] = categorySelect[i];
                        categoryArray.push(cData);
                    }
                    
                    jsonData["categories"] = categoryArray;
                 }
                 
                 //ostype
                 var osType = $(this).find("select[id='osType']").val();
                 if(osType != -1){
                    jsonData["osType"] = osType
                 }
                 var refId = $(this).find("select[id='refId']").val();
                 if(!isNull(refId)){
                    jsonData["refId"] = refId;
                 }
                  var defaultPrice = $(this).find("input[isos='defaultPrice']").val();
                 if(!isNull(defaultPrice)){
                    jsonData["defaultPrice"] = defaultPrice;
                 }
                 
                var jsonString = $.toJSON(jsonData);
                createProductItem(jsonString);
                $(this).dialog("destroy");
            }
        },
        {
            text: '<spring:message code="cancel.button"/>',
            click: function() {
                $(this).dialog("destroy");
            }
        }]
    });
    loadCategories("categoriesSelect");
    bindOSTypeSelect();
    $("#osType").selectmenu();
    $("#refId").selectmenu();
    $("#available").selectmenu();
    $(createProduct).dialog("open");
   }




function createProductItem(jsonString) {
    var pd = showProcessingDialog();
    $.ajax({
        url: Server + "/create",
        type: "POST",
        dataType:"json",
        contentType:"application/json;charset=utf-8",
        data: jsonString,
        cache: false,
        success: function(data) {
            $(pd).dialog("close");
            try {

                if(data.success){
                    printMessage('<spring:message code="product.create.success"/>');
                    loadProducts(pageIndex, pageSize);
                }
                if(data.error){
                    printMessage(data.error);
                }

            } catch(e) {
                printMessage("Data Broken: [" + e + "]");
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            pd.dialog("close");
            printError(jqXHR, textStatus, errorThrown);
            return false;
        }
    });
}

function showEditPrice(which){
    //remove old elements
    if($("#editPrice").length > 0){
        $("#editPrice").remove();
    }
    var row = $(which).parents(".dataRow").first();
    var id = $(row).find("input[isos='id']").val();
    
    var defaultPrice =  $(row).find("input[isos='defaultPrice']").val();
    var editPrice = $.tmpl("editPrice", [{
        id: "editPrice"
    }]).appendTo("#mainBody");

    editPrice = $(editPrice).dialog({
        title: '<spring:message code="product.edit.title"/>',
        modal: true,
        autoOpen: false,
        resizable: false,
        show: "slide",
        hide: "slide",
        width: "400px",
        buttons: [{
            text: '<spring:message code="confirm.button"/>',
            click: function() {
               var jsonData = {};
               var defaultPrice = $(this).find("input[isos='defaultPrice']").val();
               if(!isNull(defaultPrice)){
                  jsonData["value"] = defaultPrice;
               }
               jsonData["itemSpecificationId"] = id;
               var jsonString = $.toJSON(jsonData);
                
               updateProductPrice(jsonString);
               $(this).dialog("destroy");
            }
        },
        {
            text: '<spring:message code="cancel.button"/>',
            click: function() {
                $(this).dialog("destroy");
            }
        }]
    });
    $(editPrice).find("input[isos='defaultPrice']").val(defaultPrice);
    $(editPrice).dialog("open");
}

function showEditProduct(which){
    //remove old elements
    if($("#editProduct").length > 0){
        $("#editProduct").remove();
    }
    var row = $(which).parents(".dataRow").first();
    var id = $(row).find("input[isos='id']").val();
    
    var available =  $(row).find("input[isos='available']").val();
    var editProduct = $.tmpl("editProduct", [{
        id: "editProduct"
    }]).appendTo("#mainBody");

    editProduct = $(editProduct).dialog({
        title: '<spring:message code="product.edit.title"/>',
        modal: true,
        autoOpen: false,
        resizable: false,
        show: "slide",
        hide: "slide",
        width: "400px",
        buttons: [{
            text: '<spring:message code="confirm.button"/>',
            click: function() {
            	 var jsonData = {};
                 var nameArray = [];
                  jsonData["id"] = id;
                 $(this).find(".i18n").each(function(){
                      var data = {};
                      var languageId = $(this).find(".isos_id").val();
                      data["languageId"]=languageId;
                      var content = $(this).find(".isos_content").val();
                      data["content"] = content;
                      nameArray.push(data);
                 });
                 jsonData["name"] = nameArray;
                  var available = $(this).find("select[isos='available']").val();
                   /* if(!jQuery.checkstr(name,"vmname")) {
                       printMessage("<spring:message code='vmname.check'/>");
                      return false;
                   } */
                   jsonData["available"] = available;
                   
                   var categoryArray = [];
                   var categorySelect = $(this).find("select[id='categoriesSelect']").val();
                  if(!isNull(categorySelect) && categorySelect.length >0){
                    for(var i=0;i<categorySelect.length;i++){
                        var cData = {};
                        cData["id"] = categorySelect[i];
                        categoryArray.push(cData);
                    }
                    
                    jsonData["categories"] = categoryArray;
                 }
                    var defaultPrice = $(this).find("input[isos='defaultPrice']").val();
                   if(!isNull(defaultPrice)){
                      jsonData["defaultPrice"] = defaultPrice;
                   }
                   
                  var jsonString = $.toJSON(jsonData);
                
               updateProductItem(jsonString);
               $(this).dialog("destroy");
            }
        },
        {
            text: '<spring:message code="cancel.button"/>',
            click: function() {
                $(this).dialog("destroy");
            }
        }]
    });
    //set former data
    $(row).find(".langId").each(function(){
        var langId = $(this).attr("lang_isos_id");
        var content = $(this).val();
        $(editProduct).find(".edit_content").each(function(){
            if($(this).attr("isos_lang_id") == langId){
                $(this).val(content);
            }
        });
    });
    $(editProduct).find("select[isos='available']").val(available);
    //set price
    var defaultPrice =  $(row).find("input[isos='defaultPrice']").val();
    $(editProduct).find("input[isos='defaultPrice']").val(defaultPrice);
    //set os type
    var osType = $(row).find("input[isos='osType']").val();
    if(parseInt(osType) ==1){
        osType="<spring:message code="flavor.type"/>";
    }else if(parseInt(osType) ==2){
        osType="<spring:message code="image.type"/>";
    } if(parseInt(osType) ==6){
        osType="<spring:message code="plan.type"/>";
    }
     $(editProduct).find(".osType").text(osType);
   //set refId
    var refId =  $(row).find("input[isos='refId']").val();
    window.console.log("refId:"+ refId);
    var refName = "";
    if(!isNull(refId)){
       var url="";
       var data={};
        var osTypeId = $(row).find("input[isos='osType']").val();
    	if(parseInt(osTypeId) == 1){
    		url="<%=request.getContextPath()%>/admin/flavor/getFlavorDetails";
    		data["flavorId"] = refId;
    	}else if(parseInt(osTypeId)==2){
    		url="<%=request.getContextPath()%>/admin/image/retrieveImage";
            data["imgId"] = refId;
    	}else if(parseInt(osTypeId)==6){
            url="<%=request.getContextPath()%>/admin/plan/retrievePlan";
            data["planId"] = refId;
        }
    	if(url!=""){
    	 getCategoryItemDetailsById(url,data,editProduct,osTypeId,setRefNameCallBack)
    	}
    	
    }
    //set categoryies
    var categories_id = new Array();
    $(row).find("input[isos='categories_id']").each(function(e){
        categories_id.push($(this).val());
    });
    window.console.log("category id edit:" + categories_id);
    loadCategories("categoriesEditSelect",categories_id);
    $("#osType").selectmenu();
    $("#refId").selectmenu();
    $("#available").selectmenu();
    
    $(editProduct).dialog("open");
}



function setRefNameCallBack(editProduct,osTypeId,data){
    if(!isNull(data)){
                 var refName = "";
                if(parseInt(osTypeId) == 1){
                    refName = data.flavorName;
		        }else if(parseInt(osTypeId)==2){
		            refName = data.name;
		        }else if(parseInt(osTypeId)==6){
                    refName = data.name;
                }
                window.console.log("ref datadat:" + data);
                window.console.log("ref name:" + refName);
     }
     $(editProduct).find(".refName").text(refName);
}


function getOSTyeList(url,selectId,optionModel){
    $.ajax({
        type: "POST",
        url: url,
        cache: false,
        dataType:"json",
        success: function(data) {
            try{
               $("#" + selectId).empty();
               $("#" + selectId).append('<option value="-1" selected><spring:message code="choose.label"/></option>');
               if (!data || data.length == 0) {
                   
               }else{
                    $.tmpl(optionModel, data).appendTo("#" + selectId);
                    $("#" + selectId).selectmenu();
                    //if(optionModel == "createFlavorOption"){
                    //   bindFlavorSelect();
                    //}
               }
                
            }catch(e){printMessage("Data Broken ["+e+"]");};
        },
        error: function(jqXHR, textStatus, errorThrown) {
            printError(jqXHR, textStatus, errorThrown);
        }
    });
}


//bind type select and get update refId  
function bindOSTypeSelect(){
    $("#osType").change(function(){
        var type = $(this).val();
        if( type == 1){
             getOSTyeList("<%=request.getContextPath()%>/admin" + "/flavor/flavorList","refId","createFlavorOption");
        }else if(type == 2){
            getOSTyeList("<%=request.getContextPath()%>/admin" + "/image/imgList","refId","createImgOption");
        }else if(type == 6){
        //todo
            getOSTyeList("<%=request.getContextPath()%>/admin/plan/planList","refId","createPlanOption");
        }
    });
}
function bindFlavorSelect(){
    $("#refId").change(function(){
        getFlavorDetails($(this).val());
    });
}

function getFlavorDetails(flavorId){
   if(isNull(flavorId) || flavorId == "-1"){
       return;
   }
  var pd=showProcessingDialog();
    $.ajax({
        type: "POST",
        url: "<%=request.getContextPath()%>/admin/flavor/getFlavorDetails",
        cache: false,
        data:{
            flavorId: flavorId
        },
        dataType:"json",
        success: function(data) {
            pd.dialog("destroy");
            try{
                if (!data || data.length == 0) {
                    $(".flavorDetails").hide();
                }else{
                    $(".flavorDetails").show();
                    $(".flavorDetails").find("span[isos='flavorName']").text(data.flavorName);
                    $(".flavorDetails").find("span[isos='vcpus']").text(data.vcpus);
                    $(".flavorDetails").find("span[isos='ram']").text(data.ram);
                    $(".flavorDetails").find("span[isos='disk']").text(data.disk);
                }
            }catch(e){printMessage("Data Broken ["+e+"]");};
        },
        error: function(jqXHR, textStatus, errorThrown) {
            pd.dialog("destroy");
            printError(jqXHR, textStatus, errorThrown);
        }
    });


}

function updateProductItem(jsonString) {
 
    var pd = showProcessingDialog();
    $.ajax({
        url: Server + "/update",
        type: "POST",
        dataType:"json",
        contentType:"application/json;charset=utf-8",
        data: jsonString,
        cache: false,
        success: function(data) {
            $(pd).dialog("close");
            try {

                if(data.success){
                    printMessage('<spring:message code="update.success"/>');
                    loadProducts(pageIndex, pageSize);
                }
                if(data.error){
                    printMessage('<spring:message code="update.failed"/>');
                }

            } catch(e) {
                printMessage("Data Broken: [" + e + "]");
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            pd.dialog("close");
            printError(jqXHR, textStatus, errorThrown);
            return false;
        }
    });
}


function updateProductPrice(jsonString) {
 
    var pd = showProcessingDialog();
    $.ajax({
        url: Server + "/editPrice",
        type: "POST",
        dataType:"json",
        contentType:"application/json;charset=utf-8",
        data: jsonString,
        cache: false,
        success: function(data) {
            $(pd).dialog("close");
            try {

                if(data.success){
                    printMessage('<spring:message code="update.success"/>');
                    loadProducts(pageIndex, pageSize);
                }
                if(data.error){
                    printMessage('<spring:message code="update.failed"/>');
                }

            } catch(e) {
                printMessage("Data Broken: [" + e + "]");
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            pd.dialog("close");
            printError(jqXHR, textStatus, errorThrown);
            return false;
        }
    });
}


function showRemoveProduct(which){
    var id=$(which).parents(".dataRow").first().find("input[isos='id']").val();
    var name=$(which).parents(".dataRow").first().find("input[isos='name']").val();
    
    if(!confirm("<spring:message code='remove.confirm'/>")) return;
    
    var pd=showProcessingDialog();
    $.ajax({
        type: "POST",
        url: Server+"/remove",
        cache: false,
        data: {
            productId: id
        },
        success: function(data) {
            pd.dialog("destroy");
            try{
                var msg="";
                if(data.success){
                    msg="<spring:message code='remove.success'/>";
                }else if(data.error){
                   msg="<spring:message code='remove.failed'/>";
               } 
                
                printMessage(msg);
                loadProducts(pageIndex, pageSize);
                
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