package org.ppj.pp.event.core.utils;

public class HttpUtil {

    public static String getXxlAdminUrl(String env) {

        switch (env) {
            case "dev":
            case "test":
                return "http://your_test_admin_center/pp-event-control-center";
            case "uat":
                return "http://your_uat_admin_center/pp-event-control-center";
            case "prd":
                return "http://your_prd_admin_center/pp-event-control-center";
            default:
                throw new RuntimeException("getXxlAdminUrl failed. env is illegal");
        }
    }

    public static String getPPEventAdminAddress(String env) {

        switch (env) {
            case "dev":
            case "test":
                return "http://your_test_admin_center/pp-event-control-center/eventinfo";
            case "uat":
                return "http://your_uat_admin_center/pp-event-control-center/eventinfo";
            case "prd":
                return "http://your_prd_admin_center/pp-event-control-center/eventinfo";
            default:
                throw new RuntimeException("getPPEventAdminAddress failed. env is illegal");
        }
    }
}
