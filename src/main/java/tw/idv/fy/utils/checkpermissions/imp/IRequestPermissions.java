package tw.idv.fy.utils.checkpermissions.imp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import tw.idv.fy.utils.checkpermissions.R;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public interface IRequestPermissions extends ActivityCompat.OnRequestPermissionsResultCallback {

    int REQUEST_DUMMY = "REQUEST_DUMMY".hashCode() & 0xF;

    default void finalAction() {
        final Activity activity;
        if (this instanceof Activity) {
            activity = (Activity) this;
        } else throw new IllegalArgumentException("must be instanceof Activity");
        ActivityCompat.finishAfterTransition(activity);
    }

    default boolean checkPermissionsProcess(String[] requestedPermissions) {
        final Activity activity;
        if (this instanceof Activity) {
            activity = (Activity) this;
        } else throw new IllegalArgumentException("must be instanceof Activity");
        if (CheckAllPermissionsGranted(activity, requestedPermissions)) return true;
        RequestPermissions(activity, requestedPermissions);
        return false;
    }

    @SuppressWarnings("unused")
    static void OnRequestPermissionsResult(IRequestPermissions imp, int requestCode, @NonNull String[] requestedPermissions, @NonNull int[] grantResults) {
        boolean result = true;
        for (int grantResult : grantResults) result &= grantResult == PERMISSION_GRANTED;
        if (result) {
            imp.finalAction();
            return;
        }
        final Activity activity;
        if (imp instanceof Activity) {
            activity = (Activity) imp;
        } else throw new IllegalArgumentException("must be instanceof Activity");
        if (NeedShowRequestPermissionByMySelf(activity, requestedPermissions)) {
            activity.startActivity(new Intent(activity, OpenAppSettings.class));
        }
    }

    static boolean CheckAllPermissionsGranted(Activity activity, String[] requestedPermissions) {
        if (requestedPermissions == null) return true;
        boolean result = true;
        for (String permission : requestedPermissions) {
            result &= ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED;
        }
        return result;
    }

    static void RequestPermissions(Activity activity, String[] requestedPermissions) {
        if (!(activity instanceof ActivityCompat.OnRequestPermissionsResultCallback)) {
            android.util.Log.w("FY-CheckPermissions",
                    "Need Implements ActivityCompat.OnRequestPermissionsResultCallback"
            );
            return;
        }
        ActivityCompat.requestPermissions(activity, requestedPermissions, REQUEST_DUMMY);
    }

    static boolean NeedShowRequestPermissionByMySelf(Activity activity, String[] requestedPermissions) {
        boolean needShowRequestPermissionByMySelf = false;
        for (String permission : requestedPermissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED) continue;
            /*
                shouldShowRequestPermissionRationale
                return: true-系統會顯示權限要求, false-系統不會顯示權限要求 (注意, 已同意便不會顯示)
             */
            needShowRequestPermissionByMySelf |= !ActivityCompat
                    .shouldShowRequestPermissionRationale(activity, permission);
        }
        return needShowRequestPermissionByMySelf;
    }

    static void StartAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, REQUEST_DUMMY);
    }

    class OpenAppSettings extends Activity {
        @Override
        protected void onResume() {
            super.onResume();
            new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert)
                    .setCancelable(false)
                    .setTitle(R.string.tw_idv_fy_utils_checkpermissions_open_settings_title)
                    .setPositiveButton(android.R.string.ok, (d, w) -> {
                        StartAppSettings(this);
                        finish();
                    })
                    .show();
        }
    }
}
