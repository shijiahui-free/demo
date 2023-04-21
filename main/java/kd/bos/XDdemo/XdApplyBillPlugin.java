package kd.bos.XDdemo;

import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import kd.bos.form.control.AttachmentPanel;
import org.apache.commons.lang3.StringUtils;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.ClientProperties;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.field.DateEdit;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.TimeServiceHelper;
import kd.bos.servicehelper.org.OrgServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;

/**
 * 员工申请单
 *
 * @author lxlic
 */
public class XdApplyBillPlugin extends AbstractBillPlugIn {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 注册监听事件
        // this.addItemClickListeners("tbmain"); // 监听整个工具栏，"tbmain"工具栏标识 ※
        // 该监听注册已经在父类中注册过，这里不需要重复注册
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String key = evt.getItemKey();
        // 单据列表工具栏："开始消毒"按钮
        if ("rt00_bar_xd".equals(key)) {
            // 0.仅在申请进入时间当天可操作
            Date entryDate = (Date) this.getModel().getValue("rt00_entry_date");
            long deffDays = DateUtil.diffDate("yyyy-MM-dd", entryDate, new Date());
            //deffDays != 0
            if (false) {
                this.getView().showMessage("申请日期当天才能开始消毒！！");
                evt.setCancel(true);
                return;
            } else {
                // 1.需要根据申请进入车间匹配到消毒方案。
                DynamicObject dynamicObject = (DynamicObject) this.getModel().getValue("rt00_entry_cj");
                long cjId = (long) dynamicObject.getPkValue();
                // 查询消毒方案
                List<QFilter> searchFilterList = new ArrayList<>();
                searchFilterList.add(new QFilter("rt00_version_status", "=", "B")); // A: 历史版本 B:最新版本
                searchFilterList.add(new QFilter("orgentity.fk_rt00_orgfield", "=", cjId));
                DynamicObject dot = BusinessDataServiceHelper.loadSingle("rt00_xd_scheme",
                        "rt00_version_status,rt00_fl_org_entity.orgentity", searchFilterList.toArray(new QFilter[]{}));
                // 2.如有方案，则生成消毒记录单，分录是要进行的消毒步骤，是根据对应消毒方案的各消毒等级中的消毒步骤顺序生成的。具体取值规则请查看消毒记录单字段说明部分。
                if (dot == null) {
                    this.getView().showMessage("进入车间消毒方案不存在，请联系管理员！！");
                }
            }
        } else if ("bar_submit".equals(key)) {
            //提交前校验附件是否为空
            //获取附件控制器
            AttachmentPanel attachmentPanel = getControl("attachmentpanel");
            if (attachmentPanel.getAttachmentData().size() <= 0) {
                this.getView().showMessage("附件不能为空");
                evt.setCancel(true);
            }
        }
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
    }

    /**
     * 前后台事件执行完毕后触发该事件
     */
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if (StringUtils.equals("submit", afterDoOperationEventArgs.getOperateKey())) {
            OperationResult opResult = afterDoOperationEventArgs.getOperationResult();
            if (opResult != null && opResult.isSuccess()) {
                // 读取界面上"状态字段值
                String status = (String) this.getModel().getValue("billstatus");
                if (status.equals("B")) {
                    java.util.HashMap<String, Object> map = new java.util.HashMap<>();
                    map.put(ClientProperties.ForeColor, "orange");
                    this.getView().updateControlMetadata("billstatus", map);
                }
                // this.getView().close(); // 关闭页面
            }
        }
        if (StringUtils.equals("rt00_bar_xd", afterDoOperationEventArgs.getOperateKey())) {
            this.getView().close(); // 关闭页面
            // 打开消毒记录页面
//			BillShowParameter billShowParameter = new BillShowParameter();
//			billShowParameter.setFormId("rt00_xd_record");
//			billShowParameter.getOpenStyle().setShowType(ShowType.NewTabPage);
//			billShowParameter.setStatus(OperationStatus.EDIT);
//			QFilter qFilter = new QFilter("rt00_xd_level", QCP.equals, obj.getPkValue());
//			billShowParameter.getShowParameter().setFilter(qFilter);
//			this.getView().showForm(billShowParameter);
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        // 获取用户id
        long userId = UserServiceHelper.getCurrentUserId();
        // 设置当前用户为申请人
        this.getModel().setValue("rt00_user_apply", userId);
        // 设置用户主业务组织为默认的申请部门
        this.getModel().setValue("rt00_department", UserServiceHelper.getUserMainOrgId(userId));
        OrgUnitServiceHelper.getRootOrgId();
        // 获取公司
        @SuppressWarnings("deprecation")
        Map<String, Object> mapOrg = OrgServiceHelper
                .getCompanyfromOrg(Long.valueOf(UserServiceHelper.getUserMainOrgId(userId)));
        this.getModel().setValue("rt00_company", mapOrg.get("id"));
        // 控制申请进入时间范围（只能选今天及以后的日期）
        DateEdit dre = this.getControl("rt00_entry_date");
        dre.setMinDate(new Date());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        // 设置字体颜色（状态有A:保存、B:已提交（橙色）、C:审核中、 D:已审核（绿色）、E:消毒中、F:已完成消毒(红色)、G:废弃）
        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        if (this.getModel().getValue("billstatus") != null) {
            this.getView().setVisible(false, "rt00_bar_xd");
            if (this.getModel().getValue("billstatus").equals("A")) { // 暂存
                map.put(ClientProperties.ForeColor, "grey");
            } else if (this.getModel().getValue("billstatus").equals("B")) { // 已提交
                map.put(ClientProperties.ForeColor, "orange");
            } else if (this.getModel().getValue("billstatus").equals("C")) { // 审核中
                map.put(ClientProperties.ForeColor, "green");
            } else if (this.getModel().getValue("billstatus").equals("D")) { // 已审核
                map.put(ClientProperties.ForeColor, "green");

                // 申请进入车间的负责人打开审核通过的申请单才能看见“开始消毒”按钮
                List<Long> userIds = new ArrayList<>();
                userIds.add(UserServiceHelper.getCurrentUserId());
                List<Map<String, Object>> list = UserServiceHelper.get(userIds);
                Map<String, Object> userEntity = list.get(0);
                List<Map<String, Object>> posList = (List<Map<String, Object>>) userEntity.get("entryentity");
                boolean isincharge = (boolean) posList.get(0).get("isincharge"); // 是否为负责人
                if (isincharge) {
                    this.getView().setVisible(true, "rt00_bar_xd");
                }
            } else if (this.getModel().getValue("billstatus").equals("E")) { // E:消毒中(红色)
                map.put(ClientProperties.ForeColor, "grey");
            } else if (this.getModel().getValue("billstatus").equals("F")) { // F:已完成消毒(红色)
                map.put(ClientProperties.ForeColor, "red");
            } else {
                map.put(ClientProperties.ForeColor, "#fffff");
            }
        }
        this.getView().updateControlMetadata("billstatus", map);
    }

    @Override
    public void afterLoadData(EventObject e) {
        System.out.println("=========1111===============");
        super.afterLoadData(e);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        // TODO Auto-generated method stub
        super.propertyChanged(e);
        //获取动态表单界面数据模型接口
        IDataModel model = this.getModel();
        //获取表单数据包
        DynamicObject object = (DynamicObject) model.getDataEntity().get("rt00_entry_cj");
        //实体的属性对象实体的标识
        String name = e.getProperty().getName();
        if (object == null) return;
        //同时设置具有相同含义的QFilter条件，用于选单数据查询
        QFilter qFilter = new QFilter("rt00_fl_org_entity.rt00_orgfield", QCP.equals, object.getPkValue());
        switch (name) {
            case "rt00_entry_cj":
                DynamicObject queryOne = QueryServiceHelper.queryOne("rt00_xd_scheme", "id,rt00_fl_org_entity.rt00_orgfield", new QFilter[]{qFilter});
                if (queryOne == null) {
                    this.getView().showTipNotification("该车间没有消毒方案,请重新选择");
                    model.setValue("rt00_entry_cj", null);
                    break;
                }
                model.setValue("rt00_xdfa", queryOne.getLong("id"));
                model.setValue("rt00_entry_date", TimeServiceHelper.now());
                break;
            default:
                break;
        }
    }
}
