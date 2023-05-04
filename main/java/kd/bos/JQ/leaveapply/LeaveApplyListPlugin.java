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
}
