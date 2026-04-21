lcdpApiIntegration.logPage = {

    getGridParam : function () {
        var _this = this;
        return {
            type : 'grid',
            id : 'lcdp-api-integration-edit-list-grid',
            url : lcdpApiIntegration.baseUrl + '/queries/raw',
            service : 'lcdpApiIntegrationServiceImpl',
            dbTable : 'T_LCDP_API',
            columns : lcdpApiIntegration.getEditListColumns(),
            onLoadSuccess : function (data) {
                if (!Gikam.isEmpty(data)) {
                    this.activeRowByIndex(0);
                } else {
                    Gikam.cleanCompData([ 'lcdp-api-integration-edit-list-request-grid', 'lcdp-api-integration-edit-list-response-grid' ]);
                }
            },
            onRowActive : function (index, row) {

                Gikam.getAsyncComp('lcdp-api-integration-edit-list-request-grid').done(function (grid) {
                    if(row.activatedFlag == '1'){
                        grid.showToolbar()
                    }else {
                        grid.hideToolbar()
                    }

                    grid.refresh({
                        url : Gikam.printf(lcdpApiIntegration.baseUrl + '/{id}/requests/queries', {
                            id : row.id
                        })
                    });
                });
            },
            toolbar : [ {
                type : 'button',
                text : 'LCDP.MODULE.API_INTEGRATIONS.BUTTON.NOTIFIER',
                icon : 'setting',
                onClick : function () {

                    workspace.window.load(IFM_CONTEXT + '/secure/core/module/lcdp/api-notifiers/page/edit-list');

                }
            } ]
        }
    },

    getReqGrid : function () {
        var _this = this;
        return {
            type : 'grid',
            id : 'lcdp-api-integration-edit-list-request-grid',
            generalButtonGroup:["refresh","config"],
            onCellClick : function (field, row) {
                field === 'requestBody' && _this.loadClobPage(row.requestBody, 'T_LCDP_API_REQ.REQUESTBODY');
                field === 'requestHeader' && _this.loadClobPage(row.requestHeader, 'HEADER');
            },
            onRendered : function () {
            }, requestData : {
                requestDateTime_DGOE : Gikam.DateUtils.formatter(new Date(), 'yyyy-MM-dd'),
                requestDateTime_DLOE : Gikam.DateUtils.formatter(new Date(), 'yyyy-MM-dd')
            },
            onRowActive : function (index, row) {

                Gikam.getAsyncComp('lcdp-api-integration-edit-list-response-grid').done(function (grid) {
                    grid.refresh({
                        url : Gikam.printf(lcdpApiIntegration.baseUrl + '/requests/{id}/responses/queries', {
                            id : row.id
                        })
                    });
                });
            },
            toolbar : [ {
                type : 'form',
                fields : [ {
                    field : 'requestDateTime_DGOE',
                    title: 'T_LCDP_API_REQ.REQUESTDATETIME_DGOE',
                    type : 'date',
                    value : Gikam.DateUtils.formatter(new Date(), 'yyyy-MM-dd')
                } ,{
                    field : 'requestDateTime_DLOE',
                    title: 'T_LCDP_API_REQ.REQUESTDATETIME_DLOE',
                    type : 'date',
                    value : Gikam.DateUtils.formatter(new Date(), 'yyyy-MM-dd')
                }]
            },{
                type : 'button',
                text : 'LCDP.MODULE.API_INTEGRATIONS.BUTTON.RETRY',
                icon : 'refresh',
                onClick : function () {

                    Gikam.getAsyncComp('lcdp-api-integration-edit-list-request-grid').done(function (grid) {
                        var selections = grid.getSelections();

                        if (selections.length != 1) {
                            Gikam.alert('GIKAM.TIP.CHOOSE_ONE_ITEM');
                            return;
                        }
                        Gikam.confirm('CLOUD.GIKAM.EQUIPTS.TIP.TIP', 'LCDP.API.TIP.CONFIRM_RETRY_REQ', function () {
                            Gikam.post(Gikam.printf(lcdpApiIntegration.baseUrl + '/requests/{id}/action/reset', {
                                id : selections[0].id
                            })).always(function () {
                                Gikam.getComp('lcdp-api-integration-edit-list-request-grid').refreshRowById(selections[0].id);

                                Gikam.getComp('lcdp-api-integration-edit-list-response-grid').refresh();
                            });
                        });

                    });
                }
            },{
                type : 'button',
                text : 'LCDP.MODULE.API_INTEGRATIONS.BUTTON.MODIFY',
                icon : 'refresh',
                onClick : function () {

                    Gikam.getAsyncComp('lcdp-api-integration-edit-list-request-grid').done(function (grid) {
                        var selections = grid.getSelections();
                        if (selections.length != 1) {
                            Gikam.alert('GIKAM.TIP.CHOOSE_ONE_ITEM');
                            return;
                        }
                        var modal = Gikam.create('modal', {
                            id : 'lcdp-api-integration-test-modal',
                            title : 'T_LCDP_API_REQ.SERVICE_NAME',
                            showCloseBtn : true,
                        });
                        Gikam.create('layout', {
                            renderTo : modal.window.$dom,
                            north : {
                                height : 50,
                                items : [ {
                                    type : 'btnGroup',
                                    margin : '10px 10px 10px 10px',
                                    items : [ {
                                        type : 'button',
                                        text : 'GIKAM.BUTTON.SAVE',
                                        icon : 'view-audit',
                                        onClick : function() {

                                            Gikam.confirm('CLOUD.GIKAM.EQUIPTS.TIP.TIP', 'LCDP.API.TIP.CONFIRM_SAVE_REQ', function () {
                                                Gikam.post(Gikam.printf(lcdpApiIntegration.baseUrl + '/requests/{id}/action/editreset', {
                                                    id : selections[0].id
                                                }), Gikam.getJsonWrapper({
                                                    testReqBody : Gikam.getComp('core-form-test-testReqBody').props.value,
                                                    requestHeader : Gikam.getComp('core-form-test-testHeader').props.value
                                                })).always(function () {
                                                    Gikam.getLastModal().close();
                                                    Gikam.getComp('lcdp-api-integration-edit-list-request-grid').refresh();
                                                });

                                            })
                                        }
                                    } ]
                                } ]
                            },
                            center : {
                                type : 'form',
                                id : 'lcdp-api-integration-edit-base-info-form',
                                items :[{
                                    field : 'testReqBody',
                                    id : 'core-form-test-testReqBody',
                                    title : 'T_LCDP_API.REQUEST_BODY',
                                    type : 'codeEditor',
                                    colspan : 2,
                                    height : 200,
                                    language : 'json',
                                    value : selections[0].requestBody
                                }, {
                                    field : 'testHeader',
                                    id : 'core-form-test-testHeader',
                                    title : 'T_LCDP_API.REQUEST_HEADER',
                                    type : 'codeEditor',
                                    colspan : 2,
                                    height : 200,
                                    language : 'json',
                                    value : selections[0].requestHeader
                                } ]
                            }
                        });
                    });
                }
            } ],
            onLoadSuccess : function (data) {
                if (!Gikam.isEmpty(data)) {
                    this.activeRowByIndex(0);
                } else {
                    Gikam.cleanCompData([ 'lcdp-api-integration-edit-list-response-grid' ]);
                }
            },
            columns : lcdpApiIntegration.getReqColumnList(),
        };
    },

    getResGrid : function () {
        var _this = this;
        return {
            type : 'grid',
            id : 'lcdp-api-integration-edit-list-response-grid',
            generalButtonGroup:["refresh","config"],
            onCellClick : function (field, row) {
                field === 'log' && _this.loadClobPage(row.log, 'T_LCDP_API_RES.LOG');
                field === 'responseBody' && _this.loadClobPage(row.responseBody, 'T_LCDP_API_RES.RESPONSEBODY');
            },
            onRendered : function () {

            },
            columns : lcdpApiIntegration.getResColumnList(),
        }
    },

    loadClobPage : function (data, title) {

        var modal = Gikam.create('modal', {
            title : title,
            width : 1100,
            height : 550
        });

        var form = Gikam.create('form', {
            renderTo : modal.window.$dom,
            titleWidth : 0,
            fields : [ {
                field : 'log',
                /*type : 'textarea',*/
                type : 'codeEditor',
                language: 'json',
                value : data,
                height : 500
            } ],
            columns : 1
        });

    },

    create : function () {
        var _this = this;
        Gikam.create('layout', {
            id : 'lcdp-api-integration-edit-list-layout',
            renderTo : workspace.window.$dom,
            west : {
                width : '40%',
                items : [ this.getGridParam() ]
            },
            center : {
                items : [ this.getReqGrid(), this.getResGrid() ]
            }
        });
    },

    init : function () {
        this.create();
    }
};
