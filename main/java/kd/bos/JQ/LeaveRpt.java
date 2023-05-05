package kd.bos.JQ;

import kd.bos.algo.DataSet;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.servicehelper.QueryServiceHelper;

/**
 * 请假报表
 *
 * @author sjh
 * on 2023/5/5
 */
public class LeaveRpt extends AbstractReportListDataPlugin {
    private static String WMQ_LEAVE_APPLY_BILL = "wmq_leave_apply_bill";

    private static String[] FIELD_KEY_OF_FREIGHT = {"wmq_userfield", "wmq_applyday", "billstatus", "createtime"};

    private static String[] FIELDS = {"wmq_userfield", "wmq_yi", "wmq_er", "wmq_san", "wmq_si", "wmq_wu", "wmq_liu",
            "wmq_qi", "wmq_billstatus"};



    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        StringBuilder selectSettlementFields = new StringBuilder();
        selectSettlementFields.append(FIELD_KEY_OF_FREIGHT[0]).append(" AS ").append(FIELDS[0]).append(", ")
                .append(FIELD_KEY_OF_FREIGHT[1]).append(" AS ").append(FIELDS[1]).append(", ")
                .append(FIELD_KEY_OF_FREIGHT[2]).append(", ").append(FIELD_KEY_OF_FREIGHT[3]).append(" vessel");

        DataSet dispatchDataSet = QueryServiceHelper.queryDataSet(this.getClass().getName() + WMQ_LEAVE_APPLY_BILL,
                WMQ_LEAVE_APPLY_BILL, selectSettlementFields.toString(), null, null);
        return null;
    }
}
