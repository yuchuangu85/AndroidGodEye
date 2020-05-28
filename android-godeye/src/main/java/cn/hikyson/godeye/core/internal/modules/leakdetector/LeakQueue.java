package cn.hikyson.godeye.core.internal.modules.leakdetector;

import androidx.annotation.IntDef;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import androidx.collection.ArrayMap;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @deprecated use {@link Leak } instead
 */
@Deprecated
public class LeakQueue{

    /**
     * @deprecated use {@link Leak } instead
     */
    @Deprecated
    @Keep
    public static class LeakMemoryInfo implements Serializable, Comparable<LeakMemoryInfo> {
        public static final SimpleDateFormat DF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);

        @Retention(RetentionPolicy.SOURCE)
        @StringDef({Fields.REF_KEY, Fields.LEAK_TIME, Fields.STATUS_SUMMARY, Fields.STATUS, Fields.LEAK_OBJ_NAME, Fields.PATH_TO_ROOT, Fields.LEAK_MEMORY_BYTES})
        public @interface Fields {
            public static final String REF_KEY = "referenceKey";
            public static final String LEAK_TIME = "leakTime";
            public static final String STATUS_SUMMARY = "statusSummary";
            public static final String STATUS = "status";
            public static final String LEAK_OBJ_NAME = "leakObjectName";
            public static final String PATH_TO_ROOT = "pathToGcRoot";
            public static final String LEAK_MEMORY_BYTES = "leakMemoryBytes";
        }

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({Status.STATUS_INVALID, Status.STATUS_DETECT, Status.STATUS_START, Status.STATUS_PROGRESS, Status.STATUS_RETRY, Status.STATUS_DONE})
        public @interface Status {
            public static final int STATUS_INVALID = -1;
            public static final int STATUS_DETECT = 0;
            public static final int STATUS_START = 1;
            public static final int STATUS_PROGRESS = 2;
            public static final int STATUS_RETRY = 3;
            public static final int STATUS_DONE = 4;
        }

        public String referenceKey = "";
        @Nullable
        public LeakRefInfo leakRefInfo = null;
        public String leakTime = "";
        public String statusSummary = "";
        public @Status
        int status = Status.STATUS_INVALID;
        //泄漏的对象
        public String leakObjectName = "";
        //到gcroot的最短路径
        public List pathToGcRoot = new ArrayList<>();
        //泄漏字节
        public long leakMemoryBytes = 0L;

        public LeakMemoryInfo(String referenceKey) {
            this.referenceKey = referenceKey;
        }

        @Override
        public int compareTo(@NonNull LeakMemoryInfo o) {
            if (this == o) {
                return 0;
            }
            try {
                return DF.parse(this.leakTime).compareTo(DF.parse(o.leakTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public String toString() {
            return "LeakMemoryInfo{" +
                    "referenceKey='" + referenceKey + '\'' +
                    ", leakRefInfo='" + leakRefInfo + '\'' +
                    ", leakTime='" + leakTime + '\'' +
                    ", statusSummary='" + statusSummary + '\'' +
                    ", status=" + status +
                    ", leakObjectName='" + leakObjectName + '\'' +
                    ", pathToGcRoot=" + pathToGcRoot +
                    ", leakMemoryBytes=" + leakMemoryBytes +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LeakMemoryInfo that = (LeakMemoryInfo) o;
            return referenceKey.equals(that.referenceKey);
        }

        @Override
        public int hashCode() {
            return referenceKey.hashCode();
        }
    }

    private ArrayMap<String, Map<String, Object>> mLeakMemoryInfoArrayMap;

    private LeakQueue() {
        mLeakMemoryInfoArrayMap = new ArrayMap<>();
    }

    private static class InstanceHolder {
        private static LeakQueue sINSTANCE = new LeakQueue();
    }

    public static LeakQueue instance() {
        return InstanceHolder.sINSTANCE;
    }

}
