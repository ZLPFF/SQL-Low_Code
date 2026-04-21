
var baseUrl = "[(${moduleTmpl.masterScriptPath})]";
var pageParam = CURRENT_WINDOW.getPageParam();

function getGridRequestData() {
    return pageParam;
}
function loadDetailPage(field, row) {
    if (field === '[(${link.fieldName})]') {
        workspace.window.loadPage('[(${moduleTmpl.detailPagePath})]', { id: row.id, sourcePage: 'search'});
    }

[# th:if="${moduleTmpl.bpFlag==2}"]
    if (field === 'wfnodename'){
        loadWfDetailPage(field, row);
    }
[/]
}

[# th:if="${moduleTmpl.bpFlag==2}"]
function loadWfDetailPage(field, row) {
    Gikam.create('WorkflowDesigner', {
        renderTo: workspace.window.$dom,
        procId: row.wfprocinstid,
        readonly: true
    });
}
[/]