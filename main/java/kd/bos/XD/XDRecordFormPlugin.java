package kd.bos.XD;

import com.kingdee.bos.util.backport.Arrays;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.BizDataEventArgs;
import kd.bos.form.container.Wizard;
import kd.bos.form.control.Button;
import kd.bos.form.control.Steps;
import kd.bos.form.control.StepsOption;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.StepEvent;
import kd.bos.form.control.events.WizardStepsListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.mvc.bill.BillView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * zpp消毒记录单：
 * 1：页面加载:将 单据头的消毒方案导入单据体
 * 2：
 */
public class XDRecordFormPlugin extends AbstractFormPlugin implements WizardStepsListener {
    public void initialize() {
        //初始化监听控件
        addClickListeners("wmq_buttonap");
        Wizard wizard = this.getControl("wmq_wizardap");
        wizard.addWizardStepsListener(this);
    }

    @Override
    public void beforeClick(BeforeClickEvent evt) {
        super.beforeClick(evt);

        Button button = (Button) evt.getSource();
        if (button.getKey().equals("wmq_buttonap")) {
            if (this.getModel().getValue("wmq_picturefield") == null || "".equals(this.getModel().getValue("wmq_picturefield"))) {
                this.getView().showMessage("必须上传照片！");
                evt.setCancel(true);
            } else {
                //获取当前单据体的内容
                DynamicObjectCollection step_entryentity = this.getModel().getEntryEntity("wmq_step_entryentity");
                //是否是最后一步的变量
                boolean ifEndFlag = true;
                for (DynamicObject entryData : step_entryentity) {
                    //进行中的步骤改成已完成
                    if ("B".equals(entryData.get("wmq_xiaodustatus"))) {
                        entryData.set("wmq_xiaodustatus", "C");
                        entryData.set("wmq_xiaoduphoto", this.getModel().getValue("wmq_picturefield"));
                    }
                    //未进行的改成进行中
                    if ("A".equals(entryData.get("wmq_xiaodustatus"))) {
                        entryData.set("wmq_xiaodustatus", "B");
                        //如果有未进行的改成进行中,则说明不是最后一步
                        ifEndFlag = false;
                        //改完下一条就退出循环
                        break;
                    }
                }

                if (ifEndFlag) {
                    //所有步骤全部完成，则 都整单完成(提交)
                    this.getModel().setValue("billstatus", "B");
                    this.getView().invokeOperation("save");
                }
                this.getView().updateView();
            }
        }
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

        Object billno = this.getModel().getValue("billno");
        //根据记录单号查询数据库
        QFilter qFilter = new QFilter("billno", QCP.equals, billno);
        DynamicObject[] noteExist = BusinessDataServiceHelper.load("wmq_xiaodu_notes",
                "wmq_step_entryentity.wmq_xiaodustatus", qFilter.toArray());
        //如果已存在此数据不在进行分录数据初始化
        if (noteExist == null) {
            //分录初始化
            noteEntityInitialize();
            //消毒记录向导--初始化
            DynamicObjectCollection wmq_step_entryentity = this.getModel().getEntryEntity("wmq_step_entryentity");
            wizardInitialize(wmq_step_entryentity);
        } else {
            DynamicObjectCollection step_entryentity = this.getModel().getEntryEntity("wmq_step_entryentity");
            wizardInitialize(step_entryentity);
        }
    }

    @Override
    public void update(StepEvent paramStepEvent) {
        int stepInt = paramStepEvent.getValue();
        this.getView().setVisible(true, "wmq_buttonap");
        String currentstep = this.getPageCache().get("currentstep");
        if (currentstep != null) {
            this.getView().setVisible(currentstep.equals(String.valueOf(stepInt)), "wmq_buttonap");
        } else {
            this.getView().setVisible(false, "wmq_buttonap");
        }
        this.getModel().setValue("wmq_picturefield", this.getModel().getValue("wmq_xiaoduphoto", stepInt));
    }

