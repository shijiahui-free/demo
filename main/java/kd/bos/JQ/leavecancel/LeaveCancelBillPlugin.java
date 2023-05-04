package kd.bos.JQ.leavecancel;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.exception.KDBizException;
import kd.bos.ext.fi.util.QueryUtil;
import kd.bos.form.ShowType;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.form.field.ComboEdit;
import kd.bos.form.field.ComboItem;
import kd.bos.list.IListView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

/**
 * 销假单插件
 */
public class LeaveCancelBillPlugin extends AbstractBillPlugIn implements HyperLinkClickListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        addItemClickListeners("tbmain");

        // 监听分录行
        EntryGrid entryGrid = this.getView().getControl("entryentity");
        entryGrid.addHyperClickListener(this);
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        //销假单的第一行的请假天数
        int wmq_leaveday = (int) this.getModel().getValue("wmq_leaveday", 0);
        List<ComboItem> comboItems = new ArrayList<>();
        for (int i = 1; i <= wmq_leaveday; i++) {
            ComboItem comboItem = new ComboItem();
            //下拉标题
            comboItem.setCaption(new LocaleString(String.valueOf(i)));
            //下拉值
            comboItem.setValue(String.valueOf(i));
            comboItems.add(comboItem);
        }
        //销假天数控件
        ComboEdit comboEdit = this.getControl("wmq_xjday");
        comboEdit.setComboItems(comboItems);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String itemKey = evt.getItemKey();
        if ("bar_submit".equals(itemKey)) {
            //获取销假单选择行的请假申请单单号
            String billNo_apply = getSelectRowBillNo();
            //检验上游请假单的状态（由于请假单只能在审核状态才能下推销假单，此处不用在校验请假单状态）
            DynamicObject pvdo_leaveapply_bill = BusinessDataServiceHelper.loadSingle("wmq_leave_apply_bill",
                    "id,billno,billstatus", new QFilter[]{new QFilter("billno", QCP.equals, billNo_apply)});
            pvdo_leaveapply_bill.set("billstatus", "D");
            SaveServiceHelper.update(pvdo_leaveapply_bill);
        } else if ("bar_audit".equals(itemKey)) {
            //1、返还对应员工的假期剩余天数
            //销假天数
            String wmq_xjday = (String) this.getModel().getValue("wmq_xjday", 0);
            int xjday = Integer.parseInt(wmq_xjday);
            //实际申请 年假
            int wmq_yearleave = (int) this.getModel().getValue("wmq_yearleave");

            DynamicObject bos_userId = (DynamicObject) this.getModel().getValue("wmq_userfield");

            //当前单据申请人的假期余额信息 只有两条数据 年假和调休假
            DynamicObject[] holidaymsgs = BusinessDataServiceHelper.load(
                    "wmq_leave_info_bill",
                    "wmq_leavetype,wmq_year,wmq_quarter,wmq_userfield,wmq_days",
                    new QFilter[]{new QFilter("wmq_userfield.number", QCP.equals, bos_userId.getString("number"))});
            //销假天数   实际申请年假
            if (xjday < wmq_yearleave) {
                this.getModel().setValue("wmq_yearleave", wmq_yearleave - xjday);
                holidaymsgs[0].set("wmq_days", holidaymsgs[0].getLong("wmq_days") + xjday);
            } else {
                this.getModel().setValue("wmq_yearleave", 0);
                this.getModel().setValue("wmq_txleave", ((int) this.getModel().getValue("wmq_txleave")) - (xjday - wmq_yearleave));

                for (DynamicObject holidaymsg : holidaymsgs) {
                    if (holidaymsg.getString("wmq_leavetype").equals("A")) {
                        holidaymsg.set("wmq_days", holidaymsg.getLong("wmq_days") + wmq_yearleave);
                    } else {
                        holidaymsg.set("wmq_days", holidaymsg.getLong("wmq_days") + xjday - wmq_yearleave);
                    }
                }
            }
            SaveServiceHelper.update(holidaymsgs);
            this.getView().invokeOperation("save");

            //2、对应的请假单状态变为已销假
            //获取销假单选择行的请假申请单单号
            String billNo_apply = getSelectRowBillNo();
            DynamicObject pvdo_leaveapply_bill = BusinessDataServiceHelper.loadSingle("wmq_leave_apply_bill",
                    "id,billno,billstatus",
                    new QFilter[]{new QFilter("billno", QCP.equals, billNo_apply)});
            pvdo_leaveapply_bill.set("billstatus", "E");
            SaveServiceHelper.update(pvdo_leaveapply_bill);

            this.getView().updateView();
        }
    }

    /**
     * 获取销假单选择行的请假申请单单号
     * 销假单销假信息分录为单选
     */
    public String getSelectRowBillNo() {
        //获取单据体控件
        EntryGrid entryGrid = this.getControl("entryentity");
        //获取选中行，数组为行号，从0开始int[]
        int[] selectRows = entryGrid.getSelectRows();
        //获取单据体数据集合
        DynamicObjectCollection entity = this.getModel().getEntryEntity("entryentity");

        String wmq_qjdh = null;
        if (selectRows != null && selectRows.length > 0) {
            for (int selectRow : selectRows) {
                //获取选中行的单据体数据
                DynamicObject dynamicObject = entity.get(selectRow);
                //选中行请假单号
                wmq_qjdh = dynamicObject.getString("wmq_qjdh");
            }
        }

        if (wmq_qjdh == null) {
            throw new KDBizException("提交前，请先补充销假信息并勾选");
        }
        return wmq_qjdh;
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);

        //获取值改变字段的标识
        String name = e.getProperty().getName();
        if ("wmq_xjday".equals(name)) {
            //销假天数
            String wmq_xjday = (String) this.getModel().getValue("wmq_xjday", 0);
            int xjday = Integer.parseInt(wmq_xjday);

            //请假天数
            int wmq_leaveday = (int) this.getModel().getValue("wmq_leaveday", 0);
            //实际请假天数
            this.getModel().setValue("wmq_sjts", wmq_leaveday - xjday);
        }
    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent hyperLinkClickEvent) {
        //  获取超链接点击的属性名
        String fieldName = hyperLinkClickEvent.getFieldName();
        //  获取点击分录行的下标
        int rowIndex = hyperLinkClickEvent.getRowIndex();
        if (StringUtils.containsIgnoreCase(fieldName, "wmq_qjdh")) {

            // 获取超链接信息  单据编号
            Object checkNo = this.getModel().getValue(fieldName, rowIndex);
            // 查询单据详情
            DynamicObject customerAskRecord = BusinessDataServiceHelper.loadSingle("wmq_leave_apply_bill", "id",
                    new QFilter("billno", QCP.equals, checkNo).toArray());

            BillShowParameter billShowParameter = new BillShowParameter();
            // 设置打开单据的标识
            billShowParameter.setFormId("wmq_leave_apply_bill");
            // 设置打开单据的id
            billShowParameter.setPkId(customerAskRecord.getPkValue());
            // 设置打开的样式
            billShowParameter.getOpenStyle().setShowType(ShowType.Modal);

            this.getView().showForm(billShowParameter);
        }
    }
}
