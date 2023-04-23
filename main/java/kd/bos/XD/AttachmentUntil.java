package kd.bos.XD;

import kd.bos.cache.CacheFactory;
import kd.bos.cache.TempFileCache;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.MainEntityType;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.IPageCache;
import kd.bos.servicehelper.AttachmentDto;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.MetadataServiceHelper;
import kd.bos.session.EncreptSessionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author zhangbo
 * @Description //TODO 附件工具类$
 * @Date $ $
 * @Param $
 * @return $
 */
public class AttachmentUntil {

    /**
     * 从页面缓存中获取附件，当附件上传未保存时使用
     */
    public static List<Map<String, Object>> getTempAttachments(IPageCache pageCache, String pageId) {
        String cacheJsonString = pageCache.get("TampAttCache" + pageId);
        // 所有附件面板的所有附件文件
        Map<String, Object> allAttachmentsInfo = null;
        if (!StringUtils.isEmpty(cacheJsonString)) {
            allAttachmentsInfo = SerializationUtils.fromJsonString(cacheJsonString, Map.class);
        }
        if (!(allAttachmentsInfo != null && allAttachmentsInfo.size() > 0)) {
            return null;
        }
        // 一个附件面板中的所有附件文件
        List<Map<String, Object>> attachments = null;
        for (String attachmentPanelKey : allAttachmentsInfo.keySet()) {
            attachments = (List<Map<String, Object>>) allAttachmentsInfo.get(attachmentPanelKey);
        }
        return attachments;
    }

    /**
     * 附件拷贝到目标单 附件存储在文件服务器，实体表中有记录
     *
     * @param targetBillNo 目标单标识
     * @param targetPkid   目标单pkid
     * @param attachKey    目标单附件面板标识
     * @param attachments  源附件信息集合
     */
    public static void uploadTargetAttachments(String targetBillNo, Object targetPkid, String attachKey, List<Map<String, Object>> attachments) {
        for (Map<String, Object> attachItem : attachments) {
            //获取附件文件的对象
            DynamicObject obj = AttachmentServiceHelper.getAttCreatorByUID((String) attachItem.get("uid"));
            //获取附件相关信息
            AttachmentDto attachmentDto = AttachmentServiceHelper.getAttachmentInfoByAttPk(obj.getPkValue());
            //获取文件的物理路径
            String resourcePath = attachmentDto.getResourcePath();
            //获取临时文件缓存
            TempFileCache cache = CacheFactory.getCommonCacheFactory().getTempFileCache();
            //将文件转换成输入流
            FileService fs = FileServiceFactory.getAttachmentFileService();
            InputStream inputStream = fs.getInputStream(resourcePath);
            //将文件流存入临时文件缓存（拷贝完成）（最后一个参数为缓存有效期，7200秒）
            String tempUrl = cache.saveAsUrl((String) attachItem.get("name"), inputStream, 7200);
            //获取文件的缓存路径
            tempUrl = EncreptSessionUtils.encryptSession(tempUrl);
            //获取域名前缀
            String address = RequestContext.get().getClientFullContextPath();
            if (!address.endsWith("/")) {
                address = address + "/";
            }
            //拼接url路径
            String tempUrl3 = address + tempUrl;
            //获取appId
            MainEntityType dataEntityType = MetadataServiceHelper.getDataEntityType(targetBillNo);
            String appId = dataEntityType.getAppId();
            //获取附件名称
            String name = (String) attachItem.get("name");
            //将文件缓存中的附件文件上传到正式文件服务器
            String path = AttachmentServiceHelper.saveTempToFileService(tempUrl3, appId, targetBillNo, targetPkid, name);
            //将新文件的物理路径存入map
            attachItem.put("url", path);
            //修改时间格式处理
            attachItem.put("lastModified", ((Timestamp)attachItem.get("lastModified")).getTime());
            //备注
            Object description = attachItem.get("description");
            attachItem.put("description", description);
        }
        //维护单据和附件的关系（非文件上传）
        AttachmentServiceHelper.upload(targetBillNo, targetPkid, attachKey, attachments);
    }

