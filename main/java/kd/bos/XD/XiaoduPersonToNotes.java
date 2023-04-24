package kd.bos.XD;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.BillEntityType;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.ExtendedDataEntitySet;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.ArrayList;
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
     * @remark 事件在目标单据生成完毕后触发，插件可以在这个事件对生成的目标单数据进行最后的调整
     */
    @Override
    public void afterConvert(AfterConvertEventArgs e) {
        this.printEventInfo("afterConvert", "");
        ExtendedDataEntitySet targetExtDataEntitySet = e.getTargetExtDataEntitySet();
        Map<String, List<ExtendedDataEntity>> extDataEntityMap = targetExtDataEntitySet.getExtDataEntityMap();
        List<ExtendedDataEntity> wmq_xiaodu_notes = extDataEntityMap.get("wmq_xiaodu_notes");

        if (wmq_xiaodu_notes.size() > 0) {
            ExtendedDataEntity extendedDataEntity = wmq_xiaodu_notes.get(0);
            //获取目标单（消毒记录单）
            DynamicObject xiaoDuNote = extendedDataEntity.getDataEntity();
            //车间ID
            String chejianId = xiaoDuNote.get("wmq_applytoworkshop.id").toString();
            //查询消毒方案
            QFilter qFilter = new QFilter("useorg,id", QCP.equals, chejianId);
            DynamicObject wmq_xiaodu_plans = BusinessDataServiceHelper.loadSingle("wmq_xiaodu_plan", "id,number,name", qFilter.toArray());
            DynamicObject plan = BusinessDataServiceHelper.loadSingle(wmq_xiaodu_plans.getPkValue(), "wmq_xiaodu_plan");
            //设置消毒方案到消毒记录单中
            //xiaoDuNote.set("wmq_xiaodu_plan", plan);

            if (plan != null) {
                //wmq_leveentry  消毒方案--消毒等级分录
                DynamicObjectCollection xdLevelRows = plan.getDynamicObjectCollection("wmq_leveentry");

                //消毒记录单的步骤分录--》wmq_step_entryentity
                DynamicObjectCollection rdEntity = xiaoDuNote.getDynamicObjectCollection("wmq_step_entryentity");
                rdEntity.clear();
                int counter = 0;

                DynamicObjectCollection stepRows = plan.getDynamicObjectCollection("wmq_stepentry");
                for (DynamicObject stepRow : stepRows) {
                    // 消毒记录单分录--创建行实例
                    DynamicObject obj = (DynamicObject) rdEntity.getDynamicObjectType().createInstance();

                    // 设置消毒步骤
                    DynamicObject step = stepRow.getDynamicObject("wmq_xdstep");
                    obj.set("wmq_xiaodustep", step);
                    // 设置消毒等级
                    ORM orm = ORM.create();
                    QFilter[] filters = new QFilter[]{new QFilter("id", QCP.equals, step.getPkValue())};
                    DynamicObject dynamicObject = orm.queryOne("wmq_xiaodu_step", filters);
                    obj.set("wmq_xiaodulevel", dynamicObject.getDynamicObject("wmq_xdlevel"));

                    // 设置消毒状态
                    if (counter == 0) {
                        obj.set("wmq_xiaodustatus", "B");  //B 进行中
                    } else {
                        obj.set("wmq_xiaodustatus", "A");  //A 未进行
                    }
                    counter = counter + 1;
                    rdEntity.add(obj);
                }


                //上游附件下推携带到下游附件
                List<DynamicObject> convertSource = (List<DynamicObject>) extendedDataEntity.getValue("ConvertSource");
                for (DynamicObject dynamicObject : convertSource) {
                    long sourceId = dynamicObject.getLong("id");
                    List<Map<String, Object>> attachments = AttachmentServiceHelper.getAttachments("wmq_personnel", sourceId, "attachmentpanel");
                    AttachmentUntil.uploadTargetAttachments("wmq_xiaodu_notes", xiaoDuNote.getPkValue(), "attachmentpanel", attachments);
                }


                //人员申请单的单据状态变成消毒中。
                //开始消毒成功后，对应员工申请单状态为进行中
                String applyid = xiaoDuNote.get("wmq_applyno").toString();   //消毒记录单转化时记录人员申请单申请单号
                List<QFilter> searchFilterList2 = new ArrayList<>();
                searchFilterList2.add(new QFilter("billno", "=", applyid));
                DynamicObject dot2 = BusinessDataServiceHelper.loadSingle("wmq_personnel", "billstatus,billno",
                        searchFilterList2.toArray(new QFilter[]{}));
                dot2.set("billstatus", "D");//消毒中
                SaveServiceHelper.update(dot2);
            }
        }
    }

    private void printEventInfo(String eventName, String argString) {
        String msg = String.format("%s : %s", eventName, argString);
        System.out.println(msg);
    }
}
