function inputcheck(id, sensitive){
	var params = document.getElementById(id).value;
	var arr_sensitive = sensitive.split(',');
	for ( var i = 0; i <arr_sensitive.length; i++){
    	if(params.indexOf(arr_sensitive[i])>-1){
			document.getElementById(id).value = params.replace(arr_sensitive[i],'');
		}
	}
}
//将json串转换成树形结构
function transData(data, idStr, pidStr, chindrenStr) {
	var a = [];
	if($.isArray && $.isArray(data)){
		a = data;
	}else if(Object.prototype.toString.call(data) === "[object Array]"){
		a = data;
	}else if(typeof data === "string"){
		try{
			a = JSON.parse(data);
		}catch(e){
			a = [];
		}
	}
	var r = [], hash = {}, id = idStr, pid = pidStr, children = chindrenStr, i = 0, j = 0, len = a.length;
	
	for (; i < len; i++) {
		hash[a[i][id]] = a[i];
	}
	
	for (; j < len; j++) {
		var aVal = a[j], hashVP = hash[aVal[pid]];
		//alert(JSON.stringify(hashVP));
		if (hashVP) {
			!hashVP[children] && (hashVP[children] = []);
			hashVP[children].push(aVal);
		} else {
			r.push(aVal);
		}
		//alert(JSON.stringify(r));
	}
	return JSON.stringify(r);
}


Date.prototype.format = function(fmt) { 
     var o = { 
        "M+" : this.getMonth()+1,                 //月份 
        "d+" : this.getDate(),                    //日 
        "h+" : this.getHours(),                   //小时 
        "m+" : this.getMinutes(),                 //分 
        "s+" : this.getSeconds(),                 //秒 
        "q+" : Math.floor((this.getMonth()+3)/3), //季度 
        "S"  : this.getMilliseconds()             //毫秒 
    }; 
    if(/(y+)/.test(fmt)) {
            fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
    }
     for(var k in o) {
        if(new RegExp("("+ k +")").test(fmt)){
             fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
         }
     }
    return fmt; 
};   


