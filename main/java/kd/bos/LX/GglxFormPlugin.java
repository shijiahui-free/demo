package kd.bos.LX;


import com.grapecity.documents.excel.S;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.coderule.api.CodeRuleInfo;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.IPageCache;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.coderule.CodeRuleServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

/**
 * 广告立项插件
 *
 * @author sjh
 * on 2023/5/6
 */
public class GglxFormPlugin extends AbstractBillPlugIn implements BeforeF7SelectListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        addItemClickListeners("advcontoolbarap");  //分录工具栏标识

        BasedataEdit basedataEdit = this.getView().getControl("wmq_advertiser");  //添加基础资料beforeF7监听
        basedataEdit.addBeforeF7SelectListener(this);
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
    }

//    private String getEntryCode() {
//        DynamicObject entry = BusinessDataServiceHelper.newDynamicObject("wmq_bidt_entry_code"); //分录规则单据标识
//        entry.set("wmq_sourceuser", this.getModel().getValue("creator"));
//        CodeRuleInfo codeRule = CodeRuleServiceHelper.getCodeRule(entry.getDataEntityType().getName(), entry, null);
//        String entryCode = CodeRuleServiceHelper.getNumber(codeRule, entry);
//        return entryCode;
//    }

    public void itemClick(ItemClickEvent evt) {
//        if ("tb_new".equals(evt.getItemKey())) {
//            DynamicObjectCollection entryentity = this.getModel().getEntryEntity("entryentity");
//            int currentRow = this.getModel().getEntryCurrentRowIndex("entryentity");
//            String entryCode = getEntryCode();
//            this.getModel().setValue("wmq_billnoson", entryCode, currentRow);
//        }

        if ("tb_new".equals(evt.getItemKey())) {
            //新建行
            this.getModel().createNewEntryRow("entryentity");
            int currentRow = this.getModel().getEntryRowCount("entryentity");
            String billno = this.getModel().getValue("billno").toString();
            String i = billno + "_" + currentRow;
            this.getModel().setValue("wmq_billnoson", i, currentRow - 1);
        } else if ("tb_del".equals(evt.getItemKey())) {
            DynamicObjectCollection entryentity = this.getModel().getEntryEntity("entryentity");
            String billno = this.getModel().getValue("billno").toString();
            int j = 0;
            for (int i = 0; i < entryentity.size(); i++) {
                DynamicObject dynamicObject = entryentity.get(i);
                String billsub = billno + "_" + ++j;
                dynamicObject.set("wmq_billnoson", billsub);
            }
            this.getView().updateView("entryentity");
        }
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent evt) {
        String fieldKey = evt.getProperty().getName();
        if (StringUtils.equals(fieldKey, "wmq_advertiser")) { // wmq_advertiser--广告商字段
            int index = this.getModel().getEntryCurrentRowIndex("entryentity");
            DynamicObject obj = (DynamicObject) this.getModel().getValue("wmq_delivery_method", index);
            if (obj == null) {
                this.getView().showErrorNotification("请先选择投放方式");
                evt.setCancel(true);
                return;
            }
            QFilter qFilter = new QFilter("wmq_delivery_method", QCP.equals, obj.getPkValue());
            List<QFilter> list = new ArrayList<>();
            list.add(qFilter);
            evt.setCustomQFilters(list);
        }
    }


}
