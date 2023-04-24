package kd.bos.XDdemo;

import com.aliyun.odps.utils.StringUtils;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.BillList;
import kd.bos.list.IListView;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

/**
 * 消毒方案
 *
 * @author lxlic
 */
public class XdSchemeListPlugin extends AbstractFormPlugin {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 注册监听事件
        this.addItemClickListeners("toolbarap"); // 监听整个工具栏，"tbmain"工具栏标识
    }


    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        // TODO Auto-generated method stub
        super.afterDoOperation(afterDoOperationEventArgs);
        //通过此种方法可以获取选中行的数据
        String operationKey = afterDoOperationEventArgs.getOperateKey();
        IListView listview = (IListView) this.getView();
        ListSelectedRowCollection selectedRows = listview.getSelectedRows();
        Object[] primaryKeyValues = selectedRows.getPrimaryKeyValues();
//        if (selectedRows.size() > 1) {//选中一行
        if (false) {//选中一行
            Object primaryKeyValue = primaryKeyValues[0];
            DynamicObject rt00_xd_scheme = BusinessDataServiceHelper.loadSingle(primaryKeyValue, "rt00_xd_scheme");
            DynamicObjectCollection orgs = (DynamicObjectCollection) rt00_xd_scheme.get("rt00_fl_org_entity");

            String version_status = rt00_xd_scheme.getString("rt00_version_status");

            if ((version_status.equals("A") || orgs.size() >= 1) && operationKey.equals("assginorg")) {
                this.getView().showMessage("不可分配组织!");

            } else {
                switch (operationKey) {
                    case "assginorg":
                        BillList billList = this.getControl("billlistap");
                        ListSelectedRowCollection selectedRow = billList.getSelectedRows();
                        Set<Long> schemeId = new HashSet<>(selectedRow.size());
                        for (ListSelectedRow listSelectedRow : selectedRow) {
                            if (listSelectedRow != null && listSelectedRow.getPrimaryKeyValue() != null) {
                                Long entryId = (Long) listSelectedRow.getPrimaryKeyValue();
                                schemeId.add(entryId);
                            }
                        }

                        FormShowParameter fsp = new FormShowParameter();
                        fsp.setFormId("rt00_assingorg1");
                        fsp.getOpenStyle().setShowType(ShowType.Modal);
                        fsp.setCustomParam("entryid", schemeId);
                        fsp.setCloseCallBack(new CloseCallBack(this, "assignorg"));
                        this.getView().showForm(fsp);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (StringUtils.equals(closedCallBackEvent.getActionId(), "assignorg") && null != closedCallBackEvent.getReturnData()) {
//            List<Map<String, Object>> returnData = (List<Map<String, Object>>) closedCallBackEvent.getReturnData();
            DynamicObjectCollection returnData = (DynamicObjectCollection) closedCallBackEvent.getReturnData();
            int count = returnData.size();
            IListView listview = (IListView) this.getView();
            ListSelectedRowCollection selectedRows = listview.getSelectedRows();
            Long id = (Long) selectedRows.get(0).getPrimaryKeyValue();
            DynamicObject scheme = BusinessDataServiceHelper.loadSingle(id, "rt00_xd_scheme");


            DynamicObjectCollection orgEntity = (DynamicObjectCollection) scheme.get("rt00_fl_org_entity");
            orgEntity.clear();
            for (DynamicObject obj : returnData) {
                DynamicObject org = orgEntity.addNew();
                org.set("rt00_orgfield", obj.get("rt00_orgfield"));
//                orgEntity.add(org);
            }
            scheme.set("rt00_fl_org_entity", orgEntity);
            OperationResult operationResult = OperationServiceHelper.executeOperate("save", "rt00_xd_scheme", new DynamicObject[]{scheme}, OperateOption.create());
            this.getView().invokeOperation("refresh");
        }

    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        //点击消毒方案列表上面的分配组织按钮

        String key = evt.getItemKey();
        if ("rt00_bar_asign_org".equals(key)) {
            IListView listview = (IListView) this.getView();
            ListSelectedRowCollection selectedRows = listview.getSelectedRows();

            if (selectedRows.size() > 0) {
                // 最新版本的消毒方案才能分配组织
                Long id = (Long) selectedRows.get(0).getPrimaryKeyValue();
                DynamicObject scheme = BusinessDataServiceHelper.loadSingle(id, "rt00_xd_scheme");
                if ("A".equals(scheme.getString("rt00_version_status"))) {
                    this.getView().showMessage("最新版本的消毒方案才能分配组织");
                    return;
                }
                Set<Long> schemeIds = new HashSet<>(selectedRows.size());
                for (ListSelectedRow listSelectedRow : selectedRows) {
                    if (listSelectedRow != null && listSelectedRow.getPrimaryKeyValue() != null) {
                        Long entryId = (Long) listSelectedRow.getPrimaryKeyValue();
                        schemeIds.add(entryId);
                    }
                }
                FormShowParameter fsp = new FormShowParameter();
                fsp.setFormId("rt00_assingorg1");
                fsp.getOpenStyle().setShowType(ShowType.Modal);
                fsp.setCustomParam("entryid", schemeIds);
                fsp.setCloseCallBack(new CloseCallBack(this, "assignorg"));
                this.getView().showForm(fsp);
            }

        }
    }
}
