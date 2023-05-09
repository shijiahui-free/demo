package kd.bos.LX;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.ShowType;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.math.BigDecimal;
import java.util.EventObject;

/**
 * 报账单插件
 *
 * @author sjh
 * on 2023/5/8
 */
public class BzFromPlugin extends AbstractBillPlugIn implements HyperLinkClickListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        addItemClickListeners("tbmain");

        // 监听分录行
        EntryGrid entryGrid = this.getView().getControl("entryentity");
        entryGrid.addHyperClickListener(this);
    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent hyperLinkClickEvent) {
        //  获取超链接点击的属性名
        String fieldName = hyperLinkClickEvent.getFieldName();
        //  获取点击分录行的下标
        int rowIndex = hyperLinkClickEvent.getRowIndex();
        if (StringUtils.containsIgnoreCase(fieldName, "wmq_billnoson")) {

            // 获取超链接信息  单据编号
            Object checkNo = this.getModel().getValue(fieldName, rowIndex);
            // 查询单据详情
            DynamicObject customerAskRecord = BusinessDataServiceHelper.loadSingle("wmq_lx_bill", "id",
                    new QFilter("entryentity.wmq_billnoson", QCP.equals, checkNo).toArray());

            BillShowParameter billShowParameter = new BillShowParameter();
            // 设置打开单据的标识
            billShowParameter.setFormId("wmq_lx_bill");
            // 设置打开单据的id
            billShowParameter.setPkId(customerAskRecord.getPkValue());
            // 设置打开的样式
            billShowParameter.getOpenStyle().setShowType(ShowType.Modal);

            this.getView().showForm(billShowParameter);
        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        //获取值改变字段的标识
        String name = e.getProperty().getName();

        if ("wmq_bz_balance".equals(name)) {
            //报账金额 wmq_bz_balance <= wmq_can_balance 可报金额
            int entryentity = this.getModel().getEntryCurrentRowIndex("entryentity");

            Object wmq_bz_balance = this.getModel().getValue("wmq_bz_balance", 0);
            Object wmq_can_balance = this.getModel().getValue("wmq_can_balance", entryentity);

            BigDecimal bigDecimal = new BigDecimal(wmq_bz_balance.toString());
            BigDecimal bigDecimal2 = new BigDecimal(wmq_can_balance.toString());

            if (bigDecimal.compareTo(bigDecimal2) > 0) {
                this.getModel().setValue("wmq_bz_balance", 0, entryentity);
                this.getView().showErrorNotification("报账金额 wmq_bz_balance <= wmq_can_balance 可报金额");
            }
        }
    }
}
