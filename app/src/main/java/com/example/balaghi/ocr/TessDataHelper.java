package com.example.balaghi.ocr;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TessDataHelper {

    private static final String TAG = "TessDataHelper";
    private static final String[] LANGUAGES = {"ara", "eng"};

    /**
     * ينسخ ملفات tessdata من assets إلى مجلد داخلي (مرة وحدة فقط).
     * لازم تستدعيها بخيط منفصل (Background Thread) لأنها I/O.
     */
    public static String prepareTessData(Context context) {
        File tessDir = new File(context.getFilesDir(), "tesseract");
        File tessDataDir = new File(tessDir, "tessdata");

        if (!tessDataDir.exists()) {
            tessDataDir.mkdirs();
        }

        for (String lang : LANGUAGES) {
            File outFile = new File(tessDataDir, lang + ".traineddata");
            if (!outFile.exists()) {
                copyAssetFile(context, "tessdata/" + lang + ".traineddata", outFile);
            }
        }

        return tessDir.getAbsolutePath();
    }

    private static void copyAssetFile(Context context, String assetPath, File outFile) {
        try (InputStream in = context.getAssets().open(assetPath);
             OutputStream out = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();

        } catch (IOException e) {
            Log.e(TAG, "فشل نسخ ملف اللغة: " + assetPath, e);
        }
    }
}