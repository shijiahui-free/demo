package kd.bos.XDdemo;

import java.util.ArrayList;
import java.util.List;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.ExtendedDataEntitySet;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
import kd.bos.entity.botp.plugin.args.BeforeGetSourceDataEventArgs;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 单据转换插件
 *
 * @author lxlic
 */
public class XdApply2RecordConvertPlugin extends AbstractConvertPlugIn {

    @Override
    public void beforeGetSourceData(BeforeGetSourceDataEventArgs e) {
        // QFilter filter = new QFilter("entryentity.lockcount",QCP.large_than,"0");
        // e.getQFilters().add(filter);
        // TODO Auto-generated method stub
        super.beforeGetSourceData(e);
    }

    @Override
    public void afterConvert(AfterConvertEventArgs e) {
        ExtendedDataEntitySet targetExtDataEntitySet = e.getTargetExtDataEntitySet();
        // 取目标单，单据头数据包 （可能会生成多张单，是个数组）
        String targetEntityNumber = this.getTgtMainType().getName();
        // 获取所有目标单
        ExtendedDataEntity[] billDataEntitys = targetExtDataEntitySet.FindByEntityKey(targetEntityNumber);
        // 逐单处理
        for (ExtendedDataEntity billDataEntity : billDataEntitys) {
            // 取当前目标单据体（消毒记录单）
            DynamicObject bill = billDataEntity.getDataEntity();
            long cjId = (long) ((DynamicObject) bill.get("rt00_entry_cj")).getPkValue();// 车间ID
            // 查询指定车间消毒方案
            List<QFilter> searchFilterList = new ArrayList<>();
            searchFilterList.add(new QFilter("rt00_version_status", "=", "B")); // A: 历史版本 B:最新版本
            searchFilterList.add(new QFilter("rt00_fl_org_entity.rt00_orgfield", "=", cjId));
            DynamicObject dot = BusinessDataServiceHelper.loadSingle("rt00_xd_scheme",
                    "rt00_version_status, rt00_fl_level_entity.rt00_fl_level, rt00_fl_level_subentity.rt00_fl_level_step,rt00_fl_org_entity orgentity",
                    searchFilterList.toArray(new QFilter[]{}));

            // 消毒记录单：消毒方案
            DynamicObject xdScheme = bill.getDynamicObject("rt00_base_scheme");
            bill.set("rt00_base_scheme", xdScheme);
            bill.set("rt00_base_scheme_id", dot.getPkValue());

            DynamicObjectCollection xdLevelRows = (DynamicObjectCollection) dot.get("rt00_fl_level_entity");
            // 目标单据体（消毒记录单据体）
            DynamicObjectCollection rdEntity = bill.getDynamicObjectCollection("rt00_xd_record_entity");
            rdEntity.clear();
            int counter = 0;
            for (DynamicObject entryRow : xdLevelRows) {
                DynamicObject xdLevel = entryRow.getDynamicObject("rt00_fl_level"); // 获取每一行消毒记录等级（对应多个消毒步骤）
                DynamicObjectCollection stepRows = (DynamicObjectCollection) entryRow.get("rt00_fl_level_subentity");
                for (DynamicObject stepRow : stepRows) {
                    DynamicObject obj = (DynamicObject) rdEntity.getDynamicObjectType().createInstance(); // 创建行实例
                    // 消毒等级
                    obj.set("rt00_xd_level", xdLevel);
                    obj.set("rt00_xd_level_id", xdLevel.getPkValue());
                    // 消毒步骤
                    DynamicObject step = stepRow.getDynamicObject("rt00_fl_level_step");
                    obj.set("rt00_xd_step", step);
                    obj.set("rt00_xd_step_id", step.getPkValue());
                    // 消毒状态
                    if (counter == 0) {
                        obj.set("rt00_xd_billstatus", "B");
                    } else {
                        obj.set("rt00_xd_billstatus", "A");
                    }
                    counter = counter + 1;
                    rdEntity.add(obj);
                }
            }
            //开始消毒成功后，对应员工申请单状态为进行中
            String applyid = bill.get("rt00_xd_apply_no").toString();//申请单
            List<QFilter> searchFilterList2 = new ArrayList<>();
            searchFilterList2.add(new QFilter("billno", "=", applyid));
            DynamicObject dot2 = BusinessDataServiceHelper.loadSingle("rt00_xd_apply", "billstatus,billno",
                    searchFilterList2.toArray(new QFilter[]{}));
            dot2.set("billstatus", "E");//消毒中
            SaveServiceHelper.update(dot2);
        }
        super.afterConvert(e);
    }
}
