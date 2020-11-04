package net.cctv3.GuoleGouji;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestOpenCVActivity extends AppCompatActivity {
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    // 初始化图片
                    Toast.makeText(TestOpenCVActivity.this, "Copied assets images -_-||", Toast.LENGTH_SHORT).show();
                    ImageView oldImageView = findViewById(R.id.opencv_old);
                    oldImageView.setImageURI(file2Uri(new File(getFilesDir(), "Screenshot.png")));
                    new CountCardsThread().start();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv);
        new CopyAssets2FilesThread().start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 测试OpenCV配置
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Toast.makeText(TestOpenCVActivity.this, "OpenCV config: Ok", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(TestOpenCVActivity.this, "OpenCV config: Error", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Uri file2Uri(File file) {
        return FileProvider.getUriForFile(this, "net.cctv3.DuoleGouji.fileProvider", file);
    }

    class CopyAssets2FilesThread extends Thread {
        // Copy assets下的资源文件到Files里
        @Override
        public void run() {
            // 牌的模板数据
            for (String s : StringUtils.assetsNames()) {
                File file = new File(getFilesDir(), s);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        IOUtils.copy(getAssets().open(s), fos);
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    }

    class CountCardsThread extends Thread {
        @Override
        public void run() {
            for (String s : StringUtils.assetsNames()) {
                long start = System.currentTimeMillis();
                System.out.println("Counting: " + s);
                Mat template = Imgcodecs.imread(new File(getFilesDir(), s).getAbsolutePath());
                Mat source = Imgcodecs.imread(new File(getFilesDir(), "Screenshot.png").getAbsolutePath());
                // 创建于原图相同的大小，储存匹配度
                Mat result = Mat.zeros(source.rows() - template.rows() + 1, source.cols() - template.cols() + 1, CvType.CV_32FC1);
                // 调用模板匹配方法
                Imgproc.matchTemplate(source, template, result, Imgproc.TM_SQDIFF_NORMED);
                // 规格化
                Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1);
                // 获得最可能点，MinMaxLocResult是其数据格式，包括了最大、最小点的位置x、y
                Core.MinMaxLocResult mlr = Core.minMaxLoc(result);
                Point matchLoc = mlr.minLoc;
                // 在原图上的对应模板可能位置画一个绿色矩形
                // Core.rectangle(source, matchLoc, new Point(matchLoc.x + template.width(), matchLoc.y + template.height()), new Scalar(0, 255, 0));
                Imgproc.rectangle(source, matchLoc, new Point(matchLoc.x + template.width(), matchLoc.y + template.height()), new Scalar(0, 255, 0));
                File file = new File(getFilesDir(), "Result" + "_" + s);
                try {
                    file.createNewFile();
                    Imgcodecs.imwrite(file.getAbsolutePath(), source);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long end = System.currentTimeMillis();
                System.out.println("Used time: " + (end - start) + "ms");
            }
        }
    }
}