package kd.bos.XDdemo;


import java.util.*;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.BadgeInfo;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.datamodel.events.BeforePackageDataEvent;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeCreateListDataProviderArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.mvc.list.ListDataProvider;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.workflow.api.BizProcessStatus;
import org.apache.commons.lang3.StringUtils;

public class XdApplyListPlugin extends AbstractListPlugin {

    /**
     * (non-Javadoc)
     *
     * @param args
     * @Description 列表界面创建列表数据前查询工作流当前处理人写入当前列表单据
     */
    @Override
    public void beforeCreateListDataProvider(BeforeCreateListDataProviderArgs args) {
        super.beforeCreateListDataProvider(args);
        args.setListDataProvider(new ListDataProvider() {
            @Override
            public DynamicObjectCollection getData(int start, int limit) {  //组装单据对应的当前处理人
                DynamicObjectCollection dynObjs = super.getData(start, limit);
                List<String> ids = new ArrayList<>();
                dynObjs.forEach((v) -> {
                    ids.add(String.valueOf(v.getPkValue()));
                });
                if (ids.size() > 0) {
                    setNextAuditor(dynObjs, ids);
                }
                return dynObjs;
            }
        });
    }

    /**
     * 设置当前处理人
     */
    private void setNextAuditor(DynamicObjectCollection collection, List<String> ids) {
        Map<String, String> nextAuditor = getNextAuditor(ids);
        collection.forEach((v) -> {
            String id = String.valueOf(v.getPkValue());
            v.set("rt00_chuliren", nextAuditor.get(id) == null ? "" : nextAuditor.get(id));
        });
    }

    /**
     * 描述：根据单据的id获取单据当前的工作流节点当前处理人信息
     */
    public static Map<String, String> getNextAuditor(List<String> pkIds) {
        Map<String, String> nodeMap = new HashMap<>(10);
        String[] ids = new String[pkIds.size()];
        pkIds.toArray(ids);
        Map<String, List<BizProcessStatus>> allPro = WorkflowServiceHelper.getBizProcessStatus(ids);
        Iterator<Map.Entry<String, List<BizProcessStatus>>> var5 = allPro.entrySet().iterator();

        while (var5.hasNext()) {
            String pkid = var5.next().getKey();
            List<BizProcessStatus> node = allPro.get(pkid);
            node.forEach((e) -> {
                String nodeStr = e.getCurrentNodeName();
                String auditor = e.getParticipantName();
                if (auditor != null && !"".equals(auditor.trim())) {
                    nodeStr = auditor;
                }
                nodeMap.put(pkid, nodeStr);
            });
        }
        return nodeMap;
    }

    @Override
    public void setFilter(SetFilterEvent e) {
        // TODO Auto-generated method stub
        super.setFilter(e);
        // 获取用户id
        long userId = UserServiceHelper.getCurrentUserId();
        QFilter q = new QFilter("rt00_user_apply", QCP.equals, userId);
        QFilter qFilter = new QFilter("id", QCP.equals, userId);
        DynamicObject query = QueryServiceHelper.queryOne("bos_user", "entryentity.isincharge,entryentity.dpt", new QFilter[]{qFilter});
        Boolean object = (Boolean) query.get("entryentity.isincharge");
        Object dept = query.get("entryentity.dpt");
        if (object) {
            QFilter q2 = new QFilter("rt00_entry_cj", QCP.equals, dept);
            e.getQFilters().add(q2);
        } else {
            e.getQFilters().add(q);
        }
    }

