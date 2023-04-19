package kd.bos.XDdemo;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.form.container.Wizard;
import kd.bos.form.control.Button;
import kd.bos.form.control.Steps;
import kd.bos.form.control.StepsOption;
import kd.bos.form.control.events.StepEvent;
import kd.bos.form.control.events.WizardStepsListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.util.StringUtils;

import java.util.*;

public class SZGuideInfoPlugin extends AbstractFormPlugin implements WizardStepsListener {
    private final static String WIZARDAP_KEY = "rt00_wizardap";

    @Override
    public void registerListener(EventObject e) {
        Wizard wizard = this.getControl(WIZARDAP_KEY);
        wizard.addWizardStepsListener(this);
        Button button = this.getControl("rt00_finishbtn");
        button.addClickListener(this);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        Wizard wizard = this.getControl(WIZARDAP_KEY);

        // 设置完成按钮不可见
        this.getView().setVisible(false, "rt00_finishbtn");

        // 获取消毒步骤分录
        DynamicObjectCollection steps = this.getModel().getEntryEntity("rt00_xd_record_entity");
        List<StepsOption> stepsOptions = wizard.getStepsOptions();
        stepsOptions.clear();
        // 当前序号
        int currentIndex = 0;
        for (int i = 0; i < steps.size(); i++) {
            DynamicObject step = steps.get(i);
            StepsOption stepsOption0 = new StepsOption();
            // 设置标题-消毒等级
            stepsOption0.setTitle(new LocaleString(step.getString("rt00_xd_level")));
            // 设置描述-消毒步骤
            stepsOption0.setDescription(new LocaleString(step.getString("rt00_xd_step")));
            // 设置状态-A	未进行	B	进行中	C	已完成
            switch (step.getString("rt00_xd_billstatus")) {
                case "A":
                    stepsOption0.setStatus("wait");
                    break;
                case "B":
                    currentIndex = i;
                    this.getModel().setValue("rt00_index", currentIndex);
                    stepsOption0.setStatus(Steps.PROCESS);
                    this.getView().setVisible(true, "rt00_finishbtn");
                    // 设置图片
                    this.getModel().setValue("rt00_infectimg", step.get("rt00_xd_img"));
                    break;
                case "C":
                    stepsOption0.setStatus(Steps.FINISH);
                    break;
                default:
                    break;
            }
            stepsOptions.add(stepsOption0);
        }

        // 更新步骤条设置
        wizard.setStepsOptions(stepsOptions);

        // 设置当前节点
        Map<String, Object> currentStepMap = new HashMap<>();
        currentStepMap.put("currentStep", currentIndex);
        currentStepMap.put("currentStatus", Steps.PROCESS);
        // 更新当前节点
        wizard.setWizardCurrentStep(currentStepMap);

    }

    @Override
    public void afterBindData(EventObject e) {
        Wizard wizard = this.getControl(WIZARDAP_KEY);

        // 设置完成按钮不可见
        this.getView().setVisible(false, "rt00_finishbtn");

        // 获取消毒步骤分录
        DynamicObjectCollection steps = this.getModel().getEntryEntity("rt00_xd_record_entity");
        List<StepsOption> stepsOptions = wizard.getStepsOptions();
        stepsOptions.clear();
        // 当前序号
        int currentIndex = -1;
        for (int i = 0; i < steps.size(); i++) {
            DynamicObject step = steps.get(i);
            StepsOption stepsOption0 = new StepsOption();
            // 设置标题-消毒等级
            DynamicObject level = (DynamicObject) step.get("rt00_xd_level");
            stepsOption0.setTitle(new LocaleString(level.getString("name")));
            // 设置描述-消毒步骤
            DynamicObject infectStep = (DynamicObject) step.get("rt00_xd_step");
            stepsOption0.setDescription(new LocaleString(infectStep.getString("name")));
            // 设置状态-A	未进行	B	进行中	C	已完成
            switch (step.getString("rt00_xd_billstatus")) {
                case "A":
                    stepsOption0.setStatus("wait");
                    break;
                case "B":
                    currentIndex = i;
                    this.getModel().setValue("rt00_index", currentIndex);
                    stepsOption0.setStatus(Steps.PROCESS);
                    this.getView().setVisible(true, "rt00_finishbtn");
                    // 设置图片
                    this.getModel().setValue("rt00_infectimg", step.get("rt00_xd_img"));
                    break;
                case "C":
                    stepsOption0.setStatus(Steps.FINISH);
                    if (i == steps.size() - 1) {
                        currentIndex = i;
                        this.getModel().setValue("rt00_index", currentIndex);
                        // 设置图片
                        this.getModel().setValue("rt00_infectimg", step.get("rt00_xd_img"));
                    }

                    break;
                default:
                    break;
            }
            stepsOptions.add(stepsOption0);
        }

        // 更新步骤条设置
//        wizard.setStepsOptions(stepsOptions);
        wizard.setWizardStepsOptions(stepsOptions);

        // 设置当前节点

        Map<String, Object> currentStepMap = new HashMap<>();
        currentStepMap.put("currentStep", currentIndex);
        currentStepMap.put("currentStatus", Steps.PROCESS);
        // 更新当前节点
        wizard.setWizardCurrentStep(currentStepMap);


    }

