package kd.bos.JQ;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDException;
import kd.bos.message.api.MessageChannels;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.servicehelper.workflow.MessageCenterServiceHelper;
import kd.bos.workflow.engine.msg.info.MessageInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 定时任务
 */
public class LeaveTask extends AbstractTask {

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        //调休假
        QFilter qf1 = new QFilter("pvdo_combofield", QCP.equals, "B");
        long userId = UserServiceHelper.getCurrentUserId();
        //当前登录人
        QFilter qf2 = new QFilter("pvdo_userfield.id", QCP.equals, userId);
        //只查调休假余额
        DynamicObject single = BusinessDataServiceHelper.loadSingle("pvdo_holidaymsg",
                "pvdo_combofield,pvdo_year,pvdo_quarter,pvdo_userfield,pvdo_days,pvdo_orgfield",
                new QFilter[]{qf1.and(qf2)});
        if (single != null) {
            //员工项目
            String username = single.getString("pvdo_userfield.name");
            //调休假余额
            long pvdo_days = single.getLong("pvdo_days");

            MessageInfo msg = new MessageInfo();
            msg.setTitle("调休假期清零提醒");//消息的标题
            StringBuffer str = new StringBuffer("亲爱的").append(username).append("，你还有")
                    .append(pvdo_days).append("天调休假，请在本季度休完，否则到时会自动清零哦！");
            msg.setContent(str.toString());//消息内容

            List<Long> users = new ArrayList<>();
            users.add(Long.parseLong(String.valueOf(userId)));
            msg.setUserIds(users);
            msg.setTag("提醒");
            msg.setType(MessageInfo.TYPE_WARNING);
            msg.setEntityNumber("pvdo_holidaymsg");
            msg.setNotifyType(MessageChannels.EMAIL.getNumber());
            MessageCenterServiceHelper.sendMessage(msg);
        }
    }

}
