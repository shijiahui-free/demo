package kd.bos.JQ.leavecancel;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.EventObject;

/**
 * 销假单列表插件
 */
public class LeaveCancelListPlugin extends AbstractListPlugin {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        addItemClickListeners("toolbar");
    }

//    @Override
//    public void setFilter(SetFilterEvent e) {
//        super.setFilter(e);
//        long userId = UserServiceHelper.getCurrentUserId();
//        DynamicObject bos_user = BusinessDataServiceHelper.loadSingle(userId, "bos_user");
//        DynamicObjectCollection docs = bos_user.getDynamicObjectCollection("entryentity");
//        for (DynamicObject doc : docs) {
//            if (!doc.getBoolean("isincharge")) {
//                e.addCustomQFilter(new QFilter("pvdo_userfield.id", QCP.equals, userId));
//            }
//        }
//    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String itemKey = evt.getItemKey();
        if ("tblcheck".equals(itemKey)) {
            ListSelectedRowCollection selectedRows = getSelectedRows();   //获取选中的数据
            if (selectedRows != null && selectedRows.size() > 0) {
                for (ListSelectedRow listSelectedRow : selectedRows) {  //遍历选中数据的单据
                    Object primaryKeyValue = listSelectedRow.getPrimaryKeyValue(); //获取单据pkid
                    //获取单据信息
                    DynamicObject loadSingle = BusinessDataServiceHelper.loadSingle(primaryKeyValue, "wmq_xiaojia_bill");
                    //获取单据体
                    DynamicObjectCollection dynamicObjectCollection = loadSingle.getDynamicObjectCollection("entryentity");
                    if (dynamicObjectCollection != null && dynamicObjectCollection.size() > 0) {
                        for (DynamicObject entrydata : dynamicObjectCollection) {
                            //1、返还对应员工的假期剩余天数
                            //销假天数
                            int xjday = entrydata.getInt("wmq_xjday");
                            //实际申请年假
                            int wmq_yearleave = loadSingle.getInt("wmq_yearleave");
                            //获取申请人
                            DynamicObject bos_userId = loadSingle.getDynamicObject("wmq_userfield");

                            //当前单据申请人的假期余额信息 只有两条数据 年假和调休假
                            DynamicObject[] holidaymsgs = BusinessDataServiceHelper.load(
                                    "wmq_leave_info_bill",
                                    "wmq_leavetype,wmq_year,wmq_quarter,wmq_userfield,wmq_days",
                                    new QFilter[]{new QFilter("wmq_userfield.number", QCP.equals, bos_userId.getString("number"))});

                            //销假天数   实际申请年假  (优先返还年假)
                            if (xjday < wmq_yearleave) {
                                loadSingle.set("wmq_yearleave", wmq_yearleave - xjday);
                                for (DynamicObject holidaymsg : holidaymsgs) {
                                    if (holidaymsg.getString("wmq_leavetype").equals("A")) {//A为年假类型
                                        holidaymsg.set("wmq_days", holidaymsg.getLong("wmq_days") + xjday);
                                    }
                                }
                            } else {
                                loadSingle.set("wmq_yearleave", 0);
                                loadSingle.set("wmq_txleave", ((int) loadSingle.get("wmq_txleave")) - (xjday - wmq_yearleave));
                                for (DynamicObject holidaymsg : holidaymsgs) {
                                    if (holidaymsg.getString("wmq_leavetype").equals("A")) {
                                        holidaymsg.set("wmq_days", holidaymsg.getLong("wmq_days") + wmq_yearleave);
                                    } else {
                                        holidaymsg.set("wmq_days", holidaymsg.getLong("wmq_days") + xjday - wmq_yearleave);
                                    }
                                }
                            }
                            SaveServiceHelper.update(holidaymsgs);

                            //2、对应的请假单状态变为已销假
                            //获取销假单选择行的请假申请单单号
                            String billNo_apply = entrydata.getString("wmq_qjdh");
                            DynamicObject pvdo_leaveapply_bill = BusinessDataServiceHelper.loadSingle("wmq_leave_apply_bill",
                                    "id,billno,billstatus",
                                    new QFilter[]{new QFilter("billno", QCP.equals, billNo_apply)});
                            pvdo_leaveapply_bill.set("billstatus", "E");
                            SaveServiceHelper.update(pvdo_leaveapply_bill);
                        }
                    }
                    SaveServiceHelper.update(loadSingle);
                }
            }
        }
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
    }
}
