package com.dudu.mobile.datahandler;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Toast;

import com.dudu.mobile.activity.ContextUtil;
import com.dudu.mobile.activity.WebActivity;
import com.dudu.mobile.entity.ContactsEntity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * 联系人工具类
 */
public class ContactsUtil {

    public static void insertContacts(final ContactsEntity contactsEntity, final Handler handler) {

        if (contactsEntity != null) {

            if (TextUtils.isEmpty(contactsEntity.getName())) {
                Toast.makeText(ContextUtil.getInstance(), "姓名不能为空!", Toast.LENGTH_SHORT).show();
            }

            if (TextUtils.isEmpty(contactsEntity.getPhone())) {
                Toast.makeText(ContextUtil.getInstance(), "手机号码不能为空!"
                        , Toast.LENGTH_SHORT).show();
            }

            // 先要判断联系人存在不存在,只有不存在才会需要保存
            boolean isFalg = QueryContactByNamePhone(ContextUtil.getInstance()
                    , contactsEntity.getName(), contactsEntity.getPhone());

            if (isFalg) {
                Toast.makeText(ContextUtil.getInstance(), "手机中已经存在该联系人!"
                        , Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        // 插入一条空数据,获取到联系人id
                        ContentValues values = new ContentValues();
                        Uri rawContentUri = ContextUtil.getInstance().getContentResolver()
                                .insert(ContactsContract.RawContacts.CONTENT_URI, values);
                        long id = ContentUris.parseId(rawContentUri);

                        // 存姓名
                        if (!TextUtils.isEmpty(contactsEntity.getName())) {
                            HashMap<String, String> content = new HashMap<String, String>();
                            content.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME
                                    , contactsEntity.getName());
                            // 名字首字母
                            content.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME
                                    , contactsEntity.getName());
                            importHeaderInfoToData(ContextUtil.getInstance()
                                    , id, content, HEADINFO_TYPE_STRUCTUREDNAME);
                        }

                        // 存公司,职位
                        if (!(TextUtils.isEmpty(contactsEntity.getCompany())
                                && TextUtils.isEmpty(contactsEntity.getPosition()))) {
                            HashMap<String, String> content_company = new HashMap<String, String>();
                            if (!TextUtils.isEmpty(contactsEntity.getCompany())) {
                                content_company.put(ContactsContract.CommonDataKinds.Organization.COMPANY
                                        , contactsEntity.getCompany());
                            }
                            if (!TextUtils.isEmpty(contactsEntity.getPosition())) {
                                content_company.put(ContactsContract.CommonDataKinds.Organization.TITLE
                                        , contactsEntity.getPosition());
                            }
                            importHeaderInfoToData(ContextUtil.getInstance()
                                    , id, content_company, HEADINFO_TYPE_ORGANIZATION);
                        }

                        // 存手机号码
                        if (!TextUtils.isEmpty(contactsEntity.getPhone())) {
                            // 支持多个手机号码,以逗号隔开
                            String[] phones = contactsEntity.getPhone().split(",");
                            if (phones.length > 0) {
                                for (String phone : phones) {
                                    importCommonBodyToData(ContextUtil.getInstance(), id, BODYINFO_TYPE_PHONE
                                            , phone
                                            , ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, "label");
                                }
                            }
                        }

                        // 存座机号码
                        if (!TextUtils.isEmpty(contactsEntity.getTel())) {
                            // 支持多个座机号码,以逗号隔开
                            String[] tels = contactsEntity.getTel().split(",");
                            if (tels.length > 0) {
                                for (String tel : tels) {
                                    importCommonBodyToData(ContextUtil.getInstance(), id, BODYINFO_TYPE_PHONE
                                            , tel
                                            , ContactsContract.CommonDataKinds.Phone.TYPE_HOME, "label");
                                }
                            }
                        }

                        // 存电子邮件
                        if (!TextUtils.isEmpty(contactsEntity.getEmail())) {
                            importCommonBodyToData(ContextUtil.getInstance(), id, BODYINFO_TYPE_EMAIL
                                    , contactsEntity.getEmail()
                                    , ContactsContract.CommonDataKinds.Email.TYPE_WORK, "label");
                        }

                        //家庭地址
                        if (!TextUtils.isEmpty(contactsEntity.getAddress())) {
                            importCommonBodyToData(ContextUtil.getInstance(), id, BODYINFO_TYPE_STRUCTUREDPOSTAL
                                    , contactsEntity.getAddress()
                                    , ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME, "label");
                        }

                        //公司地址
                        if (!TextUtils.isEmpty(contactsEntity.getCompanyaddress())) {
                            importCommonBodyToData(ContextUtil.getInstance(), id, BODYINFO_TYPE_STRUCTUREDPOSTAL
                                    , contactsEntity.getCompanyaddress()
                                    , ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK, "label");
                        }

                        //IM(暂时固定是QQ号码)
                        if (!TextUtils.isEmpty(contactsEntity.getIm())) {
                            importIMToData(ContextUtil.getInstance(), id
                                    , ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ
                                    , contactsEntity.getIm()
                                    , ContactsContract.CommonDataKinds.Im.TYPE_WORK, "label");
                        }

                        // 下载头像
                        if (!TextUtils.isEmpty(contactsEntity.getHeadImage())) {
                            URL imageURl = new URL(contactsEntity.getHeadImage());
                            URLConnection con = imageURl.openConnection();
                            con.connect();
                            InputStream in = con.getInputStream();
                            final Bitmap bitmap = BitmapFactory.decodeStream(in);
                            // 插入头像
                            importPhotoToData(ContextUtil.getInstance(), id, bitmap);
                        }

//                        handler.sendEmptyMessage(10);
                        handler.sendEmptyMessage(WebActivity.MSG_TOAST);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }

    }

