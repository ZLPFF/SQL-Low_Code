
var baseUrl = "[(${moduleTmpl.masterScriptPath})]";
var pageParam = CURRENT_WINDOW.getPageParam();

[# th:if="${moduleTmpl.bpFlag==1}"]
function approve() {
    var grid = Gikam.getComp('[(${moduleTmpl.moduleName})]-audit-grid');

    Gikam.create('workflow').pass({
        data: grid.getSelections(),
        pageObject: pageObject
    }).done(function () {
        grid.refresh();
    });
}

function reject() {
    var grid = Gikam.getComp('[(${moduleTmpl.moduleName})]-audit-grid');

    Gikam.create('workflow').reject({
        data: grid.getSelections(),
        pageObject: pageObject
    }).done(function () {
        grid.refresh();
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
function loadWfDetailPage(field, row) {
    if (field === 'wfnodename'){
        Gikam.create('WorkflowDesigner', {
            renderTo: workspace.window.$dom,
            procId: row.wfprocinstid,
            readonly: true
        });
    }
}

var pageObject = {
    baseUrl: baseUrl,
    workflow: {
        dbTable: '[(${masterTableName})]'
    }
}

function approve() {
    var grid = Gikam.getComp('[(${moduleTmpl.moduleName})]-audit-grid');

    Gikam.create('bizWorkflow').pass({
        data : grid.getSelections(),
        pageObject : pageObject
    }).done(function() {
        grid.refresh();
    });
}

function reject() {
    var grid = Gikam.getComp('[(${moduleTmpl.moduleName})]-audit-grid');

    Gikam.create('bizWorkflow').reject({
        data : grid.getSelections(),
        pageObject : pageObject
    }).done(function() {
        grid.refresh();
    });
}
[/]
function getGridRequestData() {
    return pageParam;
}
[# th:each="vo:${moduleTmpl.childTableList}"]
function get[(${vo.functionName})]RequestData() {
    var grid =  Gikam.getComp('[(${moduleTmpl.moduleName})]-audit-grid');
    return {
        [(${vo.associatedField})]_SEQ: grid.getActivedRow().id
    }
}
[/]

