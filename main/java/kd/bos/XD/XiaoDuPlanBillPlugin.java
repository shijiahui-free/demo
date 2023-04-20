package kd.bos.XD;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.CloneUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.*;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * 消毒方案插件
 *
 * @author sjh
 * on 2023/2/24
 */
public class XiaoDuPlanBillPlugin extends AbstractBillPlugIn implements BeforeF7SelectListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 监听消毒步骤的事件
        BasedataEdit xdStept = this.getView().getControl("wmq_xdstep"); //基础资料--消毒步骤(wmq_xdstep)
        xdStept.addBeforeF7SelectListener(this);
        // 注册监听事件
        this.addItemClickListeners("tbmain"); // 监听整个工具栏，"tbmain"工具栏标识
    }


//        @Override
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

    /**
     * @param e 界面数据包构建完毕，生成指令，刷新前端字段值、控件状态之后，触发此事件
     */
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

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);

        String itemKey = evt.getItemKey(); //点击的工具栏按钮标识
        if ("bar_save".equals(itemKey)) {
            // 1.新增时首次发布，发布后版本状态为最新版本，版本号为1
            // 2.再次修改发布时，最新版本的方案才能发布；
            // 如果消毒方案没有被消毒记录单引用过，则保存修改，
            // 如果已经产生过消毒记录单，则发布后另存为一个新方案，版本状态为最新版本，版本号+1，旧方案版本状态为历史版本。

            // 查询消毒记录单是否引用过此消毒方案
            List<QFilter> searchFilterList = new ArrayList<>();
            searchFilterList.add(new QFilter("wmq_xiaodu_plan.number", QCP.equals, this.getModel().getValue("number")));
            DynamicObject dots = BusinessDataServiceHelper.loadSingle("wmq_xiaodu_notes", "id, billno", searchFilterList.toArray(new QFilter[]{}));
            if (dots == null) { // 没有被引用过
                this.getView().updateView();
            } else {
                DynamicObject wmq_xiaodu_plan = this.getModel().getDataEntity(true);
                DynamicObject clone = (DynamicObject) new CloneUtils(true, true).clone(wmq_xiaodu_plan);
                int version = (int) this.getModel().getValue("wmq_version") + 1;
                clone.set("wmq_version", version); // 更新：版本号 + 1
                clone.set("wmq_versionstatus", "A");//新方案的版本状态为最新版本


               OperationResult saveOperate = SaveServiceHelper.saveOperate("wmq_xiaodu_plan", new DynamicObject[]{clone}, OperateOption.create());

                //SaveServiceHelper.save(new DynamicObject[]{clone});
//                if (saveOperate.isSuccess()) {
//                    wmq_xiaodu_plan.set("wmq_versionstatus", "B");
//                    SaveServiceHelper.update(wmq_xiaodu_plan);
//                }
            }
        }
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent evt) {
        String fieldKey = evt.getProperty().getName();
        if (StringUtils.equals(fieldKey, "wmq_xdstep")) { // 选择步骤时
            // 获取等级分录选中行  wmq_leveentry--分录标识
            int index = this.getModel().getEntryCurrentRowIndex("wmq_leveentry");
            // 获取选中行中的消毒等级
            DynamicObject obj = (DynamicObject) this.getModel().getValue("wmq_xdlevel", index);
            //wmq_xdlevel--分录中的消毒等级字段的标识
            QFilter qFilter = new QFilter("wmq_xdlevel", QCP.equals, obj.getPkValue());
            ListShowParameter showParameter = (ListShowParameter) evt.getFormShowParameter();
            showParameter.getListFilterParameter().setFilter(qFilter);
        }
    }
}
