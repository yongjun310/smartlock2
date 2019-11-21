package com.smart.lock.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.smart.lock.common.SlideConstants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class FileUtils {

    public static void saveObject(Context ctx, String name, Object obj) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = ctx.openFileOutput(name, Context.MODE_MULTI_PROCESS);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        } catch (Exception e) {
            e.printStackTrace();
            // 这里是保存文件产生异常
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // fos流关闭异常
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    // oos流关闭异常
                    e.printStackTrace();
                }
            }
        }
    }

    public static Object getObject(Context ctx, String name) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = ctx.openFileInput(name);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            // 这里是读取文件产生异常
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // fis流关闭异常
                    e.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    // ois流关闭异常
                    e.printStackTrace();
                }
            }
        }
        // 读取产生异常，返回null
        return null;
    }

    public static void addFileLog(String log) {
        File dirFile = new File(SlideConstants.EXTERNAL_IMAGE_PATH);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        String content = String.format("[%s]%s\n", DisplayUtils.formatDateTimeString(Calendar.getInstance().getTime()), log);

        File logFile = new File(SlideConstants.EXTERNAL_IMAGE_PATH + "log.txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(logFile, true);
            fos.write(content.getBytes());
            fos.flush();
        } catch (Exception e) {
            Log.e("file log:", e.toString());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e("file log:", e.toString());
                }
            }
        }
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public static boolean downloadImage(String imageUrl, String localPath) {
        InputStream is = null;
        FileOutputStream fos = null;
        Log.d("downloadImage", imageUrl + ":" + localPath);
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(imageUrl);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            float length = entity.getContentLength();
            is = entity.getContent();
            File file = new File(localPath);
            if (file.exists()) {
            	file.delete();
			}
            //File oldFile = new File(localPath + ".tmp");
            if (is != null) {
                fos = new FileOutputStream(file);

                byte[] buf = new byte[1024];
                int ch = -1;

                while ((ch = is.read(buf)) != -1) {
                    fos.write(buf, 0, ch);
                }
            }

            fos.flush();
            if (fos != null) {
                fos.close();
            }

            //oldFile.renameTo(new File(localPath));

            Log.d("return file", file.getName() + ":" + file.length());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String contentIdToLocalPath(int contentId) {
        final String filePrefix = SlideConstants.EXTERNAL_PHOTO_IMAGE_PATH;
        File dirFile = new File(filePrefix);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        return filePrefix + contentId + ".jpg";
    }

    public static InputStream downloadImageStream(String path, int width,
                                                  int height) throws Exception {
        path = URLEncoder.encode(path, "utf-8");
        path = path.replace("%3A", ":").replace("%2F", "/").replace("%3F", "?");
        // FIXME HARDCODE
        path = path
                + String.format(SlideConstants.QINIU_PIC_PARAM, height, width);
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return conn.getInputStream();
        }
        return null;
    }

    public static void saveImage(Bitmap bm, String fileName)
            throws IOException {
        FileOutputStream fos = null;
        File myCaptureFile = new File(fileName);
        if (myCaptureFile.exists())
            return;
        fos = new FileOutputStream(myCaptureFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        fos.flush();
        fos.close();
    }

    public static void saveImage(Bitmap bm, String fileName, String filePrefix)
            throws IOException {
        FileOutputStream fos = null;

        File dirFile = new File(filePrefix);
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                return;
            }
        }
        File myCaptureFile = new File(filePrefix + fileName);
        if (myCaptureFile.exists())
            return;
        fos = new FileOutputStream(myCaptureFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        fos.flush();
        fos.close();
    }

    public static int getFileSize(String filePath) {
        int size = 0;
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }

    public static Bitmap loadBitmapFromFile(String filepath) {
        FileInputStream fs = null;
        BufferedInputStream bs = null;
        try {
            fs = new FileInputStream(filepath);
            bs = new BufferedInputStream(fs);
            return BitmapFactory.decodeStream(bs);
        } catch (Exception e) {
            Log.e("loadBitmapFromFile exception", e.toString());
        }
        return null;
    }

    public static Bitmap loadResizedBitmap(Context ctx, String filename,
                                           int width, int height, boolean exact) {
        Bitmap bitmap = null;
        FileInputStream fs = null;
        BufferedInputStream bs = null;
        try {
            fs = new FileInputStream(filename);
            bs = new BufferedInputStream(fs);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inTargetDensity = ctx.getResources().getDisplayMetrics().densityDpi;
            options.inTempStorage = new byte[5 * 1024 * 1024];
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(bs, null, options);
            if (options.outHeight > 0 && options.outWidth > 0) {
                options.inJustDecodeBounds = false;
                options.inSampleSize = 2;
                while (options.outWidth / options.inSampleSize > width
                        && options.outHeight / options.inSampleSize > height) {
                    options.inSampleSize++;
                }
                options.inSampleSize--;

                bitmap = BitmapFactory.decodeFile(filename, options);
                if (bitmap != null && exact) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, width, height,
                            false);
                }
            }
        } catch (Exception ex) {
            Log.e("file bitmap", ex.toString());
        } finally {
            try {
                bs.close();
                fs.close();
            } catch (Exception ex) {
                Log.e("file bitmap", ex.toString());
            }
        }
        return bitmap;
    }

    public static Bitmap loadBitmapFromStream(Context ctx, InputStream is) {
        Bitmap bitmap = null;
        try {
            byte[] data = readStream(is);
            if (data != null) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
        } catch (Exception ex) {
            Log.e("file bitmap", ex.toString());
        }
        return bitmap;
    }

    /**
     * 得到图片字节流 数组大小
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    public static String getImageDir(Context ctx) {
        String filePrefix = SlideConstants.EXTERNAL_IMAGE_PATH;

        if (DeviceUtils.getAvailableExternalMemorySize() < 10 * 1024 * 1024) {
            filePrefix = ctx.getCacheDir().getAbsolutePath() + File.separator;
        }
        return filePrefix;
    }

    // 根缓存目录
    private static String cacheRootPath = "";

    /**
     * sd卡是否可用
     *
     * @return
     */
    public static boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 创建根缓存目录
     *
     * @return
     */
    public static String createRootPath() {
        if (isSdCardAvailable()) {
            // /sdcard/Android/data/<application package>/cache
            cacheRootPath = App.mContext.getExternalCacheDir()
                    .getPath();
        } else {
            // /data/data/<application package>/cache
            cacheRootPath = App.mContext.getCacheDir().getPath();
        }
        return cacheRootPath;
    }

    /**
     * 创建文件夹
     *
     * @param dirPath
     * @return 创建失败返回""
     */
    private static String createDir(String dirPath) {
        try {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dirPath;
    }

    /**
     * 获取图片缓存目录
     *
     * @return 创建失败,返回""
     */
    public static String getImageCachePath() {
        String path = createDir(createRootPath() + File.separator + "img"
                + File.separator);
        return path;
    }

    /**
     * 获取图片裁剪缓存目录
     *
     * @return 创建失败,返回""
     */
    public static String getImageCropCachePath() {
        String path = createDir(createRootPath() + File.separator + "imgCrop"
                + File.separator);

        return path;
    }

    /**
     * 删除文件或者文件夹
     *
     * @param file
     */
    public static void deleteFileOrDirectory(File file) {
        try {
            if (file.isFile()) {
                file.delete();
                return;
            }
            if (file.isDirectory()) {
                File[] childFiles = file.listFiles();
                // 删除空文件夹
                if (childFiles == null || childFiles.length == 0) {
                    file.delete();
                    return;
                }
                // 递归删除文件夹下的子文件
                for (int i = 0; i < childFiles.length; i++) {
                    deleteFileOrDirectory(childFiles[i]);
                }
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将内容写入文件
     *
     * @param filePath
     *            eg:/mnt/sdcard/demo.txt
     * @param content
     *            内容
     */
    public static void writeFileSdcard(String filePath, String content,
                                       boolean isAppend) {

        try {
            FileOutputStream fout = new FileOutputStream(filePath, isAppend);
            byte[] bytes = content.getBytes();

            fout.write(bytes);

            fout.close();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}
