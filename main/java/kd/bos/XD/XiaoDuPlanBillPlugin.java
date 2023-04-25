package kd.bos.XD;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.CloneUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.ClientActions;
import kd.bos.form.IClientViewProxy;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.operate.FormOperate;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.ORM;
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

    /**
     * @param e 界面数据包构建完毕，生成指令，刷新前端字段值、控件状态之后，触发此事件
     */
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        ORM orm = ORM.create();
        QFilter[] filters = new QFilter[]{new QFilter("status", QCP.equals, "C")};
        List<DynamicObject> materialList = orm.query("wmq_xiaodu_level", filters);
        if (materialList.size() <= 0) {
            return;
        }
        // 设置分录的行数为多少
        this.getModel().batchCreateNewEntryRow("wmq_leveentry", materialList.size() - 1);
        // 循环所有可用的等级基础资料
        for (int i = 0; i < materialList.size(); i++) {
            this.getModel().setValue("wmq_xdlevel", materialList.get(i).get("id"), i);
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

    /**
     * 1 事件介绍
     * 插件可以在此事件，根据各字段值数据，重新设置控件、字段的可用、可见性等。
     * 不要在此事件，修改字段值。
     * 请参阅beforeBindData事件说明，了解本事件与beforeBindData事件的区别。
     * <p>
     * 2 事件触发时机
     * 界面数据包构建完毕，生成指令，刷新前端字段值、控件状态之后，触发此事件。
     */
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
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


    /**
     * @param args 1 事件介绍
     *             插件可以在此事件：
     *             提示确认消息；
     *             校验数据，取消操作的执行；
     *             传递给自定义操作参数给操作服务、操作插件。
     *             <p>
     *             2 事件触发时机
     *             用户点击按钮、菜单，执行绑定的操作逻辑前，触发此事件；
     */
    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();

        // 单据列表工具栏："发布"按钮
        if ("save".equals(formOperate.getOperateKey())) {
            // 1.新增时首次发布，发布后版本状态为最新版本，版本号为1
            // 2.再次修改发布时，最新版本的方案才能发布；
            Object versionstatus = this.getModel().getValue("wmq_versionstatus");
            if ("B".equals(versionstatus)) {
                this.getView().showTipNotification("最新版本的消毒方案才能修改消毒步骤和发布");
                args.setCancel(true);
                return;
            }

            // 如果消毒方案没有被消毒记录单引用过，则保存修改，
            // 如果已经产生过消毒记录单，则发布后另存为一个新方案，版本状态为最新版本，版本号+1，旧方案版本状态为历史版本。
            List<QFilter> searchFilterList = new ArrayList<>();
            searchFilterList.add(new QFilter("wmq_xiaodu_plan.number", QCP.equals, this.getModel().getValue("number")));
            DynamicObject dots = BusinessDataServiceHelper.loadSingle("wmq_xiaodu_notes", "id, billno", searchFilterList.toArray(new QFilter[]{}));
            if (dots != null) {
                DynamicObject wmq_xiaodu_plan = this.getModel().getDataEntity(true);
                //克隆新对象
                DynamicObject clone = (DynamicObject) new CloneUtils(true, true).clone(wmq_xiaodu_plan);

                int version = (int) this.getModel().getValue("wmq_version") + 1;
                clone.set("wmq_version", version); // 更新：版本号 + 1
                clone.set("wmq_versionstatus", "A");//新方案的版本状态为最新版本

                //直接保存，不校验 （save与saveOperate的区别）
                //SaveServiceHelper.save(new DynamicObject[]{clone}, OperateOption.create());
                OperationResult saveOperate = SaveServiceHelper.saveOperate("wmq_xiaodu_plan", new DynamicObject[]{clone}, OperateOption.create());
                if (saveOperate.isSuccess()) {
                    wmq_xiaodu_plan.set("wmq_versionstatus", "B");
                    SaveServiceHelper.update(wmq_xiaodu_plan);
                } else {
                    this.getView().showErrorNotification(saveOperate.getMessage());
                    args.setCancel(true);
                }
            }
        }
    }

    /**
     * @param evt 插件可以在此事件，设置基础资料列表过滤条件，或者打开其他资料选择界面。
     *            用户点击基础资料字段，打开基础资料选择列表界面前，触发此事件。
     */
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
