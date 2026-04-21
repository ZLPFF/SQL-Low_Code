'use strict';

Gikam.bpApprove = function (gridId, data) {
    var modal = Gikam.create('modal', {
        title : 'LCDP.MODULE.BPS.MODAL.OPERATION_PAGE',
        width : 500,
        height : 400,
        showCloseBtn : true
    });
    Gikam.create('layout', {
        renderTo : modal.window.$dom,
        north : {
            height : 5,
            items : [ {
                type : 'btnToolbar',
                items : [ {
                    type : 'button',
                    text : 'GIKAM.BUTTON.CONFIRM',
                    icon : 'default',
                    onClick : function () {
                        Gikam.put(IFM_CONTEXT + '/secure/core/module/lcdp/bp-managers/action/approval',
                            Gikam.getJsonWrapper(Gikam.getComp('lcdp-biz-process-common-form').getData(), [ '', data ])).done(function (data) {
                            Gikam.getLastModal().close();
                            Gikam.getComp(gridId).refresh();
                        });
                    }
                }, {
                    type : 'button',
                    text : 'GIKAM.BUTTON.CANCEL',
                    icon : 'cancel',
                    onClick : function () {
                        Gikam.getLastModal().close();
                    }
                } ]
            } ]
        },
        center : {
            items : [ {
                type : 'tab',
                fill : true,
                panels : [ {
                    items : [ {
                        type : 'form',
                        id : 'lcdp-biz-process-common-form',
                        columns : 1,
                        fields : [ {
                            field : 'actionId',
                            title : 'T_LCDP_BIZ_PROCESS_NODE_ACTION.BPACTIONNAME',
                            type : 'select',
                            validators : [ 'notEmpty' ]
                        }, {
                            field : 'comment',
                            title : 'T_LCDP_BIZ_PROCESS_COMMENT.COMMENTS'
                        } ],
                        onRendered : function () {
                            Gikam.getComp('lcdp-biz-process-common-form').setSelectOptions('actionId', []);

                            Gikam.post(IFM_CONTEXT + '/secure/core/module/lcdp/bp-managers/bp-node-actions/queries',
                                Gikam.getJsonWrapper({
                                    menuId : workspace.activeMenuNode.id
                                }, [ '', data ])).done(function (data) {
                                var dataList = [];
                                data.forEach(function (item) {
                                    dataList.push({
                                        text : item.bpActionName,
                                        value : item.id
                                    })
                                })
                                Gikam.getComp('lcdp-biz-process-common-form').setSelectOptions('actionId', dataList);
                            });
                        }
                    } ]
                } ]
            } ]
        }
    })
};

Gikam.openProcessStatus = function (bizId, tableName, bpId) {
    Gikam.create('modal', {
        title : "LCDP.MODULE.BPS.MODAL.PROCESSVIEW",
        url : Gikam.printf(IFM_CONTEXT + '/secure/core/module/lcdp/bps/page/process-status-detail?bizId={bizId}&tableName={tableName}&bpId={bpId}', {
            bizId : bizId,
            tableName : tableName,
            bpId : bpId
        })
    });
};