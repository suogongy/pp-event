<!DOCTYPE html>
<html>
<head>
    <#import "../common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <!-- DataTables -->
    <link rel="stylesheet"
          href="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">
    <title>任务调度中心</title>
    <style>
        .ellipsis {
            /*溢出隐藏*/
            overflow: hidden;
            /*css提供的文字溢出后自动隐藏并显示...*/
            text-overflow: ellipsis;
            width: 30px;
            /*设置文本不换行*/
            white-space: nowrap;
            border: solid 1px red;
        }
    </style>
</head>
<body class="hold-transition skin-blue sidebar-mini <#if cookieMap?exists && cookieMap["xxljob_adminlte_settings"]?exists && "off" == cookieMap["xxljob_adminlte_settings"].value >sidebar-collapse</#if>">
<div class="wrapper">
    <!-- header -->
    <@netCommon.commonHeader />
    <!-- left -->
    <@netCommon.commonLeft "eventinfo" />

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>PP事件</h1>
        </section>

        <!-- Main content -->
        <section class="content">

            <div class="row">
                <div class="col-xs-3">
                    <div class="input-group">
                        <span class="input-group-addon">执行器</span>
                        <select class="form-control" id="jobGroup">
                            <#list JobGroupList as group>
                                <option value="${group.id}"
                                        <#if jobGroup==group.id>selected</#if> >${group.title}</option>
                            </#list>
                        </select>
                    </div>
                </div>

                <div class="col-xs-1">
                    <button class="btn btn-block btn-primary" id="searchBtn">搜索</button>
                </div>
                <div class="col-xs-2">
                    <button class="btn btn-block btn-info " id="resetBtn">失败事件批量重置</button>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <div class="box">
                        <#--<div class="box-header hide">
                            <h3 class="box-title">调度列表</h3>
                        </div>-->
                        <div class="box-body">
                            <table id="event_list" class="table table-bordered table-striped" width="100%" style="white-space: nowrap; ">
                                <thead>
                                <tr>
                                    <th name="id">事件id</th>
                                    <th name="jobGroup">执行器</th>
                                    <th name="status">事件状态</th>
                                    <th name="retriedCount">已重试次数</th>
                                    <th name="methodInvocationContent">事件信息</th>
                                    <th>操作</th>
                                </tr>
                                </thead>
                                <tbody></tbody>
                                <tfoot></tfoot>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- footer -->
    <@netCommon.commonFooter />
</div>

<@netCommon.commonScript />
<!-- DataTables -->
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<!-- moment -->
<script src="${request.contextPath}/static/adminlte/bower_components/moment/moment.min.js"></script>
<#-- cronGen -->
<script src="${request.contextPath}/static/plugins/cronGen/cronGen.js"></script>
<script src="${request.contextPath}/static/js/eventinfo.index.1.js"></script>
</body>
</html>
