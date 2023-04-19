package kd.bos.XDdemo;

import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.entity.EntryType;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.datamodel.events.BizDataEventArgs;
import kd.bos.entity.datamodel.events.GetEntityTypeEventArgs;
import kd.bos.entity.property.IntegerProp;
import kd.bos.entity.property.TextProp;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDException;
import kd.bos.form.ClientActions;
import kd.bos.form.ClientProperties;
import kd.bos.form.IClientViewProxy;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.control.events.ClickListener;
import kd.bos.form.events.LoadCustomControlMetasArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.metadata.entity.commonfield.IntegerField;
import kd.bos.metadata.entity.commonfield.TextField;
import kd.bos.metadata.form.control.EntryAp;
import kd.bos.metadata.form.control.EntryFieldAp;

public class XdReportFormPlugin extends AbstractFormPlugin {
	private final static String KEY_ENTRYENTITY = "rt00_entryentity";
	
	@Override
	public void beforeBindData(EventObject e) {
		// TODO Auto-generated method stub
		super.beforeBindData(e);
		// 单据体添加的字段，注入到单据体表格的控件编程模型中
		EntryAp dynamicEntryAp = this.createDynamicEntryAp();
		EntryGrid entryGrid = this.getView().getControl(KEY_ENTRYENTITY);
		List<Control> fieldEdits = dynamicEntryAp.buildRuntimeControl().getItems();
		for (Control fieldEdit : fieldEdits) {
			fieldEdit.setView(this.getView());
			entryGrid.getItems().add(fieldEdit);
		}
	}

	@Override
	public void afterBindData(EventObject e) {
		// TODO Auto-generated method stub
		super.afterBindData(e);
		// 绑定数据后修改合计行的背景色和前景色
		EntryGrid entryGrid = this.getView().getControl(KEY_ENTRYENTITY);
		// entryGrid.setRowBackcolor("#ffd4aa", new int[] { entryGrid.getRuleCount() - 1
		// });

		IClientViewProxy clientViewProxy = (IClientViewProxy) entryGrid.getView().getService(IClientViewProxy.class);
		ClientActions.createRowStyleBuilder().setRows(new int[] { entryGrid.getRuleCount() - 1 })
				.setBackColor("#ffff00").setForeColor("#ff0000").buildStyle().build()
				.invokeControlMethod(clientViewProxy, entryGrid.getKey());

	}

	@Override
	public void loadCustomControlMetas(LoadCustomControlMetasArgs e) {
		// TODO Auto-generated method stub
		super.loadCustomControlMetas(e);

		// 动态创建单据体
		EntryAp entryAp = this.createDynamicEntryAp();

		// 动态添加单据体字段
		Map<String, Object> mapEntry = new HashMap<>();
		mapEntry.put(ClientProperties.Id, KEY_ENTRYENTITY);
		mapEntry.put(ClientProperties.Columns, entryAp.createControl().get(ClientProperties.Columns));
		e.getItems().add(mapEntry);
	}

	@Override
	public void registerListener(EventObject e) {
		// TODO Auto-generated method stub
		super.registerListener(e);

		Button ctl = this.getControl("rt00_btn_query");
		ctl.addClickListener(new ClickListener() {
			public void beforeClick(BeforeClickEvent evt) {

			}

			public void click(EventObject evt) {
				String year = getModel().getValue("rt00_txt_year").toString();
				if (year == null || year.equals("")) {
					return;
				}
				XdReportListDataQuery query = new XdReportListDataQuery();
				fillDataModel(query.buildDataSet(year));
			}
		});
	}

	private void fillDataModel(DataSet dataSet) {
		DynamicObjectCollection entryCol = this.getModel().getDataEntity(true)
				.getDynamicObjectCollection(KEY_ENTRYENTITY);
		entryCol.clear();
		DynamicObjectType type = entryCol.getDynamicObjectType();
		for (Row row : dataSet) {
			DynamicObject obj = new DynamicObject(type);
			obj.set("snumber", row.getString(0));
			obj.set("employee_id", row.getString(1));
			for (int i = 1; i <= 12; i++) {
				obj.set("month_" + i, row.getInteger(i + 1));
			}
			entryCol.add(obj);
		}

		this.getView().updateView();

	}

	@Override
	public void createNewData(BizDataEventArgs e) {
		this.registDynamicProps(this.getModel().getDataEntityType());
		DynamicObject dataEntity = new DynamicObject(this.getModel().getDataEntityType());
		e.setDataEntity(dataEntity);
	}