    @Override
    public void update(StepEvent arg0) {
        int index = arg0.getValue(); // 前端发送的步骤序号
        // 点击时，若进行中的步骤没有上传图片则给出提示。
        String img = (String)this.getModel().getValue("rt00_infectimg");
        if (StringUtils.isEmpty(img)) {
            this.getView().showMessage("请上传图片");
        } else {
            this.getModel().setValue("rt00_index", index);
            DynamicObject step = this.getModel().getEntryEntity("rt00_xd_record_entity").get(index);
            Wizard wizard = this.getControl(WIZARDAP_KEY);
            DynamicObjectCollection steps = this.getModel().getEntryEntity("rt00_xd_record_entity");
            List<StepsOption> stepsOptions = wizard.getStepsOptions();

            if ("B".equals(step.getString("rt00_xd_billstatus"))) {
                this.getView().setVisible(true, "rt00_finishbtn");
            } else {
                this.getView().setVisible(false, "rt00_finishbtn");
            }
            // 设置图片
            this.getModel().setValue("rt00_infectimg", step.get("rt00_xd_img"));
        }
    }

    @Override
    public void click(EventObject evt) {
        String key = ((Button) evt.getSource()).getKey();
        if ("rt00_finishbtn".equals(key)) {
            DynamicObjectCollection steps = this.getModel().getEntryEntity("rt00_xd_record_entity");

            Wizard wizard = this.getControl(WIZARDAP_KEY);
            Object img = this.getModel().getValue("rt00_infectimg");
            String str = (String) this.getModel().getValue("rt00_index");
            int rowIndex = Integer.valueOf(str);
            // 更新对应的分录值
            this.getModel().setValue("rt00_xd_img", this.getModel().getValue("rt00_infectimg"), rowIndex);
            this.getModel().setValue("rt00_xd_billstatus", "C", rowIndex);
            if ((rowIndex + 1) < steps.size()) {
                this.getModel().setValue("rt00_xd_billstatus", "B", rowIndex + 1);
                this.getModel().setValue("rt00_index", rowIndex + 1);
                // 更新节点
                Map<String, Object> currentStepMap = new HashMap<>();
                currentStepMap.put("currentStep", rowIndex + 1);
                currentStepMap.put("currentStatus", Steps.PROCESS);
                // 更新当前节点
                wizard.setWizardCurrentStep(currentStepMap);
                // 设置图片
                this.getModel().setValue("rt00_infectimg", steps.get(rowIndex + 1).get("rt00_xd_img"));
            } else {
                // 最后一个消毒步骤完成后，消毒记录单的单据状态变成已完成，对应的人员申请单的状态变成已完成消毒；
                this.getModel().setValue("billstatus", "B");
                // 设置完成按钮不可见
                this.getView().setVisible(false, "rt00_finishbtn");
                // 更新人员申请单的状态
                String pplyno =  this.getModel().getValue("rt00_xd_apply_no").toString(); //人员申请单状态修改
                List<QFilter> searchFilterList2 = new ArrayList<>();
                searchFilterList2.add(new QFilter("billno", "=", pplyno));
                DynamicObject dot2 = BusinessDataServiceHelper.loadSingle("rt00_xd_apply", "billstatus,billno",
                        searchFilterList2.toArray(new QFilter[] {}));
                dot2.set("billstatus", "F");//已完成消毒
                SaveServiceHelper.update(dot2);
            }
            this.getModel().setValue("rt00_xd_finish_date", new Date(), rowIndex);

            SaveServiceHelper.save(new DynamicObject[]{this.getModel().getDataEntity()});


        }
    }


}
