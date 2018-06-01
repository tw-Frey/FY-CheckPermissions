package tw.idv.fy.utils.checkpermissions;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import tw.idv.fy.utils.checkpermissions.imp.IRequestPermissions;

public class CheckPermissionsActivity extends Activity implements IRequestPermissions {

    public static final String REQUESTED_PERMISSIONS_KEY = "requestedPermissions";
    private String[] requestedPermissions = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestedPermissions = getIntent().getStringArrayExtra(REQUESTED_PERMISSIONS_KEY);
        if (requestedPermissions != null) return;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_PERMISSIONS
            );
            requestedPermissions = packageInfo.requestedPermissions;
        } catch (Throwable e) {
            android.util.Log.w("FY-CheckPermissions", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermissionsProcess(requestedPermissions)) {
            ActivityCompat.finishAfterTransition(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] requestedPermissions, @NonNull int[] grantResults) {
        /* TODO: IRequestPermissions.OnRequestPermissionsResult 要再化簡 */
        IRequestPermissions.OnRequestPermissionsResult(this, requestCode, requestedPermissions, grantResults);
    }
}
