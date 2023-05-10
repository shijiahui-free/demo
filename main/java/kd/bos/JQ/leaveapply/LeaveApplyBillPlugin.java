package kd.bos.JQ.leaveapply;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.plugin.support.util.Assert;
import kd.bos.form.ClientProperties;
import kd.bos.form.ICloseCallBack;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.field.DateRangeEdit;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 假期申请单插件
 */
public class LeaveApplyBillPlugin extends AbstractBillPlugIn implements ICloseCallBack {
    private static final String LEAVE_TIME = "wmq_leavetime";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        addItemClickListeners("bar_submit");
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //设置公司取值
        DynamicObject wmq_company = (DynamicObject) this.getView().getModel().getValue("wmq_company");
        if (wmq_company != null) {
            this.getModel().setValue("wmq_company", wmq_company.get("id"));
        } else {
            long currentUserId = UserServiceHelper.getCurrentUserId();
            List<Long> userIds = new ArrayList<>(1);
            userIds.add(currentUserId);
            Map<Long, Long> companyMap = UserServiceHelper.getCompanyByUserIds(userIds);
            this.getModel().setValue("wmq_company", companyMap.get(currentUserId));
        }

        //请假时间最小范围从明天开始取
        DateRangeEdit headFieldEdit = this.getView().getControl("wmq_leavetime");
        headFieldEdit.setMinDate(addAndSubtractDaysByCalendar(new Date(), 1));

