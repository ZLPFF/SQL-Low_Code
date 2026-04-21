var pageParam = CURRENT_WINDOW.getPageParam();
var baseUrl = "[(${moduleTmpl.masterScriptPath})]";
[# th:each="vo:${moduleTmpl.childTableList}"]
var base[(${vo.childUrlName})]Url = "[(${vo.scriptPath})]";
[/]

function back() {
   workspace.window.goBack();
}

function detailFormParam() {
    return {
        id: pageParam.id
    }
}

[# th:each="vo:${moduleTmpl.childTableList}"]
function get[(${vo.functionName})]RequestData() {
    return {
        [(${vo.associatedField})]_SEQ: pageParam.id
    }
}
[/]
[# th:each="vo:${moduleTmpl.childTableList}"]
function insert[(${vo.functionName})]() {

    Gikam.getComp('[(${moduleTmpl.moduleName})]-detail-[(${vo.compName})]-grid').insert(base[(${vo.childUrlName})]Url+".insertData" , [{
        [(${vo.associatedField})]: Gikam.getComp('[(${moduleTmpl.moduleName})]-detail-base-info-form').getData().id
    }]);
}

function delete[(${vo.functionName})]() {
    Gikam.getComp('[(${moduleTmpl.moduleName})]-detail-[(${vo.compName})]-grid').deleteRows(base[(${vo.childUrlName})]Url+".deleteData");
}
[/]
[# th:if="${moduleTmpl.bpFlag}"]
var pageConfigParam = {
    // 草稿状态详情页
    edit: {
        readonly: false,
        button: {
            'detail-button-approve': {
                hidden: true
            },
            'detail-button-reject': {
                hidden: true
            }
        }
    },
    // 审核中详情页
    audit: {
        readonly: true,
        button: {
            'detail-button-approve': {
                hidden: false
            },
            'detail-button-reject': {
                hidden: false
            }
        }
    },
    // 查询详情页
    search: {
        readonly: true,
        button: {
            'detail-button-submit': {
                hidden: true
            },
            'detail-button-approve': {
                hidden: true
            },
            'detail-button-reject': {
                hidden: true
            }
        }
    }
}

// 不同详情页页面配置
function onBeforePageConfigLoad() {
    var pageConfigId = 'edit'; // 默认页面配置id
    if (Gikam.isNotEmpty(pageParam) && Gikam.isNotEmpty(pageParam.sourcePage)) {
        pageConfigId = pageParam.sourcePage;
    }

    return pageConfigParam[pageConfigId];
}
[/]
[# th:if="${moduleTmpl.bpFlag==1}"]
function submit() {
    var form = Gikam.getComp('[(${moduleTmpl.moduleName})]-detail-base-info-form');
    Gikam.create('workflow').submit({
        data: [form.getData()],
        pageObject: pageObject
    }).done(function () {
        workspace.window.goBack();
    });
}

function approve() {
    var form = Gikam.getComp('[(${moduleTmpl.moduleName})]-detail-base-info-form');

    Gikam.create('workflow').pass({
        data: [form.getData()],
        pageObject: pageObject
    }).done(function () {
        workspace.window.goBack();
    });
}

function reject() {
    var form = Gikam.getComp('[(${moduleTmpl.moduleName})]-detail-base-info-form');

    Gikam.create('workflow').reject({
        data: [form.getData()],
        pageObject: pageObject
    }).done(function () {
        workspace.window.goBack();
    });
}

var pageObject = {
    baseUrl: baseUrl,
    workflow: {
        dbTable: '[(${masterTableName})]'
    }
}
[/]
[# th:if="${moduleTmpl.bpFlag==2}"]
var pageObject = {
    baseUrl: baseUrl,
    workflow: {
        dbTable: '[(${masterTableName})]'
    }
}

function submit() {
    var form = Gikam.getComp('[(${moduleTmpl.moduleName})]-detail-base-info-form');

    Gikam.create('bizWorkflow').submit({
        data: [form.getData()],
        pageObject : pageObject
    }).done(function() {
        workspace.window.goBack();
    });
}

function approve() {
    var form = Gikam.getComp('[(${moduleTmpl.moduleName})]-detail-base-info-form');

    Gikam.create('bizWorkflow').pass({
        data: [form.getData()],
        pageObject : pageObject
    }).done(function() {
        workspace.window.goBack();
    });
}

function reject() {
    var form = Gikam.getComp('[(${moduleTmpl.moduleName})]-detail-base-info-form');

    Gikam.create('bizWorkflow').reject({
        data: [form.getData()],
        pageObject : pageObject
    }).done(function() {
        workspace.window.goBack();
    });
}
[/]