package com.example.av_sample;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

/**
 * Android 6.0 权限申请
 * <p> 联系人
 * Manifest.permission.WRITE_CONTACTS
 * Manifest.permission.GET_ACCOUNTS
 * Manifest.permission.READ_CONTACTS
 * <p> 电话
 * Manifest.permission.READ_CALL_LOG
 * Manifest.permission.READ_PHONE_STATE
 * Manifest.permission.CALL_PHONE
 * Manifest.permission.WRITE_CALL_LOG
 * Manifest.permission.USE_SIP
 * Manifest.permission.PROCESS_OUTGOING_CALLS
 * Manifest.permission.ADD_VOICEMAIL
 * <p> 日历
 * Manifest.permission.READ_CALENDAR
 * Manifest.permission.WRITE_CALENDAR
 * <p> 相机
 * Manifest.permission.CAMERA
 * <p> 传感器
 * Manifest.permission.BODY_SENSORS
 * <p> 定位
 * Manifest.permission.ACCESS_FINE_LOCATION
 * Manifest.permission.ACCESS_COARSE_LOCATION
 * <p> 读写
 * Manifest.permission.READ_EXTERNAL_STORAGE
 * Manifest.permission.WRITE_EXTERNAL_STORAGE
 * <p> 麦克风
 * Manifest.permission.RECORD_AUDIO
 * <p> 短信
 * Manifest.permission.READ_SMS
 * Manifest.permission.RECEIVE_WAP_PUSH
 * Manifest.permission.RECEIVE_MMS
 * Manifest.permission.RECEIVE_SMS
 * Manifest.permission.SEND_SMS
 *
 * @author wuzhongsheng
 */
public class PermissionManager {
    private static final String TAG = PermissionManager.class.getSimpleName();

    private Activity activity;

    private List<String> permissionList = new ArrayList<>();

    private AlertDialog alertDialog;

    private String grantedMessage;
    private String deniedMessage;

    public PermissionManager(Activity activity) {
        this.activity = activity;
    }

    private Activity getActivity() {
        return activity;
    }

    private List<String> getPermissionList() {
        return permissionList;
    }

    private String getGrantedMessage() {
        return grantedMessage;
    }

    private String getDeniedMessage() {
        return deniedMessage;
    }

    private void logger(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }

    /**
     * 授权时提示语
     *
     * @param message 提示语
     * @return PermissionManager
     */
    public PermissionManager setGrantedMessage(String message) {
        if (message != null && !message.equals(getGrantedMessage())) {
            this.grantedMessage = message;
        }
        return this;
    }

    /**
     * 拒绝时提示语
     *
     * @param message 提示语
     * @return PermissionManager
     */
    public PermissionManager setDeniedMessage(String message) {
        if (message != null && !message.equals(getDeniedMessage())) {
            this.deniedMessage = message;
        }
        return this;
    }

    /**
     * 申请权限
     *
     * @param permission 权限
     * @return true:已有权限 false:无该权限
     */
    public boolean requestPermissions(String permission) {
        return requestPermissions(0, permission);
    }

