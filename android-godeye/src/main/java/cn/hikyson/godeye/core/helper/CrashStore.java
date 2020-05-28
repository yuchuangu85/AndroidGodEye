package cn.hikyson.godeye.core.helper;

import android.Manifest;
import android.content.Context;
import android.os.Environment;

import androidx.core.content.PermissionChecker;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.hikyson.godeye.core.GodEye;
import cn.hikyson.godeye.core.exceptions.UninstallException;
import cn.hikyson.godeye.core.internal.modules.crash.CrashInfo;
import cn.hikyson.godeye.core.utils.IoUtil;
import cn.hikyson.godeye.core.utils.JsonUtil;
import cn.hikyson.godeye.core.utils.L;
import io.reactivex.Observable;


public class CrashStore {
    public static Observable<List<CrashInfo>> observeCrashAndCache(Context context, String storeDirName) {
        return Observable.create(emitter -> {
            try {
                GodEye.instance().observeModule(GodEye.ModuleName.CRASH, (List<CrashInfo> maps) -> {
                    if (!maps.isEmpty()) {
                        storeCrash(context, maps, storeDirName);
                    }
                    emitter.onNext(restoreCrash(context, storeDirName));
                    emitter.onComplete();
                });
            } catch (UninstallException e) {
                L.d(GodEye.ModuleName.CRASH + " is not installed.");
            }
        });
    }

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS_Z", Locale.US);
    private static final SimpleDateFormat FORMATTER_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    private static final int sMaxStoreCount = 10;
    private static final String SUFFIX = ".crash";
    private static final FilenameFilter mCrashFilenameFilter = (dir, filename) -> filename.endsWith(SUFFIX);

    private static synchronized void storeCrash(Context context, List<CrashInfo> crashInfos, String storeDirName) {
        List<File> crashFiles = null;
        try {
            crashFiles = Arrays.asList(Objects.requireNonNull(makeSureCrashDir(context, storeDirName).listFiles(mCrashFilenameFilter)));
        } catch (Throwable e) {
            L.e(e);
        }
        if (crashFiles != null && crashFiles.size() >= sMaxStoreCount) {
            Collections.sort(crashFiles, (o1, o2) -> {
                try {
                    return Long.compare(Objects.requireNonNull(FORMATTER.parse(o2.getName())).getTime(), FORMATTER.parse(o1.getName()).getTime());
                } catch (Throwable e) {
                    return Long.compare(o2.lastModified(), o1.lastModified());
                }
            });
            for (int i = 0; i < (crashFiles.size() + 1 - sMaxStoreCount); i++) {
                try {
                    deleteIfExists(crashFiles.get(crashFiles.size() - i - 1));
                } catch (Exception e) {
                    L.e(e);
                }
            }
        }
        for (CrashInfo crashInfo : crashInfos) {
            File file = null;
            try {
                file = getStoreFile(context, getStoreFileName(Objects.requireNonNull(FORMATTER_2.parse(crashInfo.crashTime)).getTime()), storeDirName);
            } catch (Throwable e) {
                L.e(e);
            }
            if (file == null) {
                continue;
            }
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(file);
                fileWriter.write(JsonUtil.toJson(crashInfo));
            } catch (IOException e) {
                L.e(e);
            } finally {
                IoUtil.closeSilently(fileWriter);
            }
        }
        L.d("CrashStore storeCrash count:%s", crashInfos.size());
    }

    private static void deleteIfExists(File file) throws Exception {
        if (file.exists() && !file.delete()) {
            throw new Exception("deleteIfExists failed");
        }
    }

    private static synchronized List<CrashInfo> restoreCrash(Context context, String storeDirName) {
        File[] crashFiles = new File[0];
        try {
            crashFiles = makeSureCrashDir(context, storeDirName).listFiles(mCrashFilenameFilter);
        } catch (IOException e) {
            L.e(e);
        }
        List<CrashInfo> crashInfos = new ArrayList<>();
        if (crashFiles == null || crashFiles.length == 0) {
            return crashInfos;
        }
        for (File crashFile : crashFiles) {
            FileReader reader = null;
            try {
                reader = new FileReader(crashFile);
                crashInfos.add(JsonUtil.sGson.fromJson(reader, CrashInfo.class));
            } catch (Throwable e) {
                L.e(e);
            } finally {
                IoUtil.closeSilently(reader);
            }
        }
        // sort by crash time [2019-09-04 12:00:00.crash,2019-09-02 12:00:00.crash,2019-09-02 11:00:00.crash]
        Collections.sort(crashInfos, (o1, o2) -> {
            try {
                return Long.compare(Objects.requireNonNull(FORMATTER_2.parse(o2.crashTime)).getTime(), FORMATTER_2.parse(o1.crashTime).getTime());
            } catch (Throwable throwable) {
                L.e(throwable);
            }
            return 0;
        });
        L.d("CrashStore restoreCrash count:%s", crashInfos.size());
        return crashInfos;
    }

    private static String getStoreFileName(long crashTime) {
        return FORMATTER.format(new Date(crashTime)) + SUFFIX;
    }

    private static File getStoreFile(Context context, String fileName, String storeDirName) throws IOException {
        File file = new File(makeSureCrashDir(context, storeDirName), fileName);
        if (file.exists() && !file.delete()) {
            throw new IOException(file.getAbsolutePath() + " already exist and delete failed");
        }
        return file;
    }

    private static File makeSureCrashDir(Context context, String storeDirName) throws IOException {
        File crashDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                && PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            File crashTotalDir = new File(context.getExternalCacheDir(), "AndroidGodEye_Crash");
            crashDir = new File(crashTotalDir, storeDirName);
        } else {
            File crashTotalDir = new File(context.getCacheDir(), "AndroidGodEye_Crash");
            crashDir = new File(crashTotalDir, storeDirName);
        }
        if (!crashDir.exists() && !crashDir.mkdirs()) {
            throw new IOException("Can not find crash directory.");
        }
        return crashDir;
    }
}
