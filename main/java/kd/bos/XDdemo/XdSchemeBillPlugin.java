package kd.bos.XDdemo;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import kd.bos.dataentity.entity.CloneUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.events.AfterAddRowEventArgs;
import kd.bos.entity.datamodel.events.BeforeAddRowEventArgs;
import kd.bos.entity.datamodel.events.BizDataEventArgs;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.LoadDataEventArgs;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.report.CellStyle;
import kd.bos.form.ClientActions;
import kd.bos.form.IClientViewProxy;
import kd.bos.form.builder.ListRowStyleBuilder;
import kd.bos.form.control.AbstractGrid;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 消毒方案
 *
 * @author lxlic
 */
public class XdSchemeBillPlugin extends AbstractFormPlugin {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 注册监听事件
        this.addItemClickListeners("tbmain"); // 监听整个工具栏，"tbmain"工具栏标识
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String key = evt.getItemKey();
        // 单据列表工具栏："发布"按钮
        if ("bar_save".equals(key)) {
            // 1.新增时首次发布，发布后版本状态为最新版本，版本号为1
            // 2.再次修改发布时，最新版本的方案才能发布；
            // 如果消毒方案没有被消毒记录单引用过，则保存修改，
            // 如果已经产生过消毒记录单，则发布后另存为一个新方案，版本状态为最新版本，版本号+1，旧方案版本状态为历史版本。
            List<QFilter> searchFilterList = new ArrayList<>();
            searchFilterList.add(new QFilter("number", "=", this.getModel().getValue("number")));
            DynamicObject[] dot = BusinessDataServiceHelper.load("rt00_xd_scheme",
                    "id, number,rt00_version_status, rt00_scheme_version,creator,modifier,name,status,enable,masterid,createtime,modifytime,rt00_fl_level,rt00_fl_level_remark,rt00_fl_level_step,rt00_fl_step_remark,rt00_orgfield", searchFilterList.toArray(new QFilter[]{}));


            // 非新增方案的情况
            if (dot != null && dot.length > 0) {
                // 查询消毒记录是否引用过
                List<QFilter> filterList = new ArrayList<>();
                filterList.add(new QFilter("rt00_base_scheme", "=", dot[0].get("id")));
                DynamicObject[] dots = BusinessDataServiceHelper.load("rt00_xd_record", "id, billno",
                        filterList.toArray(new QFilter[]{}));
                if (dots == null || dots.length == 0) { // 没有被引用过

                } else {
                    this.getModel().setValue("rt00_version_status", "A"); // 旧方案版本状态为历史版本
                }
            }
        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        // 查询基础资料： 消毒等级rt00_fl_level_entity

        ORM orm = ORM.create();
        QFilter[] filters = new QFilter[]{new QFilter("status", "=", "C")};
        List<DynamicObject> materialList = orm.query("rt00_xd_level", filters);
        if (materialList.size() > 0 && materialList != null) {
            // 设置分录的行数为多少
            this.getModel().batchCreateNewEntryRow("rt00_fl_level_entity", materialList.size() - 1);

            // 循环所有的物料
            for (int i = 0; i < materialList.size(); i++) {
                this.getModel().setValue("rt00_fl_level", materialList.get(i).get("id"), i);
            }
            EntryGrid entryGrid = this.getView().getControl("rt00_fl_level_entity");
            IClientViewProxy clientViewProxy = (IClientViewProxy) entryGrid.getView().getService(IClientViewProxy.class);
            ClientActions.createRowStyleBuilder().setRows(new int[]{entryGrid.getRuleCount() - 1})
                    .setBackColor("#ffff00").setForeColor("#ff0000").buildStyle().build()
                    .invokeControlMethod(clientViewProxy, entryGrid.getKey());
            ClientActions.createRowStyleBuilder().setRows(new int[]{entryGrid.getRuleCount() - 2})
                    .setBackColor("#BFFF00").setForeColor("#00FF00").buildStyle().build()
                    .invokeControlMethod(clientViewProxy, entryGrid.getKey());
            ClientActions.createRowStyleBuilder().setRows(new int[]{entryGrid.getRuleCount() - 3})
                    .setBackColor("#0A2A0A").setForeColor("#8A0868").buildStyle().build()
                    .invokeControlMethod(clientViewProxy, entryGrid.getKey());


        }
        super.afterCreateNewData(e);
    }