    /**
     * 申请权限
     *
     * @param requestCode 请求码
     * @param permission  权限
     * @return true:已有权限 false:无该权限
     */
    public boolean requestPermissions(final int requestCode, String permission) {
        boolean isNeedRequest = false;


        if (permission == null || permission.isEmpty()) {
            return !isNeedRequest;
        }

        int checkSelfPermission = PackageManager.PERMISSION_GRANTED;
        try {
            checkSelfPermission = ActivityCompat.checkSelfPermission(getActivity(), permission);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            isNeedRequest = true;

            final String[] requestPermissionRationaleStrings = {permission};
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                logger("shouldShowRequestPermissionRationale :" + permission);
                if (getGrantedMessage() != null) {
                    showPermissionRationaleDialog(getGrantedMessage(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), requestPermissionRationaleStrings, requestCode);
                        }
                    });
                } else {
                    ActivityCompat.requestPermissions(getActivity(), requestPermissionRationaleStrings, requestCode);
                }
            } else {
                logger("requestPermissions :" + permission);
                ActivityCompat.requestPermissions(getActivity(), requestPermissionRationaleStrings, requestCode);
            }

        } else {
            isNeedRequest = false;
        }

        return !isNeedRequest;
    }

    /**
     * 申请权限
     *
     * @param permissions 权限数组
     * @return true:已有权限 false:无该权限
     */
    public boolean requestPermissions(String... permissions) {
        return requestPermissions(0, permissions);
    }

    /**
     * 申请权限
     *
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @return true:已有权限 false:无该权限
     */
    public boolean requestPermissions(final int requestCode, String... permissions) {
        boolean isNeedRequest = false;

        getPermissionList().clear();
        Collections.addAll(getPermissionList(), permissions);

        if (getPermissionList() == null || getPermissionList().isEmpty()) {
            return !isNeedRequest;
        }

        List<String> requestPermissionRationaleList = null;
        List<String> requestPermissionList = null;

        for (int i = 0; i < getPermissionList().size(); i++) {
            String permission = getPermissionList().get(i);
            int checkSelfPermission = PackageManager.PERMISSION_GRANTED;
            try {
                checkSelfPermission = ActivityCompat.checkSelfPermission(getActivity(), permission);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                isNeedRequest = true;

                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                    logger("shouldShowRequestPermissionRationale: " + permission);
                    if (requestPermissionRationaleList == null) {
                        requestPermissionRationaleList = new ArrayList<>();
                    }
                    requestPermissionRationaleList.add(permission);
                } else {
                    logger("unShouldShowRequestPermissionRationale: " + permission);
                    if (requestPermissionList == null) {
                        requestPermissionList = new ArrayList<>();
                    }
                    requestPermissionList.add(permission);
                }
            }
        }

        if (requestPermissionRationaleList != null && !requestPermissionRationaleList.isEmpty()) {
            logger("requestPermissionRationaleList: " + requestPermissionRationaleList.size());
            final String[] requestPermissionRationaleStrings = requestPermissionRationaleList.toArray(new String[requestPermissionRationaleList.size()]);
            if (getGrantedMessage() != null) {
                showPermissionRationaleDialog(getGrantedMessage(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(), requestPermissionRationaleStrings, requestCode);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(getActivity(), requestPermissionRationaleStrings, requestCode);
            }
        }

        if (requestPermissionList != null && !requestPermissionList.isEmpty()) {
            logger("requestPermissionList: " + requestPermissionList.size());
            ActivityCompat.requestPermissions(getActivity(), requestPermissionList.toArray(new String[requestPermissionList.size()]), requestCode);
        }

        return !isNeedRequest;
    }

    /**
     * 申请结果返回
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 结果数组
     * @return true:权限申请成功 false:权限申请失败
     */
    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        logger("requestCode: " + requestCode + ", permissions: " + permissions.length + ", grantResults: " + grantResults.length);
        StringBuilder deniedGroupName = null;

        boolean isNeedRequest = false;
        for (int i = 0; i < Math.min(permissions.length, grantResults.length); i++) {
            int result = grantResults[i];
            String permission = permissions[i];
            if (PackageManager.PERMISSION_GRANTED != result) {
                logger(permission + " request denied！");
                isNeedRequest = true;

                // 获取被拒的群组名称
                String groupName = getPermissionGroupName(permission);
                if (groupName != null) {
                    if (deniedGroupName == null) {
                        deniedGroupName = new StringBuilder();
                    }
                    if (deniedGroupName.length() == 0) {
                        deniedGroupName.append(groupName);
                    } else if (deniedGroupName.indexOf(groupName) == -1) {
                        deniedGroupName.append("、").append(groupName);
                    }
                }
            } else {
                logger(permission + " request granted！");
            }
        }

        // 解析被拒的群组名称
        if (deniedGroupName != null && deniedGroupName.length() != 0) {
            int start = deniedGroupName.indexOf("、");
            if (start != -1) {
                int end = deniedGroupName.lastIndexOf("、");
                if (start == end) {
                    deniedGroupName = deniedGroupName.replace(start, end + 1, "和");
                } else {
                    deniedGroupName = deniedGroupName.replace(end, end + 1, "和");
                }
            }
        }

        if (isNeedRequest) {
            showPermissionRationaleDialog(getDeniedMessage(), deniedGroupName != null ? deniedGroupName.toString() : null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getActivity().getPackageName(), null));
                    getActivity().startActivity(intent);
                }
            });
        }
        return !isNeedRequest;
    }

    private String getPermissionGroupName(String permission) {
        String groupName = null;
        switch (permission) {
            case Manifest.permission.WRITE_CONTACTS:
            case Manifest.permission.GET_ACCOUNTS:
            case Manifest.permission.READ_CONTACTS:
                groupName = "联系人";
                break;
            case Manifest.permission.READ_CALL_LOG:
//            case Manifest.permission.READ_PHONE_STATE:
            case Manifest.permission.CALL_PHONE:
            case Manifest.permission.WRITE_CALL_LOG:
            case Manifest.permission.USE_SIP:
            case Manifest.permission.PROCESS_OUTGOING_CALLS:
            case Manifest.permission.ADD_VOICEMAIL:
                groupName = "电话";
                break;
            case Manifest.permission.READ_CALENDAR:
            case Manifest.permission.WRITE_CALENDAR:
                groupName = "日历";
                break;
            case Manifest.permission.CAMERA:
                groupName = "照相机";
                break;
            case Manifest.permission.BODY_SENSORS:
                groupName = "传感器";
                break;
            case Manifest.permission.ACCESS_FINE_LOCATION:
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                groupName = "定位";
                break;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                groupName = "存储";
                break;
            case Manifest.permission.RECORD_AUDIO:
                groupName = "麦克风";
                break;
            case Manifest.permission.READ_SMS:
            case Manifest.permission.RECEIVE_WAP_PUSH:
            case Manifest.permission.RECEIVE_MMS:
            case Manifest.permission.RECEIVE_SMS:
            case Manifest.permission.SEND_SMS:
                groupName = "短信";
                break;
            default:
                break;
        }
        return groupName;
    }

    /**
     * 显示授权时弹窗
     *
     * @param message  显示信息
     * @param listener 监听确定
     */
    private void showPermissionRationaleDialog(String message, DialogInterface.OnClickListener listener) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("申请权限");
            alertDialog.setButton(BUTTON_NEGATIVE, getActivity().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }
        alertDialog.setButton(BUTTON_POSITIVE, getActivity().getString(android.R.string.ok), listener);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    /**
     * 显示拒绝时弹窗
     *
     * @param message         显示信息
     * @param deniedGroupName 被拒的群组名称
     * @param listener        监听确定
     */
    private void showPermissionRationaleDialog(String message, String deniedGroupName, DialogInterface.OnClickListener listener) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("权限被拒绝");
            alertDialog.setButton(BUTTON_NEGATIVE, getActivity().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }
        alertDialog.setButton(BUTTON_POSITIVE, getActivity().getString(android.R.string.ok), listener);
        if (message != null) {
            alertDialog.setMessage(message);
        } else if (deniedGroupName != null) {
            alertDialog.setMessage("当前需要访问" + deniedGroupName + "权限，请到系统设置打开相应的权限！");
        } else {
            alertDialog.setMessage("当前应用权限被禁用，请到系统设置打开相应的权限！");
        }
        alertDialog.show();
    }
}