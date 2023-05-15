package kd.bos.LX;


import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.ClientProperties;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
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

    public void itemClick(ItemClickEvent evt) {
        if ("tb_new".equals(evt.getItemKey())) {
            //新建行
            this.getModel().createNewEntryRow("entryentity");
            int currentRow = this.getModel().getEntryRowCount("entryentity");
            //单据编号
            String billno = this.getModel().getValue("billno").toString();

            //给立项子单号赋值
            if (currentRow < 10) {
                this.getModel().setValue("wmq_billnoson", billno + "-0" + currentRow, currentRow - 1);
            } else {
                this.getModel().setValue("wmq_billnoson", billno + "-" + currentRow, currentRow - 1);
            }
        } else if ("tb_del".equals(evt.getItemKey())) {
            DynamicObjectCollection entryentity = this.getModel().getEntryEntity("entryentity");
            String billno = this.getModel().getValue("billno").toString();
            int j = 0;
            for (DynamicObject dynamicObject : entryentity) {
                String billsub = billno + "_" + ++j;
                dynamicObject.set("wmq_billnoson", billsub);
            }
            this.getView().updateView("entryentity");
        } else if ("wmq_zero_clearing".equals(evt.getItemKey())) {
            //获取单据体控件
            EntryGrid entryGrid = this.getControl("entryentity");
            //获取选中行，数组为行号，从0开始int[]
            int[] selectRows = entryGrid.getSelectRows();
            //获取单据体数据集合
            DynamicObjectCollection entity = this.getModel().getEntryEntity("entryentity");
            if (selectRows != null && selectRows.length > 0) {
                for (int selectRow : selectRows) {
                    //获取选中行的单据体数据
                    DynamicObject dot = entity.get(selectRow);
                    //如选择的分录申请金额-已报金额=0，则该行分录无需清0。
                    if (dot.getBigDecimal("wmq_apply_amount").subtract(dot.getBigDecimal("wmq_yibao_amount")).compareTo(BigDecimal.ZERO) <= 0) {
                        this.getView().showTipNotification("第" + (selectRow + 1) + "行申请金额=已报金额无需清0");
                        continue;
                    }
                    //若可用余额已经=0则该行分录无需清0
                    if (dot.getBigDecimal("wmq_can_balance").compareTo(BigDecimal.ZERO) == 0) {
                        this.getView().showTipNotification("第" + (selectRow + 1) + "行可用余额已经为0无需清0");
                        continue;
                    }

                    //wmq_lx_bill--广告立项单标识  生产一张新的立项申请单
                    DynamicObject data = BusinessDataServiceHelper.newDynamicObject("wmq_lx_bill");
                    //为新增单据头赋值
                    data.set("billstatus", "C");
                    data.set("wmq_lx_date", this.getModel().getValue("wmq_lx_date"));
                    data.set("wmq_userfield", this.getModel().getValue("wmq_userfield"));
                    data.set("wmq_company", this.getModel().getValue("wmq_company"));
                    data.set("org", this.getModel().getValue("org"));
                    data.set("wmq_lx_type", "0");
                    //会审人
                    // 获取多选基础资料的值(此时为空)
                    DynamicObjectCollection currencyColl = data.getDynamicObjectCollection("wmq_reviewer");
                    // 获取待赋值的币别数据
                    DynamicObject[] currencyArr = this.getMulbaseDatas();
                    // 方案一(简单, 推荐)
                    for (DynamicObject currency : currencyArr) {
                        DynamicObject newCurrency = new DynamicObject(currencyColl.getDynamicObjectType());
                        newCurrency.set("fbasedataId", currency);
                        currencyColl.add(newCurrency);
                    }
                    // 多选基础资料字段赋值
                    data.set("wmq_reviewer", currencyColl);

                    //为新增分录赋值
                    DynamicObjectCollection entryentity = data.getDynamicObjectCollection("entryentity"); //
                    DynamicObject entry = new DynamicObject(entryentity.getDynamicObjectType());
                    entry.set("wmq_billnoson", dot.get("wmq_billnoson"));
                    entry.set("wmq_delivery_method", dot.get("wmq_delivery_method"));
                    entry.set("wmq_advertiser", dot.get("wmq_advertiser"));
                    entry.set("wmq_product", dot.get("wmq_product"));
                    //申请金额=原立项申请单.分录.申请金额-原立项申请单.分录.已报金额
                    entry.set("wmq_apply_amount", dot.getBigDecimal("wmq_apply_amount").subtract(dot.getBigDecimal("wmq_yibao_amount")));
                    entry.set("wmq_approval_amount", dot.get("wmq_apply_amount"));
                    entry.set("wmq_can_balance", dot.get("wmq_can_balance"));
                    entry.set("wmq_yibao_amount", dot.get(0));
                    entry.set("wmq_status", "未报");
                    entryentity.add(entry);

                    OperationResult operationResult = SaveServiceHelper.saveOperate("wmq_lx_bill", new DynamicObject[]{data}, OperateOption.create());
                    if (operationResult.isSuccess()) {//保存成功
                        //进行提示
                        this.getView().showTipNotification("生成成功");
                        //并修改源单对应的分录行的数据
                        dot.set("wmq_can_balance", 0);
                        dot.set("wmq_status", "已关闭");
                        //更新源单的分录行并刷新
                        SaveServiceHelper.update(dot);
                        this.getView().updateView("entryentity");
                    } else {
                        this.getView().showTipNotification("生成失败");
                    }
                }
            }

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

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

        //单据状态
        String billstatus = (String) this.getModel().getValue("billstatus");

        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        //不同状态设置不同颜色
        if (billstatus != null) {
            switch (billstatus) {
                case "A":  // 保存
                    map.put(ClientProperties.ForeColor, "pink");
                    break;
                case "B":  // 已提交
                    map.put(ClientProperties.ForeColor, "blue");
                    break;
                case "C":  // 审核通过
                    map.put(ClientProperties.ForeColor, "red");
                    break;
                case "D":  // 废弃
                    map.put(ClientProperties.ForeColor, "green");
                    break;
                case "E":  // 关闭
                    map.put(ClientProperties.ForeColor, "purple");
                    break;
                default:
                    map.put(ClientProperties.ForeColor, "#fffff");
                    break;
            }
        }
        this.getView().updateControlMetadata("billstatus", map);
    }


    private DynamicObject[] getMulbaseDatas() {

        DynamicObjectCollection coll = (DynamicObjectCollection) getModel().getValue("wmq_reviewer");
        Object[] idList = new Object[coll.size()];

        for (int i = 0; i < coll.size(); i++) {
            DynamicObject basedataObj = coll.get(i).getDynamicObject("fbasedataid");
            Long basedataId = (Long) basedataObj.getPkValue();
            idList[i] = basedataId;
        }

        String selectProperties = "id, number, name";
        QFilter filter = new QFilter("id", QCP.in, idList);
        return BusinessDataServiceHelper.load("bos_user", selectProperties, filter.toArray());
    }

}
