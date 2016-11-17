package com.microsoft.azure.mobile.crashes;

import com.microsoft.azure.mobile.crashes.utils.ErrorLogHelper;
import com.microsoft.azure.mobile.utils.MobileCenterLog;
import com.microsoft.azure.mobile.utils.storage.StorageHelper;

import java.io.File;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

public class WrapperSdkExceptionManager {
    /**
     * File extension for data files created by this class.
     */
    private static final String dataFileExtension = ".dat";

    /**
     * In-memory storage to be set by a wrapper SDK that contains the data it needs to save
     */
    private static byte[] mManagedExceptionData = null;

    /**
     * Contains wrapper SDK data that has been loaded into memory
     */
    private static Map<String, byte[]> mManagedExceptionDataContainer = new HashMap<>();

    /**
     * Check if there is managed exception data in memory
     *
     * @return true if there is exception data in memory
     */
    public static boolean hasManagedExceptionData() {
        return mManagedExceptionData != null;
    }

    /**
     * Store wrapper exception data in memory. This should only be used by a wrapper SDK.
     *
     * @param data  byte array containing the data a wrapper SDK needs to save
     */
    public static void setManagedExceptionData(byte[] data) {
        mManagedExceptionData = data;
    }

    /**
     * Store the in-memory managed exception data to disk
     *
     * @param reportId  The associated error UUID
     */
    public static void saveManagedExceptionData(UUID reportId) {
        if (reportId == null) {
            MobileCenterLog.error(Crashes.LOG_TAG, "Trying to save managed exception data with null UUID");
            return;
        }
        mManagedExceptionDataContainer.put(reportId.toString(), mManagedExceptionData);
        File dataFile = getFile(reportId);
        try {
            StorageHelper.InternalStorage.writeObject(dataFile, mManagedExceptionData);
            mManagedExceptionData = null;
        }
        catch (IOException e) {
            MobileCenterLog.error(Crashes.LOG_TAG, "Error writing managed exception data to file", e);
        }
        mManagedExceptionData = null;
    }

    /**
     * Delete managed exception data from disk and store it in memory
     *
     * @param reportId    The associated error UUID
     */
    public static void deleteManagedExceptionData(UUID reportId) {
        if (reportId == null) {
            MobileCenterLog.error(Crashes.LOG_TAG, "Trying to delete managed exception data with null UUID");
            return;
        }
        File dataFile = getFile(reportId);
        loadManagedExceptionData(reportId); //save the exception data in memory before deleting
        StorageHelper.InternalStorage.delete(dataFile);
    }

    /**
     * Load managed exception data into memory
     *
     * @param reportId    The associated error UUID
     * @return The data loaded into memory
     */
    private static byte[] loadManagedExceptionData(UUID reportId) {
        if (reportId == null) {
            MobileCenterLog.error(Crashes.LOG_TAG, "Trying to load managed exception data with null UUID");
            return null;
        }
        return loadManagedExceptionData(reportId.toString());
    }

    /**
     * Load managed exception data into memory
     *
     * @param reportIdString   String representation of the associated error UUID
     * @return The data loaded into memory
     */
    public static byte[] loadManagedExceptionData(String reportIdString) {
        byte[] dataBytes = mManagedExceptionDataContainer.get(reportIdString);
        if (dataBytes != null) {
            return dataBytes;
        }

        File dataFile = getFile(reportIdString);

        try {
            dataBytes = StorageHelper.InternalStorage.readObject(dataFile);
            if (dataBytes != null) {
                mManagedExceptionDataContainer.put(reportIdString, dataBytes);
            }
            return dataBytes;
        }
        catch (ClassNotFoundException e) {
            MobileCenterLog.error(Crashes.LOG_TAG, "Cannot read managed exception data file " + dataFile.getName(), e);
        }
        catch (IOException e) {
            MobileCenterLog.error(Crashes.LOG_TAG, "Cannot access managed exception data file " + dataFile.getName(), e);
        }
        return null;
    }

    /**
     * Get a file object for managed exception data
     *
     * @param reportId    The associated error UUID
     * @return The corresponding file object
     */
    private static File getFile(UUID reportId) {
        if (reportId == null) {
            return null;
        }
        return getFile(reportId.toString());
    }

    /**
     * Get a file object for managed exception data
     *
     * @param reportIdString   String representation of the associated error UUID
     * @return The corresponding file object
     */
    private static File getFile(String reportIdString) {
        if (reportIdString == null) {
            return null;
        }
        File errorStorageDirectory = ErrorLogHelper.getErrorStorageDirectory();
        String filename = reportIdString + dataFileExtension;
        return new File(errorStorageDirectory, filename);
    }
}

