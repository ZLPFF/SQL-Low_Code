lcdpDatabaseManager.detailPage = {

    loading : false,
    executeId : null,

    breakRequest : function () {
        button.setLoading(false);
    },

    getCenterCodeEditorHeight : function () {
        var tabHeight = Gikam.getComp("lcdp-database-manager-result-tab").model.$el.clientHeight;
        return tabHeight - 70;
    },

    setLoadingState : function (state, sqlContent) {
        var executeButton = Gikam.getComp("lcdp-database-manager-detail-button-execute");
        var breakButton = Gikam.getComp("lcdp-database-manager-detail-button-break");
        var scriptForm = Gikam.getComp('lcdp-database-manager-result-sql-form');
        this.loading = state;
        scriptForm.setReadonly(state);
        if (state) {
            executeButton.setLoading(true);
            if (Gikam.isNotEmpty(sqlContent)) {
                if (!sqlContent.trim().toLowerCase().startsWith('select')) {
                    executeButton.hide();
                    breakButton.show();
                }
            }

        } else {
            executeButton.show();
            breakButton.hide();
            executeButton.setLoading(false);
        }
    },

    getBreakButtonOptions : function () {
        var _this = this;
        return {
            id : "lcdp-database-manager-detail-button-break",
            type : 'button',
            iconType : 'suspend-circle',
            color : '#ff362e',
            text : 'GIKAM.BUTTON.EXECUTE.INTERRUPT',
            onClick : function () {
                _this.breakExecution();
            }
        }
    },

    breakExecution : function () {
        var _this = this;
        Gikam.post(lcdpDatabaseManager.baseUrl + '/break-execution', Gikam.getJsonWrapper({
            executeId : _this.executeId
        })).done(function (data) {
            _this.setLoadingState(false);
            _this.executeId = null;
        })
    },

    getBtnToolbarOptions : function () {

        var _this = this;
        return {
            type : 'btnToolbar',
            items : [ {
                id : "lcdp-database-manager-detail-button-execute",
                type : 'button',
                text : 'GIKAM.BUTTON.EXECUTE',
                iconType : 'ms-start-circle',
                color : '#4e8fff',
                onClick : function () {
                    if (_this.loading) {
                        _this.breakRequest();
                        return;
                    }
                    var scriptForm = Gikam.getComp('lcdp-database-manager-result-sql-form');
                    var sqlContent = scriptForm.getData().sqlContent;

                    if (Gikam.isEmpty(sqlContent)) {
                        Gikam.alert('LCDP.MODULE.DATABASE.TIP.INPUT_SQL');
                        return;
                    }

                    _this.setLoadingState(true, sqlContent);
                    if (sqlContent) {
                        _this.executeId = Gikam.uuid();
                        Gikam.post(lcdpDatabaseManager.baseUrl + '/execute', Gikam.getJsonWrapper({
                            f : {
                                sqlContent : sqlContent,
                                executeId : _this.executeId
                            },
                            n : 1,
                            s : "50"
                        })).done(function (r) {
                            var dataArray = r;
                            var executeResult = '';
                            var panels = [];
                            var resultIndex = 1;
                            dataArray.forEach(function (item, index) {
                                var success = item.success;
                                if (success) {
                                    if (Gikam.isNotEmpty(item.sql)) {
                                        executeResult += "sql:" + item.sql + '\n';
                                        executeResult += Gikam.propI18N('LCDP.MODULE.DATABASE.TIP.EXECUTE_TIMES') + item.usedTime + '\n\n';
                                    }
                                    if (Gikam.isNotEmpty(item.rows)) {
                                        var gridColumns = [];
                                        gridColumns.push({index : true})
                                        item.columnList.forEach(function (column) {
                                            gridColumns.push({
                                                title : column.columnName,
                                                field : column.columnName
                                            })
                                        });

                                        var gridPanel = {
                                            title : Gikam.propI18N('LCDP.MODULE.DATABASE.TITLE.RESULT') + (resultIndex),
                                            items : [
                                                {
                                                    type : 'grid',
                                                    id : 'lcdp-database-manager-result-list-grid' + index,
                                                    url : lcdpDatabaseManager.baseUrl + '/execute-singleton',
                                                    requestData : {
                                                        sqlContent : item.sql,
                                                    },
                                                    columns : gridColumns,
                                                    recordTotalAsync : false,
                                                    toolbarHidden : true
                                                } ]
                                        }
                                        panels.push(gridPanel);

                                    }
                                    resultIndex++;
                                } else {
                                    if (Gikam.isNotEmpty(item.sql)) {
                                        executeResult += "sql:" + item.sql + '\n';
                                        executeResult += Gikam.propI18N('LCDP.MODULE.DATABASE.TIP.EXECUTE_TIMES') + item.usedTime + '\n';
                                        executeResult += Gikam.propI18N('LCDP.MODULE.DATABASE.TIP.ERROR_MSG') + item.result + '\n\n';
                                    }
                                }
                            });

                            var formPanel = {
                                title : 'LCDP.MODULE.DATABASE.TITLE.INFO',
                                id : 'one',
                                items : [ {
                                    type : 'form',
                                    id : 'lcdp-database-manager-result-form',
                                    columns : 1,
                                    fields : [ {
                                        field : 'executeResult',
                                        type : "codeEditor",
                                        language : 'log',
                                        value : executeResult,
                                        height : _this.getCenterCodeEditorHeight(),
                                        onRendered(editor) {
                                            const lineCount = editor.getModel().getLineCount();
                                            editor.revealLineInCenter(lineCount);
                                        }
                                    } ]
                                } ]
                            }
                            panels.unshift(formPanel);
                            Gikam.getComp('lcdp-database-manager-result-tab').refreshPanels(panels);

                            // Gikam.getComp('lcdp-database-manager-result-form').setData({
                            //     executeResult : executeResult
                            // });
                        }).always(function () {
                            _this.setLoadingState(false);
                        })
                    }

                }
            }, this.getBreakButtonOptions() ]
        }

    },

    getNorthTabOptions : function () {
        return {
            type : 'tab',
            panels : [ {
                title : 'LCDP.MODULE.DATABASE.TITLE.EDIT_SQL',
                items : [ {
                    type : 'form',
                    id : 'lcdp-database-manager-result-sql-form',
                    columns : 1,
                    fields : [ {
                        field : 'sqlContent',
                        type : 'codeEditor',
                        language : 'sql',
                        height : 235,
                        validators : [ "notEmpty" ]
                    } ]
                } ]

            } ]

        }
    },

    getCenterTabOptions : function () {
        return {
            type : 'tab',
            id : 'lcdp-database-manager-result-tab',
            panels : [ {
                title : 'LCDP.MODULE.DATABASE.TITLE.EXECUTE_RESULT',
                id : 'detail',
                items : [ {
                    type : 'form',
                    id : 'lcdp-database-manager-result-view-form',
                    columns : 1,
                    fields : [ {
                        field : 'executeResult',
                        type : "codeEditor",
                        language : 'log',
                        value : Gikam.propI18N('LCDP.MODULE.DATABASE.TIP.SHOW_RESULT'),
                        height : 100
                    } ]
                } ]
            } ]
        }
    },

    createLayout : function () {
		var _this = this;
        Gikam.create('layout', {
            renderTo : workspace.window.$dom,
            id : 'north-south-layout',
            north : {
                height : 300,
                items : [ this.getBtnToolbarOptions(), this.getNorthTabOptions() ]
            },
            center : {
                items : [ this.getCenterTabOptions() ]
            },
			onRendered: function() {
				_this.setLoadingState(false);
			}
        });
    },


    init : function (param) {
        workspace.window.removeTitle();
        this.param = param;
        this.createLayout();
    }
}