	/**
	 * 创建一个单据体表格，并向其中动态添加字段
	 */
	private EntryAp createDynamicEntryAp() {

		EntryAp entryAp = new EntryAp();
		entryAp.setKey("entryap");

		// 动态添加一个文本字段
		EntryFieldAp fieldAp = new EntryFieldAp();

		fieldAp.setId("snumber");
		fieldAp.setKey("snumber");
		fieldAp.setName(new LocaleString("序号"));
		fieldAp.setFireUpdEvt(true); // 即时触发值更新事件
		TextField field0 = new TextField();
		field0.setId("snumber");
		field0.setKey("snumber");
		fieldAp.setField(field0);
		fieldAp.setLock("submit,audit,new,edit"); // 控制锁定性
		entryAp.getItems().add(fieldAp);

		fieldAp = new EntryFieldAp();
		fieldAp.setId("employee_id");
		fieldAp.setKey("employee_id");
		fieldAp.setName(new LocaleString("员工"));
		fieldAp.setFireUpdEvt(true); // 即时触发值更新事件
		TextField field1 = new TextField();
		field1.setId("employee_id");
		field1.setKey("employee_id");
		fieldAp.setField(field1);
		fieldAp.setLock("submit,audit,new,edit"); // 控制锁定性
		entryAp.getItems().add(fieldAp);

		for (int i = 1; i <= 12; i++) {
			fieldAp = new EntryFieldAp();
			fieldAp.setId("month_" + i);
			fieldAp.setKey("month_" + i);
			fieldAp.setName(new LocaleString(i + "月"));
			fieldAp.setFireUpdEvt(true); // 即时触发值更新事件
			fieldAp.setLock("submit,audit,new,edit"); // 控制锁定性
			IntegerField ifield = new IntegerField();
			ifield.setId("month_" + i);
			ifield.setKey("month_" + i);
			fieldAp.setField(ifield);
			entryAp.getItems().add(fieldAp);
		}

		return entryAp;
	}

	/**
	 * 此事件在系统要用到表单主实体模型时触发
	 * 
	 * @param e
	 * @remark 插件修改原始主实体，注册自定义属性，返回新的主实体给系统
	 */
	@Override
	public void getEntityType(GetEntityTypeEventArgs e) {

		// 取原始的主实体
		MainEntityType oldMainType = e.getOriginalEntityType();
		// 复制主实体
		MainEntityType newMainType = null;
		try {
			newMainType = (MainEntityType) oldMainType.clone();
		} catch (CloneNotSupportedException exp) {
			throw new KDException(exp, new ErrorCode("LoadCustomControlMetasSample", exp.getMessage()));
		}

		// 为自定义的文本字段，向主实体注册文本属性
		this.registDynamicProps(newMainType);

		// 回传主实体给系统
		e.setNewEntityType(newMainType);
	}

	private void registDynamicProps(MainEntityType newMainType) {
		// 向单据体动态注册一个新的文本属性
		EntryType entryType = (EntryType) newMainType.getAllEntities().get(KEY_ENTRYENTITY);

		TextProp textProp1 = new TextProp();

		textProp1.setName("snumber"); // 标识
		textProp1.setDisplayName(new LocaleString("序号")); // 标题

		textProp1.setDbIgnore(true); // 此字段不需到物理表格取数
		textProp1.setAlias(""); // 物理字段名
		// 把新字段，注册到单据体
		entryType.registerSimpleProperty(textProp1);

		TextProp textProp2 = new TextProp();

		textProp2.setName("employee_id"); // 标识
		textProp2.setDisplayName(new LocaleString("员工")); // 标题

		textProp2.setDbIgnore(true); // 此字段不需到物理表格取数
		textProp2.setAlias(""); // 物理字段名
		// 把新字段，注册到单据体
		entryType.registerSimpleProperty(textProp2);

		for (int i = 1; i <= 12; i++) {
			IntegerProp intProp = new IntegerProp();
			// intProp.setScale(-8);
			intProp.setName("month_" + i);
			intProp.setDisplayName(new LocaleString(i + "月")); // 标题
			intProp.setDbIgnore(true); // 此字段不需到物理表格取数
			intProp.setAlias(""); // 物理字段名

			// 把新字段，注册到单据体
			entryType.registerSimpleProperty(intProp);
		}
	}
}
