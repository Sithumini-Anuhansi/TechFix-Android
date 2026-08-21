package com.techfix.app.util;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageHelper {
    public static final String FILE_PROVIDER = "com.techfix.app.fileprovider";

    public static File createImageFile(Context context) throws IOException {
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dir = new File(context.getFilesDir(), "photos");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create photos directory");
        }
        return File.createTempFile("TF_" + stamp + "_", ".jpg", dir);
    }

    public static Uri uriFor(Context context, File file) {
        return FileProvider.getUriForFile(context, FILE_PROVIDER, file);
    }
}