    public static final int INFO_TYPE_NONE = -1;
    public static final int HEADINFO_TYPE_STRUCTUREDNAME = INFO_TYPE_NONE + 1;
    public static final int HEADINFO_TYPE_ORGANIZATION = INFO_TYPE_NONE + 2;
    public static final int BODYINFO_TYPE_PHONE = INFO_TYPE_NONE + 3;
    public static final int BODYINFO_TYPE_EMAIL = INFO_TYPE_NONE + 4;
    public static final int BODYINFO_TYPE_NICKNAME = INFO_TYPE_NONE + 5;
    public static final int BODYINFO_TYPE_WEBSITE = INFO_TYPE_NONE + 6;
    public static final int BODYINFO_TYPE_EVENT = INFO_TYPE_NONE + 7;
    public static final int BODYINFO_TYPE_RELATION = INFO_TYPE_NONE + 8;
    public static final int BODYINFO_TYPE_SIPADDRESS = INFO_TYPE_NONE + 9;
    public static final int BODYINFO_TYPE_STRUCTUREDPOSTAL = INFO_TYPE_NONE + 10;
    public static final int BODYINFO_TYPE_NOTE = INFO_TYPE_NONE + 11;

    /**
     * Header信息包含StructuredName和Organization两个大类信息，这两个大类信息均只占据Data表中一行
     *
     * @param context        客户内容解析器
     * @param rawContactid   待插入的联系人id
     * @param content        待插入的StructuredName数据内容,需客户提供
     * @param HeaderInfoType 待插入的大类信息类型：1 ： StructuredName ， 2：Organization
     *                       a)插入StructuredName,则对于Data表中的数据列状况如下(data1列中存的是DISPLAY_NAME，依次类推)：
     *                       1：DISPLAY_NAME  2：GIVEN_NAME  3：FAMILY_NAME
     *                       4：PREFIX        5：MIDDLE_NAME 6：SUFFIX
     *                       7：PHONETIC_GIVEN_NAME  8：PHONETIC_MIDDLE_NAME  9：PHONETIC_FAMILY_NAME
     *                       b)插入Organization，则对于Data表中数据列的状况如下：
     *                       1：COMPANY      2: TYPE          3:LABEL
     *                       4:TITLE         5:DEPARTMENT     6:JOB_DESCRIPTION
     *                       7:SYMBOL        8:PHONETIC_NAME  9:OFFIC_LOCATION
     */
    public static void importHeaderInfoToData(Context context, long rawContactid,
                                              HashMap<String, String> content, int HeaderInfoType) {
        if (context == null || rawContactid < 0 || content == null) {
            return;
        }
        ContentValues value = new ContentValues();
        value.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactid);
        switch (HeaderInfoType) {
            case HEADINFO_TYPE_STRUCTUREDNAME:
                value.put(ContactsContract.Data.MIMETYPE
                        , ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                break;
            case HEADINFO_TYPE_ORGANIZATION:
                value.put(ContactsContract.Contacts.Data.MIMETYPE
                        , ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
                break;
            default:
                break;
        }
        /**
         * 将content中非空数据项导入CV中
         */
        for (int i = 1; i < 10; i++) {
            String keyName = "data" + i;
            if (content.containsKey(keyName)) {
                value.put(keyName, content.get(keyName));
            }
        }
        ContentResolver cr = context.getContentResolver();
        if (cr != null) {
            cr.insert(android.provider.ContactsContract.Data.CONTENT_URI, value);
        }
        value.clear();
    }

    /**
     * CommonBod部分包括电话、电子邮件、昵称、网站、联系事件、关系、sip地址、地址、注释
     * 在Data表中，这八个大类信息存储有着共同特点。
     * data1 ： 有效信息，如电话号码、邮件地址、即时通讯号等，就是事件用户输入的数据内容。
     * data2 ： 信息类型，如Phone大类包含很多小类，如移动、家庭、工作等，其它大类也是如此。
     * data3 ： 标签，当信息类型时用户自定义时，该项纪录自定义的信息类型，如用户自定义一个标签：亲人号码。
     * 把对这八类信息的插入组合到一个方法中，
     */
    public static void importCommonBodyToData(Context context, long rawContactid, int bodytype
            , String content, int type, String label) {
        if (context == null || rawContactid < 0 || bodytype < 0 || content == null
                || type < 0 || label == null) {
            return;
        }
        ContentValues value = new ContentValues();
        switch (bodytype) {
            case BODYINFO_TYPE_PHONE:
                value.put(ContactsContract.Data.MIMETYPE
                        , ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                break;
            case BODYINFO_TYPE_EMAIL:
                value.put(ContactsContract.Data.MIMETYPE
                        , ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                break;
            case BODYINFO_TYPE_NICKNAME:
                value.put(ContactsContract.Data.MIMETYPE
                        , ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
                break;
            case BODYINFO_TYPE_WEBSITE:
                value.put(ContactsContract.Data.MIMETYPE
                        , ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
                break;
            case BODYINFO_TYPE_EVENT:
                value.put(ContactsContract.Data.MIMETYPE
                        , ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE);
                break;
            case BODYINFO_TYPE_RELATION:
                value.put(ContactsContract.Data.MIMETYPE
                        , ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE);
                break;
            case BODYINFO_TYPE_SIPADDRESS:
                value.put(ContactsContract.Data.MIMETYPE
                        , ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE);
                break;
            case BODYINFO_TYPE_STRUCTUREDPOSTAL:
                value.put(ContactsContract.Data.MIMETYPE
                        , ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
                break;
            case BODYINFO_TYPE_NOTE:
                value.put(ContactsContract.Data.MIMETYPE
                        , ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE);
                break;
            default:
                break;
        }
        value.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactid);

        value.put(ContactsContract.Data.DATA1, content);
        //对于注释类型信息，无有效type值
        if (bodytype != BODYINFO_TYPE_NOTE) {
            value.put(ContactsContract.Data.DATA2, type);
        }
        //标签仅对于自定义的信息类型有效
        if (type == ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM
                && bodytype != BODYINFO_TYPE_NOTE) {
            value.put(ContactsContract.Data.DATA3, label);
        }
        ContentResolver cr = context.getContentResolver();
        if (cr != null) {
            cr.insert(android.provider.ContactsContract.Data.CONTENT_URI, value);
        }

        value.clear();
    }


    /**
     * 插入头像
     *
     * @param context
     * @param rawContactid
     * @param sourceBitmap
     */
    public static void importPhotoToData(Context context, long rawContactid, Bitmap sourceBitmap) {
        if (context == null || rawContactid < 0) {
            return;
        }
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        byte[] avatar = os.toByteArray();
        ContentValues value = new ContentValues();
        value.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactid);
        value.put(ContactsContract.Data.MIMETYPE
                , ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        value.put(ContactsContract.CommonDataKinds.Photo.PHOTO, avatar);

        ContentResolver cr = context.getContentResolver();
        if (cr != null) {
            cr.insert(ContactsContract.Data.CONTENT_URI, value);
        }
        value.clear();
    }

    /**
     * 插入即时通讯
     *
     * @param context
     * @param rawContactid
     * @param protocol
     * @param content
     * @param type
     * @param label
     */
    public static void importIMToData(Context context, long rawContactid, int protocol
            , String content, int type, String label) {
        if (context == null || rawContactid < 0 || protocol < 0 || content == null
                || type < 0 || label == null) {
            return;
        }
        ContentValues value = new ContentValues();
        value.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactid);
        value.put(ContactsContract.Data.MIMETYPE
                , ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);
        value.put(ContactsContract.CommonDataKinds.Im.PROTOCOL, protocol);
        value.put(ContactsContract.CommonDataKinds.Im.DATA, content);
        value.put(ContactsContract.CommonDataKinds.Im.TYPE, type);
        if (type == ContactsContract.CommonDataKinds.Im.TYPE_CUSTOM) {
            value.put(ContactsContract.CommonDataKinds.Im.LABEL, label);
        }
        ContentResolver cr = context.getContentResolver();
        if (cr != null) {
            cr.insert(android.provider.ContactsContract.Data.CONTENT_URI, value);
        }
        value.clear();
    }

    /**
     * 根据姓名和手机号码,查询联系人是否存在(传入的值不能为空)
     *
     * @param context
     * @param name
     * @param phone
     * @return true 存在 false 不存在
     */
    public static boolean QueryContactByNamePhone(Context context, String name, String phone) {
        boolean isFalg = false;

        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI
                , new String[]{ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER}
                , ContactsContract.Contacts.DISPLAY_NAME + "=?"
                , new String[]{name}, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getCount() > 0) {
                    int isHas = Integer.parseInt(cursor.getString(cursor.
                            getColumnIndex(ContactsContract.Contacts.
                                    HAS_PHONE_NUMBER)));

                    String id = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.Contacts._ID));

                    if (isHas > 0) {
                        Cursor c = context.getContentResolver().query(ContactsContract.
                                        CommonDataKinds.Phone.CONTENT_URI
                                , new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +
                                        " = " + id, null, null);
                        if (c != null) {
                            while (c.moveToNext()) {
                                String number = c.getString(c.getColumnIndex(ContactsContract.
                                        CommonDataKinds.Phone.NUMBER));
                                String[] phones = phone.split(",");
                                for (String p : phones) {
                                    if (!TextUtils.isEmpty(number) && number.equals(p)) {
                                        isFalg = true;
                                        break;
                                    }
                                }
                                if (isFalg) {
                                    break;
                                }
                            }
                            c.close();
                        }
                    }
                }
                cursor.close();
                if (isFalg) {
                    break;
                }
            }
        }

        return isFalg;
    }

}
