package kd.bos.JQ.leavecancel;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;

import java.util.EventObject;

/**
 * 销假单列表插件
 */
public class LeaveCancelListPlugin extends AbstractListPlugin {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        addItemClickListeners("tblcheck");
    }

    @Override
    public void setFilter(SetFilterEvent e) {
        super.setFilter(e);
        long userId = UserServiceHelper.getCurrentUserId();
        DynamicObject bos_user = BusinessDataServiceHelper.loadSingle(userId, "bos_user");
        DynamicObjectCollection docs = bos_user.getDynamicObjectCollection("entryentity");
        for (DynamicObject doc : docs) {
            if (!doc.getBoolean("isincharge")) {
                e.addCustomQFilter(new QFilter("pvdo_userfield.id", QCP.equals, userId));
            }
        }
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
    }
}
