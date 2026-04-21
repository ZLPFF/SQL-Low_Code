lcdpResource.pageChoosePage = {

    getGridParam : function () {
        var _this = this, columns = lcdpResource.getPageChooseListColumns();
        _this.param.single ? columns.unshift({
            radio : true
        }) : columns.unshift({
            checkbox : true
        });
        return {
            type : 'grid',
            id : 'lcdp-resource-page-choose-list-grid',
            url : lcdpResource.baseUrl + '/pages/queries/choosable',
            filterOpen : true,
            requestData : this.param,
            columns : columns,
            toolbar : [ {
                type : 'button',
                text : 'GIKAM.BUTTON.CONFIRM',
                icon : 'select',
                onClick : function () {
                    Gikam.getLastModal().close(Gikam.getComp('lcdp-resource-page-choose-list-grid').getSelections());
                }
            } ]
        }
    },

    create : function () {
        var _this = this;
        Gikam.create('layout', {
            id : 'lcdp-resource-page-choose-list-layout',
            renderTo : Gikam.getLastModal().window.$dom,
            center : {
                items : [ this.getGridParam() ]
            }
        });
    },

    init : function (param) {
        this.param = param;
        this.create();
    }
};
