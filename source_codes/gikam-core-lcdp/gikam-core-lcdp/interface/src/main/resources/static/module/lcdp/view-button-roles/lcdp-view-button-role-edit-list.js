lcdpViewButtonRole.editPage = {

    getMenuTree : function() {
        var _this = this;
        return {
            type : 'tree',
            id : 'core-menu-edit-list-tree',
            name : '菜单信息',
            url : IFM_CONTEXT + '/secure/core/module/sys/menus/edit-tree/root',
            async : false,
            baseUrl : lcdpViewButtonRole.menuUrl,
            nodeTextField : 'menuName',
            draggable : true,
            onNodeSelected : function(nodeId) {
                _this.onTreeSelected(nodeId);
                _this.activeTreeId = nodeId;
            },
            onLoadSuccess : function(data) {
                if (data.length > 0) {
                    this.selectNodeById(data[0].id);
                }
            },

        }
    },


    onTreeSelected : function(nodeId) {
        var _this = this;
        if (Gikam.isNotEmpty(nodeId)) {
            Gikam.getComp('lcdp-view-button-role-edit-list-grid') && Gikam.getComp('lcdp-view-button-role-edit-list-grid').cleanData()
            Gikam.getComp('lcdp-view-button-role-button-edit-list-grid') && Gikam.getComp('lcdp-view-button-role-button-edit-list-grid').cleanData()

            Gikam.getComp('lcdp-view-button-role-edit-list-grid').refresh({
                url : lcdpViewButtonRole.baseUrl + '/queries/raw',
                requestData : {
                    menuId_EQ : nodeId
                }
            })
        }
    },


    getGridParam : function() {
        var _this = this;
        return {
            type : 'tab',
            panels : [ {
                title : "页面信息",
                items : [ {
                    type : 'grid',
                    id : 'lcdp-view-button-role-edit-list-grid',
                    url : lcdpViewButtonRole.baseUrl + '/queries/raw',
                    service : 'lcdpViewButtonRoleServiceImpl',
                    dbTable : 'T_LCDP_VIEW_BUTTON_ROLE',
                    columns : lcdpViewButtonRole.getEditListColumns(),
                    deleteFormatter : function(row) {
                        return row.resourceName || row.id;
                    },
                    onLoadSuccess : function(data) {

                    },
                    onRowActive : function(index, row) {
                        Gikam.getAsyncComp('lcdp-view-button-role-button-edit-list-grid').done(function(grid) {

                            grid.refresh({
                                url : lcdpViewButtonRole.baseUrl + '/btn/action/queries', requestData : {
                                    resourceId : row.resourceId, effectVersion : row.effectVersion,resourceName:row.resourceName,resourceHistoryId:row.resourceHistoryId
                                }
                            });
                        });
                    },
                    toolbar : [ {
                        type : 'button',
                        text : 'GIKAM.BUTTON.INSERT',
                        icon : 'add',
                        onClick : function() {
                            var menuId = Gikam.getComp('core-menu-edit-list-tree').getSelectedNodeId();
                            Gikam.create('modal', {
                                url : IFM_CONTEXT + '/secure/core/module/lcdp/resources/page/page-choose-list'+Gikam.param({
                                    menuId : menuId,
                                    service : 'viewButtonRole'
                                }),
                                title : '低代码页面选择页面',
                                width : '80%',

                                onAfterClose : function (rows) {

                                    if (Gikam.isEmpty(rows)) {
                                        return;
                                    }

                                    var chooseData = rows.map(function(item) {
                                        return {
                                            resourceId : item.id,
                                            menuId : menuId,
                                            resourceName : item.resourceName,
                                            effectVersion: item.effectVersion,
                                            path: item.path,
                                            resourceDesc:  item.resourceDesc
                                        }
                                    });
                                    Gikam.getComp('lcdp-view-button-role-edit-list-grid').insert(lcdpViewButtonRole.baseUrl, chooseData);
                                }
                            });
                        }
                    }, {
                        type : 'button',
                        text : 'GIKAM.BUTTON.DELETE',
                        icon : 'remove-row',
                        onClick : function() {
                            Gikam.getComp('lcdp-view-button-role-edit-list-grid').deleteRows(lcdpViewButtonRole.baseUrl);
                        }
                    } ]
                } ]
            } ]
        }
    },
    getBtnGridParam : function() {
        var _this = this;
        return {
            type : 'tab',
            panels : [ {
                title : "按钮权限",
                items : [ {
                    type : 'grid',
                    id : 'lcdp-view-button-role-button-edit-list-grid',
                    url : lcdpViewButtonRole.baseUrl + '/btn/action/queries',
                    service : 'lcdpViewButtonRoleServiceImpl',
                    dbTable : 'T_LCDP_VIEW_BUTTON_ROLE',
                    columns : lcdpViewButtonRole.getChooseListColumns(),
                    onBeforeUpdate:function (row, keys){
                        var buttonDataId =  Gikam.getComp('lcdp-view-button-role-button-edit-list-grid').getActivedRow().buttonDataId;
                        row.buttonDataId = buttonDataId;
                        return row;
                    },
                    onLoadSuccess : function(data) {

                    },
                    onRowActive : function(index, row) {

                    }
                } ]
            } ]
        }
    },


    create : function() {
        var _this = this;
        Gikam.create('layout', {
            id : 'lcdp-view-button-role-edit-list-layout',
            renderTo : workspace.window.$dom,
            west : {
                width : '20%',
                items : [ this.getMenuTree() ]
            },
            center : {
                items : [ this.getGridParam() ]
            },
            east :{
                width : '45%',
                items : [ this.getBtnGridParam() ]
            }
        });
    },

    init : function() {
        this.create();
    }
};