    /**
     * 消毒记录分录--初始化
     */
    public void noteEntityInitialize() {
        //消毒方案
        DynamicObject plan = (DynamicObject) this.getModel().getValue("wmq_xiaodu_plan");
        DynamicObject rtdl_ds = BusinessDataServiceHelper.loadSingle(plan.getPkValue(), "wmq_xiaodu_plan");
        if (rtdl_ds != null) {
            //wmq_leveentry  消毒方案--消毒等级分录
            DynamicObjectCollection xdLevelRows = rtdl_ds.getDynamicObjectCollection("wmq_leveentry");

            //消毒记录单_的分录entryentity
            DynamicObject dataEntity = this.getModel().getDataEntity();
            DynamicObjectCollection rdEntity = (DynamicObjectCollection) this.getModel().getValue("wmq_step_entryentity");
            rdEntity.clear();
            int counter = 0;

            for (DynamicObject xdLevelRow : xdLevelRows) {
                //wmq_xdlevel  消毒等级
                DynamicObject rtdl_fl_level = xdLevelRow.getDynamicObject("wmq_xdlevel");
                DynamicObjectCollection stepRows = rtdl_ds.getDynamicObjectCollection("wmq_stepentry");
                for (DynamicObject stepRow : stepRows) {
                    DynamicObject obj = (DynamicObject) rdEntity.getDynamicObjectType().createInstance(); // 创建行实例
                    // 消毒等级
                    obj.set("wmq_xiaodulevel", rtdl_fl_level);
                    // 消毒步骤
                    DynamicObject step = stepRow.getDynamicObject("wmq_xdstep");
                    obj.set("wmq_xiaodustep", step);

                    // 消毒状态
                    if (counter == 0) {
                        obj.set("wmq_xiaodustatus", "B");  //B 进行中
                    } else {
                        obj.set("wmq_xiaodustatus", "A");  //A 未进行
                    }
                    counter = counter + 1;
                    rdEntity.add(obj);
                }
            }
            this.getModel().setValue("wmq_step_entryentity", rdEntity);
        }
    }

    /**
     * 消毒记录向导--初始化
     */
    public void wizardInitialize(DynamicObjectCollection wmq_step_entryentity) {
        Wizard wizard = this.getControl("wmq_wizardap");
        // 获取设计时的步骤条设置
        List<StepsOption> stepsOptions = wizard.getStepsOptions();
        //初始化步骤条
        stepsOptions.clear();
        int i = 0;
        int currentindex = -1;
        //每次进入的时候重置一下页面缓存的当前步数,currentstep这个会在update方法中使用的
        if (this.getPageCache().get("currentstep") != null) {
            this.getPageCache().remove("currentstep");
        }

        if (wmq_step_entryentity.size() > 0) {
            for (DynamicObject step : wmq_step_entryentity) {
                StepsOption stepsOption0 = new StepsOption();
                String leaveName = step.getDynamicObject("wmq_xiaodulevel").getString("name");
                stepsOption0.setTitle(new LocaleString(leaveName));
                String stepName = step.getDynamicObject("wmq_xiaodustep").getString("name");
                stepsOption0.setDescription(new LocaleString(stepName));

                switch (step.getString("wmq_xiaodustatus")) {
                    case "A":
                        stepsOption0.setStatus(Steps.PROCESS);
                        i++;
                        break;
                    case "B":
                        currentindex = i;
                        this.getPageCache().put("currentstep", String.valueOf(currentindex));
                        i++;
                        break;
                    case "C":
                        stepsOption0.setStatus(Steps.FINISH);
                        i++;
                        break;
                }
                stepsOptions.add(stepsOption0);
            }
            // 更新步骤条设置
            wizard.setWizardStepsOptions(stepsOptions);

            // 设置当前节点
            Map<String, Object> currentStepMap = new HashMap<>();

            if (currentindex >= 0) {
                currentStepMap.put("currentStep", currentindex);
                currentStepMap.put("currentStatus", Steps.PROCESS);
                this.getModel().setValue("wmq_picturefield", this.getModel().getValue("wmq_xiaoduphoto", i - 1));
            } else {
                currentStepMap.put("currentStep", i - 1);
                currentStepMap.put("currentStatus", Steps.FINISH);
                this.getView().setVisible(false, "wmq_buttonap");
            }
            // 更新当前节点
            wizard.setWizardCurrentStep(currentStepMap);
        }
    }
}