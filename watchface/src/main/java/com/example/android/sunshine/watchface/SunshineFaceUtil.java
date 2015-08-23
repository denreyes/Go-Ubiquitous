package com.example.android.sunshine.watchface;

import android.graphics.Color;

import com.google.android.gms.wearable.DataItem;

/**
 * Created by DJ on 8/23/2015.
 */
public class SunshineFaceUtil {
    private static final String TAG = "SunshineFaceUtil";

    public static final String KEY_BACKGROUND_COLOR = "BACKGROUND_COLOR";
    public static final String KEY_TRIAD_COLOR = "TRIAD_COLOR";
    public static final String KEY_HOURS_COLOR = "HOURS_COLOR";
    public static final String KEY_MINUTES_COLOR = "MINUTES_COLOR";
    public static final String KEY_DATE_COLOR = "DATE_COLOR";
    public static final String KEY_MAX_COLOR = "MAX_COLOR";
    public static final String KEY_MIN_COLOR = "MIN_COLOR";

    /**
     * The path for the {@link DataItem} containing {@link SunshineFace} configuration.
     */
    public static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";

    /**
     * Name of the default interactive mode background color and the ambient mode background color.
     */
    public static final String COLOR_NAME_AMBIENT_TRIAD = "Black";
    public static final int COLOR_VALUE_AMBIENT_TRIAD =
            parseColor(COLOR_NAME_AMBIENT_TRIAD);

    public static final String COLOR_NAME_AMBIENT_BACKGROUND = "Black";
    public static final int COLOR_VALUE_AMBIENT_BACKGROUND =
            parseColor(COLOR_NAME_AMBIENT_BACKGROUND);

    public static final String COLOR_NAME_AMBIENT_HOUR_TEXT = "White";
    public static final int COLOR_VALUE_AMBIENT_HOUR_TEXT =
            parseColor(COLOR_NAME_AMBIENT_HOUR_TEXT);

    public static final String COLOR_NAME_AMBIENT_MINUTE_TEXT = "White";
    public static final int COLOR_VALUE_AMBIENT_MINUTE_TEXT =
            parseColor(COLOR_NAME_AMBIENT_MINUTE_TEXT);

    public static final String COLOR_NAME_AMBIENT_DATE_TEXT = "White";
    public static final int COLOR_VALUE_AMBIENT_DATE_TEXT =
            parseColor(COLOR_NAME_AMBIENT_DATE_TEXT);

    public static final String COLOR_NAME_AMBIENT_MAX_TEXT = "White";
    public static final int COLOR_VALUE_AMBIENT_MAX_TEXT =
            parseColor(COLOR_NAME_AMBIENT_MAX_TEXT);

    public static final String COLOR_NAME_AMBIENT_MIN_TEXT = "White";
    public static final int COLOR_VALUE_AMBIENT_MIN_TEXT =
            parseColor(COLOR_NAME_AMBIENT_MIN_TEXT);
//
//    /**
//     * Callback interface to perform an action with the current config {@link DataMap} for
//     * {@link SunshineFace}.
//     */
//    public interface FetchConfigDataMapCallback {
//        /**
//         * Callback invoked with the current config {@link DataMap} for
//         * {@link SunshineFace}.
//         */
//        void onConfigDataMapFetched(DataMap config);
//    }
//
    private static int parseColor(String colorName) {
        return Color.parseColor(colorName.toLowerCase());
    }
//
//    /**
//     * Asynchronously fetches the current config {@link DataMap} for {@link SunshineFace}
//     * and passes it to the given callback.
//     * <p>
//     * If the current config {@link DataItem} doesn't exist, it isn't created and the callback
//     * receives an empty DataMap.
//     */
//    public static void fetchConfigDataMap(final GoogleApiClient client,
//                                          final FetchConfigDataMapCallback callback) {
//        Wearable.NodeApi.getLocalNode(client).setResultCallback(
//                new ResultCallback<NodeApi.GetLocalNodeResult>() {
//                    @Override
//                    public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
//                        String localNode = getLocalNodeResult.getNode().getId();
//                        Uri uri = new Uri.Builder()
//                                .scheme("wear")
//                                .path(SunshineFaceUtil.PATH_WITH_FEATURE)
//                                .authority(localNode)
//                                .build();
//                        Wearable.DataApi.getDataItem(client, uri)
//                                .setResultCallback(new DataItemResultCallback(callback));
//                    }
//                }
//        );
//    }
//
//    /**
//     * Overwrites (or sets, if not present) the keys in the current config {@link DataItem} with
//     * the ones appearing in the given {@link DataMap}. If the config DataItem doesn't exist,
//     * it's created.
//     * <p>
//     * It is allowed that only some of the keys used in the config DataItem appear in
//     * {@code configKeysToOverwrite}. The rest of the keys remains unmodified in this case.
//     */
//    public static void overwriteKeysInConfigDataMap(final GoogleApiClient googleApiClient,
//                                                    final DataMap configKeysToOverwrite) {
//
//        SunshineFaceUtil.fetchConfigDataMap(googleApiClient,
//                new FetchConfigDataMapCallback() {
//                    @Override
//                    public void onConfigDataMapFetched(DataMap currentConfig) {
//                        DataMap overwrittenConfig = new DataMap();
//                        overwrittenConfig.putAll(currentConfig);
//                        overwrittenConfig.putAll(configKeysToOverwrite);
//                        SunshineFaceUtil.putConfigDataItem(googleApiClient, overwrittenConfig);
//                    }
//                }
//        );
//    }
//
//    /**
//     * Overwrites the current config {@link DataItem}'s {@link DataMap} with {@code newConfig}.
//     * If the config DataItem doesn't exist, it's created.
//     */
//    public static void putConfigDataItem(GoogleApiClient googleApiClient, DataMap newConfig) {
//        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE);
//        DataMap configToPut = putDataMapRequest.getDataMap();
//        configToPut.putAll(newConfig);
//        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
//                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
//                    @Override
//                    public void onResult(DataApi.DataItemResult dataItemResult) {
//                        if (Log.isLoggable(TAG, Log.DEBUG)) {
//                            Log.d(TAG, "putDataItem result status: " + dataItemResult.getStatus());
//                        }
//                    }
//                });
//    }
//
//    private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {
//
//        private final FetchConfigDataMapCallback mCallback;
//
//        public DataItemResultCallback(FetchConfigDataMapCallback callback) {
//            mCallback = callback;
//        }
//
//        @Override
//        public void onResult(DataApi.DataItemResult dataItemResult) {
//            if (dataItemResult.getStatus().isSuccess()) {
//                if (dataItemResult.getDataItem() != null) {
//                    DataItem configDataItem = dataItemResult.getDataItem();
//                    DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
//                    DataMap config = dataMapItem.getDataMap();
//                    mCallback.onConfigDataMapFetched(config);
//                } else {
//                    mCallback.onConfigDataMapFetched(new DataMap());
//                }
//            }
//        }
//    }

    private SunshineFaceUtil() { }
}
