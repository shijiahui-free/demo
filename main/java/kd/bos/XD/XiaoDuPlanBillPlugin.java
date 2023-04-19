package kd.bos.XD;

import kd.bos.dataentity.entity.CloneUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.datamodel.events.BizDataEventArgs;
import kd.bos.form.*;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.plugin.sample.bill.list.template.ItemClick;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.taxc.bdtaxr.formplugin.monitor.timertask.BussinessDataCheckService;

import javax.management.Query;
import java.util.*;

/**
 * 消毒方案插件
 *
 * @author sjh
 * on 2023/2/24
 */
public class XiaoDuPlanBillPlugin extends AbstractFormPlugin {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 注册监听事件
        this.addItemClickListeners("tbmain"); // 监听整个工具栏，"tbmain"工具栏标识
    }


    //    @Override
//    public void itemClick(ItemClickEvent evt) {
//        super.itemClick(evt);
//
//        if ("wmq_baritemap".equals(evt.getItemKey())) {
//            FormShowParameter showParameter1 = new FormShowParameter();
//            showParameter1.setFormId("");
//            ListShowParameter showParameter = new ListShowParameter();
//            showParameter.setBillFormId("wmq_xiaodu_level");
//            // 设置选择状态
//            showParameter.setLookUp(true);
//            // 设置模板id
//            showParameter.setFormId("bos_listf7");
//            showParameter.getOpenStyle().setShowType(ShowType.Modal);
//            StyleCss styleCss = new StyleCss();
//            styleCss.setWidth("800");
//            styleCss.setHeight("600");
//            showParameter.getOpenStyle().setInlineStyleCss(styleCss);
//
//            showParameter.setCloseCallBack(new CloseCallBack(this, "test"));
//            this.getView().showForm(showParameter);
//        }
//    }
//
//    @Override
//    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
//        super.closedCallBack(closedCallBackEvent);
//        if (closedCallBackEvent.getActionId().equals("test")) {
//            ListSelectedRowCollection returnData = (ListSelectedRowCollection) closedCallBackEvent.getReturnData();
//            for (ListSelectedRow row : returnData) {
//                String billStatus = row.getBillStatus();
//            }
//        }
//    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

        QFilter qFilter = new QFilter("status", QCP.equals, "C");
        DynamicObject[] xiaoduLevels = BusinessDataServiceHelper.load("wmq_xiaodu_level", "number,name", qFilter.toArray());

        if (xiaoduLevels.length <= 0) {
            return;
        }

        int a = this.getModel().getEntryEntity("wmq_leveentry") == null ? 0 : this.getModel().getEntryEntity("wmq_leveentry").size();
        if (a == 0) {
            this.getModel().batchCreateNewEntryRow("wmq_leveentry", xiaoduLevels.length);//批量新建行
            // 循环所有的物料
            for (int i = 0; i < xiaoduLevels.length; i++) {
                this.getModel().setValue("wmq_xdlevel", xiaoduLevels[i].get("id"), i);
            }
        }


        //设置不同的分录行的颜色
        EntryGrid entryGrid = this.getView().getControl("wmq_leveentry");
        IClientViewProxy clientViewProxy = (IClientViewProxy) entryGrid.getView().getService(IClientViewProxy.class);
        ClientActions.createRowStyleBuilder().setRows(new int[]{entryGrid.getRuleCount()})
                .setBackColor("green").setForeColor("black").buildStyle().build()
                .invokeControlMethod(clientViewProxy, entryGrid.getKey());
        ClientActions.createRowStyleBuilder().setRows(new int[]{entryGrid.getRuleCount() + 1})
                .setBackColor("yellow").setForeColor("black").buildStyle().build()
                .invokeControlMethod(clientViewProxy, entryGrid.getKey());
        ClientActions.createRowStyleBuilder().setRows(new int[]{entryGrid.getRuleCount() + 2})
                .setBackColor("pink").setForeColor("black").buildStyle().build()
                .invokeControlMethod(clientViewProxy, entryGrid.getKey());


    }

//    @Override
//    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
//        super.beforeDoOperation(args);
//        FormOperate formOperate = (FormOperate) args.getSource();
//        if (StringUtils.equals("save", formOperate.getOperateKey())) {
//            // 1.新增时首次发布，发布后版本状态为最新版本，版本号为1
//            // 2.再次修改发布时，最新版本的方案才能发布；
//            // 如果消毒方案没有被消毒记录单引用过，则保存修改，
//            // 如果已经产生过消毒记录单，则发布后另存为一个新方案，版本状态为最新版本，版本号+1，旧方案版本状态为历史版本。
//
//            // 查询消毒记录是否引用过
//            List<QFilter> searchFilterList = new ArrayList<>();
//            searchFilterList.add(new QFilter("wmq_xiaodu_plan.number", QCP.equals, this.getModel().getValue("number")));
//
//            DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("wmq_xiaodu_plan", "*", searchFilterList.toArray(new QFilter[]{}));
//            DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle(dynamicObject.getPkValue(), "wmq_xiaodu_plan");
//
//            DynamicObject clone = (DynamicObject) new CloneUtils(true, true).clone(dynamicObject1);
//            int version = (int) this.getModel().getValue("wmq_version") + 1;
//            clone.set("wmq_version", version); // 更新：版本号 + 1
//            clone.set("wmq_versionstatus", "A");//新方案的版本状态为最新版本
//        }
//
//    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);

        String itemKey = evt.getItemKey(); //点击的工具栏按钮标识
        if ("bar_save".equals(itemKey)) {
            // 1.新增时首次发布，发布后版本状态为最新版本，版本号为1
            // 2.再次修改发布时，最新版本的方案才能发布；
            // 如果消毒方案没有被消毒记录单引用过，则保存修改，
            // 如果已经产生过消毒记录单，则发布后另存为一个新方案，版本状态为最新版本，版本号+1，旧方案版本状态为历史版本。

            // 查询消毒记录是否引用过
            List<QFilter> searchFilterList = new ArrayList<>();
            searchFilterList.add(new QFilter("wmq_xiaodu_plan.number", QCP.equals, this.getModel().getValue("number")));

            DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("wmq_xiaodu_plan", "*", searchFilterList.toArray(new QFilter[]{}));
            DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle(dynamicObject.getPkValue(), "wmq_xiaodu_plan");

            DynamicObject clone = (DynamicObject) new CloneUtils(true, true).clone(dynamicObject1);
            int version = (int) this.getModel().getValue("wmq_version") + 1;
            clone.set("wmq_version", version); // 更新：版本号 + 1
            clone.set("wmq_versionstatus", "A");//新方案的版本状态为最新版本

            DynamicObject dots = BusinessDataServiceHelper.loadSingle("wmq_xiaodu_notes", "id, billno", searchFilterList.toArray(new QFilter[]{}));
            if (dots == null) { // 没有被引用过
                this.getView().updateView();
            } else {
                this.getModel().setValue("wmq_versionstatus", "B"); // 旧方案版本状态为历史版本
            }
        }
    }
}
