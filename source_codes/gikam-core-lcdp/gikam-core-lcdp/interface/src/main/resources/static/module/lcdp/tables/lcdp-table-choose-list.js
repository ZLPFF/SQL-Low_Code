lcdpTable.choosePage = {

    getGridParam : function () {
        var _this = this, columns = lcdpTable.getChooseListColumns();
        _this.param.single ? columns.unshift({
            radio : true
        }) : columns.unshift({
            checkbox : true
        });
        return {
            type : 'grid',
            id : 'lcdp-table-choose-list-grid',
            url : lcdpTable.baseUrl + '/queries/choosable',
            requestData : _this.param,
            filterOpen : true,
            page: true,
            columns : columns,
            toolbar : [ {
                type : 'button',
                text : 'GIKAM.BUTTON.CONFIRM',
                icon : 'select',
                onClick : function () {
                    Gikam.getLastModal().close(Gikam.getComp('lcdp-table-choose-list-grid').getSelections());
                }
            } ]
        }
    },

    create : function () {
        var _this = this;
        Gikam.create('layout', {
            id : 'lcdp-table-choose-list-layout',
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