    @Override
    public void itemClick(ItemClickEvent evt) {

        String key = evt.getItemKey();
        // 单据列表工具栏："发布"按钮
        if ("bar_save".equals(key)) {
            // 1.新增时首次发布，发布后版本状态为最新版本，版本号为1
            // 2.再次修改发布时，最新版本的方案才能发布；
            // 如果消毒方案没有被消毒记录单引用过，则保存修改，
            // 如果已经产生过消毒记录单，则发布后另存为一个新方案，版本状态为最新版本，版本号+1，旧方案版本状态为历史版本。
            List<QFilter> searchFilterList = new ArrayList<>();
            searchFilterList.add(new QFilter("number", "=", this.getModel().getValue("number")));
            //查询数据库的意思   rt00_xd_scheme的标识 
            DynamicObject[] dot = BusinessDataServiceHelper.load("rt00_xd_scheme",
                    "id, number,rt00_version_status, rt00_scheme_version,creator,modifier,name,status,enable,masterid,createtime,modifytime,rt00_fl_level,rt00_fl_level_remark,rt00_fl_level_step,rt00_fl_step_remark,rt00_orgfield,rt00_fl_level_subentity", searchFilterList.toArray(new QFilter[]{}));


            // 非新增方案的情况
            if (dot != null && dot.length > 0) {
                // 查询消毒记录是否引用过
                List<QFilter> filterList = new ArrayList<>();
                filterList.add(new QFilter("rt00_base_scheme", "=", dot[0].get("id")));
                DynamicObject[] dots = BusinessDataServiceHelper.load("rt00_xd_record", "id, billno",
                        filterList.toArray(new QFilter[]{}));
                if (dots == null || dots.length == 0) { // 没有被引用过

                } else {
                    // 保存新纪录

                    DynamicObject clone = (DynamicObject) new CloneUtils(true, true).clone(dot[0]);
                    int version = (int) this.getModel().getValue("rt00_scheme_version") + 1;
                    clone.set("rt00_scheme_version", version); // 更新：版本号 + 1
                    clone.set("rt00_version_status", "B");//新方案的版本状态为最新版本
                    DynamicObject o = (DynamicObject) this.getModel().getEntryEntity("rt00_fl_level_entity").get(0).get("rt00_fl_level");
                    clone.getDynamicObjectCollection("rt00_fl_level_entity").get(0).set("rt00_fl_level", o);


					/*//Object oo = this.getModel().getEntryEntity("rt00_fl_level_subentity").get(0).get("rt00_fl_level_step");
                    DynamicObject levelEntity = (DynamicObject) this.getModel().getEntryEntity("rt00_fl_level_entity").get(0);
                    DynamicObjectCollection levelSubentity =levelEntity.getDynamicObjectCollection("rt00_fl_level_subentity");
					//clone.getDynamicObjectCollection("rt00_fl_level_subentity").get(0).set("rt00_fl_level_step",levelSubentity.get(0));
                    Object rt00_fl_level_step = this.getModel();*/

                    /*DynamicObject billObj = this.getModel().getDataEntity(true);
                    // 先取单据体行集合，之后取单据体行数据包
                    DynamicObjectCollection parentRowObjs = billObj.getDynamicObjectCollection("rt00_fl_level_entity");
                    DynamicObject parentRowObj = parentRowObjs.get(0);
                    // 从单据体行数据包中，取子单据体行集合
                    DynamicObjectCollection subRowObjs = parentRowObj.getDynamicObjectCollection("rt00_fl_level_subentity");
                    Object rt00_fl_level_step =  subRowObjs.get(0).getDynamicObject(2).get("name");
                    //String rt00_fl_level_step = (String) subRowObjs.get(0).get("rt00_fl_level_step");
					clone.getDynamicObjectCollection("rt00_fl_level_subentity").get(0).set("rt00_fl_level_step",rt00_fl_level_step);
					//clone.getDynamicObjectCollection("rt00_fl_level_subentity").get(0).set("rt00_fl_level_step",rt00_fl_level_step);*/
                    SaveServiceHelper.save(new DynamicObject[]{clone});
                    this.getView().showTipNotification("保存成功");
                }
            }else{
//                SaveServiceHelper.save(new DynamicObject[]{this.getModel().getDataEntity()});
//                this.getView().showTipNotification("保存成功");
            }
        }

        //super.itemClick(evt);
    }
}
