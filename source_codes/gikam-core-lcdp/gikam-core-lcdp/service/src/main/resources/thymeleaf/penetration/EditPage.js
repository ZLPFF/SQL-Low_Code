
var baseUrl = "[(${moduleTmpl.masterScriptPath})]";
var pageParam = CURRENT_WINDOW.getPageParam();
var detailPagePath = "[(${moduleTmpl.detailPagePath})]";
function getGridRequestData() {
    return pageParam;
}

function insert() {
    [# th:if="${moduleTmpl.preInsertFlag}"]
    //来自右侧的预新增
    Gikam.preInsert({
        modalTitle: Gikam.propI18N('LCDP.MODULE.ADD'),
        page: '[(${preInsertFormPath})]',
        formId: '[(${moduleTmpl.moduleName})]-detail-base-info-form',
        fields: ['[(${preInsert.fieldName})]'],
        url: baseUrl + '.insertData'
    }).done(function (id) {
        workspace.window.loadPage('[(${moduleTmpl.detailPagePath})]', {id : id});
    });
    [/]
    [# th:unless="${moduleTmpl.preInsertFlag}"]
    var wrapper = Gikam.getJsonWrapper(null, ["", [{}]]);
    Gikam.postText(baseUrl + '.insertData', wrapper).done(function (id) {
        workspace.window.loadPage(detailPagePath, { id: id });
    });
    [/]
}

function deleteRows() {
    Gikam.getComp('[(${moduleTmpl.moduleName})]-edit-grid').deleteRows(baseUrl + '.deleteData');

}

function loadDetailPage(field, row) {
    if (field === '[(${link.fieldName})]'){
        workspace.window.loadPage(detailPagePath, {id : row.id,sourcePage: 'edit'});
    }
[# th:if="${moduleTmpl.bpFlag==2}"]
    if (field === 'wfnodename'){
        loadWfDetailPage(field, row);
    }
[/]
}
[# th:if="${moduleTmpl.bpFlag==1}"]
function submit() {
    var grid = Gikam.getComp('[(${moduleTmpl.moduleName})]-edit-grid');

    Gikam.create('workflow').submit({
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
    Gikam.create('WorkflowDesigner', {
        renderTo: workspace.window.$dom,
        procId: row.wfprocinstid,
        readonly: true
    });
}

var pageObject = {
    baseUrl: baseUrl,
    workflow: {
        dbTable: '[(${masterTableName})]'
    }
}

function submit() {
    var grid = Gikam.getComp('[(${moduleTmpl.moduleName})]-edit-grid');

    Gikam.create('bizWorkflow').submit({
        data : grid.getSelections(),
        pageObject : pageObject
    }).done(function() {
        grid.refresh();
    });
}
[/]