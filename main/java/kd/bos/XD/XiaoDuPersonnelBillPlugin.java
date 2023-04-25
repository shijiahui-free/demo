package kd.bos.XD;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.ClientProperties;
import kd.bos.form.control.AttachmentPanel;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.form.plugin.IFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;

import java.util.*;

/**
 * 人员申请单插件
 *
 * @author sjh
 * on 2023/2/27
 */
public class XiaoDuPersonnelBillPlugin extends AbstractFormPlugin implements IFormPlugin {

    @Override
    public void registerListener(EventObject event) {
        super.registerListener(event);
        this.addItemClickListeners("tbmain");
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        //自动带出申请人所在公司
        super.afterBindData(e);

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
    }

    /**
     * 1 事件介绍
     * 插件可以在此事件，根据各字段值数据，重新设置控件、字段的可用、可见性等。
     * 不要在此事件，修改字段值。
     * 请参阅beforeBindData事件说明，了解本事件与beforeBindData事件的区别。
     * <p>
     * 2 事件触发时机
     * 界面数据包构建完毕，生成指令，刷新前端字段值、控件状态之后，触发此事件。
     *
     * @param e
     */
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        Object billstatus = this.getModel().getValue("billstatus");
        HashMap<String, Object> fieldMap = new HashMap<>();
        if ("A".equals(billstatus)) {
            //设置前景色
            fieldMap.put(ClientProperties.ForeColor, "black");
        } else if ("B".equals(billstatus)) {
            //设置前景色
            fieldMap.put(ClientProperties.ForeColor, "orange");
        } else if ("C".equals(billstatus)) {
            //设置前景色
            fieldMap.put(ClientProperties.ForeColor, "green");
        } else if ("E".equals(billstatus)) {
            //设置前景色
            fieldMap.put(ClientProperties.ForeColor, "red");
        }
        //同步指定元数据到按钮控件
        this.getView().updateControlMetadata("billstatus", fieldMap);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        //获取表单数据包--申请进入车间（wmq_applytoworkshop）
        DynamicObject object = (DynamicObject) this.getModel().getDataEntity().get("wmq_applytoworkshop");
        if (object == null) {
            return;
        }

        //获取值改变字段的标识
        String name = e.getProperty().getName();
        //同时设置具有相同含义的QFilter条件，用于选单数据查询
        QFilter qFilter = new QFilter("useorg.id", QCP.equals, object.getPkValue());

        switch (name) {
            case "wmq_applytoworkshop":
                DynamicObject queryOne = QueryServiceHelper.queryOne("wmq_xiaodu_plan", "id", new QFilter[]{qFilter});
                if (queryOne == null) {
                    this.getView().showTipNotification("该车间没有消毒方案,请重新选择");
                    this.getModel().setValue("wmq_applytoworkshop", null);
                    break;
                }
                this.getModel().setValue("wmq_xdfa", queryOne.getLong("id"));
                break;
            default:
                break;
        }
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);

        FormOperate operation = (FormOperate) args.getSource();
        String key = operation.getOperateKey();
        if (StringUtils.equals("submit", key)) {
            AttachmentPanel attachment = this.getControl("attachmentpanel");
            List<Map<String, Object>> attachmentData = attachment.getAttachmentData();
            if (CollectionUtils.isEmpty(attachmentData)) {
                this.getView().showTipNotification("附件不能为空！");
                args.setCancel(Boolean.TRUE);
            }
        }
    }
}
