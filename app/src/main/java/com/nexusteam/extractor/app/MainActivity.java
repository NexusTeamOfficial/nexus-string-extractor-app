package com.nexusteam.extractor.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.nexusteam.framework.xml.string.StringExtractor;
import com.nexusteam.framework.xml.string.core.TransformationResult;
import com.nexusteam.framework.xml.string.exception.ExtractionException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etResPath;
    private TextInputEditText etSavePath;
    private TextInputEditText etPrefix;
    private MaterialCheckBox cbDryRun;
    private MaterialCheckBox cbBackup;
    private MaterialButton btnExecute;
    private MaterialButton btnGrantPermission;
    private MaterialCardView cardPermission;
    private android.widget.TextView tvPermissionStatus;
    private android.widget.TextView tvLog;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final StringBuilder logBuffer = new StringBuilder();

    private ActivityResultLauncher<Uri> openResTree;
    private ActivityResultLauncher<Uri> openSaveTree;
    private ActivityResultLauncher<Intent> manageStorageLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etResPath = findViewById(R.id.etResPath);
        etSavePath = findViewById(R.id.etSavePath);
        etPrefix = findViewById(R.id.etPrefix);
        cbDryRun = findViewById(R.id.cbDryRun);
        cbBackup = findViewById(R.id.cbBackup);
        btnExecute = findViewById(R.id.btnExecute);
        btnGrantPermission = findViewById(R.id.btnGrantPermission);
        cardPermission = findViewById(R.id.cardPermission);
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);
        tvLog = findViewById(R.id.tvLog);

        File suggested = new File(Environment.getExternalStorageDirectory(), "AndroidStudioProjects");
        etResPath.setText(suggested.getAbsolutePath() + "/MyApp/app/src/main/res");

        setupLaunchers();
        bindClicks();
        refreshPermissionUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPermissionUi();
    }

    private void setupLaunchers() {
        openResTree = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                uri -> {
                    if (uri != null) {
                        takePersistablePermission(uri);
                        String path = uriToFilesystemPath(uri);
                        if (path != null) {
                            etResPath.setText(path);
                            appendLog("Res path set: " + path);
                        } else {
                            etResPath.setText(uri.toString());
                            appendLog("Got tree URI (type full path if needed): " + uri);
                        }
                    }
                });

        openSaveTree = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                uri -> {
                    if (uri != null) {
                        takePersistablePermission(uri);
                        String path = uriToFilesystemPath(uri);
                        if (path != null) {
                            File strings = new File(path, "strings.xml");
                            etSavePath.setText(strings.getAbsolutePath());
                            appendLog("Save path set: " + strings.getAbsolutePath());
                        } else {
                            appendLog("Save folder URI: " + uri + " — type path manually");
                        }
                    }
                });

        manageStorageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> refreshPermissionUi());
    }

    private void bindClicks() {
        findViewById(R.id.btnBrowseRes).setOnClickListener(v -> openResTree.launch(null));
        findViewById(R.id.btnBrowseSave).setOnClickListener(v -> openSaveTree.launch(null));
        findViewById(R.id.btnClearLog).setOnClickListener(v -> {
            logBuffer.setLength(0);
            tvLog.setText(R.string.log_hint);
        });
        btnGrantPermission.setOnClickListener(v -> requestAllFilesAccess());
        btnExecute.setOnClickListener(v -> runExtraction());
    }

    private void runExtraction() {
        if (!hasStorageAccess()) {
            toast("Grant All files access first");
            requestAllFilesAccess();
            return;
        }

        String resPath = textOf(etResPath);
        if (TextUtils.isEmpty(resPath)) {
            toast("Enter res/ path");
            return;
        }
        File resDir = new File(resPath);
        if (!resDir.isDirectory()) {
            toast("Not a directory: " + resPath);
            appendLog("ERROR: not a directory: " + resPath);
            return;
        }
        if (!new File(resDir, "layout").isDirectory()) {
            appendLog("WARNING: layout/ not found under " + resPath);
        }

        String savePath = textOf(etSavePath);
        String prefix = textOf(etPrefix);
        boolean dryRun = cbDryRun.isChecked();
        boolean backup = cbBackup.isChecked();

        btnExecute.setEnabled(false);
        appendLog("──── Starting extraction ────");
        appendLog("resDir  = " + resDir.getAbsolutePath());
        appendLog("save    = " + (TextUtils.isEmpty(savePath) ? "(default values/strings.xml)" : savePath));
        appendLog("dryRun  = " + dryRun + "  backup = " + backup);
        if (!TextUtils.isEmpty(prefix)) appendLog("prefix  = " + prefix);

        executor.execute(() -> {
            try {
                StringExtractor.Builder builder = StringExtractor.builder()
                        .resDir(resDir)
                        .dryRun(dryRun)
                        .backup(backup)
                        .verbose(true);
                if (!TextUtils.isEmpty(savePath)) builder.stringsXml(savePath);
                if (!TextUtils.isEmpty(prefix)) builder.prefix(prefix);

                TransformationResult result = builder.build().extract();

                runOnUiThread(() -> {
                    appendLog("DONE");
                    appendLog("  layouts scanned : " + result.getLayoutsScanned());
                    appendLog("  layouts modified: " + result.getLayoutsModified());
                    appendLog("  strings extracted: " + result.getStringsExtracted());
                    appendLog("  dryRun: " + result.isDryRun());
                    toast("Done — " + result.getStringsExtracted() + " string(s)");
                    btnExecute.setEnabled(true);
                });
            } catch (ExtractionException e) {
                runOnUiThread(() -> {
                    appendLog("ERROR: " + e.getMessage());
                    if (e.getCause() != null) appendLog("  cause: " + e.getCause().getMessage());
                    toast("Extraction failed");
                    btnExecute.setEnabled(true);
                });
            } catch (Throwable t) {
                runOnUiThread(() -> {
                    appendLog("FATAL: " + t.getClass().getSimpleName() + ": " + t.getMessage());
                    toast("Unexpected error");
                    btnExecute.setEnabled(true);
                });
            }
        });
    }

    private boolean hasStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                manageStorageLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                manageStorageLauncher.launch(intent);
            }
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1001);
        }
    }

    private void refreshPermissionUi() {
        boolean ok = hasStorageAccess();
        if (ok) {
            cardPermission.setCardBackgroundColor(0xFFE8F5E9);
            tvPermissionStatus.setText(R.string.permission_granted);
            btnGrantPermission.setVisibility(View.GONE);
        } else {
            cardPermission.setCardBackgroundColor(0xFFFFF3E0);
            tvPermissionStatus.setText(R.string.permission_message);
            btnGrantPermission.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        refreshPermissionUi();
    }

    private void takePersistablePermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (SecurityException ignored) {
        }
    }

    @Nullable
    private String uriToFilesystemPath(Uri uri) {
        if (uri == null) return null;
        String s = uri.toString();
        if (s.startsWith("file://")) return Uri.decode(s.substring(7));
        if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
            String docId = uri.getLastPathSegment();
            if (docId != null && docId.contains(":")) {
                String[] parts = docId.split(":", 2);
                String type = parts[0];
                String rel = parts.length > 1 ? parts[1] : "";
                if ("primary".equalsIgnoreCase(type)) {
                    return new File(Environment.getExternalStorageDirectory(), rel).getAbsolutePath();
                }
                return "/storage/" + type + "/" + rel;
            }
        }
        return null;
    }

    private String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void appendLog(String line) {
        String ts = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        logBuffer.append('[').append(ts).append("] ").append(line).append('\n');
        tvLog.setText(logBuffer.toString());
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