        //从假期信息单据获取用户拥有的假期信息
        DynamicObject user = (DynamicObject) this.getModel().getValue("wmq_userfield");
        QFilter qFilter1 = new QFilter("wmq_userfield.number", QCP.equals, user.getString("number"));
        DynamicObject[] holidayInfo = BusinessDataServiceHelper.load("wmq_leave_info_bill",
                "wmq_leavetype,wmq_year,wmq_quarter,wmq_userfield,wmq_userfield.number,wmq_days"
                , new QFilter[]{qFilter1});
        long days = 0;
        for (DynamicObject dynamicObject : holidayInfo) {
            days += dynamicObject.getLong("wmq_days");
            if (dynamicObject.getString("wmq_leavetype").equals("A")) {
                //拥有年假
                this.getModel().setValue("wmq_yynj", dynamicObject.getLong("wmq_days"));
                //设置年假余额
                this.getModel().setValue("wmq_njye", dynamicObject.getLong("wmq_days"));
            } else {
                //拥有调休
                this.getModel().setValue("wmq_yytx", dynamicObject.getLong("wmq_days"));
                //设置调休余额
                this.getModel().setValue("wmq_txye", dynamicObject.getLong("wmq_days"));
            }
        }
        this.getModel().setValue("wmq_canapplyday", days, 0);
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterCreateNewData(e);

        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        if (this.getModel().getValue("billstatus") != null) {
            if (this.getModel().getValue("billstatus").equals("A")) { // 暂存
                map.put(ClientProperties.ForeColor, "grey");
            } else if (this.getModel().getValue("billstatus").equals("B")) { // 已提交
                map.put(ClientProperties.ForeColor, "blue");
            } else if (this.getModel().getValue("billstatus").equals("C")) { // 已审核
                map.put(ClientProperties.ForeColor, "green");
            } else if (this.getModel().getValue("billstatus").equals("D")) { // 销假中
                map.put(ClientProperties.ForeColor, "red");
            } else if (this.getModel().getValue("billstatus").equals("E")) { // E:已销假
                map.put(ClientProperties.ForeColor, "yellow");
            } else {
                map.put(ClientProperties.ForeColor, "#fffff");
            }
        }
        this.getView().updateControlMetadata("billstatus", map);
    }

    /**
     * @param dateTime 待处理的日期
     * @param n        加减天数
     * @return
     */
    public static Date addAndSubtractDaysByCalendar(Date dateTime, int n) {
        java.util.Calendar calstart = java.util.Calendar.getInstance();
        calstart.setTime(dateTime);
        calstart.add(java.util.Calendar.DAY_OF_WEEK, n);
        return calstart.getTime();
    }


    @Override
    //只有已审核的才能下推，点击前判断
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String itemKey = evt.getItemKey();
        if ("wmq_xiaojia".equals(itemKey)) {//wmq_xiaojia 销假下推按钮
            Object billstatus = this.getModel().getValue("billstatus");
            if (!"C".equals(billstatus)) {
                this.getView().showMessage("只有状态为已审核的单据才能进行销假!");
                evt.setCancel(true);
            }
        }
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if ("bar_submit".equals(itemKey)) {//提交按钮
            DynamicObject user = (DynamicObject) this.getModel().getValue("wmq_userfield");
            QFilter qFilter = new QFilter("wmq_userfield.number", QCP.equals, user.getString("number"));
            DynamicObject[] holidayInfo = BusinessDataServiceHelper.load("wmq_leave_info_bill",
                    "wmq_leavetype,wmq_year,wmq_quarter,wmq_userfield,wmq_userfield.number,wmq_days"
                    , new QFilter[]{qFilter});

            int wmq_txye = (int) this.getModel().getValue("wmq_txye"); //调休假余额
            int wmq_njye = (int) this.getModel().getValue("wmq_njye"); //年假余额

            for (DynamicObject holidaymsg : holidayInfo) {
                if (holidaymsg.getString("wmq_leavetype").equals("A")) {
                    holidaymsg.set("wmq_days", wmq_njye);
                } else {
                    holidaymsg.set("wmq_days", wmq_txye);
                }
                SaveServiceHelper.update(holidaymsg);
            }
        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        DateRangeEdit headFieldEdit = this.getView().getControl("wmq_leavetime");
        String key_headdatestart = headFieldEdit.getStartDateFieldKey();
        String key_headdateend = headFieldEdit.getEndDateFieldKey();

        Integer max = (Integer) this.getModel().getValue("wmq_canapplyday", 0);

        String name = e.getProperty().getName();
        if ("wmq_leavetime_enddate".equals(name)) {
            Date start = (Date) this.getModel().getValue(key_headdatestart);
            Date end = (Date) this.getModel().getValue(key_headdateend);
            Long aLong = diffDate(start, end);
            if (aLong > max) {
                this.getModel().setValue(key_headdatestart, addAndSubtractDaysByCalendar(new Date(), 1));
                this.getModel().setValue(key_headdateend, addAndSubtractDaysByCalendar(new Date(), 2));

                this.getModel().setValue("wmq_applyday", 0, 0);
                this.getView().showTipNotification("申请天数不能大于可申请天数！");
            } else {
                this.getModel().setValue("wmq_applyday", Math.toIntExact(aLong), 0);
            }
        } else if ("wmq_applyday".equals(name)) {//先减调休，不够再减年假
            //申请天数
            int wmq_applyday = Integer.parseInt(this.getModel().getValue("wmq_applyday", 0).toString());
            //当前剩余调休假
            int wmq_yytx = Integer.parseInt(this.getModel().getValue("wmq_yytx").toString());
            if (wmq_applyday < wmq_yytx) {
                this.getModel().setValue("wmq_sqtx", wmq_applyday);
            } else {
                this.getModel().setValue("wmq_sqtx", wmq_yytx);
                this.getModel().setValue("wmq_sqnj", wmq_applyday - wmq_yytx);
            }
        }
    }

    private Long diffDate(Date startDate, Date endDate) {
        // 设置转换的日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // 开始时间
            startDate = sdf.parse(sdf.format(startDate));
            // 结束时间
            endDate = sdf.parse(sdf.format(endDate));
        } catch (ParseException e) {
            System.out.println(e);
        }
        // 得到相差的天数
        return (endDate.getTime() - startDate.getTime()) / (60 * 60 * 24 * 1000) + 1;
    }


}
