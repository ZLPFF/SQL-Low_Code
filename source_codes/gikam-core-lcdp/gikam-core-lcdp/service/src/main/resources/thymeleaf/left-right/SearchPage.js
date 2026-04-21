
var baseUrl = "[(${moduleTmpl.masterScriptPath})]";
var pageParam = CURRENT_WINDOW.getPageParam();

function getGridRequestData() {
    return pageParam;
}
[# th:each="vo:${moduleTmpl.childTableList}"]
function get[(${vo.functionName})]RequestData() {
    var grid =  Gikam.getComp('[(${moduleTmpl.moduleName})]-search-grid');
    return {
        [(${vo.associatedField})]_SEQ: grid.getActivedRow().id
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
[/]