    /**
     * 附件拷贝到目标单 附件存储在缓存中
     *
     * @param targetBillNo 目标单标识
     * @param targetPkid   目标单pkid
     * @param attachKey    目标单附件面板标识
     * @param attachments  源附件信息集合
     */
    public static void uploadTargetTempAttachments(String targetBillNo, Object targetPkid, String attachKey, List<Map<String, Object>> attachments) {
        for (Map<String, Object> attachItem : attachments) {
            //获取url路径
            String tempUrl = attachItem.get("url").toString();
            //获取appId
            MainEntityType dataEntityType = MetadataServiceHelper.getDataEntityType(targetBillNo);
            String appId = dataEntityType.getAppId();
            //获取附件名称
            String name = (String) attachItem.get("name");
            //将文件缓存中的附件文件上传到正式文件服务器
            String path = AttachmentServiceHelper.saveTempToFileService(tempUrl, appId, targetBillNo, targetPkid, name);
            //将新文件的物理路径存入map
            attachItem.put("url", path);
            //修改时间格式处理
            Timestamp lastModified = new Timestamp((long) attachItem.get("lastModified"));
            attachItem.put("lastModified", lastModified.getTime());
            //备注
            Object description = attachItem.get("description");
            attachItem.put("description", description);
        }
        //维护单据和附件的关系（非文件上传）
        AttachmentServiceHelper.upload(targetBillNo, targetPkid, attachKey, attachments);
    }

    /**
     * 复制单个附件文件
     *
     * @param attachment 源附件文件
     * @return
     */
    public static Map<String, Object> copyAttachment(Map<String, Object> attachment) {
        TempFileCache tempFileCache = CacheFactory.getCommonCacheFactory().getTempFileCache();
        String tempUrl = String.valueOf(attachment.get("url"));
        String uid = String.valueOf(attachment.get("uid"));
        String fileName = String.valueOf(attachment.get("name"));
        InputStream inputStream = (InputStream) tempFileCache.getInputStream(tempUrl);
        // ------ 复制文件 ------
        Map<String, Object> newAttachment = new HashMap<String, Object>();
        // lastModified:时间戳
        long time = new Date().getTime();
        newAttachment.put("lastModified", time);
        // name:文件名(含文件格式)
        StringBuffer newNameBuffer = new StringBuffer(fileName);
        newNameBuffer.insert(fileName.lastIndexOf("."), "-新");
        newAttachment.put("name", newNameBuffer.toString());
        // size:文件大小
        try {
            newAttachment.put("size", inputStream.available());
        } catch (IOException e) {
            // ignore
        }
        newAttachment.put("status", "success");
        // type:文件类型
        newAttachment.put("type", String.valueOf(attachment.get("type")));
        // uid
        StringBuffer newUid = new StringBuffer("rc-upload-");
        newUid.append(time);
        newUid.append("-");
        String uidIndex = null;
        int index = uid.lastIndexOf("-");
        if (index > 0) {
            uidIndex = uid.substring(index + 1);
        }
        newUid.append(uidIndex);
        newAttachment.put("uid", newUid.toString());
        // url:附件在附件服务器上的位置
        StringBuffer newUrl = new StringBuffer(RequestContext.get().getClientFullContextPath());
        if (!newUrl.toString().endsWith("/")) {
            newUrl.append("/");
        }
        String tempNewUrl = tempFileCache.saveAsUrl(newNameBuffer.toString(), inputStream, 2 * 60 * 60);
        tempNewUrl = EncreptSessionUtils.encryptSession(tempNewUrl);
        newUrl.append(tempNewUrl);
        newAttachment.put("url", newUrl.toString());
        return newAttachment;
    }
}
