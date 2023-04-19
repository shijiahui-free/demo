package kd.bos.XD;

import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.EventObject;

/**
 * 消毒方案列表插件
 *
 * @author sjh
 * on 2023/3/3
 */
public class XiaoDuPlanListPlugin extends AbstractFormPlugin {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 注册监听事件
        this.addItemClickListeners("toolbarap"); // 监听整个工具栏，"tbmain"工具栏标识
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);

        if ("wmq_baritemap".equals(evt.getItemKey())) {
            FormShowParameter fsp = new FormShowParameter();
            fsp.setFormId("wmq_fazz");
            fsp.getOpenStyle().setShowType(ShowType.Modal);
            //fsp.setCustomParam("entryid", schemeId);
            fsp.setCloseCallBack(new CloseCallBack(this, "test"));
            this.getView().showForm(fsp);
        }
    }



    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (closedCallBackEvent.getActionId().equals("test")) {
            ListSelectedRowCollection returnData = (ListSelectedRowCollection) closedCallBackEvent.getReturnData();
//            for (ListSelectedRow row : returnData) {
//                String billStatus = row.getBillStatus();
//            }
        }
    }
}
