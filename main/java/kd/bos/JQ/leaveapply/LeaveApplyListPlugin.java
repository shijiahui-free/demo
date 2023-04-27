package kd.bos.JQ.leaveapply;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.events.ListRowClickEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;

import java.util.EventObject;
import java.util.List;

/**
 * 假期申请单列表插件
 */
public class LeaveApplyListPlugin extends AbstractListPlugin {

    @Override
    public void afterBindData(EventObject e) {
        super.beforeBindData(e);
    }

    @Override
    public void listRowClick(ListRowClickEvent evt) {
        super.listRowClick(evt);
        /*IListView view = (IListView) this.getView();
        ListSelectedRow selectedRow = view.getCurrentSelectedRowInfo();
        if (!selectedRow.getBillStatus().equals("C")){
            this.getView().setEnable(false,"pvdo_baritemap");
            this.getView().showErrorNotification("只有状态为已审核的单据才能进行销假");
        }else{
            this.getView().setEnable(true,"pvdo_baritemap");
        }*/
    }

    @Override
    public void setFilter(SetFilterEvent e) {
        super.setFilter(e);

        long userId = UserServiceHelper.getCurrentUserId();
        DynamicObject bos_user = BusinessDataServiceHelper.loadSingle(userId, "bos_user");
        DynamicObjectCollection docs = bos_user.getDynamicObjectCollection("entryentity");
        QFilter qFilter1 = null;
        QFilter qFilter2 = null;
        for (DynamicObject doc : docs) {
            if (!doc.getBoolean("isincharge")) {
                //qFilter1=new QFilter("pvdo_userfield.id", QCP.equals,userId);
                e.addCustomQFilter(new QFilter("pvdo_userfield.id", QCP.equals, userId));
            }
        }

        //TODO 排序 默认是降序，怎么改成升序
        e.setOrderBy("billstatus");
        e.setOrderBy("pvdo_datetimefield");

        //无用
        List<QFilter> qFilters = e.getQFilters();
        String orderBy = e.getOrderBy();
        System.out.println(orderBy + qFilters);


    }

    /*@Override
    public void setFilter(SetFilterEvent e) {
        super.setFilter(e);
        long currentUserId = UserServiceHelper.getCurrentUserId();
        DynamicObject bosUser = BusinessDataServiceHelper.loadSingle(currentUserId, "bos_user");
        DynamicObjectCollection docs = bosUser.getDynamicObjectCollection("entryentity");

        QFilter qFilter1 = null;
        QFilter qFilter2 = null;
        for (DynamicObject doc : docs) {
            if (doc.getBoolean("isincharge")) {
                qFilter1 = new QFilter("wmq_applytoworkshop.id", QCP.equals, doc.getDynamicObject("dpt").getPkValue());
            } else {
                qFilter2 = new QFilter("wmq_user.id", QCP.equals, currentUserId);
            }
        }
        e.addCustomQFilter(qFilter1);
    }*/
}
