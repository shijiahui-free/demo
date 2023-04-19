package kd.bos.XD;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.BillEntityType;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.ExtendedDataEntitySet;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.List;
import java.util.Map;

/**
 * 消毒人员申请下推消毒记录单
 *
 * @author sjh
 * on 2023/3/6
 */
public class XiaoduPersonToNotes extends AbstractConvertPlugIn {
    private void getContext() {
        BillEntityType srcMainType = this.getSrcMainType();

        // 目标单主实体
        BillEntityType tgtMainType = this.getTgtMainType();

    }

    /**
     * 单据转换后事件，最后执行
     *
     * @param e
     * @remark 插件可以在这个事件中，对生成的目标单数据，进行最后的修改
     */
    @Override
    public void afterConvert(AfterConvertEventArgs e) {
        this.printEventInfo("afterConvert", "");
        BillEntityType srcMainType = this.getSrcMainType();
        ExtendedDataEntitySet targetExtDataEntitySet = e.getTargetExtDataEntitySet();
        Map<String, List<ExtendedDataEntity>> extDataEntityMap = targetExtDataEntitySet.getExtDataEntityMap();
        List<ExtendedDataEntity> wmq_xiaodu_notes = extDataEntityMap.get("wmq_xiaodu_notes");
        if (wmq_xiaodu_notes.size() > 0) {
            ExtendedDataEntity extendedDataEntity = wmq_xiaodu_notes.get(0);
            //获取目标单（消毒记录单）
            DynamicObject xiaoDuNote = extendedDataEntity.getDataEntity();
            String id = xiaoDuNote.get("wmq_applytoworkshop.id").toString();
            //查询消毒方案
            QFilter qFilter = new QFilter("useorg,id", QCP.equals, id);
            DynamicObject wmq_xiaodu_plans = BusinessDataServiceHelper.loadSingle("wmq_xiaodu_plan", "id,number,name", qFilter.toArray());
            DynamicObject rtdl_ds = BusinessDataServiceHelper.loadSingle(wmq_xiaodu_plans.getPkValue(), "wmq_xiaodu_plan");
            //设置消毒方案到消毒记录单中
            xiaoDuNote.set("wmq_xiaodu_plan", rtdl_ds);

            if (rtdl_ds != null) {
                //wmq_leveentry  消毒方案--消毒等级分录
                DynamicObjectCollection xdLevelRows = rtdl_ds.getDynamicObjectCollection("wmq_leveentry");

                //消毒记录单的分录--》wmq_step_entryentity
                DynamicObjectCollection rdEntity = xiaoDuNote.getDynamicObjectCollection("wmq_step_entryentity");
                rdEntity.clear();
                int counter = 0;

                for (DynamicObject xdLevelRow : xdLevelRows) {
                    //wmq_xdlevel  消毒等级
                    DynamicObject rtdl_fl_level = xdLevelRow.getDynamicObject("wmq_xdlevel");
                    DynamicObjectCollection stepRows = rtdl_ds.getDynamicObjectCollection("wmq_stepentry");
                    for (DynamicObject stepRow : stepRows) {
                        // 消毒记录单分录--创建行实例
                        DynamicObject obj = (DynamicObject) rdEntity.getDynamicObjectType().createInstance();
                        // 设置消毒等级
                        obj.set("wmq_xiaodulevel", rtdl_fl_level);
                        // 设置消毒步骤
                        DynamicObject step = stepRow.getDynamicObject("wmq_xdstep");
                        obj.set("wmq_xiaodustep", step);

                        // 设置消毒状态
                        if (counter == 0) {
                            obj.set("wmq_xiaodustatus", "B");  //B 进行中
                        } else {
                            obj.set("wmq_xiaodustatus", "A");  //A 未进行
                        }
                        counter = counter + 1;
                        rdEntity.add(obj);
                    }
                }
                //将分录数据赋值回消毒记录单
                xiaoDuNote.set("wmq_step_entryentity", rdEntity);
            }
        }
    }

    private void printEventInfo(String eventName, String argString) {
        String msg = String.format("%s : %s", eventName, argString);
        System.out.println(msg);
    }
}
