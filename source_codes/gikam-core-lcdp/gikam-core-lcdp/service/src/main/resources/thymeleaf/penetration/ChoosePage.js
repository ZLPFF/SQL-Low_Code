
var baseUrl = "[(${moduleTmpl.masterScriptPath})]";
var pageParam = CURRENT_WINDOW.getPageParam();


function [(${moduleTmpl.moduleName})]_choose_list_grid_selection_getselectiontype() {
    return pageParam.single ? 'radio' : 'checkbox';
}

function confirm() {
    var selections = Gikam.getComp('[(${moduleTmpl.moduleName})]-choose-list-grid').getSelections();
    Gikam.getLastModal().close(selections);
}

function cancel() {
    Gikam.getLastModal().close();
}

function getGridRequestData() {
    return pageParam;
}

