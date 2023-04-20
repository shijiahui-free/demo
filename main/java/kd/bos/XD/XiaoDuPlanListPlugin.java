package kd.bos.XD;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.IListView;
import kd.bos.list.ListShowParameter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.EventObject;

/**
 * 消毒方案列表插件
 *
 * @author sjh
 * on 2023/3/3
 */
public class XiaoDuPlanListPlugin extends AbstractFormPlugin {
    //消毒方案
    private final String XDFA = "wmq_xiaodu_plan";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 注册监听事件
        this.addItemClickListeners("toolbarap"); // 监听整个工具栏，"tbmain"工具栏标识
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        //点击消毒方案列表上面的分配组织按钮  wmq_baritemap
        //分配组织 rtdl_disorg
        if ("wmq_baritemap".equals(evt.getItemKey())) {
            //获取选中的数据
            IListView listview = (IListView) this.getView();
            ListSelectedRowCollection selectedRows = listview.getSelectedRows();

            if (selectedRows.size() > 0) {
                Long id = (Long) selectedRows.get(0).getPrimaryKeyValue();
                DynamicObject scheme = BusinessDataServiceHelper.loadSingle(id, XDFA);
                // 最新版本的消毒方案才能分配组织
                if ("B".equals(scheme.getString("wmq_versionstatus"))) {
                    this.getView().showMessage("最新版本的消毒方案才能分配组织");
                    return;
                }

                ListShowParameter listShowParameter = new ListShowParameter();
                listShowParameter.setBillFormId("bos_org");
                //设置弹出页面的打开方式
                listShowParameter.getOpenStyle().setShowType(ShowType.Modal);
                //设置为不能多选，如果为true则表示可以多选
                listShowParameter.setMultiSelect(false);
                //设置打开页面的大小
                StyleCss inlineStyleCss = new StyleCss();
                inlineStyleCss.setHeight("580");
                inlineStyleCss.setWidth("960");
                listShowParameter.getOpenStyle().setInlineStyleCss(inlineStyleCss);

                //bos_templatetreelistf7：标准F7树形列表    epm_lefttreerightlistf7
                listShowParameter.setLookUp(true);
                listShowParameter.setFormId("bos_orgtreelistf7");

                listShowParameter.setCloseCallBack(new CloseCallBack(this, "assignorg"));
                //弹出F7选择界面
                this.getView().showForm(listShowParameter);
            }
        }
    }


    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (StringUtils.equals(closedCallBackEvent.getActionId(), "assignorg") && null != closedCallBackEvent.getReturnData()) {
            //从动态表单页面获取的returnData数据
            ListSelectedRowCollection returnData = (ListSelectedRowCollection) closedCallBackEvent.getReturnData();

            String orgpk = returnData.get(0).getPrimaryKeyValue().toString();


            IListView listview = (IListView) this.getView();
            ListSelectedRowCollection selectedRows = listview.getSelectedRows();
            Long id = (Long) selectedRows.get(0).getPrimaryKeyValue();
            DynamicObject scheme = BusinessDataServiceHelper.loadSingle(id, XDFA);


            DynamicObjectCollection orgEntity = (DynamicObjectCollection) scheme.get("wmq_org_entry");
            orgEntity.clear();
            DynamicObject orgs = orgEntity.addNew();
            orgs.set("wmq_orgfield", orgpk);
            scheme.set("wmq_org_entry", orgEntity);

            OperationResult operationResult = OperationServiceHelper.executeOperate("save", XDFA, new DynamicObject[]{scheme}, OperateOption.create());
            this.getView().invokeOperation("refresh");
        }
    }
}
