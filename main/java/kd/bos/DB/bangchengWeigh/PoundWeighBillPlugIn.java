package kd.bos.DB.bangchengWeigh;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.chart.PointLineChart;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.*;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.events.ClientCallBackEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;

import java.util.EventObject;
import java.util.Map;

/**
 * 磅秤称重页面插件
 */
public class PoundWeighBillPlugIn extends AbstractBillPlugIn implements RowClickEventListener {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        EntryGrid control = this.getControl("wmq_nofinishpound_entry");
        control.addRowClickListener(this);

        addItemClickListeners("tbmain");

        //addClickListeners("wmq_tareweight_btn", "wmq_grossweight_btn");

    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //查询状态为暂存的磅单信息，添加到未完成的磅单列表
        DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load("wmq_bangdan_info_bill",
                "id,billno,billstatus,wmq_relevancybillno,wmq_poundlisttype,wmq_carnum," +
                        "wmq_material,wmq_unittype,wmq_unit,wmq_unithid,wmq_tare,wmq_gross,wmq_weight,wmq_taretime," +
                        "wmq_grosstime,wmq_lastweighingtime",
                new QFilter[]{new QFilter("billstatus", QCP.equals, "A")});
        if (dynamicObjects.length != 0) {
            int length = dynamicObjects.length;
            int rowCount = this.getModel().getEntryRowCount("wmq_nofinishpound_entry");
            if (length > rowCount) {
                this.getModel().batchCreateNewEntryRow("wmq_nofinishpound_entry", length - rowCount);
            }
            for (int i = 0; i < length; i++) {
                this.setEntryDynamicObjectCollection(dynamicObjects[i], i);
            }
        }
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        //点线图设置
        PointLineChart pointLineChart = this.getControl("wmq_pointlinechartap");
        PointLineChartHelper pointLineChartHelper = new PointLineChartHelper();
        pointLineChartHelper.drawChart(pointLineChart, this.getModel().getEntryEntity("wmq_nofinishpound_entry"), null);

        //往来单位文本字段的展示
        Object wmqUnittype = this.getModel().getValue("wmq_unittype");
        if (wmqUnittype == null || wmqUnittype.equals("")) {
            this.getView().setVisible(false, "wmq_unithid");
        }

        //间隔循环调用
        //参数一：标识  参数二：间隔多长时间后触发（单位：毫秒）
        //第一步：有一个事件去调用这个方法（可以是单击事件，界面加载事件等等）
        this.getView().addClientCallBack("ACTION_CLICK", 10000);
    }


    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if (itemKey.equals("bar_save")) {
            //OperationResult operationResult = SaveServiceHelper.saveOperate("save", "wmq_poundedit_bill", new DynamicObject[]{obj}, OperateOption.create());
        }
    }

//    @Override
//    public void beforeClick(BeforeClickEvent evt) {
//        super.beforeClick(evt);
//        Control source = (Control) evt.getSource();
//        String key = source.getKey();
//        EntryGrid entryGrid = this.getControl("wmq_nofinishpound_entry");
//        int[] selectRows = entryGrid.getSelectRows();
//        Boolean details = this.getDetails();
//        if ("wmq_tareweight_btn".equals(key) || "wmq_grossweight_btn".equals(key)) {
//            if (selectRows.length == 0 && !details) {
//                FormShowParameter fsp = new FormShowParameter();
//                fsp.setFormId("wmq_poundprompt");
//                fsp.setCustomParam("content", "哈哈哈");
//                fsp.getOpenStyle().setShowType(ShowType.Modal);
//                fsp.setCloseCallBack(new CloseCallBack(this, "closeCallBack"));
//                this.getView().showForm(fsp);
//                evt.setCancel(true);
//            }
//        }
//    }

    //Details
    private Boolean getDetails() {
        Object wmq_poundlisttype = this.getModel().getValue("wmq_poundlisttype");
        Object billno = this.getModel().getValue("billno");
        Object wmqCarnum = this.getModel().getValue("wmq_carnum");
        Object wmqMaterial = this.getModel().getValue("wmq_material");
        Object wmqUnittype = this.getModel().getValue("wmq_unittype");
        Object wmqUnit = this.getModel().getValue("wmq_unit");
        Object wmqUnithid = this.getModel().getValue("wmq_unithid");

        if (wmq_poundlisttype == null && billno == "" && wmqCarnum == "" && wmqMaterial == null && wmqUnittype == null && wmqUnit == null && wmqUnithid == "") {
            return false;
        } else {
            return true;
        }
    }