(function(window, $){
    if(!$){
        return;
    }

    var pageDescriptions = {
        "学院管理": "维护学院基础信息，为班级、教师和学生数据提供组织归属。",
        "班级管理": "维护专业班级与学院关系，保障学生导入和课程绑定数据准确。",
        "教师管理": "维护教师账号、工号、联系方式和所属学院。",
        "学生管理": "维护学生账号、学号、班级和联系方式，支持批量导入。",
        "AI大模型管理": "配置融合数据分析使用的大模型供应商、接口地址和启用状态。",
        "课程管理": "维护课程信息，并管理课程绑定学生名单与导入识别结果。",
        "融合数据分析": "上传教学大纲并识别成绩组成、课程目标和考核比例，确认后写入系统。",
        "成绩管理": "按课程维护学生成绩，支持模板下载、Excel 导入和总评计算。",
        "总评成绩查询": "查看个人课程总评成绩和相关成绩信息。",
        "课程质量分析": "基于课程目标考核比例计算学生与课程目标达成度。",
        "课程质量分析图表": "通过雷达图、柱状图和散点图查看课程目标达成情况。",
        "课程质量报告": "编辑、生成和打印课程质量分析报告。",
        "调查问卷管理": "维护课程调查问卷、题目与选项分值。",
        "调查问卷成绩查看": "查看问卷提交与统计结果，支持重新发起测试。",
        "调查问卷统计": "汇总问卷反馈结果，帮助分析课程教学质量。",
        "调查问卷": "按课程提交问卷反馈，支持查看已开放问卷。",
        "公告管理": "发布、查询和查看系统公告。",
        "个人信息": "维护个人密码并查看当前账号基础资料。",
        "个人信息管理": "维护个人密码并查看当前账号基础资料。"
    };

    function cleanText(text){
        return $.trim(String(text || "").replace(/\s+/g, " "));
    }

    function ajaxErrorMessage(request, fallback){
        var defaultMessage = fallback || "网络连接错误，请联系管理员";
        if(!request){
            return defaultMessage;
        }
        var responseText = "";
        if(request.responseText){
            responseText = String(request.responseText)
                .replace(/<script[\s\S]*?<\/script>/gi, " ")
                .replace(/<style[\s\S]*?<\/style>/gi, " ")
                .replace(/<[^>]+>/g, " ");
            responseText = cleanText(responseText);
        }
        if(responseText){
            return responseText.length > 160 ? responseText.substring(0, 160) + "..." : responseText;
        }
        if(request.status === 403){
            return "无权访问该功能";
        }
        if(request.status === 401 || request.status === 302){
            return "登录已过期，请重新登录";
        }
        if(request.status >= 500){
            return "服务器处理失败，请联系管理员";
        }
        return defaultMessage;
    }

    function toastAjaxError(request, fallback){
        var message = ajaxErrorMessage(request, fallback);
        if(parent && parent.window && parent.window.toastralert){
            parent.window.toastralert("error", message);
            return;
        }
        if(window.toastralert){
            window.toastralert("error", message);
            return;
        }
        window.alert(message);
    }

    function parseBreadcrumb($breadcrumb){
        var $clone = $breadcrumb.clone();
        $clone.find("button,.btn,script,style").remove();
        $clone.find("i").remove();
        var raw = cleanText($clone.text()).replace(/首页/g, "").replace(/^系统管理\s*/g, "");
        var parts = $.map(raw.split(/[>＞]/), function(item){
            item = cleanText(item);
            return item ? item : null;
        });
        if(parts.length === 0){
            parts = [cleanText(document.title).replace(/^task$/i, "") || "业务页面"];
        }
        return {
            title: parts[parts.length - 1] || "业务页面",
            parts: parts
        };
    }

    function initContentPage(){
        var $body = $("body");
        if($body.hasClass("modern-admin-shell") || $body.hasClass("modern-content-ready")){
            return;
        }

        var $breadcrumb = $(".breadcrumb:first");
        if($breadcrumb.length === 0){
            return;
        }

        $body.addClass("modern-content-page modern-content-ready");
        var info = parseBreadcrumb($breadcrumb);
        var title = info.title || "业务页面";
        var desc = pageDescriptions[title] || "请在当前页面完成数据查询、维护和业务操作。";
        $breadcrumb.find("button.btn,a.btn").remove();

        var $header = $("<header class=\"content-page-header\"></header>");
        var $copy = $("<div class=\"content-page-copy\"></div>");
        $copy.append($("<h1 class=\"content-page-title\"></h1>").text(title));
        $copy.append($("<div class=\"content-page-desc\"></div>").text(desc));
        $header.append($copy);
        $breadcrumb.before($header).addClass("breadcrumb-modernized");

        if(/^task$/i.test(cleanText(document.title)) || /^check$/i.test(cleanText(document.title))){
            document.title = title;
        }
    }

    function actionMeta($action){
        return {
            id: String($action.attr("id") || "").toLowerCase(),
            text: cleanText($action.text()),
            title: cleanText($action.attr("title")),
            onclick: String($action.attr("onclick") || "")
        };
    }

    function isLegacyRefreshAction($action){
        var meta = actionMeta($action);
        return meta.title === "刷新" ||
            meta.text === "刷新" ||
            meta.onclick.indexOf("location.replace(location.href)") >= 0;
    }

    function isFilterAction($action){
        var meta = actionMeta($action);
        var token = [meta.id, meta.text, meta.title].join(" ");
        if(isLegacyRefreshAction($action)){
            return true;
        }
        return /query|search|reset|查询|搜索|重置/.test(token);
    }

    function isDangerAction($action){
        var meta = actionMeta($action);
        var token = [meta.id, meta.text, meta.title].join(" ");
        return /delete|remove|batch|删除|批量/.test(token);
    }

    function isPageLevelAction($action){
        if(isLegacyRefreshAction($action) || isFilterAction($action) || isDangerAction($action)){
            return false;
        }
        var meta = actionMeta($action);
        var token = [meta.id, meta.text, meta.title].join(" ");
        return /add|new|create|import|upload|download|template|save|print|preview|generate|recalculate|calculate|submit|confirm|toggle|enable|disable|default|test|新增|添加|导入|上传|下载模板|下载|保存|打印|预览|生成|计算|重新计算|提交|测试|启用|禁用|默认/.test(token);
    }

    function normalizeButtonState($button){
        var meta = actionMeta($button);
        $button.removeAttr("style");
        $button.find(".icon").addClass("fa");
        var token = [meta.id, meta.text, meta.title].join(" ");
        if(isDangerAction($button)){
            $button.removeClass("btn-primary btn-info btn-success btn-warning btn-default").addClass("btn-danger");
        }else if(/query|search|查询|搜索/.test(token) || /add|new|create|新增|添加/.test(token)){
            $button.removeClass("btn-info btn-success btn-warning btn-danger btn-default").addClass("btn-primary");
        }else if(/import|upload|导入|上传/.test(token)){
            $button.removeClass("btn-primary btn-success btn-warning btn-danger btn-default").addClass("btn-info");
        }else if(/reset|view|columns|export|download|template|重置|视图|列|导出|下载/.test(token)){
            $button.removeClass("btn-primary btn-info btn-success btn-warning btn-danger").addClass("btn-default");
        }
    }

    function setupFilterCard(){
        var $form = $("#formSearch:first");
        if($form.length === 0 || $form.data("selfEvalFilterReady")){
            return;
        }
        var $card = $form.closest(".page-toolbar,.teacher-toolbar,.student-toolbar,.report-toolbar,.survey-filter,.stats-filter,.answer-filter");
        if($card.length === 0){
            $card = $form.closest(".panel-body,div").first();
        }
        $card.addClass("list-filter-card list-action-filter-bar");

        var $left = $card.children(".list-action-filter-left:first");
        if($left.length === 0){
            $left = $("<div class=\"list-action-filter-left\"></div>");
            $card.prepend($left);
        }
        if(!$form.parent().is($left)){
            $left.append($form.detach());
        }

        var $right = $card.children(".list-action-filter-right:first");
        if($right.length === 0){
            $right = $("<div class=\"list-action-filter-right\"></div>");
            $card.append($right);
        }

        $form.addClass("list-filter-form").data("selfEvalFilterReady", true);
        $form.find(".form-group").addClass("list-filter-row");
        $form.find("input:not([type='hidden']),select,textarea").addClass("list-filter-control");
        $form.find(".help-block").addClass("list-filter-help");

        $card.find("button.btn,a.btn").each(function(){
            var $action = $(this);
            if($action.closest(".list-action-filter-right").length){
                normalizeButtonState($action);
                return;
            }
            if($action.closest(".list-action-filter-left").length && isPageLevelAction($action)){
                normalizeButtonState($action);
                $right.append($action.detach());
                return;
            }
            normalizeButtonState($action);
        });

        $right.toggle($right.children().length > 0);
    }

    function ensureActionFilterRight(){
        var $bar = $(".list-action-filter-bar:first");
        if($bar.length === 0){
            setupFilterCard();
            $bar = $(".list-action-filter-bar:first");
        }
        if($bar.length === 0){
            return $();
        }
        var $right = $bar.children(".list-action-filter-right:first");
        if($right.length === 0){
            $right = $("<div class=\"list-action-filter-right\"></div>").appendTo($bar);
        }
        return $right;
    }

    function syncTableToolbar($toolbar){
        var $left = $toolbar.children(".list-table-toolbar-left");
        var $right = $toolbar.children(".list-table-toolbar-right");
        var hasLeft = $left.children().filter(function(){
            return $.trim($(this).text()) !== "" || $(this).children().length > 0;
        }).length > 0;
        var hasRight = $right.children().filter(function(){
            return $.trim($(this).text()) !== "" || $(this).children().length > 0;
        }).length > 0;
        $left.toggle(hasLeft);
        $right.toggle(hasRight);
        $toolbar.toggle(hasLeft || hasRight);
    }

    function ensureTableToolbar($tableWrap){
        var $toolbar = $tableWrap.children(".list-table-toolbar:first");
        if($toolbar.length === 0){
            $toolbar = $(
                "<div class=\"list-table-toolbar\">" +
                    "<div class=\"list-table-toolbar-left\"></div>" +
                    "<div class=\"list-table-toolbar-right\"></div>" +
                "</div>"
            );
            $tableWrap.prepend($toolbar);
        }
        return $toolbar;
    }

    function moveBootstrapTableTools($tableWrap, $bootstrapTable){
        var $toolbar = ensureTableToolbar($tableWrap);
        var $left = $toolbar.children(".list-table-toolbar-left");
        var $right = $toolbar.children(".list-table-toolbar-right");

        var $legacyToolbar = $tableWrap.find("#toolbar:first");
        if($legacyToolbar.length && !$legacyToolbar.closest(".list-table-toolbar").length){
            $legacyToolbar.removeClass("btn-group").addClass("list-batch-actions");
            $left.append($legacyToolbar.detach());
        }

        if($bootstrapTable.length === 0){
            return;
        }
        var $fixedToolbar = $bootstrapTable.children(".fixed-table-toolbar:first");
        if($fixedToolbar.length === 0){
            return;
        }
        $fixedToolbar.find(".bs-bars").each(function(){
            var $bars = $(this);
            $bars.children().each(function(){
                var $child = $(this);
                if($child.attr("id") === "toolbar" && !$child.closest(".list-table-toolbar").length){
                    $child.removeClass("btn-group").addClass("list-batch-actions");
                    $left.append($child.detach());
                }
            });
            if($.trim($bars.text()) === "" && $bars.children().length === 0){
                $bars.remove();
            }
        });
        $fixedToolbar.find(".search").remove();

        var $filterRight = ensureActionFilterRight();
        var $newTableTools = $fixedToolbar.find(".columns,.export");
        if($filterRight.length && $newTableTools.length){
            $filterRight.children(".columns,.export").remove();
        }
        $newTableTools.each(function(){
            var $tool = $(this);
            if($filterRight.length){
                $filterRight.append($tool.detach().removeClass("pull-right"));
            }else if(!$tool.closest(".list-table-toolbar").length){
                $right.append($tool.detach().removeClass("pull-right"));
            }
        });
        $filterRight.toggle($filterRight.children().length > 0);
        $filterRight.find("button.btn,a.btn").each(function(){
            normalizeButtonState($(this));
        });
        syncTableToolbar($toolbar);
        if($fixedToolbar.children().length === 0){
            $fixedToolbar.addClass("is-normalized-toolbar");
        }
    }

    function setupTableCard(){
        var $table = $("#table:first");
        if($table.length === 0){
            return;
        }
        var $bootstrapTable = $table.closest(".bootstrap-table");
        var $tableWrap = $table.closest(".table-wrap,.teacher-table-wrap,.student-table-wrap,.report-table-wrap,.panel-body");
        if($bootstrapTable.length){
            $tableWrap = $bootstrapTable.parent();
        }
        if($tableWrap.length === 0){
            return;
        }
        $tableWrap.addClass("list-table-card");
        moveBootstrapTableTools($tableWrap, $bootstrapTable);
        $tableWrap.find(".btn").each(function(){
            normalizeButtonState($(this));
        });
        $tableWrap.find(".search").remove();
        $tableWrap.find(".columns button,.export button").attr("title", function(index, current){
            return current || cleanText($(this).text()) || "表格工具";
        });
        $tableWrap.find("table .btn-xs").addClass("btn-row-action");
    }

    function normalizeListPage(){
        if($("#table:first").length === 0){
            return;
        }
        $("body").addClass("modern-list-page");
        setupFilterCard();
        setupTableCard();
    }

    function configureBootstrapTable(){
        if(!$.fn.bootstrapTable || $.fn.bootstrapTable.__selfEvalConfigured){
            return;
        }
        var oldBootstrapTable = $.fn.bootstrapTable;
        var wrappedBootstrapTable = function(){
            if(arguments.length > 0 && typeof arguments[0] === "object"){
                delete arguments[0].height;
                arguments[0].search = false;
            }
            var result = oldBootstrapTable.apply(this, arguments);
            setTimeout(normalizeListPage, 0);
            setTimeout(normalizeListPage, 120);
            return result;
        };
        for(var key in oldBootstrapTable){
            if(Object.prototype.hasOwnProperty.call(oldBootstrapTable, key)){
                wrappedBootstrapTable[key] = oldBootstrapTable[key];
            }
        }
        $.fn.bootstrapTable = wrappedBootstrapTable;
        $.fn.bootstrapTable.__selfEvalConfigured = true;
        $.extend($.fn.bootstrapTable.defaults, {
            classes: "table table-hover modern-data-table",
            undefinedText: "-",
            iconsPrefix: "fa",
            icons: {
                paginationSwitchDown: "fa-caret-square-o-down",
                paginationSwitchUp: "fa-caret-square-o-up",
                refresh: "fa-refresh",
                toggle: "fa-toggle-on",
                columns: "fa-th-list",
                detailOpen: "fa-plus",
                detailClose: "fa-minus",
                export: "fa-download"
            },
            formatLoadingMessage: function(){
                return "<div class=\"loading-state\">正在加载数据，请稍候...</div>";
            },
            formatNoMatches: function(){
                return "<div class=\"empty-state\">暂无符合条件的数据，请调整筛选条件或新增记录。</div>";
            }
        });
    }

    function enhanceFileInputs(){
        $("input[type='file']").each(function(){
            var $input = $(this);
            if($input.data("selfEvalFileReady")){
                return;
            }
            $input.data("selfEvalFileReady", true);
            $input.attr("title", $input.attr("title") || "选择文件");
        });
    }

    function ensureConfirmDialog(){
        var $dialog = $("#selfEvalConfirmDialog");
        if($dialog.length){
            return $dialog;
        }
        $dialog = $(
            "<div class=\"modal fade selfeval-confirm\" id=\"selfEvalConfirmDialog\" tabindex=\"-1\" role=\"dialog\" aria-hidden=\"true\">" +
                "<div class=\"modal-dialog\">" +
                    "<div class=\"modal-content\">" +
                        "<div class=\"modal-header\">" +
                            "<button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"关闭\"><span>&times;</span></button>" +
                            "<h4 class=\"modal-title\"><i class=\"fa fa-exclamation-circle\"></i><span>确认操作</span></h4>" +
                        "</div>" +
                        "<div class=\"modal-body\">" +
                            "<p class=\"confirm-message\"></p>" +
                            "<p class=\"confirm-detail\"></p>" +
                        "</div>" +
                        "<div class=\"modal-footer\">" +
                            "<button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">取消</button>" +
                            "<button type=\"button\" class=\"btn btn-danger confirm-submit\">确认</button>" +
                        "</div>" +
                    "</div>" +
                "</div>" +
            "</div>"
        );
        $("body").append($dialog);
        return $dialog;
    }

    function confirmAction(options){
        options = options || {};
        var deferred = $.Deferred();
        var $dialog = ensureConfirmDialog();
        var danger = options.type !== "default";
        $dialog.find(".modal-title span").text(options.title || "确认操作");
        $dialog.find(".confirm-message").text(options.message || "确定执行该操作？");
        $dialog.find(".confirm-detail").text(options.detail || "").toggle(!!options.detail);
        $dialog.find(".confirm-submit")
            .removeClass("btn-danger btn-primary")
            .addClass(danger ? "btn-danger" : "btn-primary")
            .text(options.confirmText || "确认");
        $dialog.off("click.selfEvalConfirm")
            .on("click.selfEvalConfirm", ".confirm-submit", function(){
                $dialog.modal("hide");
                if($.isFunction(options.onConfirm)){
                    options.onConfirm();
                }
                deferred.resolve(true);
            });
        $dialog.off("hidden.bs.modal.selfEvalConfirm")
            .on("hidden.bs.modal.selfEvalConfirm", function(){
                if(deferred.state() === "pending"){
                    deferred.reject(false);
                }
            });
        $dialog.modal("show");
        return deferred.promise();
    }

    function confirmDanger(message, detail, callback){
        if(!$.isFunction(callback)){
            return;
        }
        if($.fn.modal){
            confirmAction({
                title: "删除确认",
                message: message || "确定要删除该信息？",
                detail: detail || "删除后不可恢复，请确认当前选择无误。",
                confirmText: "确认删除"
            }).done(callback);
        }else if(window.confirm(message || "确定要删除该信息？")){
            callback();
        }
    }

    function setButtonBusy(button, busy, text){
        var $button = $(button);
        if($button.length === 0){
            return;
        }
        if(busy){
            if(!$button.data("selfEvalOriginalHtml")){
                $button.data("selfEvalOriginalHtml", $button.html());
            }
            $button.prop("disabled", true).addClass("is-loading");
            $button.html("<i class=\"fa fa-spinner fa-spin\"></i> " + (text || "处理中"));
        }else{
            $button.prop("disabled", false).removeClass("is-loading");
            if($button.data("selfEvalOriginalHtml")){
                $button.html($button.data("selfEvalOriginalHtml"));
                $button.removeData("selfEvalOriginalHtml");
            }
        }
    }

    function setStatusMessage(target, type, message, actionHtml){
        var $target = $(target);
        if($target.length === 0){
            return;
        }
        var safeType = type || "info";
        var iconMap = {
            success: "fa-check-circle",
            error: "fa-times-circle",
            warning: "fa-exclamation-triangle",
            info: "fa-info-circle",
            loading: "fa-spinner fa-spin"
        };
        var html = "<div class=\"state-message state-" + safeType + "\">" +
            "<i class=\"fa " + (iconMap[safeType] || iconMap.info) + "\"></i>" +
            "<span>" + $("<div/>").text(message || "").html() + "</span>" +
            (actionHtml || "") +
            "</div>";
        $target.html(html);
    }

    function clearFieldError($field){
        $field.closest(".form-group").removeClass("has-error has-success");
        $field.next(".field-feedback").remove();
    }

    function setFieldError($field, message){
        clearFieldError($field);
        $field.closest(".form-group").addClass("has-error");
        $("<div class=\"field-feedback\"></div>").text(message || "请完善该字段").insertAfter($field);
    }

    function validateField($field){
        if($field.prop("disabled") || $field.prop("readonly") || $field.attr("type") === "hidden"){
            return true;
        }
        var required = $field.prop("required") || $field.data("required") || $field.closest(".form-group").find(".control-label,.form-label,label").text().indexOf("*") >= 0;
        var value = $.trim($field.val());
        clearFieldError($field);
        if(required && !value){
            setFieldError($field, "该字段不能为空");
            return false;
        }
        if($field.attr("type") === "email" && value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)){
            setFieldError($field, "请输入有效邮箱");
            return false;
        }
        if(value){
            $field.closest(".form-group").addClass("has-success");
        }
        return true;
    }

    function setupFieldValidation(){
        $(document)
            .off("blur.selfEvalValidate change.selfEvalValidate", ".modal input.form-control,.modal select.form-control,.modal textarea.form-control")
            .on("blur.selfEvalValidate change.selfEvalValidate", ".modal input.form-control,.modal select.form-control,.modal textarea.form-control", function(){
                validateField($(this));
            });
    }

    function enhanceModals(){
        $(".modal").each(function(){
            var $modal = $(this);
            if($modal.data("selfEvalModalReady")){
                return;
            }
            $modal.data("selfEvalModalReady", true).addClass("modern-modal");
            $modal.find(".panel-info,.panel-default").addClass("form-section");
            $modal.find(".panel-heading").addClass("form-section-title");
            $modal.on("shown.bs.modal", function(){
                $(this).find("input:visible,select:visible,textarea:visible").first().focus();
            });
        });
    }

    function enhanceImportModals(){
        $("#importFileModal").each(function(){
            var $modal = $(this);
            $modal.addClass("modern-upload-modal");
            $modal.find("form").addClass("upload-panel");
            $modal.find("input[type='file']").closest(".form-group,.import-line").addClass("upload-file-row");
            $modal.find("#msg,#studentMsg,.analysis-msg").addClass("upload-status");
            $modal.find("#scoreImportPreview,.preview-table-wrap,.import-preview-wrap").addClass("upload-preview");
        });
    }

    function enhanceActionSemantics(){
        $("[id^='btn_']").each(function(){
            var $button = $(this);
            var meta = actionMeta($button);
            var token = [meta.id, meta.text, meta.title].join(" ");
            if(/delete|删除/.test(token)){
                $button.attr("data-action-tone", "danger");
            }
            if(/import|upload|导入|上传/.test(token)){
                $button.attr("data-action-tone", "upload");
            }
        });
    }

    function refreshUI(){
        enhanceFileInputs();
        enhanceModals();
        enhanceImportModals();
        enhanceActionSemantics();
    }

    function markBusyAjax(){
        $(document).ajaxSend(function(){
            $("body").addClass("ajax-busy");
        });
        $(document).ajaxStop(function(){
            $("body").removeClass("ajax-busy");
        });
    }

    window.SelfEvalUI = {
        initContentPage: initContentPage,
        normalizeListPage: normalizeListPage,
        configureBootstrapTable: configureBootstrapTable,
        enhanceFileInputs: enhanceFileInputs,
        refresh: refreshUI,
        confirm: confirmAction,
        confirmDanger: confirmDanger,
        setButtonBusy: setButtonBusy,
        setStatusMessage: setStatusMessage,
        validateField: validateField,
        ajaxErrorMessage: ajaxErrorMessage,
        toastAjaxError: toastAjaxError
    };

    configureBootstrapTable();
    markBusyAjax();
    $(function(){
        initContentPage();
        normalizeListPage();
        setTimeout(normalizeListPage, 160);
        setupFieldValidation();
        refreshUI();
        setTimeout(refreshUI, 180);
    });
})(window, window.jQuery);

