package kd.bos.XDdemo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kd.bos.algo.Algo;
import kd.bos.algo.DataSet;
import kd.bos.algo.DataType;
import kd.bos.algo.Row;
import kd.bos.algo.RowMeta;
import kd.bos.algo.RowMetaFactory;
import kd.bos.algo.input.CollectionInput;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.report.ReportColumn;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

public class XdReportListDataQuery {
	private List<String> FIELDS = new ArrayList<String>();

	private List<DataType> DATATYPES = new ArrayList<DataType>();

	private List<String> COLUMNTYPES = new ArrayList<String>();

	private String BILL_FREIGHT_PLAN = "rt00_xd_apply";

	public DataSet buildDataSet(String year) {

		FIELDS.add("序号");
		DATATYPES.add(DataType.StringType);
		COLUMNTYPES.add(ReportColumn.TYPE_TEXT);

		FIELDS.add("员工");
		DATATYPES.add(DataType.StringType);
		COLUMNTYPES.add(ReportColumn.TYPE_TEXT);

		for (int i = 1; i <= 12; i++) {
			FIELDS.add(i + "月");
			DATATYPES.add(DataType.IntegerType);
			COLUMNTYPES.add(ReportColumn.TYPE_DECIMAL);
		}

		List<QFilter> searchFilterList = new ArrayList<>();
		if (!year.equals("")) {
			searchFilterList.add(new QFilter("rt00_entry_date", QCP.like, year + "%"));
		}

		// 查询有多少员工
		ORM orm = ORM.create();
		DataSet creater = orm
				.queryDataSet(this.getClass().getName() + BILL_FREIGHT_PLAN, BILL_FREIGHT_PLAN, "rt00_user_apply.name",
						searchFilterList.toArray(new QFilter[] {}), null)
				.groupBy(new String[] { "rt00_user_apply.name" }).finish();

		// 创建一个空的DataSet
		Collection<Object[]> rows = new ArrayList<Object[]>();
		RowMeta rowMeta = RowMetaFactory.createRowMeta(FIELDS.toArray(new String[] {}),
				DATATYPES.toArray(new DataType[] {}));
		CollectionInput inputs = new CollectionInput(rowMeta, rows);
		DataSet resultDataSet = Algo.create(this.getClass().getName()).createDataSet(inputs);

		Integer[] createTotal = new Integer[12];
		for (int i = 1; i <= 12; i++) {
			createTotal[i - 1] = 0;
		}
		int snumber = 1;
		for (Row row : creater.copy()) {
			Object[] tempData = new Object[FIELDS.size()];
			tempData[0] = snumber;
			tempData[1] = row.getString(0);
			snumber++;

			for (int i = 1; i <= 12; i++) {

				List<QFilter> searchFilterList2 = new ArrayList<>();

				String month = year + "-" + StringUtils.leftPad(String.valueOf(i), 2, "0");

				if (!year.equals("")) {
					searchFilterList2.add(new QFilter("rt00_entry_date", QCP.like, month + "%"));
					searchFilterList2.add(new QFilter("rt00_user_apply.name", QCP.equals, row.getString(0)));
					searchFilterList2.add(new QFilter("billstatus", QCP.equals, "F"));
				}

				// 查询请假单数据
				DataSet createrSet = QueryServiceHelper
						.queryDataSet(this.getClass().getName() + BILL_FREIGHT_PLAN, BILL_FREIGHT_PLAN,
								"rt00_user_apply.name,rt00_entry_date,1 total",
								searchFilterList2.toArray(new QFilter[] {}), null)
						.groupBy(new String[] { "rt00_user_apply.name" }).sum("total").finish();

				for (Row row2 : createrSet.copy()) {
					tempData[i + 1] = row2.getInteger(1);
					createTotal[i - 1] = createTotal[i - 1] + row2.getInteger(1);
				}
			}

			rows.add(tempData);
		}

		// 加上合计行
		Object[] tempData = new Object[FIELDS.size()];
		tempData[0] = "合计";
		tempData[1] = "";
		for (int i = 1; i <= 12; i++) {
			tempData[i + 1] = createTotal[i - 1];
		}
		rows.add(tempData);

		return resultDataSet;
	}

	public List<String> getFields() {
		return FIELDS;
	}

	public List<DataType> getDataTypes() {
		return DATATYPES;
	}

	public List<String> getColumnTypes() {
		return COLUMNTYPES;
	}
}
