package kd.bos.XDdemo;

import java.util.EventObject;

import org.apache.commons.lang3.StringUtils;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;

/**
 * 消毒步骤表单插件
 * 
 * @author lxlic
 *
 */
public class XdSteptInfoBillPlugin extends AbstractBillPlugIn implements BeforeF7SelectListener {

	/**
	 * 注册监听
	 */
	@Override
	public void registerListener(EventObject e) {
		super.registerListener(e);

		// 监听消毒步骤的事件
		BasedataEdit xdStept = this.getView().getControl("rt00_fl_level_step");
		xdStept.addBeforeF7SelectListener(this);

	}

	@Override
	public void beforeF7Select(BeforeF7SelectEvent evt) {
		String fieldKey = evt.getProperty().getName();
		if (StringUtils.equals(fieldKey, "rt00_fl_level_step")) { // 选择步骤时
			// 获取等级分录选中行
			int index = this.getModel().getEntryCurrentRowIndex("rt00_fl_level_entity");
			// 获取选中行中的消毒等级
			DynamicObject obj = (DynamicObject) this.getModel().getValue("rt00_fl_level", index);
			QFilter qFilter = new QFilter("rt00_xd_level", QCP.equals, obj.getPkValue());
			ListShowParameter showParameter = (ListShowParameter) evt.getFormShowParameter();
			showParameter.getListFilterParameter().setFilter(qFilter);
		}
	}
}