    /**
     * 功能描述:给开始消毒按钮添加徽标
     */
    @Override
    public void beforePackageData(BeforePackageDataEvent e) {
        super.beforePackageData(e);
        // 获取用户id
        long userId = UserServiceHelper.getCurrentUserId();
        QFilter q = new QFilter("rt00_user_apply", QCP.equals, userId);
        QFilter qFilter = new QFilter("id", QCP.equals, userId);
        DynamicObject query = QueryServiceHelper.queryOne("bos_user", "entryentity.isincharge,entryentity.dpt", new QFilter[]{qFilter});
        Boolean isincharge = (Boolean) query.get("entryentity.isincharge");
        Object dept = query.get("entryentity.dpt");
        if (isincharge) {
            QFilter q2 = new QFilter("rt00_entry_cj", QCP.equals, dept);
            Toolbar toolbar = this.getView().getControl("toolbarap");
            QFilter q3 = new QFilter("billstatus", QCP.equals, "D");
            DynamicObjectCollection dynamicObjects = QueryServiceHelper.query("rt00_xd_apply", "id", new QFilter[]{q3,q2});
            BadgeInfo info = new BadgeInfo();
            info.setCount(dynamicObjects.size());
            toolbar.setBadgeInfo("rt00_startdisinfect", info);
        }

    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 注册监听事件
        this.addClickListeners("toolbarap");
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String key = evt.getItemKey();
        // 单据列表工具栏："开始消毒"按钮
        if ("rt00_startdisinfect".equals(key)) {
            ListSelectedRowCollection selectRows = this.getSelectedRows();
            if (selectRows.size() != 1) {
                this.getView().showMessage("请选中一条数据！！");
                evt.setCancel(true);
                return;
            }
            ListSelectedRow selectRow = selectRows.get(0);
            DynamicObject applyObject = BusinessDataServiceHelper.loadSingle(selectRow.getPrimaryKeyValue(), "rt00_xd_apply");
            // 2.选中已审核的申请单时，按钮才可点击，否则按钮不可用；
            if (!"D".equals(applyObject.get("billstatus"))) {
                evt.setCancel(true);
                return;
            }
            // 3.非“申请进入时间”当天操作提示“申请日期当天才能开始消毒”
            Date entryDate = (Date) applyObject.get("rt00_entry_date");
            long deffDays = DateUtil.diffDate("yyyy-MM-dd", entryDate, new Date());
           if (deffDays != 0) {
               this.getView().showMessage("申请日期当天才能开始消毒！！");
                evt.setCancel(true);
                return;
           }
            // 非“申请进入车间”的负责人操作提示“你不是申请进入车间的负责人，没有权限进行消毒！”
            long userId = UserServiceHelper.getCurrentUserId();
            List<Long> userIds = new ArrayList<>();
            userIds.add(userId);
            List<Map<String, Object>> userInfo = UserServiceHelper.getPosition(userIds);
            if (!userInfo.isEmpty()) {
                List<Map<String,Object>> positions = (List<Map<String,Object>>) userInfo.get(0).get("entryentity");
                boolean isIncharge = (boolean)positions.get(0).get("isincharge");
                if (!isIncharge){
                    this.getView().showMessage("你不是申请进入车间的负责人，没有权限进行消毒！");
                    evt.setCancel(true);
                    return;
                }
            }


            // 1.需要根据申请进入车间匹配到消毒方案。
            DynamicObject dynamicObject = (DynamicObject) applyObject.get("rt00_entry_cj");
            long cjId = (long) dynamicObject.getPkValue();
            // 查询消毒方案
            List<QFilter> searchFilterList = new ArrayList<>();
            searchFilterList.add(new QFilter("rt00_version_status", "=", "B")); // A: 历史版本 B:最新版本
            searchFilterList.add(new QFilter("orgentity.fk_rt00_orgfield", "=", cjId));
            DynamicObject dot = BusinessDataServiceHelper.loadSingle("rt00_xd_scheme",
                    "rt00_version_status,rt00_fl_org_entity.orgentity", searchFilterList.toArray(new QFilter[]{}));
            // 2.如有方案，则生成消毒记录单，分录是要进行的消毒步骤，是根据对应消毒方案的各消毒等级中的消毒步骤顺序生成的。具体取值规则请查看消毒记录单字段说明部分。
            if (dot == null) {
                this.getView().showMessage("进入车间消毒方案不存在，请联系管理员！！");
            }else{
                applyObject.set("rt00_xdfa",dot);
                SaveServiceHelper.update(applyObject);
            }

        }
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String key = evt.getItemKey();
        // 提交
        if ("rt00_startdisinfect".equals(key)) {

        }

    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs e) {
        super.afterDoOperation(e);

        if (StringUtils.equals("rt00_startdisinfect", e.getOperateKey())) {
            this.getView().close(); // 关闭页面
        }
    }
}
