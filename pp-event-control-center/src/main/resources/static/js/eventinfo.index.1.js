$(function () {

    // init date tables
    var eventTable = $("#event_list").dataTable({
        "deferRender": true,
        "processing": true,
        "serverSide": true,
        "ajax": {
            url: base_url + "/eventinfo/pageList",
            type: "post",
            data: function (d) {
                var obj = {};
                obj.jobGroup = $('#jobGroup').val();
                obj.start = d.start;
                obj.length = d.length;
                return obj;
            }
        },
        "searching": false,
        "ordering": false,
        "scrollX": true,	// scroll x，close self-adaption
        "columns": [
            {
                "data": 'id',
                "bSortable": false,
                "visible": false,
                "width": '7%'
            },
            {
                "data": 'jobGroup',
                "visible": false,
            },
            {
                "data": 'status',
                "visible": true,
                "width": '7%'
            },
            {
                "data": 'retriedCount',
                "visible": true,
                "width": '7%'
            },
            {
                "data": 'methodInvocationContent',
                "visible": true,
                "width": '70%',
                "className": "ellipsis",
                "autoWidth": false,
                'createdCell': function (td, cellData, rowData, row, col) {
                    $(td).attr('title', cellData);
                },
                render: function (data, type, row) {
                    if (data.length > 150) {
                        return data.substr(0, 150) + "...(已省略)";
                    }
                    return data;
                }
            },
            {
                "data": "操作",
                "width": '10%',
                "render": function (data, type, row) {
                    return function () {
                        // html
                        tableData['key' + row.id] = row;
                        var html = '<p id="' + row.id + '" >' +
                            '<button class="btn btn-warning btn-xs event_reset" type="button">' + "重置" + '</button>  ';
                        if (row.status == '已失败') {
                            html = html + '<button class="btn btn-danger btn-xs event_remove" type="button">' + "删除" + '</button>  ';
                        }
                        html = html + '</p>';

                        return html;
                    };
                }
            }

        ],
        "language": {
            "sProcessing": I18n.dataTable_sProcessing,
            "sLengthMenu": I18n.dataTable_sLengthMenu,
            "sZeroRecords": I18n.dataTable_sZeroRecords,
            "sInfo": I18n.dataTable_sInfo,
            "sInfoEmpty": I18n.dataTable_sInfoEmpty,
            "sInfoFiltered": I18n.dataTable_sInfoFiltered,
            "sInfoPostFix": "",
            "sSearch": I18n.dataTable_sSearch,
            "sUrl": "",
            "sEmptyTable": I18n.dataTable_sEmptyTable,
            "sLoadingRecords": I18n.dataTable_sLoadingRecords,
            "sInfoThousands": ",",
            "oPaginate": {
                "sFirst": I18n.dataTable_sFirst,
                "sPrevious": I18n.dataTable_sPrevious,
                "sNext": I18n.dataTable_sNext,
                "sLast": I18n.dataTable_sLast
            },
            "oAria": {
                "sSortAscending": I18n.dataTable_sSortAscending,
                "sSortDescending": I18n.dataTable_sSortDescending
            }
        }
    });

    // table data
    var tableData = {};

    // search btn
    $('#searchBtn').on('click', function () {
        eventTable.fnDraw();
    });

    $('#resetBtn').on('click', function () {

        layer.confirm("确定批量重置失败事件？", {
            icon: 3,
            title: "系统提示",
            btn: ["确定", "取消"]
        }, function (index) {
            layer.close(index);

            $.ajax({
                type: 'PUT',
                url: base_url + "/eventinfo/event/reset/all",
                data: {
                    "jobGroup": $('#jobGroup').val()
                },
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg("批量重置失败事件处理中");
                        eventTable.fnDraw(false);
                    } else {
                        layer.msg(data.msg || "失败事件批量重置失败");
                    }
                }
            });
        });
    });

    // jobGroup change
    $('#jobGroup').on('change', function () {
        //reload
        var jobGroup = $('#jobGroup').val();
        window.location.href = base_url + "/eventinfo?jobGroup=" + jobGroup;
    });

    $("#event_list").on('click', '.event_reset', function () {

        var url = base_url + "/eventinfo/event/reset";

        var jobGroup = $('#jobGroup').val();
        var id = $(this).parent('p').attr("id");

        layer.confirm("确定事件重置?", {
            icon: 3,
            title: "系统提示",
            btn: ["确定", "取消"]
        }, function (index) {
            layer.close(index);

            $.ajax({
                type: 'PUT',
                url: url,
                data: {
                    "eventId": id,
                    "jobGroup": jobGroup
                },
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg("事件重置成功");
                        eventTable.fnDraw(false);
                    } else {
                        layer.msg(data.msg || "事件重置失败");
                    }
                }
            });
        });
    });

    $("#event_list").on('click', '.event_remove', function () {
        var url = base_url + "/eventinfo/failedevent/remove";

        var jobGroup = $('#jobGroup').val();
        var id = $(this).parent('p').attr("id");

        layer.confirm("确定删除事件?", {
            icon: 3,
            title: "系统提示",
            btn: ["确定", "取消"]
        }, function (index) {
            layer.close(index);

            $.ajax({
                type: 'PUT',
                url: url,
                data: {
                    "eventId": id,
                    "jobGroup": jobGroup
                },
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg("事件删除成功");
                        eventTable.fnDraw(false);
                    } else {
                        layer.msg(data.msg || "事件删除失败");
                    }
                }
            });
        });
    });
});
