package nl.saxion.tokonverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class InternalStorageManager {
    // Reference: https://stackoverflow.com/a/17674787

    public String saveToInternalStorage(Context c, Bitmap bitmapImage, String saveName) {
        File baseDirectory = c.getDir("imageDir", Context.MODE_PRIVATE);
        File pathToSaveTo = new File(baseDirectory, saveName);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(pathToSaveTo);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baseDirectory.getAbsolutePath();
    }

    public Bitmap loadBitmapFromStorage(String path, String saveName) {
        Bitmap b = null;
        try {
            File f = new File(path, saveName);
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }
}
