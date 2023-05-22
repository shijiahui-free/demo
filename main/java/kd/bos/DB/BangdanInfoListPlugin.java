package kd.bos.DB;

import kd.bos.filter.SchemeFilterColumn;
import kd.bos.form.events.FilterContainerInitArgs;
import kd.bos.list.plugin.AbstractListPlugin;

/**
 * 地磅信息列表插件
 *
 * @author sjh
 * on 2023/5/18
 */
public class BangdanInfoListPlugin extends AbstractListPlugin {
    @Override
    public void filterContainerInit(FilterContainerInitArgs args) {
        super.filterContainerInit(args);

        /****** 方案过滤视图下的筛选条件处理 ******/
        // 方案过滤视图动态添加过滤字段——单据状态(billstatus)
        SchemeFilterColumn statusFilter = new SchemeFilterColumn("billstatus");
        args.addFilterColumn(statusFilter);
    }

}
