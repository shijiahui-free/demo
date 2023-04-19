package kd.bos.XDdemo;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aliyun.odps.utils.StringUtils;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.ClientProperties;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ClickListener;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.control.events.ItemClickListener;
import kd.bos.form.control.events.RowClickEvent;
import kd.bos.form.control.events.RowClickEventListener;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.DateRangeEdit;
import kd.bos.form.field.MulBasedataEdit;
import kd.bos.form.field.UserEdit;
import kd.bos.form.field.events.AfterF7SelectListener;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.org.OrgServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.form.control.EntryGrid;

public class DisinfectProgrammeforPlugin extends AbstractFormPlugin
		implements BeforeF7SelectListener, RowClickEventListener {

	@Override
	public void afterCreateNewData(EventObject e) {
		super.afterCreateNewData(e);

		// 获取消毒等级所有数据
		// QFilter filterdel = new QFilter("number", QFilter.equals, "001");
		// DeleteServiceHelper.delete("paul_disinfect_level", new QFilter[] { filterdel
		// });
		QFilter filterLevel = new QFilter("number", QFilter.not_equals, "");
		DynamicObjectCollection policycolLevel = QueryServiceHelper.query("paul_disinfect_level", "id,number,name",
				new QFilter[] { filterLevel }, "");
		for (int i = 0; i < policycolLevel.size(); i++) {
			String id = policycolLevel.get(i).getString("id");
			this.getModel().setValue("disinfect_level", id, i);
		}
	}

	@Override
	public void entryRowClick(RowClickEvent evt) {
		Control source = (Control) evt.getSource();
		if (StringUtils.equals("level_entryentity", source.getKey())) {
			EntryGrid billentry = getControl("level_entryentity");
			int[] selectRows = billentry.getSelectRows();
			for (int i : selectRows) {
				DynamicObject attrValue = (DynamicObject) this.getModel().getValue("disinfect_level", i);
				String masterid = attrValue.getString("masterid");
				QFilter filterLevel = new QFilter("level", QFilter.equals, masterid);
				DynamicObjectCollection policycolLevel = QueryServiceHelper.query("paul_disinfect_step",
						"id,number,level", new QFilter[] { filterLevel }, "");
				for (int k = 0; k < policycolLevel.size(); k++) {
					String id = policycolLevel.get(k).getString("id");
					this.getModel().setValue("disinfect_step", id);
				}
			}
		}
	}

	@Override
	public void registerListener(EventObject event) {
		super.registerListener(event);
		// 单据体行点击
		EntryGrid entryGrid = this.getView().getControl("level_entryentity");
		entryGrid.addRowClickListener(this);
	}

	@Override
	public void itemClick(ItemClickEvent evt) {
		super.itemClick(evt);
		String key = evt.getItemKey();
		// 提交
		if ("bar_submit".equals(key)) {
			updateQjTs("0"); // 
			updateQjTs("1"); //
		}

	}

	private void updateQjTs(String jq_type) {
		long userId = UserServiceHelper.getCurrentUserId();
		List<QFilter> searchFilterList = new ArrayList<>();
		searchFilterList.add(new QFilter("userfield.id", QCP.equals, userId));
		searchFilterList.add(new QFilter("type", QCP.equals, jq_type));

		// DynamicObject dot = QueryServiceHelper.queryOne("rt00_sh001",
		// "jq_staff.id,jq_type,jq_days", searchFilterList.toArray(new QFilter [] {}));
		DynamicObject[] dot = BusinessDataServiceHelper.load("paul_vacation_infor", "userfield.id,type,surplusday",
				searchFilterList.toArray(new QFilter[] {}));
		int jq_days = (int) this.getModel().getValue("year_remain");
		if (jq_type.equals("1")) {
			jq_days = (int) this.getModel().getValue("tx_remain");
		}

		dot[0].set("surplusday", jq_days);

		SaveServiceHelper.update(dot);
	}

	@Override
	public void beforeF7Select(BeforeF7SelectEvent e) {
		String key = e.getProperty().getName();
		if ("disinfect_step".equals(key)) {
			String abc="sss";
			System.out.println(abc);
		}

	}



	private void disVailbe(boolean flag) {
		this.setVisible(flag);
		this.setDisenable(flag);
	}

	private void setVisible(boolean flag) {
		// “拥有”一行不可见
		this.getView().setVisible(flag, "vectorap");
		this.getView().setVisible(flag, "labelap1");
		this.getView().setVisible(flag, "year_total");
		this.getView().setVisible(flag, "tx_total");

		// “余额”一行不可见
		this.getView().setVisible(flag, "vectorap2");
		this.getView().setVisible(flag, "labelap9");
		this.getView().setVisible(flag, "year_remain");
		this.getView().setVisible(flag, "tx_remain");
	}

	private void setDisenable(boolean flag) {
		/*
		 * //锁定“请假时间” this.getView().setEnable(flag,"leavedate"); //锁定“请假说明”
		 * this.getView().setEnable(flag,"leave_reason");
		 */
	}

	@Override
	public void propertyChanged(PropertyChangedArgs e) {
		super.propertyChanged(e);
		//Object orgId=e.getChangeSet()[0].getNewValue();
		String name = e.getProperty().getName();
		if(StringUtils.equals("orgfield", name)) {
			String orgfield=this.getModel().getValue("orgfield").toString();
			QFilter filterLevel = new QFilter("orgfield", QFilter.equals, orgfield).and(new QFilter("version_status", QFilter.equals, "F"));
			DynamicObjectCollection policycolLevel = QueryServiceHelper.query("paul_disinfect_programme", "id,version_status",
					new QFilter[] { filterLevel }, "");
			if(policycolLevel.size()<0) {
				this.getView().showMessage("两个方案同时分配给同一个组织哦！");
			}
			
		}
	}

	public static Date getTomorrow() {
		LocalDate localdate = LocalDate.now().plusDays(1);
		return Date.from(localdate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

}
