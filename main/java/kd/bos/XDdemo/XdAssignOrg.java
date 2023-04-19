package kd.bos.XDdemo;

import java.util.*;

import com.aliyun.odps.utils.StringUtils;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.form.FormShowParameter;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.BillList;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;

public class XdAssignOrg extends AbstractFormPlugin {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //this.addItemClickListeners("btnok");
        Button button = this.getView().getControl("btnok");
        button.addClickListener(this);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
    	//查询到方案分配的组织
        // TODO Auto-generated method stub
        super.afterCreateNewData(e);
        IDataModel model = this.getModel();
        List<QFilter> searchFilterList = new ArrayList<>();
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        List applyBillEntryId = formShowParameter.getCustomParam("entryid");
        searchFilterList.add(new QFilter("id", QCP.in, applyBillEntryId));
        DynamicObject[] load = BusinessDataServiceHelper.load("rt00_xd_scheme", "rt00_fl_org_entity.rt00_orgfield", searchFilterList.toArray(new QFilter[]{}));
        for (int i = 0; i < load.length; i++) {
            DynamicObject bill = load[i];
            DynamicObjectCollection entry = (DynamicObjectCollection) bill.get("rt00_fl_org_entity");
            for (int j = 0; j < entry.size(); j++) {
                DynamicObject org = entry.get(j);
                model.setValue("rt00_orgfield", org.get("rt00_orgfield"), j);
            }

        }
    }

    @Override
    public void click(EventObject evt) {
        // TODO Auto-generated method stub
        super.click(evt);
        Control source = (Control) evt.getSource();
        if (StringUtils.equals("btnok", source.getKey())) {

            FormShowParameter formShowParameter = this.getView().getFormShowParameter();
            List applyBillEntryId = formShowParameter.getCustomParam("entryid");

            List<Map<String, Object>> datas = new ArrayList<>();
            DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("rt00_entryentitya");
            for (DynamicObject prams : entryEntity) {
                DynamicObject dynamicObject = prams.getDynamicObject("rt00_orgfield");
//			Object orgGroup =prams.get("rt00_orgfield.name");
                //Object orgGroup = prams.get("rt00_basedatapropfield");
                Map<String, Object> data = new HashMap<>();
                data.put("orgnumData", dynamicObject);
                data.put("entryid", applyBillEntryId);
//			data.put("orggroup",orgGroup);
                datas.add(data);

                this.getView().returnDataToParent(entryEntity);
                this.getView().close();
            }
        }


    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        // TODO Auto-generated method stub
        super.itemClick(evt);
        String click = evt.getItemKey();
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        // TODO Auto-generated method stub
        super.beforeItemClick(evt);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        //Object orgId=e.getChangeSet()[0].getNewValue();
        String name = e.getProperty().getName();
        if (StringUtils.equals("orgfield", name)) {
            String orgfield = this.getModel().getValue("orgfield").toString();
            QFilter filterLevel = new QFilter("orgfield", QFilter.equals, orgfield).and(new QFilter("version_status", QFilter.equals, "F"));
            DynamicObjectCollection policycolLevel = QueryServiceHelper.query("paul_disinfect_programme", "id,version_status",
                    new QFilter[]{filterLevel}, "");
            if (policycolLevel.size() < 0) {
                this.getView().showMessage("两个方案同时分配给同一个组织哦！");
            }

        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        // TODO Auto-generated method stub
        super.afterDoOperation(afterDoOperationEventArgs);
    }

    @Override
    public void beforeClick(BeforeClickEvent evt) {
        // TODO Auto-generated method stub
        super.beforeClick(evt);
    }

}
