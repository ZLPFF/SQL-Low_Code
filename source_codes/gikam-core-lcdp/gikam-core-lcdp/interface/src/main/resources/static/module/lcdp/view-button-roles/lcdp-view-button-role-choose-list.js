lcdpViewButtonRole.choosePage = {

    getGridParam : function () {
        var _this = this, columns = lcdpViewButtonRole.getPageChooseListColumns();
        _this.param.single ? columns.unshift({
            radio : true
        }) : columns.unshift({
            checkbox : true
        });
        return {
            type : 'grid',
            id : 'lcdp-resource-page-choose-list-grid',
            url : lcdpViewButtonRole.lcdpBaseUrl + '/pages/queries/choosable',
            filterOpen : true,
            requestData : this.param,
            columns : columns,
            onRowActive:function (index, rowData){
                Gikam.getAsyncComp('lcdp-view-button-role-choose-list-grid').done(function(grid) {
                    grid.refresh({
                        url : lcdpViewButtonRole.baseUrl + '/btn/action/queries',
                        requestData : {
                            resourceId : rowData.id,
                            resourceName : rowData.resourceName,
                            effectVersion : rowData.effectVersion,
                        },
                    });
                });
            }


        }
    },

    getButtonGridParam : function() {
        var _this = this, columns = lcdpViewButtonRole.getChooseListColumns();
        _this.param.single ? columns.unshift({
            radio : true
        }) : columns.unshift({
            checkbox : true
        });
        return {
            type : 'grid',
            id : 'lcdp-view-button-role-choose-list-grid',
            url : lcdpViewButtonRole.baseUrl + '/btn/action/queries',
            requestData : {
                // resourceId : Gikam.getComp('lcdp-view-button-role-choose-form').getData().id,
                // resourceName : Gikam.getComp('lcdp-view-button-role-choose-form').getData().resourceName,
                // effectVersion : Gikam.getComp('lcdp-view-button-role-choose-form').getData().effectVersion,
            },
            columns : columns,
            toolbar : [ {
                type : 'button',
                text : 'GIKAM.BUTTON.CONFIRM',
                icon : 'select',
                onClick : function() {
                    Gikam.getLastModal().close(Gikam.getComp('lcdp-view-button-role-choose-list-grid').getSelections());
                }
            } ]
        }
    },

    create : function() {
        var _this = this;
        Gikam.create('layout', {
            id : 'lcdp-view-button-role-choose-list-layout',
            renderTo : Gikam.getLastModal().window.$dom,
            center : {
                items : [ this.getShuttleFrameParam() ]
            }
        });
    },

    createPage : function() {
        var _this = this;
        Gikam.create('layout', {
            west : {
                width : '50%',
                items : [ _this.getGridParam() ]
            },
            center : {
                items : [ this.getButtonGridParam() ]
            },
            renderTo : Gikam.getLastModal().window.$dom,
        });
        return this;
    },

    init : function(param) {
        this.param = param;
        this.createPage();
    }
};
