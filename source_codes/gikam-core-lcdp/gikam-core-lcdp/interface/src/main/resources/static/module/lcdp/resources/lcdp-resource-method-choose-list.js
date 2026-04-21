lcdpResource.methodChoosePage = {

    getGridParam : function () {
        var _this = this, columns = lcdpResource.getMethodChooseListColumns();
        _this.param.single ? columns.unshift({
            radio : true
        }) : columns.unshift({
            checkbox : true
        });
        return {
            type : 'grid',
            id : 'lcdp-resource-method-choose-list-grid',
            url : lcdpResource.baseUrl + '/server-script-methods/api-method/queries',
            filterOpen : true,
            columns : columns,
            requestData : _this.param,
            toolbar : [ {
                type : 'button',
                text : 'GIKAM.BUTTON.CONFIRM',
                icon : 'select',
                onClick : function () {
                    Gikam.getLastModal().close(Gikam.getComp('lcdp-resource-method-choose-list-grid').getSelections());
                }
            } ]
        }
    },

    create : function () {
        var _this = this;
        Gikam.create('layout', {
            id : 'lcdp-resource-method-choose-list-layout',
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
