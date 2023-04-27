package kd.bos.JQ.leavecancel;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.field.ComboEdit;
import kd.bos.form.field.ComboItem;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * 销假单插件
 */
public class LeaveCancelBillPlugin extends AbstractBillPlugIn{
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        addItemClickListeners("bar_submit");
        addClickListeners("pvdo_textfield");
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        //销假单的第一行的请假天数
        int pvdo_integerfield2 = (int) this.getModel().getValue("pvdo_integerfield2", 0);
        List<ComboItem> comboItems=new ArrayList<>();
        for(int i=1;i<=pvdo_integerfield2; i++) {
            ComboItem comboItem = new ComboItem();
            comboItem.setCaption(new LocaleString(String.valueOf(i)));
            comboItem.setValue(String.valueOf(i));
            comboItems.add(comboItem);
        }
        ComboEdit comboEdit = (ComboEdit)this.getControl("pvdo_combofield");
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
        if("bar_submit".equals(itemKey)){
            String billNo_apply = (String) this.getModel().getValue("pvdo_textfield", 0);
            DynamicObject pvdo_leaveapply_bill = BusinessDataServiceHelper.loadSingle("pvdo_leaveapply_bill",
                    "id,billno,billstatus",
                    new QFilter[]{new QFilter("billno", QCP.equals,billNo_apply)});
            pvdo_leaveapply_bill.set("billstatus","D");
            SaveServiceHelper.update(pvdo_leaveapply_bill);
            System.out.println(pvdo_leaveapply_bill);

            //销假天数
            String pvdo_combofield1 = (String) this.getModel().getValue("pvdo_combofield", 0);
            int pvdo_combofield=Integer.valueOf(pvdo_combofield1);
            //实际申请 年假
            int pvdo_integerfield = (int) this.getModel().getValue("pvdo_integerfield");
            //请假天数
            int pvdo_integerfield2 = (int) this.getModel().getValue("pvdo_integerfield2", 0);

            //实际请假天数
            this.getModel().setValue("pvdo_integerfield3", pvdo_integerfield2-pvdo_combofield);
            this.getView().updateView();

            DynamicObject bos_userId = (DynamicObject) this.getModel().getValue("pvdo_userfield");

            //当前单据申请人的假期余额信息 只有两条数据 年假和调休假
            DynamicObject[] holidaymsgs = BusinessDataServiceHelper.load("pvdo_holidaymsg",
                    "pvdo_combofield,pvdo_year,pvdo_quarter,pvdo_userfield,pvdo_days",
                    new QFilter[]{new QFilter("pvdo_userfield.number", QCP.equals, bos_userId.getString("number"))});
            //  销假天数           实际申请 年假
            if(pvdo_combofield<pvdo_integerfield){
                this.getModel().setValue("pvdo_integerfield",pvdo_integerfield-pvdo_combofield);
                holidaymsgs[0].set("pvdo_days",holidaymsgs[0].getLong("pvdo_days")+pvdo_combofield);
            }else {
                this.getModel().setValue("pvdo_integerfield",0);
                this.getModel().setValue("pvdo_integerfield1",((int)this.getModel().getValue("pvdo_integerfield1"))-(pvdo_combofield-pvdo_integerfield));

                holidaymsgs[0].set("pvdo_days",holidaymsgs[0].getLong("pvdo_days")+pvdo_integerfield);
                holidaymsgs[1].set("pvdo_days",holidaymsgs[1].getLong("pvdo_days")+pvdo_combofield-pvdo_integerfield);
            }
            SaveServiceHelper.update(holidaymsgs);
            this.getView().updateView();
        }
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if("bar_submit".equals(itemKey)){


            /*for (DynamicObject holidaymsg : holidaymsgs) {
                if(holidaymsg.getString("pvdo_combofield").equals("A")){
                    if(pvdo_combofield==1){
                        holidaymsg.set("pvdo_days",holidaymsg.getLong("pvdo_days")+pvdo_combofield);
                    }else{
                        holidaymsg.set("pvdo_days",holidaymsg.getLong("pvdo_days")+days);
                    }
                }else {
                    holidaymsg.set("pvdo_days",holidaymsg.getLong("pvdo_days")+pvdo_combofield-days);
                }
                SaveServiceHelper.update(holidaymsg);
            }*/
        } else if ("bar_audit".equals(itemKey)) {
            String billNo_apply = (String) this.getModel().getValue("pvdo_textfield", 0);
            DynamicObject pvdo_leaveapply_bill = BusinessDataServiceHelper.loadSingle("pvdo_leaveapply_bill",
                    "id,billno,billstatus",
                    new QFilter[]{new QFilter("billno", QCP.equals,billNo_apply)});
            pvdo_leaveapply_bill.set("billstatus","E");
            SaveServiceHelper.update(pvdo_leaveapply_bill);
        }
    }

    @Override
    public void beforeClick(BeforeClickEvent evt) {
        super.beforeClick(evt);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
    }

}