//    @Override
//    public void click(EventObject evt) {
//        super.click(evt);
//        Control source = (Control) evt.getSource();
//        String key = source.getKey();
//        if ("wmq_tareweight_btn".equals(key)) {
//            FormShowParameter fsp = new FormShowParameter();
//            fsp.setFormId("wmq_poundprompt");
//            Object billno = this.getModel().getValue("billno");
//            fsp.setCustomParam("entryId", billno);
//            fsp.getOpenStyle().setShowType(ShowType.Modal);
//            fsp.setCloseCallBack(new CloseCallBack(this, "closeCallBack"));
//            this.getView().showForm(fsp);
//        }
//    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        if ("wmq_unittype".equals(name)) {
            this.getView().setVisible(true, "wmq_unithid");
        } else if ("wmq_unit".equals(name)) {
            Object wmq_unittype = this.getModel().getValue("wmq_unittype");
            if ("1".equals(wmq_unittype) || "2".equals(wmq_unittype)) {
                DynamicObject wmq_unit = (DynamicObject) this.getModel().getValue("wmq_unit");
                if (wmq_unit != null) {
                    this.getModel().setValue("wmq_unithid", wmq_unit.getString("name"));
                }
            }
        } else if ("wmq_poundlisttype".equals(name)) {
            Object wmq_poundlisttype = this.getModel().getValue("wmq_poundlisttype");
            String billno = (String) this.getModel().getValue("billno");
            //BD-001
            if ("0".equals(wmq_poundlisttype)) {
                this.getModel().setValue("wmq_poundlisttype_jc", "CG");
            } else if ("1".equals(wmq_poundlisttype)) {
                this.getModel().setValue("wmq_poundlisttype_jc", "XG");
            } else if ("2".equals(wmq_poundlisttype)) {
                this.getModel().setValue("wmq_poundlisttype_jc", "QT");
            }
        }
    }

    @Override
    public void entryRowClick(RowClickEvent evt) {
        RowClickEventListener.super.entryRowClick(evt);
        int row = evt.getRow();
        if (row != -1) {
            DynamicObjectCollection entity = this.getModel().getEntryEntity("wmq_nofinishpound_entry");
            DynamicObject dynamicObject = entity.get(row);
            this.setBillHead(dynamicObject);
        } else {
            this.cancelBillHead();
            this.getView().updateView();
        }
    }

    /**
     * 初始化未完成的磅单列表数据
     *
     * @param dynamicObject
     * @param i
     */
    private void setEntryDynamicObjectCollection(DynamicObject dynamicObject, int i) {
        DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("wmq_nofinishpound_entry");
        DynamicObject result = entryEntity.get(i);
        result.set("wmq_poundlisttype_e", dynamicObject.get("wmq_poundlisttype"));
        result.set("wmq_poundnum_e", dynamicObject.get("billno"));
        result.set("wmq_carnum_e", dynamicObject.get("wmq_carnum"));
        result.set("wmq_gross_e", dynamicObject.get("wmq_gross"));
        result.set("wmq_tare_e", dynamicObject.get("wmq_tare"));
        result.set("wmq_weight_e", dynamicObject.get("wmq_weight"));
        result.set("wmq_unithid_e", dynamicObject.getDynamicObject("wmq_unit").get("name"));
        result.set("wmq_taretime", dynamicObject.get("wmq_taretime"));
        result.set("wmq_grosstime", dynamicObject.get("wmq_grosstime"));
        result.set("wmq_lasttime", dynamicObject.get("wmq_lastweighingtime"));
        result.set("wmq_material_e", dynamicObject.get("wmq_material"));
        result.set("wmq_unittype_e", dynamicObject.get("wmq_unittype"));
        result.set("wmq_unit_e", dynamicObject.getDynamicObject("wmq_unit"));
    }

    /**
     * 选择未完成的磅单列表，设置单据头数据
     *
     * @param dynamicObject
     */
    private void setBillHead(DynamicObject dynamicObject) {
        this.getModel().setValue("wmq_poundlisttype", dynamicObject.get("wmq_poundlisttype_e"));
        this.getModel().setValue("billno", dynamicObject.get("wmq_poundnum_e"));
        this.getModel().setValue("wmq_carnum", dynamicObject.get("wmq_carnum_e"));
        this.getModel().setValue("wmq_unithid", dynamicObject.get("wmq_unithid_e"));
        this.getModel().setValue("wmq_material", dynamicObject.get("wmq_material_e"));
        this.getModel().setValue("wmq_gross", dynamicObject.get("wmq_gross_e"));
        this.getModel().setValue("wmq_tare", dynamicObject.get("wmq_tare_e"));
        this.getModel().setValue("wmq_weight", dynamicObject.get("wmq_weight_e"));
        this.getModel().setValue("wmq_unittype", dynamicObject.get("wmq_unittype_e"));
        this.getModel().setValue("wmq_unit", dynamicObject.get("wmq_unit_e"));
    }

    /**
     * 去掉选择，清除单据头数据
     */
    private void cancelBillHead() {
        this.getModel().setValue("wmq_poundlisttype", null);
        this.getModel().setValue("billno", null);
        this.getModel().setValue("wmq_carnum", null);
        this.getModel().setValue("wmq_unithid", null);
        this.getModel().setValue("wmq_material", null);
        this.getModel().setValue("wmq_tare", null);
        this.getModel().setValue("wmq_gross", null);
        this.getModel().setValue("wmq_weight", null);
        this.getModel().setValue("wmq_unittype", null);
        this.getModel().setValue("wmq_unit", null);
    }


    @Override
    public void clientCallBack(ClientCallBackEvent e) {
        Map param = e.getParam();
        String name = e.getName();
        if (StringUtils.equals("ACTION_CLICK", name)) {
            PointLineChart pointLineChart = this.getControl("wmq_pointlinechartap");
            PointLineChartHelper pointLineChartHelper = new PointLineChartHelper();


            DynamicObjectCollection wmq_bangdan_info_bill = QueryServiceHelper.query("wmq_bangdan_info_bill", "billno,wmq_weight", new QFilter[]{});

            pointLineChartHelper.drawChart(pointLineChart, null, wmq_bangdan_info_bill);
            this.getView().updateView("wmq_pointlinechartap");

            //30秒后重新回调这个方法
            this.getView().addClientCallBack("ACTION_CLICK", 30000);
        }
    }

}
