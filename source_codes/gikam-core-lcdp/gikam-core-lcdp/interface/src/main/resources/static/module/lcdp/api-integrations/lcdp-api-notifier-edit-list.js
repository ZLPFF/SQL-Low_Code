lcdpApiNotifier.editPage = {

    getGridParam : function () {
        var _this = this;
        return {
            type : 'grid',
            id : 'lcdp-api-notifier-edit-list-grid',
            url : lcdpApiNotifier.baseUrl + '/queries/raw',
            service : 'lcdpApiNotifierServiceImpl',
            dbTable : 'T_LCDP_API_NOTIFIER',
            columns : lcdpApiNotifier.getEditListColumns(),
            deleteFormatter : function (row) {
                return row.userId || row.id;
            },
            toolbar : [ {
                type : 'button',
                text : 'GIKAM.BUTTON.INSERT',
                icon : 'add',
                onClick : function () {
                    Gikam.create('modal', {
                        title : 'CORE.MODULE.MODIFICATION_USER.TITLE.USER_CHOOSE',
                        url : IFM_CONTEXT + '/secure/core/module/mdm/users/page/choose-list',
                        onAfterClose : function (rows) {
                            if (Gikam.isNotEmpty(rows)) {
                                var notifiers = rows.map(function (row) {
                                    return {
                                        userId : row.id,
                                        userName : row.userName,
                                        email : row.email,
                                        mobile : row.mobile
                                    }
                                })
                                workspace.window.showMask(true);
                                Gikam.post(lcdpApiNotifier.baseUrl, Gikam.getJsonWrapper(null, [ 'lcdpApiNotifierServiceImpl', notifiers ]))
                                    .done(function (data) {
                                        Gikam.getComp('lcdp-api-notifier-edit-list-grid').refresh()
                                    }).always(function () {
                                    workspace.window.closeMask();
                                });
                            }
                        }
                    });
                }
            }, {
                type : 'button',
                text : 'GIKAM.BUTTON.DELETE',
                icon : 'remove-row',
                onClick : function () {
                    Gikam.getComp('lcdp-api-notifier-edit-list-grid').deleteRows(lcdpApiNotifier.baseUrl);
                }
            }, {
                type : 'button',
                text : 'GIKAM.BUTTON.BACK',
                icon : 'back',
                onClick : function () {
                    workspace.window.goBack();
                }
            } ]
        }
    },

    create : function () {
        var _this = this;
        Gikam.create('layout', {
            id : 'lcdp-api-notifier-edit-list-layout',
            renderTo : workspace.window.$dom,
            center : {
                items : [ this.getGridParam() ]
            }
        });
    },

    init : function () {
        this.create();
    }
};
