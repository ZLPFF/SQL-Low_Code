//
// var baseUrl = [(${baseUrl})];
//
// function onLoadSuccess(rows) {
//
//     if (!Gikam.isEmpty(rows)) {
//         this.activeRowByIndex(0);
//     } else {
//         Gikam.cleanData([
//             'tmpl-edit-base-info-form',
//             'tmpl-edit-tmpl-line-grid'
//         ]);
//
//         Gikam.getComp('tmpl-edit-uploader').grid.cleanData();
//     }
//
// }
//
//
// function onRowActive(index, row) {
//
//     Gikam.getAsyncComp('tmpl-edit-base-info-form').done(function (form) {
//         form.loadData(row);
//     });
//     [# th:each="vo:${lists}"]
//     Gikam.getAsyncComp('[(${vo.fieldName})]-edit-tmpl-line-grid').done(function (grid) {
//         grid.refresh({
//             requestData: {
//                 NOTICEID_EQ: row.id
//             }
//         });
//     });
//     [/]
//
//     Gikam.getAsyncComp('tmpl-edit-uploader').done(function (uploader) {
//         uploader.setOptions({
//             bizId: row.id
//         });
//     });
//
// }
//
//
// function getPreInsertFields() {
//     return [{
//         field: 'title',
//         title: '标题'
//     }]
// }
//
// function insert() {
//     Gikam.preInsert({
//         modalTitle: '新增',
//         fields: getPreInsertFields(),
//         url: baseUrl + '.insertData'
//     }).done(function (id) {
//         Gikam.getComp('tmpl-edit-gird').refresh();
//     });
// }
//
// function deleteRows() {
//     Gikam.getComp('tmpl-edit-gird').deleteRows(baseUrl + '.deleteData');
//
// }
//
// function submit() {
//     var gridId = 'tmpl-edit-gird';
//     var selectedRows = Gikam.getComp(gridId).getSelections();
//     if (Gikam.isEmpty(selectedRows)) {
//         Gikam.alert("请选择至少一条数据");
//         return;
//     }
//     Gikam.bp.approve(gridId, selectedRows);
// }
//
// function insertTmplLine() {
//
//     var tmplLineInsertUrl = "liuxia.liuxia1.server.NoticePermissionService.insertData";
//
//     Gikam.getComp('tmpl-edit-tmpl-line-grid').insert(tmplLineInsertUrl, [{
//         noticeId: Gikam.getComp('tmpl-edit-gird').getActivedRow().id
//     }]);
// }
//
// function deleteTmplLine() {
//     var tmplLineDeleteUrl = "liuxia.liuxia1.server.NoticePermissionService.deleteData";
//
//     Gikam.getComp('tmpl-edit-tmpl-line-grid').deleteRows(tmplLineDeleteUrl);
// }