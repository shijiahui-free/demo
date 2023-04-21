package kd.bos.XD;

import kd.bos.XDdemo.DateUtil;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.BadgeInfo;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.filter.FilterParameter;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.form.operate.FormOperate;
import kd.bos.list.BillList;
import kd.bos.list.IListView;
import kd.bos.list.ListFilterParameter;
import kd.bos.list.ListShowParameter;
import kd.bos.list.events.ListRowClickEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;

import java.util.*;

/**
 * 人员关系列表插件
 *
 * @author sjh
 * on 2023/3/1
 */
public class XiaoDuPersonnelListPlugin extends AbstractListPlugin {
    @Override
    public void registerListener(EventObject event) {
        super.registerListener(event);
        this.addItemClickListeners("toolbarap");
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);

        if (StringUtils.equals("wmq_start_xd", evt.getItemKey())) {
            ListSelectedRowCollection selectedRows = getSelectedRows();
            ListSelectedRow selectRow = selectedRows.get(0);
            DynamicObject applyObject = BusinessDataServiceHelper.loadSingle(selectRow.getPrimaryKeyValue(), "wmq_personnel");


            // 1.选中已审核的申请单时，按钮才可点击，否则按钮不可用；
            if (!"C".equals(applyObject.get("billstatus"))) {
                this.getView().showTipNotification("已审核单据才可开始消毒");
                evt.setCancel(true);
                return;
            }

            // 2.非“申请进入时间”当天操作提示“申请日期当天才能开始消毒”
//            Date entryDate = (Date) applyObject.get("wmq_applydate");
//            long deffDays = DateUtil.diffDate("yyyy-MM-dd", entryDate, new Date());
//            if (deffDays != 0) {
//                this.getView().showMessage("申请日期当天才能开始消毒！！");
//                evt.setCancel(true);
//                return;
//            }

            // 非“申请进入车间”的负责人操作提示“你不是申请进入车间的负责人，没有权限进行消毒！”
//            long userId = UserServiceHelper.getCurrentUserId();
//            List<Long> userIds = new ArrayList<>();
//            userIds.add(userId);
//            List<Map<String, Object>> userInfo = UserServiceHelper.getPosition(userIds);
//            if (!userInfo.isEmpty()) {
//                List<Map<String, Object>> positions = (List<Map<String, Object>>) userInfo.get(0).get("entryentity");
//                boolean isIncharge = (boolean) positions.get(0).get("isincharge");
//                if (!isIncharge) {
//                    this.getView().showMessage("你不是申请进入车间的负责人，没有权限进行消毒！");
//                    evt.setCancel(true);
//                    return;
//                }
//            }

            // 4.需要根据申请进入车间匹配到消毒方案。
            DynamicObject dynamicObject = (DynamicObject) applyObject.get("wmq_applytoworkshop");
            long cjId = (long) dynamicObject.getPkValue();
            // 查询消毒方案
            List<QFilter> searchFilterList = new ArrayList<>();
            searchFilterList.add(new QFilter("wmq_versionstatus", QCP.equals, "A")); // A: 最新版本 B:历史版本
            searchFilterList.add(new QFilter("useorg", QCP.equals, cjId));
            DynamicObject dot = BusinessDataServiceHelper.loadSingle("wmq_xiaodu_plan",
                    "wmq_versionstatus,useorg", searchFilterList.toArray(new QFilter[]{}));
            // 2.如有方案，则生成消毒记录单，分录是要进行的消毒步骤，是根据对应消毒方案的各消毒等级中的消毒步骤顺序生成的。具体取值规则请查看消毒记录单字段说明部分。
            if (dot == null) {
                this.getView().showMessage("进入车间消毒方案不存在，请联系管理员！！");
            }
        }
    }

    /**
     * @param evt 列表行点击时，触发此事件
     *            选中已审核的申请单时，按钮才可点击，否则按钮不可用；
     */
    @Override
    public void listRowClick(ListRowClickEvent evt) {
        super.listRowClick(evt);
        ListSelectedRowCollection selectedRows = getSelectedRows();
        ListSelectedRow selectRow = selectedRows.get(0);

        this.getView().setEnable("C".equals(selectRow.getBillStatus()), "wmq_start_xd");
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

        Toolbar toolbar = this.getView().getControl("toolbarap");
        IListView listView = (IListView) this.getView();
        ListSelectedRowCollection s = listView.getCurrentListAllRowCollection();
        int count = 0;
        for (ListSelectedRow x : s) {
            if ("C".equals(x.getBillStatus().toString())) {
                count = count + 1;//计算审核通过的单据数
            }
        }
        //徽标对象
        BadgeInfo info = new BadgeInfo();
        info.setColor("#ff0000");
        info.setCount(count);
        info.setShowZero(true);
        toolbar.setBadgeInfo("wmq_start_xd", info);//给关闭按钮设置徽标
    }

    //@Override   //过滤列表展示数据
//    public void setFilter(SetFilterEvent e) {
//        super.setFilter(e);
//        long currentUserId = UserServiceHelper.getCurrentUserId();
//        DynamicObject bosUser = BusinessDataServiceHelper.loadSingle(currentUserId, "bos_user");
//        DynamicObjectCollection docs = bosUser.getDynamicObjectCollection("entryentity");
//
//        QFilter qFilter1 = null;
//        QFilter qFilter2 = null;
//        for (DynamicObject doc : docs) {
//            if (doc.getBoolean("isincharge")) {
//                qFilter1 = new QFilter("wmq_applytoworkshop.id", QCP.equals, doc.getDynamicObject("dpt").getPkValue());
//            } else {
//                qFilter2 = new QFilter("wmq_user.id", QCP.equals, currentUserId);
//            }
//        }
//        e.addCustomQFilter(qFilter1);
//    }

//    @Override    //同样都是过滤列表数据方法不同而已
//    public void beforeBindData(EventObject e) {
//        super.beforeBindData(e);
//        long currentUserId = UserServiceHelper.getCurrentUserId();
//        DynamicObject bosUser = BusinessDataServiceHelper.loadSingle(currentUserId, "bos_user");
//        DynamicObjectCollection docs = bosUser.getDynamicObjectCollection("entryentity");
//        BillList billlistap = this.getControl("billlistap");
//        FilterParameter queryFilterParameter = billlistap.getQueryFilterParameter();
//        QFilter qFilter1 = null;
//        QFilter qFilter2 = null;
//        for (DynamicObject doc : docs) {
//            if (doc.getBoolean("isincharge")) {
//                qFilter1 = new QFilter("wmq_applytoworkshop.id", QCP.equals, doc.getDynamicObject("dpt").getPkValue());
//            } else {
//                qFilter2 = new QFilter("wmq_user.id", QCP.equals, currentUserId);
//            }
//        }
//        queryFilterParameter.setFilter();
//    }


}
